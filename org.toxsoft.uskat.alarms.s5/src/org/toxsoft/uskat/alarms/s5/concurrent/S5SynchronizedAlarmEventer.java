package org.toxsoft.uskat.alarms.s5.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.alarms.lib.ISkAlarmEventer;
import org.toxsoft.uskat.alarms.lib.ISkAlarmServiceListener;
import org.toxsoft.uskat.concurrent.S5SynchronizedEventer;

/**
 * Синхронизация доступа к {@link ISkAlarmEventer} (декоратор)
 *
 * @author mvk
 */
public class S5SynchronizedAlarmEventer
    extends S5SynchronizedEventer<ISkAlarmEventer, ISkAlarmServiceListener>
    implements ISkAlarmEventer {

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkAlarmEventer} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedAlarmEventer( ISkAlarmEventer aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
  }

  // ------------------------------------------------------------------------------------
  // ITsEventer
  //
  @Override
  public void addListener( ISkAlarmServiceListener aListener, ITsCombiFilterParams aFilter ) {
    lockWrite( this );
    try {
      target().addListener( aListener, aFilter );
    }
    finally {
      unlockWrite( this );
    }
  }

}
