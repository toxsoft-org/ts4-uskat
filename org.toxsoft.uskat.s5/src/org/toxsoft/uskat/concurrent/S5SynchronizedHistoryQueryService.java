package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.hqserv.ISkHistoryQuery;
import org.toxsoft.uskat.core.api.hqserv.ISkHistoryQueryService;

/**
 * Синхронизация доступа к {@link ISkHistoryQueryService} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedHistoryQueryService
    extends S5SynchronizedService<ISkHistoryQueryService>
    implements ISkHistoryQueryService {

  private IStringMapEdit<S5SynchronizedHistoryQuery> activeQueries        = new StringMap<>();
  private IStringMapEdit<IOptionSet>                 activeQueriesOptions = new StringMap<>();

  /**
   * Конструктор
   *
   * @param aConnection {@link S5SynchronizedConnection} защищенное соединение
   * @throws TsNullArgumentRtException аругмент = null
   * @throws TsItemNotFoundRtException в соединении не найдена служба которую необходимо защитить
   */
  public S5SynchronizedHistoryQueryService( S5SynchronizedConnection aConnection ) {
    this( (ISkHistoryQueryService)aConnection.getUnsynchronizedService( SERVICE_ID ), aConnection.nativeLock() );
    aConnection.addService( this );
  }

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkHistoryQueryService} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedHistoryQueryService( ISkHistoryQueryService aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkHistoryQueryService aPrevTarget, ISkHistoryQueryService aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    for( String queryId : activeQueries.keys() ) {
      S5SynchronizedHistoryQuery query = activeQueries.getByKey( queryId );
      IOptionSet queryOptions = activeQueriesOptions.getByKey( queryId );
      ISkHistoryQuery newQuery = aNewTarget.createQuery( queryOptions );
      query.changeTarget( newQuery, aNewLock );
    }
  }

  // ------------------------------------------------------------------------------------
  // ISkHistoryQueryService
  //
  @Override
  public ISkHistoryQuery createQuery( IOptionSet aOptions ) {
    lockWrite( this );
    try {
      ISkHistoryQuery query = target().createQuery( aOptions );
      S5SynchronizedHistoryQuery retValue = new S5SynchronizedHistoryQuery( this, query, nativeLock() );
      activeQueries.put( query.queryId(), retValue );
      return retValue;
    }
    finally {
      unlockWrite( this );
    }
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public IStringMap<ISkHistoryQuery> activeQueries() {
    lockWrite( this );
    try {
      return (IStringMap<ISkHistoryQuery>)(Object)activeQueries;
    }
    finally {
      unlockWrite( this );
    }
  }

  // ------------------------------------------------------------------------------------
  // package API
  //
  void removeQuery( S5SynchronizedHistoryQuery aQuery ) {
    TsNullArgumentRtException.checkNull( aQuery );
    lockWrite( this );
    try {
      activeQueries.removeByKey( aQuery.queryId() );
      activeQueriesOptions.removeByKey( aQuery.queryId() );
    }
    finally {
      unlockWrite( this );
    }
  }
}
