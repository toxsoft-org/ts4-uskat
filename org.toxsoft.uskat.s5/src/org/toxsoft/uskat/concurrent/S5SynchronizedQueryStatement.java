package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.events.change.IGenericChangeEventer;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.hqserv.ESkQueryState;
import org.toxsoft.uskat.core.api.hqserv.ISkQueryStatement;

/**
 * Синхронизация доступа к {@link ISkQueryStatement} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedQueryStatement
    extends S5SynchronizedResource<ISkQueryStatement>
    implements ISkQueryStatement {

  private final S5SynchronizedHistoryQueryService  owner;
  private final S5SynchronizedGenericChangeEventer eventer;

  /**
   * Конструктор
   *
   * @param aOwner {@link S5SynchronizedHistoryQueryService} служба-собственник канала
   * @param aTarget {@link ISkQueryStatement} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedQueryStatement( S5SynchronizedHistoryQueryService aOwner, ISkQueryStatement aTarget,
      ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
    owner = TsNullArgumentRtException.checkNull( aOwner );
    eventer = new S5SynchronizedGenericChangeEventer( aTarget.genericChangeEventer(), aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkQueryStatement aPrevTarget, ISkQueryStatement aNewTarget,
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
  // ISkQueryStatement
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
      owner.removeLanguageQuery( this );
      target().close();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ESkQueryState state() {
    lockWrite( this );
    try {
      return target().state();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void prepare( String aSkatQl, IStringMap<IAtomicValue> aArgs ) {
    lockWrite( this );
    try {
      target().prepare( aSkatQl, aArgs );
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
  public Object getResult() {
    lockWrite( this );
    try {
      return target().getResult();
    }
    finally {
      unlockWrite( this );
    }
  }

}
