package org.toxsoft.uskat.sysext.alarms.impl;

import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5ThreadUtils.*;

import java.io.Serializable;

import org.toxsoft.core.log4j.Logger;
import org.toxsoft.core.tslib.bricks.strid.impl.Stridable;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;
import org.toxsoft.uskat.sysext.alarms.api.generator.ISkAlarmDataProvider;

/**
 * Абстрактный поставщик данных для формирования алармов
 *
 * @author mvk
 */
public abstract class SkAbstractAlarmDataProvider
    extends Stridable
    implements ISkAlarmDataProvider, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Слушатели изменений значений
   */
  private final IListEdit<IAlarmDataProviderListener> listeners = new ElemArrayList<>();

  /**
   * Блокировка доступа к списку слушателей
   */
  private final S5Lockable listenersLock = new S5Lockable();

  /**
   * Журнал работы
   */
  private final ILogger logger = Logger.getLogger( getClass() );

  /**
   * Конструктор
   *
   * @param aId String идентификатор поставщика
   */
  public SkAbstractAlarmDataProvider( String aId ) {
    super( aId );
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISkAlarmDataProvider
  //
  @Override
  public void addProviderListener( IAlarmDataProviderListener aListener ) {
    TsNullArgumentRtException.checkNull( aListener );
    tryLockWrite( listenersLock );
    try {
      if( !listeners.hasElem( aListener ) ) {
        listeners.add( aListener );
      }
    }
    finally {
      unlockWrite( listenersLock );
    }
  }

  @Override
  public void removeProviderListener( IAlarmDataProviderListener aListener ) {
    TsNullArgumentRtException.checkNull( aListener );
    tryLockWrite( listenersLock );
    try {
      listeners.remove( aListener );
    }
    finally {
      unlockWrite( listenersLock );
    }
  }

  // ------------------------------------------------------------------------------------
  // API для наследников
  //
  /**
   * Возвращает журнал работы поставщика
   *
   * @return {@link ILogger} журнал работы
   */
  protected final ILogger logger() {
    return logger;
  }

  /**
   * Формирование события: изменились данные поставщика
   */
  protected final void fireUpdateEvent() {
    for( IAlarmDataProviderListener listener : threadSafeList( listeners, listenersLock ) ) {
      try {
        listener.onUpdate( this );
      }
      catch( Throwable e ) {
        logger().error( e );
      }
    }
  }
}
