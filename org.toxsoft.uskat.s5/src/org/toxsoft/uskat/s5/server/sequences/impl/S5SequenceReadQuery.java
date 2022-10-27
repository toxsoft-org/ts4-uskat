package org.toxsoft.uskat.s5.server.sequences.impl;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import java.sql.Connection;
import java.sql.Statement;

import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceFactory;
import org.toxsoft.uskat.s5.server.sequences.reader.IS5SequenceReadQuery;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

/**
 * Реализация {@link IS5SequenceReadQuery}
 *
 * @author mvk
 */
final class S5SequenceReadQuery
    implements IS5SequenceReadQuery {

  private final IS5FrontendRear      frontend;
  private final String               id;
  private final IQueryInterval       interval;
  private final IS5SequenceFactory<?>  factory;
  private final Connection           connection;
  private final long                 maxExecutionTimeout;
  private final IListEdit<Statement> statements = new ElemArrayList<>( false );
  private final S5Lockable           lock       = new S5Lockable();
  private boolean                    closed;
  private final ILogger              logger     = getLogger( getClass() );

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
  public void addStatement( Statement aStatement ) {
    if( aStatement == null ) {
      throw new TsNullArgumentRtException();
    }
    TsIllegalArgumentRtException.checkTrue( closed );
    lockWrite( lock );
    try {
      statements.add( aStatement );
    }
    finally {
      unlockWrite( lock );
    }
  }

  @Override
  public void removeStatement( Statement aStatement ) {
    if( aStatement == null ) {
      throw new TsNullArgumentRtException();
    }
    lockWrite( lock );
    try {
      statements.remove( aStatement );
    }
    finally {
      unlockWrite( lock );
    }
  }

  // ------------------------------------------------------------------------------------
  // ICloseable
  //
  @Override
  public void close() {
    if( closed ) {
      return;
    }
    lockWrite( lock );
    try {
      closed = true;
      for( Statement statement : statements ) {
        try {
          statement.cancel();
        }
        catch( Throwable e ) {
          logger.error( e );
        }
      }
      statements.clear();
    }
    finally {
      unlockWrite( lock );
    }
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
