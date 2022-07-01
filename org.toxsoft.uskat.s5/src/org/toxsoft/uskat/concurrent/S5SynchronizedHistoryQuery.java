package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.bricks.events.change.IGenericChangeEventer;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.hqserv.EQueryState;
import org.toxsoft.uskat.core.api.hqserv.ISkHistoryQuery;

/**
 * Синхронизация доступа к {@link ISkHistoryQuery} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedHistoryQuery
    extends S5SynchronizedResource<ISkHistoryQuery>
    implements ISkHistoryQuery {

  private final S5SynchronizedHistoryQueryService  owner;
  private final S5SynchronizedGenericChangeEventer eventer;

  /**
   * Конструктор
   *
   * @param aOwner {@link S5SynchronizedHistoryQueryService} служба-собственник канала
   * @param aTarget {@link ISkHistoryQuery} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedHistoryQuery( S5SynchronizedHistoryQueryService aOwner, ISkHistoryQuery aTarget,
      ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
    owner = TsNullArgumentRtException.checkNull( aOwner );
    eventer = new S5SynchronizedGenericChangeEventer( aTarget.genericChangeEventer(), aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkHistoryQuery aPrevTarget, ISkHistoryQuery aNewTarget,
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
  // ISkHistoryQuery
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
  public IGwidList listGwids() {
    lockWrite( this );
    try {
      return target().listGwids();
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
  public <T extends ITemporalValue<T>> ITimedList<T> get( Gwid aGwid ) {
    lockWrite( this );
    try {
      return target().get( aGwid );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public <T extends ITemporalValue<T>> IMap<Gwid, ITimedList<T>> getAll() {
    lockWrite( this );
    try {
      return target().getAll();
    }
    finally {
      unlockWrite( this );
    }
  }
}
