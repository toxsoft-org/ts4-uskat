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
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.km5.sgw.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * M5-model of the {@link IDtoRivetInfo}.
 *
 * @author dima
 */
public class SdedDtoRivetInfoM5Model
    extends SdedDtoPropInfoM5ModelBase<IDtoRivetInfo> {

  /**
   * SingleLookup field {@link IDtoRivetInfo#rightClassId()}.
   */
  public final IM5SingleLookupKeyFieldDef<IDtoRivetInfo, ISkClassInfo> CLASS_ID = new M5SingleLookupKeyFieldDef<>(
      FID_RIGHT_CLASS_ID, ISgwM5Constants.MID_SGW_CLASS_INFO, ISgwM5Constants.FID_CLASS_ID, ISkClassInfo.class ) {

    protected ISkClassInfo doGetFieldValue( IDtoRivetInfo aEntity ) {
      return skConn().coreApi().sysdescr().findClassInfo( aEntity.rightClassId() );
    }

  };

  /**
   * M5-attribute {@link IDtoRivetInfo#count()}.
   */
  public final IM5AttributeFieldDef<IDtoRivetInfo> COUNT = new M5AttributeFieldDef<>( FID_COUNT, DDEF_INTEGER ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_RIVETED_COUNT, STR_D_RIVETED_COUNT );
      setFlags( M5FF_DETAIL );
    }

    @Override
    protected IAtomicValue doGetFieldValue( IDtoRivetInfo aEntity ) {
      return avInt( aEntity.count() );
    }

  };

  /**
   * LM for this model.
   *
   * @author hazard157
   */
  class LifecycleManager
      extends PropLifecycleManagerBase {

    public LifecycleManager( IM5Model<IDtoRivetInfo> aModel ) {
      super( aModel );
    }

    private IDtoRivetInfo makeDtoRivetInfo( IM5Bunch<IDtoRivetInfo> aValues ) {
      String id = aValues.getAsAv( FID_ID ).asString();
      ISkClassInfo classInfo = CLASS_ID.getFieldValue( aValues );
      IAtomicValue count = COUNT.getFieldValue( aValues );
      IOptionSetEdit params = new OptionSet();
      if( aValues.originalEntity() != null ) {
        params.setAll( aValues.originalEntity().params() );
      }
      params.setStr( FID_NAME, aValues.getAsAv( FID_NAME ).asString() );
      params.setStr( FID_DESCRIPTION, aValues.getAsAv( FID_DESCRIPTION ).asString() );
      IDtoRivetInfo inf = DtoRivetInfo.create1( id, classInfo.id(), count.asInt(), params );
      return inf;
    }

    @Override
    protected IDtoRivetInfo doCreate( IM5Bunch<IDtoRivetInfo> aValues ) {
      return makeDtoRivetInfo( aValues );
    }

    @Override
    protected IDtoRivetInfo doEdit( IM5Bunch<IDtoRivetInfo> aValues ) {
      return makeDtoRivetInfo( aValues );
    }

    @Override
    protected void doRemove( IDtoRivetInfo aEntity ) {
      // nop
    }

  }

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SdedDtoRivetInfoM5Model( ISkConnection aConn ) {
    super( MID_SDED_RIVET_INFO, IDtoRivetInfo.class, aConn );
    setNameAndDescription( RIVET.nmName(), RIVET.description() );
    addFieldDefs( CLASS_ID, COUNT );
  }

  @Override
  protected IM5LifecycleManager<IDtoRivetInfo> doCreateDefaultLifecycleManager() {
    return new LifecycleManager( this );
  }

  @Override
  protected IM5LifecycleManager<IDtoRivetInfo> doCreateLifecycleManager( Object aMaster ) {
    return getLifecycleManager( null );
  }

}
