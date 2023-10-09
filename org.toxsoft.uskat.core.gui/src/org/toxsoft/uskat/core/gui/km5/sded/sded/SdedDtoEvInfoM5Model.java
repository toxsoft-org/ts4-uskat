package org.toxsoft.uskat.core.gui.km5.sded.sded;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.api.sysdescr.ESkClassPropKind.*;
import static org.toxsoft.uskat.core.gui.km5.sded.IKM5SdedConstants.*;
import static org.toxsoft.uskat.core.gui.km5.sded.ISkSdedKm5SharedResources.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.m5.std.models.av.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * M5-model of the {@link IDtoEventInfo}.
 *
 * @author dima
 */
public class SdedDtoEvInfoM5Model
    extends SdedDtoPropInfoM5ModelBase<IDtoEventInfo> {

  /**
   * M5-attribute {@link IDtoRtdataInfo#isHist()}.
   */
  public final IM5AttributeFieldDef<IDtoEventInfo> IS_HIST = new M5AttributeFieldDef<>( FID_IS_HIST, DDEF_BOOLEAN ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_IS_HIST, STR_D_IS_HIST );
      setFlags( M5FF_DETAIL );
    }

    @Override
    protected IAtomicValue doGetFieldValue( IDtoEventInfo aEntity ) {
      return avBool( aEntity.isHist() );
    }

  };

  /**
   * Field {@link IDtoEventInfo#paramDefs()}.
   */
  public final IM5MultiModownFieldDef<IDtoEventInfo, IDataDef> PARAM_DEFS =
      new M5MultiModownFieldDef<>( FID_PARAM_DEFS, DataDefM5Model.MODEL_ID ) {

        @Override
        protected void doInit() {
          setNameAndDescription( STR_N_PARAM_DEFS, STR_D_PARAM_DEFS );
          setFlags( M5FF_DETAIL );
          params().setInt( IValedControlConstants.OPDEF_VERTICAL_SPAN, 5 );

        }

        protected IStridablesList<IDataDef> doGetFieldValue( IDtoEventInfo aEntity ) {
          return aEntity.paramDefs();
        }

      };

  /**
   * LM for this model.
   *
   * @author dima
   */
  class LifecycleManager
      extends PropLifecycleManagerBase {

    public LifecycleManager( IM5Model<IDtoEventInfo> aModel ) {
      super( aModel );
    }

    private IDtoEventInfo makeAttrInfo( IM5Bunch<IDtoEventInfo> aValues ) {
      String id = aValues.getAsAv( FID_ID ).asString();
      IAtomicValue isHist = IS_HIST.getFieldValue( aValues );

      IStridablesList<IDataDef> dataDefs = (IStridablesList<IDataDef>)PARAM_DEFS.getFieldValue( aValues );

      IOptionSetEdit params = new OptionSet();
      if( aValues.originalEntity() != null ) {
        params.setAll( aValues.originalEntity().params() );
      }
      params.setStr( FID_NAME, aValues.getAsAv( FID_NAME ).asString() );
      params.setStr( FID_DESCRIPTION, aValues.getAsAv( FID_DESCRIPTION ).asString() );
      IDtoEventInfo inf = DtoEventInfo.create1( id, isHist.asBool(), dataDefs, params );
      return inf;
    }

    @Override
    protected IDtoEventInfo doCreate( IM5Bunch<IDtoEventInfo> aValues ) {
      return makeAttrInfo( aValues );
    }

    @Override
    protected IDtoEventInfo doEdit( IM5Bunch<IDtoEventInfo> aValues ) {
      return makeAttrInfo( aValues );
    }

    @Override
    protected void doRemove( IDtoEventInfo aEntity ) {
      // nop
    }

  }

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SdedDtoEvInfoM5Model( ISkConnection aConn ) {
    super( MID_SDED_EVENT_INFO, IDtoEventInfo.class, aConn );
    setNameAndDescription( EVENT.nmName(), EVENT.description() );
    addFieldDefs( IS_HIST, PARAM_DEFS );
  }

  @Override
  protected IM5LifecycleManager<IDtoEventInfo> doCreateDefaultLifecycleManager() {
    return new LifecycleManager( this );
  }

  @Override
  protected IM5LifecycleManager<IDtoEventInfo> doCreateLifecycleManager( Object aMaster ) {
    return getLifecycleManager( null );
  }

}
