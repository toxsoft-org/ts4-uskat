package com.hazard157.uskat.test;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.core.tslib.gw.IGwHardConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.uskat.backend.memtext.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * Test class 1.
 *
 * @author hazard157
 */
@SuppressWarnings( { "javadoc", "boxing", "nls" } )
public class Test1
    implements Runnable, IUSkatConnected {

  // FIXME private final ISkCoreListener coreListener = aEvents -> {
  // for( SkCoreEvent e : aEvents ) {
  // TsTestUtils.pl( "===> %s", e ); //$NON-NLS-1$
  // }
  // };

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

  private static final String CID_MODEL_GIRL        = "fj.Model";    //$NON-NLS-1$
  private static final String CID_IMAGE_SET         = "fj.ImageSet"; //$NON-NLS-1$
  private static final String RID_IMGSET_MAIN_MODEL = "main_model";  //$NON-NLS-1$

  private static IDtoClassInfo makeModelGirlClass() {
    DtoClassInfo dtoClass = new DtoClassInfo( CID_MODEL_GIRL, GW_ROOT_CLASS_ID, IOptionSet.NULL );
    // TODO Test1.makeModelGirlClass()
    return dtoClass;
  }

  private static IDtoClassInfo makeImageSetClass() {
    DtoClassInfo dtoClass = new DtoClassInfo( CID_IMAGE_SET, GW_ROOT_CLASS_ID, IOptionSet.NULL );
    DtoRivetInfo rinf = DtoRivetInfo.create1( RID_IMGSET_MAIN_MODEL, CID_MODEL_GIRL, 1, OptionSetUtils.createOpSet( //
        TSID_NAME, "Main model", //
        TSID_DESCRIPTION, "Main model in theis image set (the first model in set name)" //
    ) );
    dtoClass.rivetInfos().put( rinf );
    // TODO Test1.makeModelGirlClass()
    return dtoClass;
  }

  public void runTestSequenceWithOpenConnection() {
    // classes
    IDtoClassInfo dtoClass = makeModelGirlClass();
    skSysdescr().defineClass( dtoClass );
    dtoClass = makeImageSetClass();
    skSysdescr().defineClass( dtoClass );
    IStridablesList<ISkClassInfo> llClasses = skSysdescr().listClasses();
    TsTestUtils.pl( "  Classes list:" );
    for( ISkClassInfo cinf : llClasses ) {
      TsTestUtils.pl( "    %s", StridUtils.printf( StridUtils.FORMAT_ID_NAME, cinf ) );
    }
    TsTestUtils.pl( "  total %d class(es)", llClasses.size() );

    // objects - models
    DtoObject dtoObj = new DtoObject( new Skid( CID_MODEL_GIRL, "marablake" ), IOptionSet.NULL, IStringMap.EMPTY );
    dtoObj.attrs().setStr( AID_NAME, "Mara Blake" );
    Skid skidMaraBlake = dtoObj.skid();
    skObjServ().defineObject( dtoObj );
    //
    dtoObj = new DtoObject( new Skid( CID_MODEL_GIRL, "ryana" ), IOptionSet.NULL, IStringMap.EMPTY );
    dtoObj.attrs().setStr( AID_NAME, "Ryana" );
    Skid skidRyana = dtoObj.skid();
    skObjServ().defineObject( dtoObj );

    // objects - image sets
    dtoObj = new DtoObject( new Skid( CID_IMAGE_SET, "ryana.luscious" ), IOptionSet.NULL, IStringMap.EMPTY );
    dtoObj.attrs().setStr( AID_NAME, "Luscious" );
    dtoObj.rivets().ensureSkidList( RID_IMGSET_MAIN_MODEL ).add( skidRyana );
    skObjServ().defineObject( dtoObj );
    //
    dtoObj = new DtoObject( new Skid( CID_IMAGE_SET, "ryana.charming" ), IOptionSet.NULL, IStringMap.EMPTY );
    dtoObj.attrs().setStr( AID_NAME, "Charming" );
    dtoObj.rivets().ensureSkidList( RID_IMGSET_MAIN_MODEL ).add( skidRyana );
    skObjServ().defineObject( dtoObj );
    //
    dtoObj = new DtoObject( new Skid( CID_IMAGE_SET, "ryana.coy" ), IOptionSet.NULL, IStringMap.EMPTY );
    dtoObj.attrs().setStr( AID_NAME, "Coy" );
    dtoObj.rivets().ensureSkidList( RID_IMGSET_MAIN_MODEL ).add( skidRyana );
    skObjServ().defineObject( dtoObj );

  }

  // ------------------------------------------------------------------------------------
  // Runnable
  //

  @Override
  public void run() {
    ITsContext ctx = new TsContext();
    try {
      TsTestUtils.nl();
      ISkCoreConfigConstants.REFDEF_BACKEND_PROVIDER.setRef( ctx, MtbBackendToFile.PROVIDER );
      MtbBackendToFile.OPDEF_FILE_PATH.setValue( ctx.params(), avStr( "/home/goga/mtb-test.txt" ) );
      conn.open( ctx );
      // FIXME conn.coreApi().eventer().addListener( coreListener );
      TsTestUtils.pl( conn.backendInfo().toString() );
      runTestSequenceWithOpenConnection();
      TsTestUtils.nl();
    }
    finally {
      if( conn.state().isOpen() ) {
        conn.close();
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // IUSkatConnected
  //

  @Override
  public ISkConnection skConn() {
    return conn;
  }

}
