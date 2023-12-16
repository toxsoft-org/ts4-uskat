package org.toxsoft.uskat.core.gui.km5.sded.sded;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tsgui.m5.valeds.IM5ValedConstants.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.api.sysdescr.ESkClassPropKind.*;
import static org.toxsoft.uskat.core.gui.km5.sded.IKM5SdedConstants.*;
import static org.toxsoft.uskat.core.gui.km5.sded.ISkSdedKm5SharedResources.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.m5.std.models.av.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * M5-model of the {@link IDtoAttrInfo}.
 *
 * @author hazard157
 */
public class SdedDtoAttrInfoM5Model
    extends SdedDtoPropInfoM5ModelBase<IDtoAttrInfo> {

  /**
   * Field {@link IDtoAttrInfo#dataType()}.
   */
  public final IM5SingleModownFieldDef<IDtoAttrInfo, IDataType> DATA_TYPE =
      new M5SingleModownFieldDef<>( FID_DATA_TYPE, DataTypeM5Model.MODEL_ID ) {

        @Override
        protected void doInit() {
          setNameAndDescription( STR_N_DATA_TYPE, STR_D_DATA_TYPE );
          setFlags( M5FF_DETAIL );
          params().setBool( TSID_IS_NULL_ALLOWED, false );
          params().setStr( M5_VALED_OPDEF_WIDGET_TYPE_ID, M5VWTID_INPLACE );
          params().setInt( IValedControlConstants.OPDEF_VERTICAL_SPAN, 10 );
        }

        protected IDataType doGetFieldValue( IDtoAttrInfo aEntity ) {
          return aEntity.dataType();
        }

      };

  /**
   * LM for this model.
   *
   * @author hazard157
   */
  class LifecycleManager
      extends PropLifecycleManagerBase {

    public LifecycleManager( IM5Model<IDtoAttrInfo> aModel ) {
      super( aModel );
    }

    private IDtoAttrInfo makeAttrInfo( IM5Bunch<IDtoAttrInfo> aValues ) {
      String id = aValues.getAsAv( FID_ID ).asString();
      IDataType dataType = DATA_TYPE.getFieldValue( aValues );
      IOptionSetEdit params = new OptionSet();
      if( aValues.originalEntity() != null ) {
        params.setAll( aValues.originalEntity().params() );
      }
      params.setStr( FID_NAME, aValues.getAsAv( FID_NAME ).asString() );
      params.setStr( FID_DESCRIPTION, aValues.getAsAv( FID_DESCRIPTION ).asString() );
      DtoAttrInfo inf = DtoAttrInfo.create1( id, dataType, params );
      return inf;
    }

    @Override
    protected ValidationResult doBeforeCreate( IM5Bunch<IDtoAttrInfo> aValues ) {
      IDtoAttrInfo ainf = makeAttrInfo( aValues );
      TsTestUtils.pl( "%s", ainf.toString() );
      // TODO Auto-generated method stub
      return super.doBeforeCreate( aValues );
    }

    @Override
    protected IDtoAttrInfo doCreate( IM5Bunch<IDtoAttrInfo> aValues ) {
      return makeAttrInfo( aValues );
    }

    @Override
    protected IDtoAttrInfo doEdit( IM5Bunch<IDtoAttrInfo> aValues ) {
      return makeAttrInfo( aValues );
    }

    @Override
    protected void doRemove( IDtoAttrInfo aEntity ) {
      // nop
    }

  }

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SdedDtoAttrInfoM5Model( ISkConnection aConn ) {
    super( MID_SDED_ATTR_INFO, IDtoAttrInfo.class, aConn );
    setNameAndDescription( ATTR.nmName(), ATTR.description() );
    addFieldDefs( DATA_TYPE );
  }

  @Override
  protected IM5LifecycleManager<IDtoAttrInfo> doCreateDefaultLifecycleManager() {
    return new LifecycleManager( this );
  }

  @Override
  protected IM5LifecycleManager<IDtoAttrInfo> doCreateLifecycleManager( Object aMaster ) {
    return getLifecycleManager( null );
  }

}
