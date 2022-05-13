package org.toxsoft.uskat.s5.server.backend.impl;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.server.backend.IS5FrontendAttachable;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

import ru.uskat.backend.ISkFrontendRear;

/**
 * Поддержка работы с {@link ISkFrontendRear}
 *
 * @author mvk
 */
class S5FrontendsSupport
    implements IS5FrontendAttachable {

  /**
   * Список зарегистрированных frontend
   */
  private final IListEdit<IS5FrontendRear> attachedFrontends = new ElemArrayList<>( false );

  /**
   * Список frontend для оповещения. Оптимизация, чтобы не создавать лишний объект IList при каждом оповещении
   */
  private volatile IList<IS5FrontendRear> workFrontends = null;

  /**
   * Блокировка доступа к данным класса
   */
  private final S5Lockable lock = new S5Lockable();

  /**
   * Журнал работы
   */
  private final ILogger logger = getLogger( getClass() );

  // ------------------------------------------------------------------------------------
  // Реализация IS5FrontendAttachable
  //
  @Override
  public final IList<IS5FrontendRear> attachedFrontends() {
    lockRead( lock );
    try {
      if( workFrontends == null ) {
        workFrontends = new ElemArrayList<>( attachedFrontends );
      }
      return workFrontends;
    }
    finally {
      unlockRead( lock );
    }
  }

  @Override
  public final void attachFrontend( IS5FrontendRear aFrontend ) {
    TsNullArgumentRtException.checkNull( aFrontend );
    lockWrite( lock );
    try {
      attachedFrontends.add( aFrontend );
      workFrontends = null;
    }
    finally {
      unlockWrite( lock );
    }
  }

  @Override
  public final void detachFrontend( IS5FrontendRear aFrontend ) {
    TsNullArgumentRtException.checkNull( aFrontend );
    lockWrite( lock );
    try {
      attachedFrontends.remove( aFrontend );
      workFrontends = null;
    }
    finally {
      unlockWrite( lock );
    }
  }

  // ------------------------------------------------------------------------------------
  // Методы для наследников
  //
  /**
   * Возращает журнал работы
   *
   * @return {@link ILogger} журнал работы
   */
  protected final ILogger logger() {
    return logger;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
}
