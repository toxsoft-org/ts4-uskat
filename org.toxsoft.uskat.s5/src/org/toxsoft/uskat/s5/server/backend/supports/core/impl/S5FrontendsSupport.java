package org.toxsoft.uskat.s5.server.backend.supports.core.impl;

import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.s5.server.backend.*;
import org.toxsoft.uskat.s5.server.frontend.*;
import org.toxsoft.uskat.s5.server.logger.*;
import org.toxsoft.uskat.s5.utils.threads.impl.*;

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
  private final ILogger logger = LoggerWrapper.getLogger( getClass() );

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
