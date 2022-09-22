package com.hazard157.uskat.test;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.core.tslib.gw.IGwHardConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
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
    implements Runnable, ISkConnected {

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
    conn = SkCoreUtils.createConnection();
  }

  // ------------------------------------------------------------------------------------
  // Testing sequence
  //

  private static final String CID_MODEL_GIRL        = "fj.Model";             //$NON-NLS-1$
  private static final String CID_IMAGE_SET         = "fj.ImageSet";          //$NON-NLS-1$
  private static final String RID_IMGSET_MAIN_MODEL = "main_model";           //$NON-NLS-1$
  private static final String RTDID_PREGNANT        = "pregnant_rtdataId";    //$NON-NLS-1$
  private static final String RTDID_PULSE           = "pulse_rtdataId";       //$NON-NLS-1$
  private static final String RTDID_TEMPERATURE     = "temperature_rtdataId"; //$NON-NLS-1$

  private static IDtoClassInfo makeModelGirlClass() {
    DtoClassInfo dtoClass = new DtoClassInfo( CID_MODEL_GIRL, GW_ROOT_CLASS_ID, IOptionSet.NULL );
    // TODO Test1.makeModelGirlClass()
    DtoRtdataInfo rtdinf1 = DtoRtdataInfo.create1( RTDID_PREGNANT, new DataType( EAtomicType.BOOLEAN ), true, true,
        true, 1000, OptionSetUtils.createOpSet( //
            TSID_NAME, "flag pregnant", //
            TSID_DESCRIPTION, "Flag is girl pregnant" //
        ) );
    dtoClass.rtdataInfos().put( rtdinf1 );
    DtoRtdataInfo rtdinf2 = DtoRtdataInfo.create1( RTDID_PULSE, new DataType( EAtomicType.INTEGER ), true, true, true,
        1000, OptionSetUtils.createOpSet( //
            TSID_NAME, "pulse of heart", //
            TSID_DESCRIPTION, "pulse of heart" //
        ) );
    dtoClass.rtdataInfos().put( rtdinf2 );
    DtoRtdataInfo rtdinf3 = DtoRtdataInfo.create1( RTDID_TEMPERATURE, new DataType( EAtomicType.STRING ), true, true,
        true, 1000, OptionSetUtils.createOpSet( //
            TSID_NAME, "temperature ", //
            TSID_DESCRIPTION, "temperature of body" //
        ) );
    dtoClass.rtdataInfos().put( rtdinf3 );
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
    DtoObject dtoObj = new DtoObject( new Skid( CID_MODEL_GIRL, "marablake" ) );
    dtoObj.attrs().setStr( AID_NAME, "Mara Blake" );
    // Skid skidMaraBlake = dtoObj.skid();
    skObjServ().defineObject( dtoObj );
    //
    dtoObj = new DtoObject( new Skid( CID_MODEL_GIRL, "ryana" ) );
    dtoObj.attrs().setStr( AID_NAME, "Ryana" );
    Skid skidRyana = dtoObj.skid();
    skObjServ().defineObject( dtoObj );

    // objects - image sets
    dtoObj = new DtoObject( new Skid( CID_IMAGE_SET, "ryana.luscious" ) );
    dtoObj.attrs().setStr( AID_NAME, "Luscious" );
    dtoObj.rivets().ensureSkidList( RID_IMGSET_MAIN_MODEL ).add( skidRyana );
    skObjServ().defineObject( dtoObj );
    //
    dtoObj = new DtoObject( new Skid( CID_IMAGE_SET, "ryana.charming" ) );
    dtoObj.attrs().setStr( AID_NAME, "Charming" );
    dtoObj.rivets().ensureSkidList( RID_IMGSET_MAIN_MODEL ).add( skidRyana );
    skObjServ().defineObject( dtoObj );
    //
    dtoObj = new DtoObject( new Skid( CID_IMAGE_SET, "ryana.coy" ) );
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
      MtbBackendToFile.OPDEF_FILE_PATH.setValue( ctx.params(), avStr( "/home/dmitry/mtb-test.txt" ) );
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
