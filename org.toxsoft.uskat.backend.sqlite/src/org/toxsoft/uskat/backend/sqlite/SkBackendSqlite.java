package org.toxsoft.uskat.backend.sqlite;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.backend.sqlite.ISkBackensSqliteConstants.*;
import static org.toxsoft.uskat.core.backend.ISkBackendHardConstant.*;

import java.io.*;
import java.sql.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.files.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.backend.sqlite.addons.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.backend.metainf.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * {@link ISkBackend} implementation for SQLite storage.
 *
 * @author hazard157
 */
public class SkBackendSqlite
    implements ISkBackend {

  /**
   * The backend provider singleton.
   */
  public static final ISkBackendProvider PROVIDER = new ISkBackendProvider() {

    @Override
    public ISkBackendMetaInfo getMetaInfo() {
      return SkBackendSqliteMetaInfo.INSTANCE;
    }

    @Override
    public ISkBackend createBackend( ISkFrontendRear aFrontend, ITsContextRo aArgs ) {
      return new SkBackendSqlite( aFrontend, aArgs );
    }

  };

  private static final String JDBC_DRIVER_NAME = "org.sqlite.JDBC"; //$NON-NLS-1$

  private final IListEdit<ISkServiceCreator<? extends AbstractSkService>> bsCreators = new ElemArrayList<>();

  private final IStringMapEdit<AbstractAddon> allAddons = new StringMap<>();

  private final ISkFrontendRear frontend;
  private final ITsContextRo    args;
  private final SkBackendInfo   backendInfo;

  private final BaClasses  baClasses;
  private final BaObjects  baObjects;
  private final BaClobs    baClobs;
  private final BaLinks    baLinks;
  private final BaEvents   baEvents;
  private final BaRtData   baRtData;
  private final BaCommands baCommands;
  private final BaQueries  baQueries;
  private final BaGwidDb   baGwidDb;

  /**
   * Created in {@link #initialize()} method.
   */
  private Connection sqliteConn = null;

  /**
   * Constructor.
   *
   * @param aFrontend {@link ISkFrontendRear} - the frontend
   * @param aArgs {@link ITsContextRo} - creation arguments
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SkBackendSqlite( ISkFrontendRear aFrontend, ITsContextRo aArgs ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aArgs );
    frontend = aFrontend;
    args = aArgs;
    // prepare addons
    baClasses = new BaClasses( this );
    allAddons.put( baClasses.id(), baClasses );
    baObjects = new BaObjects( this );
    allAddons.put( baObjects.id(), baObjects );
    baClobs = new BaClobs( this );
    allAddons.put( baClobs.id(), baClobs );
    baLinks = new BaLinks( this );
    allAddons.put( baLinks.id(), baLinks );
    baEvents = new BaEvents( this );
    allAddons.put( baEvents.id(), baEvents );
    baRtData = new BaRtData( this );
    allAddons.put( baRtData.id(), baRtData );
    baCommands = new BaCommands( this );
    allAddons.put( baCommands.id(), baCommands );
    baQueries = new BaQueries( this );
    allAddons.put( baQueries.id(), baQueries );
    baGwidDb = new BaGwidDb( this );
    allAddons.put( baGwidDb.id(), baGwidDb );
    // setup backendInfo
    IOptionSetEdit backendInfoValue = new OptionSet();
    OPDEF_SKBI_LOGGED_USER.setValue( backendInfoValue,
        avValobj( new SkLoggedUserInfo( ISkUserServiceHardConstants.SKID_USER_ROOT,
            ISkUserServiceHardConstants.SKID_ROLE_ROOT, ESkAuthentificationType.NONE ) ) );
    OPDEF_SKBI_NEED_THREAD_SAFE_FRONTEND.setValue( backendInfoValue, AV_FALSE );
    backendInfo = new SkBackendInfo( BACKEND_ID, System.currentTimeMillis(), backendInfoValue );
  }

  // ------------------------------------------------------------------------------------
  // IInitializable
  //

  @Override
  public void initialize() {
    // test for SQLite JDBC driver
    try {
      Class.forName( JDBC_DRIVER_NAME );
    }
    catch( ClassNotFoundException ex ) {
      throw new SkSqlRtException( ex );
    }
    // open database
    File dbFile = new File( OPDEF_DB_FILE_NAME.getValue( args.params() ).asString() );
    TsFileUtils.checkFileAppendable( dbFile );
    String url = "jdbc:sqlite:" + dbFile.getAbsolutePath(); //$NON-NLS-1$
    try {
      sqliteConn = DriverManager.getConnection( url );
    }
    catch( SQLException ex ) {
      throw new SkSqlRtException( ex );
    }
    // init addons
    for( AbstractAddon a : allAddons ) {
      a.initialize();
    }
  }

  // ------------------------------------------------------------------------------------
  // ICloseable
  //

  @Override
  public void close() {
    if( sqliteConn != null ) {
      // close addons
      for( int i = allAddons.size() - 1; i >= 0; i-- ) {
        allAddons.values().get( i ).close();
      }
      // close connection
      try {
        sqliteConn.close();
      }
      catch( SQLException ex ) {
        // just log senseless exception
        LoggerUtils.errorLogger().error( ex );
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // internal API
  //

  /**
   * Returns the SQL-connection to the database.
   *
   * @return {@link Connection} - the SQL-connection
   * @throws TsIllegalStateRtException connection was not initialized
   */
  public Connection sqlConn() {
    if( sqliteConn == null ) {
      throw new TsIllegalStateRtException();
    }
    return sqliteConn;
  }

  // ------------------------------------------------------------------------------------
  // ISkBackend
  //

  @Override
  public boolean isActive() {
    return sqliteConn != null;
  }

  @Override
  public ISkBackendInfo getBackendInfo() {
    return backendInfo;
  }

  @Override
  public ISkFrontendRear frontend() {
    return frontend;
  }

  @Override
  public ITsContextRo openArgs() {
    return args;
  }

  @Override
  public IBaClasses baClasses() {
    return baClasses;
  }

  @Override
  public IBaObjects baObjects() {
    return baObjects;
  }

  @Override
  public IBaLinks baLinks() {
    return baLinks;
  }

  @Override
  public IBaEvents baEvents() {
    return baEvents;
  }

  @Override
  public IBaClobs baClobs() {
    return baClobs;
  }

  @Override
  public IBaRtdata baRtdata() {
    return baRtData;
  }

  @Override
  public IBaCommands baCommands() {
    return baCommands;
  }

  @Override
  public IBaQueries baQueries() {
    return baQueries;
  }

  @Override
  public IBaGwidDb baGwidDb() {
    return baGwidDb;
  }

  @Override
  public IList<ISkServiceCreator<? extends AbstractSkService>> listBackendServicesCreators() {
    return bsCreators;
  }

  @Override
  public <T> T findBackendAddon( String aAddonId, Class<T> aExpectedType ) {
    TsNullArgumentRtException.checkNulls( aAddonId, aExpectedType );
    Object rawAddon = allAddons.findByKey( aAddonId );
    return aExpectedType.cast( rawAddon );
  }

  @Override
  public void sendBackendMessage( GtMessage aMessage ) {
    frontend.onBackendMessage( aMessage );
  }

}
