package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.events.change.IGenericChangeEventer;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.rtdserv.EQueryState;
import org.toxsoft.uskat.core.api.rtdserv.ISkHistDataQuery;

/**
 * Синхронизация доступа к {@link ISkHistDataQuery} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedHistDataQuery
    extends S5SynchronizedResource<ISkHistDataQuery>
    implements ISkHistDataQuery {

  private final S5SynchronizedRtDataService        owner;
  private final S5SynchronizedGenericChangeEventer eventer;

  /**
   * Конструктор
   *
   * @param aOwner {@link S5SynchronizedRtDataService} служба-собственник канала
   * @param aTarget {@link ISkHistDataQuery} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedHistDataQuery( S5SynchronizedRtDataService aOwner, ISkHistDataQuery aTarget,
      ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
    owner = TsNullArgumentRtException.checkNull( aOwner );
    eventer = new S5SynchronizedGenericChangeEventer( aTarget.genericChangeEventer(), aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkHistDataQuery aPrevTarget, ISkHistDataQuery aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // IGenericChangeEventCapable
  //
  @Override
  public IGenericChangeEventer genericChangeEventer() {
    return eventer;
  }

  // ------------------------------------------------------------------------------------
  // ISkHistDataQuery
  //
  @Override
  public String queryId() {
    lockWrite( this );
    try {
      return target().queryId();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void close() {
    lockWrite( this );
    try {
      owner.removeQuery( this );
      target().close();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public EQueryState state() {
    lockWrite( this );
    try {
      return target().state();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IGwidList prepare( IGwidList aGwids ) {
    lockWrite( this );
    try {
      return target().prepare( aGwids );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void exec( IQueryInterval aQueryInterval ) {
    lockWrite( this );
    try {
      target().exec( aQueryInterval );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void cancel() {
    lockWrite( this );
    try {
      target().cancel();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IMap<Gwid, ITimedList<ITemporalAtomicValue>> result() {
    lockWrite( this );
    try {
      return target().result();
    }
    finally {
      unlockWrite( this );
    }
  }
}
