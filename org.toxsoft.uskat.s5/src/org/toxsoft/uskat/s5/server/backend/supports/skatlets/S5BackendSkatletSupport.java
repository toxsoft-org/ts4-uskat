package org.toxsoft.uskat.s5.server.backend.supports.skatlets;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.devapi.*;
import org.toxsoft.uskat.s5.client.local.*;

/**
 * Реализация {@link ISkatletSupport}
 */
class S5BackendSkatletSupport
    implements ISkatletSupport {

  private final IS5LocalConnectionSingleton connectionFactory;
  private final SharedConnection            sharedConnection;
  private final ILogger                     logger;

  /**
   * Constructor.
   *
   * @param aConnectionFactory {@link IS5LocalConnectionSingleton} фабрика соединений
   * @param aConnection {@link ISkConnection} разделяемое(общее) соединение скатлетов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5BackendSkatletSupport( IS5LocalConnectionSingleton aConnectionFactory, ISkConnection aConnection,
      ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aConnectionFactory, aConnection, aLogger );
    connectionFactory = aConnectionFactory;
    sharedConnection = new SharedConnection( aConnection );
    logger = aLogger;
  }

  // ------------------------------------------------------------------------------------
  // ISkatletSupport
  //
  @Override
  public ISkConnection getSharedConnection() {
    return sharedConnection;
  }

  @Override
  public ISkConnection createConnection( String aName, ITsContextRo aArgs ) {
    TsNullArgumentRtException.checkNulls( aName, aArgs );
    return connectionFactory.open( aName, aArgs );
  }

  @Override
  public ILogger logger() {
    return logger;
  }

  class SharedConnection
      implements ISkConnection {

    private final ISkConnection connection;

    SharedConnection( ISkConnection aConnection ) {
      TsNullArgumentRtException.checkNull( aConnection );
      connection = aConnection;
    }

    // ------------------------------------------------------------------------------------
    // ISkConnection
    //
    @Override
    public ESkConnState state() {
      return connection.state();
    }

    @Override
    public void open( ITsContextRo aArgs ) {
      connection.open( aArgs );
    }

    @Override
    public void close() {
      logger.warning( "SharedConnection.close(): Attempt to close shared connection at:\n%s", //$NON-NLS-1$
          currentThreadStackToString() );
    }

    @Override
    public ISkCoreApi coreApi() {
      return connection.coreApi();
    }

    @Override
    public ISkBackendInfo backendInfo() {
      return connection.backendInfo();
    }

    @Override
    public void addConnectionListener( ISkConnectionListener aListener ) {
      connection.addConnectionListener( aListener );
    }

    @Override
    public void removeConnectionListener( ISkConnectionListener aListener ) {
      connection.removeConnectionListener( aListener );
    }

    @Override
    public ITsContext scope() {
      return connection.scope();
    }

  }
}
