package com.hazard157.uskat.test;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.backend.memtext.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * Test class 1.
 *
 * @author hazard157
 */
@SuppressWarnings( { "javadoc", "boxing", "nls" } )
public class Test1
    implements Runnable {

  private final ISkCoreListener coreListener = aEvents -> {
    for( SkCoreEvent e : aEvents ) {
      TsTestUtils.pl( "===> %s", e ); //$NON-NLS-1$
    }
  };

  private final ISkConnection conn;

  /**
   * Constructor.
   */
  public Test1() {
    conn = SkUtils.createConnection();
  }

  // ------------------------------------------------------------------------------------
  // Testing sequence
  //

  public void runTestSequenceWithOpenConnection() {

    ISkSysdescr ss = conn.coreApi().sysdescr();

    DtoClassInfo dtoClass = new DtoClassInfo( "testClass", IGwHardConstants.GW_ROOT_CLASS_ID, IOptionSet.NULL );
    ss.defineClass( dtoClass );
    TsTestUtils.pl( "DONE SkSysdescr.defineClass( %s )", dtoClass.id() );

    IStridablesList<ISkClassInfo> llClasses = ss.listClasses();
    TsTestUtils.pl( "DONE SkSysdescr.listClasses()" );
    TsTestUtils.pl( "  Classes list:" );
    for( ISkClassInfo cinf : llClasses ) {
      TsTestUtils.pl( "    %s", StridUtils.printf( StridUtils.FORMAT_ID_NAME, cinf ) );
    }
    TsTestUtils.pl( "  total %d class(es)", llClasses.size() );

    // ISkObjectService os = conn.coreApi().objService();

  }

  // ------------------------------------------------------------------------------------
  // Runnable
  //

  @Override
  public void run() {
    ITsContext ctx = new TsContext();
    try {
      TsTestUtils.nl();
      ISkCoreConfigConstants.REFDEF_BACKEND_PROVIDER.setRef( ctx, SkBackendMemtextFile.PROVIDER );
      conn.open( ctx );
      conn.coreApi().eventer().addListener( coreListener );
      TsTestUtils.pl( conn.backendInfo().toString() );
      runTestSequenceWithOpenConnection();
      TsTestUtils.nl();
    }
    catch( Exception ex ) {
      LoggerUtils.errorLogger().error( ex );
    }
    finally {
      if( conn.state().isOpen() ) {
        conn.close();
      }
    }
  }

}
