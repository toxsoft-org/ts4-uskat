package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.events.change.IGenericChangeEventer;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.hqserv.*;

/**
 * Синхронизация доступа к {@link ISkProcessedQuery} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedProcessedQuery
    extends S5SynchronizedResource<ISkProcessedQuery>
    implements ISkProcessedQuery {

  private final S5SynchronizedHistoryQueryService  owner;
  private final S5SynchronizedGenericChangeEventer eventer;

  /**
   * Конструктор
   *
   * @param aOwner {@link S5SynchronizedHistoryQueryService} служба-собственник канала
   * @param aTarget {@link ISkProcessedQuery} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedProcessedQuery( S5SynchronizedHistoryQueryService aOwner, ISkProcessedQuery aTarget,
      ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
    owner = TsNullArgumentRtException.checkNull( aOwner );
    eventer = new S5SynchronizedGenericChangeEventer( aTarget.genericChangeEventer(), aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkProcessedQuery aPrevTarget, ISkProcessedQuery aNewTarget,
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
  // ISkProcessedQuery
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
  public IOptionSet params() {
    lockWrite( this );
    try {
      return target().params();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void close() {
    lockWrite( this );
    try {
      owner.removeProcessedQuery( this );
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
  public void prepare( IStridablesList<ISkProcessedQueryDataArg> aArgs ) {
    lockWrite( this );
    try {
      target().prepare( aArgs );
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
  public IStridablesList<ISkProcessedQueryDataArg> listArgs() {
    lockWrite( this );
    try {
      return target().listArgs();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public boolean isArgDataReady( String aArgId ) {
    lockWrite( this );
    try {
      return target().isArgDataReady( aArgId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public <V extends ITemporal<V>> ITimedList<V> getArgData( String aDataId ) {
    lockWrite( this );
    try {
      return target().getArgData( aDataId );
    }
    finally {
      unlockWrite( this );
    }
  }
}
