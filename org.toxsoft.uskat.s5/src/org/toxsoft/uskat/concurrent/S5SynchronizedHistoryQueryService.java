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
 * Синхронизация доступа к {@link ISkHistoryQueryService} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedHistoryQueryService
    extends S5SynchronizedService<ISkHistoryQueryService>
    implements ISkHistoryQueryService {

  private IStringMapEdit<S5SynchronizedQueryRawHistory>   openHistoricQueries  = new StringMap<>();
  private IStringMapEdit<S5SynchronizedQueryProcessedData> openProcessedQueries = new StringMap<>();
  private IStringMapEdit<S5SynchronizedQueryStatement>  openLanguageQueries  = new StringMap<>();

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
    for( String queryId : openHistoricQueries.keys() ) {
      S5SynchronizedQueryRawHistory query = openHistoricQueries.getByKey( queryId );
      ISkQueryRawHistory newQuery = aNewTarget.createHistoricQuery( query.params() );
      query.changeTarget( newQuery, aNewLock );
    }
    for( String queryId : openProcessedQueries.keys() ) {
      S5SynchronizedQueryProcessedData query = openProcessedQueries.getByKey( queryId );
      ISkQueryProcessedData newQuery = aNewTarget.createProcessedQuery( query.params() );
      query.changeTarget( newQuery, aNewLock );
    }
    for( String queryId : openLanguageQueries.keys() ) {
      S5SynchronizedQueryStatement query = openLanguageQueries.getByKey( queryId );
      ISkQueryStatement newQuery = aNewTarget.createLanguageQuery( query.params() );
      query.changeTarget( newQuery, aNewLock );
    }
  }

  // ------------------------------------------------------------------------------------
  // ISkHistoryQueryService
  //
  @Override
  public ISkQueryRawHistory createHistoricQuery( IOptionSet aOptions ) {
    lockWrite( this );
    try {
      ISkQueryRawHistory query = target().createHistoricQuery( aOptions );
      S5SynchronizedQueryRawHistory retValue = new S5SynchronizedQueryRawHistory( this, query, nativeLock() );
      openHistoricQueries.put( query.queryId(), retValue );
      return retValue;
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkQueryProcessedData createProcessedQuery( IOptionSet aOptions ) {
    lockWrite( this );
    try {
      ISkQueryProcessedData query = target().createProcessedQuery( aOptions );
      S5SynchronizedQueryProcessedData retValue = new S5SynchronizedQueryProcessedData( this, query, nativeLock() );
      openProcessedQueries.put( query.queryId(), retValue );
      return retValue;
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkQueryStatement createLanguageQuery( IOptionSet aOptions ) {
    lockWrite( this );
    try {
      ISkQueryStatement query = target().createLanguageQuery( aOptions );
      S5SynchronizedQueryStatement retValue = new S5SynchronizedQueryStatement( this, query, nativeLock() );
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
  void removeHistoryQuery( S5SynchronizedQueryRawHistory aQuery ) {
    TsNullArgumentRtException.checkNull( aQuery );
    lockWrite( this );
    try {
      openHistoricQueries.removeByKey( aQuery.queryId() );
    }
    finally {
      unlockWrite( this );
    }
  }

  void removeProcessedQuery( S5SynchronizedQueryProcessedData aQuery ) {
    TsNullArgumentRtException.checkNull( aQuery );
    lockWrite( this );
    try {
      openProcessedQueries.removeByKey( aQuery.queryId() );
    }
    finally {
      unlockWrite( this );
    }
  }

  void removeLanguageQuery( S5SynchronizedQueryStatement aQuery ) {
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
