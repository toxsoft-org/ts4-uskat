package org.toxsoft.uskat.core.gui.km5.sded2.sysdecsr;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tsgui.m5.valeds.IM5ValedConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.api.sysdescr.ESkClassPropKind.*;
import static org.toxsoft.uskat.core.gui.km5.sded2.IKM5Sded2Constants.*;
import static org.toxsoft.uskat.core.gui.km5.sded2.sysdecsr.ISkResources.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.m5.std.models.av.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * M5-model of the {@link IDtoRtdataInfo}.
 *
 * @author hazard157
 */
public class Sded2DtoRtdataInfoM5Model
    extends Sded2DtoPropInfoM5ModelBase<IDtoRtdataInfo> {

  /**
   * The model ID.
   */
  public static final String MODEL_ID = SDED2_M5_ID + ".DtoRtdataInfo"; //$NON-NLS-1$

  /**
   * Field {@link IDtoRtdataInfo#dataType()}.
   */
  public final IM5SingleModownFieldDef<IDtoRtdataInfo, IDataType> DATA_TYPE =
      new M5SingleModownFieldDef<>( FID_DATA_TYPE, DataTypeM5Model.MODEL_ID ) {

        @Override
        protected void doInit() {
          setNameAndDescription( STR_RTDATA_DATA_TYPE, STR_RTDATA_DATA_TYPE_D );
          params().setBool( TSID_IS_NULL_ALLOWED, false );
          params().setStr( M5_VALED_OPDEF_WIDGET_TYPE_ID, M5VWTID_INPLACE );
          params().setInt( IValedControlConstants.OPDEF_VERTICAL_SPAN, 10 );
          params().setBool( IValedControlConstants.OPDEF_IS_HEIGHT_FIXED, false );
          params().setBool( IValedControlConstants.OPDEF_NO_FIELD_LABEL, true );
          setFlags( M5FF_DETAIL );
        }

        protected IDataType doGetFieldValue( IDtoRtdataInfo aEntity ) {
          return aEntity.dataType();
        }

      };

  /**
   * Field {@link IDtoRtdataInfo#isSync()}.
   */
  public final IM5AttributeFieldDef<IDtoRtdataInfo> IS_CURR = new M5AttributeFieldDef<>( FID_IS_CURR, DT_BOOLEAN ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_RTDATA_IS_CURR, STR_RTDATA_IS_CURR_D );
      setFlags( M5FF_DETAIL );
    }

    @Override
    protected IAtomicValue doGetFieldValue( IDtoRtdataInfo aEntity ) {
      return avBool( aEntity.isCurr() );
    }

  };

  /**
   * Field {@link IDtoRtdataInfo#isHist()}.
   */
  public final IM5AttributeFieldDef<IDtoRtdataInfo> IS_HIST = new M5AttributeFieldDef<>( FID_IS_HIST, DT_BOOLEAN ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_RTDATA_IS_HIST, STR_RTDATA_IS_HIST_D );
      setFlags( M5FF_DETAIL );
    }

    @Override
    protected IAtomicValue doGetFieldValue( IDtoRtdataInfo aEntity ) {
      return avBool( aEntity.isHist() );
    }

  };

  /**
   * Field {@link IDtoRtdataInfo#isSync()}.
   */
  public final IM5AttributeFieldDef<IDtoRtdataInfo> IS_SYNC = new M5AttributeFieldDef<>( FID_IS_SYNC, DT_BOOLEAN ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_RTDATA_IS_SYNC, STR_RTDATA_IS_SYNC_D );
      setFlags( M5FF_DETAIL );
    }

    @Override
    protected IAtomicValue doGetFieldValue( IDtoRtdataInfo aEntity ) {
      return avBool( aEntity.isSync() );
    }

  };

  /**
   * Field {@link IDtoRtdataInfo#syncDataDeltaT()}.
   */
  public final IM5AttributeFieldDef<IDtoRtdataInfo> SYNC_DELTA_T =
      new M5AttributeFieldDef<>( FID_SYNC_DELTA_T, DT_INTEGER ) {

        @Override
        protected void doInit() {
          setNameAndDescription( STR_RTDATA_SYNC_DELTA_T, STR_RTDATA_SYNC_DELTA_T_D );
          setFlags( M5FF_DETAIL );
          setDefaultValue( AvUtils.AV_1 );
        }

        @Override
        protected IAtomicValue doGetFieldValue( IDtoRtdataInfo aEntity ) {
          return avInt( aEntity.syncDataDeltaT() );
        }

      };

  /**
   * LM for this model.
   *
   * @author hazard157
   */
  class LifecycleManager
      extends LmBase {

    public LifecycleManager( IM5Model<IDtoRtdataInfo> aModel ) {
      super( aModel );
    }

    // ------------------------------------------------------------------------------------
    // implementation
    //

    private IDtoRtdataInfo makeRtdataInfo( IM5Bunch<IDtoRtdataInfo> aValues ) {
      String id = aValues.getAsAv( FID_ID ).asString();
      IDataType dataType = DATA_TYPE.getFieldValue( aValues );
      IAtomicValue isCurr = IS_CURR.getFieldValue( aValues );
      IAtomicValue isHist = IS_HIST.getFieldValue( aValues );
      IAtomicValue isSync = IS_SYNC.getFieldValue( aValues );
      IAtomicValue syncDeltaT = SYNC_DELTA_T.getFieldValue( aValues );
      IOptionSetEdit params = new OptionSet();
      if( aValues.originalEntity() != null ) {
        params.setAll( aValues.originalEntity().params() );
      }
      params.setStr( FID_NAME, aValues.getAsAv( FID_NAME ).asString() );
      params.setStr( FID_DESCRIPTION, aValues.getAsAv( FID_DESCRIPTION ).asString() );
      IDtoRtdataInfo inf = DtoRtdataInfo.create1( id, dataType, isCurr.asBool(), isHist.asBool(), isSync.asBool(),
          syncDeltaT.asLong(), params );
      return inf;
    }

    // ------------------------------------------------------------------------------------
    // LmBase
    //

    @Override
    protected ValidationResult doBeforeCreate( IM5Bunch<IDtoRtdataInfo> aValues ) {
      // no additional checks are needed
      return super.doBeforeCreate( aValues );
    }

    @Override
    protected IDtoRtdataInfo doCreate( IM5Bunch<IDtoRtdataInfo> aValues ) {
      return makeRtdataInfo( aValues );
    }

    @Override
    protected ValidationResult doBeforeEdit( IM5Bunch<IDtoRtdataInfo> aValues ) {
      // no additional checks are needed
      return super.doBeforeEdit( aValues );
    }

    @Override
    protected IDtoRtdataInfo doEdit( IM5Bunch<IDtoRtdataInfo> aValues ) {
      return makeRtdataInfo( aValues );
    }

    @Override
    protected ValidationResult doBeforeRemove( IDtoRtdataInfo aEntity ) {
      // no additional checks are needed
      return super.doBeforeRemove( aEntity );
    }

    @Override
    protected void doRemove( IDtoRtdataInfo aEntity ) {
      // nop
    }

  }

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public Sded2DtoRtdataInfoM5Model( ISkConnection aConn ) {
    super( MODEL_ID, IDtoRtdataInfo.class, ESkClassPropKind.RTDATA, aConn );
    setNameAndDescription( RTDATA.nmName(), RTDATA.description() );
    addFieldDefs( IS_CURR, IS_HIST, IS_SYNC, SYNC_DELTA_T, DATA_TYPE );
  }

  @Override
  protected IM5LifecycleManager<IDtoRtdataInfo> doCreateDefaultLifecycleManager() {
    return new LifecycleManager( this );
  }

  @Override
  protected IM5LifecycleManager<IDtoRtdataInfo> doCreateLifecycleManager( Object aMaster ) {
    return getLifecycleManager( null );
  }

}
