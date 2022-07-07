package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.hqserv.*;

/**
 * Синхронизация доступа к {@link ISkQueryService} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedHistoryQueryService
    extends S5SynchronizedService<ISkQueryService>
    implements ISkQueryService {

  private IStringMapEdit<S5SynchronizedHistoryQuery>   openHistoricQueries  = new StringMap<>();
  private IStringMapEdit<S5SynchronizedProcessedQuery> openProcessedQueries = new StringMap<>();
  private IStringMapEdit<S5SynchronizedLanguageQuery>  openLanguageQueries  = new StringMap<>();

  /**
   * Конструктор
   *
   * @param aConnection {@link S5SynchronizedConnection} защищенное соединение
   * @throws TsNullArgumentRtException аругмент = null
   * @throws TsItemNotFoundRtException в соединении не найдена служба которую необходимо защитить
   */
  public S5SynchronizedHistoryQueryService( S5SynchronizedConnection aConnection ) {
    this( (ISkQueryService)aConnection.getUnsynchronizedService( SERVICE_ID ), aConnection.nativeLock() );
    aConnection.addService( this );
  }

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkQueryService} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedHistoryQueryService( ISkQueryService aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkQueryService aPrevTarget, ISkQueryService aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    for( String queryId : openHistoricQueries.keys() ) {
      S5SynchronizedHistoryQuery query = openHistoricQueries.getByKey( queryId );
      ISkHistoryQuery newQuery = aNewTarget.createHistoricQuery( query.params() );
      query.changeTarget( newQuery, aNewLock );
    }
    for( String queryId : openProcessedQueries.keys() ) {
      S5SynchronizedProcessedQuery query = openProcessedQueries.getByKey( queryId );
      ISkProcessedQuery newQuery = aNewTarget.createProcessedQuery( query.params() );
      query.changeTarget( newQuery, aNewLock );
    }
    for( String queryId : openLanguageQueries.keys() ) {
      S5SynchronizedLanguageQuery query = openLanguageQueries.getByKey( queryId );
      ISkLanguageQuery newQuery = aNewTarget.createLanguageQuery( query.params() );
      query.changeTarget( newQuery, aNewLock );
    }
  }

  // ------------------------------------------------------------------------------------
  // ISkQueryService
  //
  @Override
  public ISkHistoryQuery createHistoricQuery( IOptionSet aOptions ) {
    lockWrite( this );
    try {
      ISkHistoryQuery query = target().createHistoricQuery( aOptions );
      S5SynchronizedHistoryQuery retValue = new S5SynchronizedHistoryQuery( this, query, nativeLock() );
      openHistoricQueries.put( query.queryId(), retValue );
      return retValue;
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkProcessedQuery createProcessedQuery( IOptionSet aOptions ) {
    lockWrite( this );
    try {
      ISkProcessedQuery query = target().createProcessedQuery( aOptions );
      S5SynchronizedProcessedQuery retValue = new S5SynchronizedProcessedQuery( this, query, nativeLock() );
      openProcessedQueries.put( query.queryId(), retValue );
      return retValue;
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkLanguageQuery createLanguageQuery( IOptionSet aOptions ) {
    lockWrite( this );
    try {
      ISkLanguageQuery query = target().createLanguageQuery( aOptions );
      S5SynchronizedLanguageQuery retValue = new S5SynchronizedLanguageQuery( this, query, nativeLock() );
      openLanguageQueries.put( query.queryId(), retValue );
      return retValue;
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IStringMap<ISkAsynchronousQuery> openQueries() {
    lockWrite( this );
    try {
      IStringMapEdit<ISkAsynchronousQuery> retValue = new StringMap<>();
      retValue.putAll( openHistoricQueries );
      retValue.putAll( openProcessedQueries );
      retValue.putAll( openLanguageQueries );
      return retValue;
    }
    finally {
      unlockWrite( this );
    }
  }

  // ------------------------------------------------------------------------------------
  // package API
  //
  void removeHistoryQuery( S5SynchronizedHistoryQuery aQuery ) {
    TsNullArgumentRtException.checkNull( aQuery );
    lockWrite( this );
    try {
      openHistoricQueries.removeByKey( aQuery.queryId() );
    }
    finally {
      unlockWrite( this );
    }
  }

  void removeProcessedQuery( S5SynchronizedProcessedQuery aQuery ) {
    TsNullArgumentRtException.checkNull( aQuery );
    lockWrite( this );
    try {
      openProcessedQueries.removeByKey( aQuery.queryId() );
    }
    finally {
      unlockWrite( this );
    }
  }

  void removeLanguageQuery( S5SynchronizedLanguageQuery aQuery ) {
    TsNullArgumentRtException.checkNull( aQuery );
    lockWrite( this );
    try {
      openProcessedQueries.removeByKey( aQuery.queryId() );
    }
    finally {
      unlockWrite( this );
    }
  }
}
