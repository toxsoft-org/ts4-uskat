package org.toxsoft.uskat.s5.server.sequences.impl;

import static org.toxsoft.core.log4j.LoggerWrapper.*;

import java.sql.*;

import javax.persistence.Version;

import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.synch.SynchronizedListEdit;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceFactory;
import org.toxsoft.uskat.s5.server.sequences.reader.IS5SequenceReadQuery;

/**
 * Реализация {@link IS5SequenceReadQuery}
 *
 * @author mvk
 */
final class S5SequenceReadQuery
    implements IS5SequenceReadQuery {

  private final IS5FrontendRear       frontend;
  private final String                id;
  private final IQueryInterval        interval;
  private final IS5SequenceFactory<?> factory;
  private final Connection            connection;
  private final long                  maxExecutionTimeout;
  private final IListEdit<Statement>  statements = new SynchronizedListEdit<>( new ElemArrayList<>( false ) );
  private boolean                     closed;
  private volatile Thread             thread;
  private final ILogger               logger     = getLogger( getClass() );

  /**
   * Конструктор
   *
   * @param aFrontend {@link IS5FrontendRear} фронтенд сформировавший запрос
   * @param aId String идентификатор запроса
   * @param aInterval {@link IQueryInterval} интервал запроса значений
   * @param aFactory {@link IS5SequenceFactory} фабрика формирования значений
   * @param aConnection {@link Connection} соединение с базой данных
   * @param aMaxExecutionTimeout long максимальное время(мсек) выполнения запроса. < 1000: без ограничения
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5SequenceReadQuery( IS5FrontendRear aFrontend, String aId, IQueryInterval aInterval, IS5SequenceFactory<?> aFactory,
      Connection aConnection, long aMaxExecutionTimeout ) {
    frontend = TsNullArgumentRtException.checkNull( aFrontend );
    id = TsNullArgumentRtException.checkNull( aId );
    interval = TsNullArgumentRtException.checkNull( aInterval );
    factory = TsNullArgumentRtException.checkNull( aFactory );
    connection = TsNullArgumentRtException.checkNull( aConnection );
    maxExecutionTimeout = aMaxExecutionTimeout;
  }

  // ------------------------------------------------------------------------------------
  // IS5SequenceReadQuery
  //
  @Override
  public IS5FrontendRear frontend() {
    return frontend;
  }

  @Override
  public String queryId() {
    return id;
  }

  @Override
  public IQueryInterval interval() {
    return interval;
  }

  @Override
  public IS5SequenceFactory<?> factory() {
    return factory;
  }

  @Override
  public long maxExecutionTimeout() {
    return maxExecutionTimeout;
  }

  @Override
  public Connection connection() {
    return connection;
  }

  @Override
  public boolean isClosed() {
    return closed;
  }

  @Override
  public void addStatement( Statement aStatement ) {
    if( aStatement == null ) {
      throw new TsNullArgumentRtException();
    }
    TsIllegalArgumentRtException.checkTrue( closed );
    statements.add( aStatement );
  }

  @Override
  public void removeStatement( Statement aStatement ) {
    if( aStatement == null ) {
      throw new TsNullArgumentRtException();
    }
    statements.remove( aStatement );
  }

  @Override
  @Version
  public void setThread( Thread aThread ) {
    TsNullArgumentRtException.checkNull( aThread );
    thread = aThread;
  }

  // ------------------------------------------------------------------------------------
  // ICloseable
  //
  @Override
  public void close() {
    if( closed ) {
      return;
    }
    for( Statement statement : statements.copyTo( new ElemArrayList<>() ) ) {
      try {
        statement.cancel();
      }
      catch( Throwable e ) {
        logger.error( e );
      }
    }
    try {
      connection.close();
    }
    catch( SQLException e ) {
      logger.error( e );
    }
    statements.clear();
    if( thread != null ) {
      thread.interrupt();
    }
    closed = true;
  }

  // ------------------------------------------------------------------------------------
  // Object
  //
  @Override
  public String toString() {
    return getClass().getSimpleName() + '.' + id;
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + id.hashCode();
    return result;
  }

  @Override
  public boolean equals( Object aObject ) {
    if( this == aObject ) {
      return true;
    }
    if( aObject == null ) {
      return false;
    }
    if( getClass() != aObject.getClass() ) {
      return false;
    }
    return id.equals( ((S5SequenceReadQuery)aObject).id );
  }
}
