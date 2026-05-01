package org.toxsoft.uskat.core.gui.km5.sded2.sysdecsr;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.api.sysdescr.ESkClassPropKind.*;
import static org.toxsoft.uskat.core.gui.km5.sded2.IKM5Sded2Constants.*;
import static org.toxsoft.uskat.core.gui.km5.sded2.sysdecsr.ISkResources.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.km5.sgw.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * M5-model of the {@link IDtoRivetInfo}.
 *
 * @author hazard157
 */
public class Sded2DtoRivetInfoM5Model
    extends Sded2DtoPropInfoM5ModelBase<IDtoRivetInfo> {

  /**
   * The model ID.
   */
  public static final String MODEL_ID = SDED2_M5_ID + ".DtoRivetInfo"; //$NON-NLS-1$

  /**
   * ID of field right class ids of link.
   */
  public static final String FID_RIGHT_CLASS_ID = "rightClassId"; //$NON-NLS-1$

  /**
   * SingleLookup field {@link IDtoRivetInfo#rightClassId()}.
   */
  public final IM5SingleLookupKeyFieldDef<IDtoRivetInfo, ISkClassInfo> CLASS_ID = new M5SingleLookupKeyFieldDef<>(
      FID_RIGHT_CLASS_ID, ISgwM5Constants.MID_SGW_CLASS_INFO, ISgwM5Constants.FID_CLASS_ID, ISkClassInfo.class ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_RIVET_CLASS_ID, STR_RIVET_CLASS_ID_D );
      setFlags( M5FF_COLUMN );
    }

    protected ISkClassInfo doGetFieldValue( IDtoRivetInfo aEntity ) {
      return skConn().coreApi().sysdescr().findClassInfo( aEntity.rightClassId() );
    }

  };

  /**
   * M5-attribute {@link IDtoRivetInfo#count()}.
   */
  public final IM5AttributeFieldDef<IDtoRivetInfo> COUNT = new M5AttributeFieldDef<>( FID_COUNT, DT_INTEGER ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_RIVET_COUNT, STR_RIVET_COUNT_D );
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
      extends LmBase {

    public LifecycleManager( IM5Model<IDtoRivetInfo> aModel ) {
      super( aModel );
    }

    // ------------------------------------------------------------------------------------
    // implementation
    //

    private IDtoRivetInfo makeRivetInfo( IM5Bunch<IDtoRivetInfo> aValues ) {
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

    // ------------------------------------------------------------------------------------
    // LmBase
    //

    @Override
    protected ValidationResult doBeforeCreate( IM5Bunch<IDtoRivetInfo> aValues ) {
      // no additional checks are needed
      return super.doBeforeCreate( aValues );
    }

    @Override
    protected IDtoRivetInfo doCreate( IM5Bunch<IDtoRivetInfo> aValues ) {
      return makeRivetInfo( aValues );
    }

    @Override
    protected ValidationResult doBeforeEdit( IM5Bunch<IDtoRivetInfo> aValues ) {
      // no additional checks are needed
      return super.doBeforeEdit( aValues );
    }

    @Override
    protected IDtoRivetInfo doEdit( IM5Bunch<IDtoRivetInfo> aValues ) {
      return makeRivetInfo( aValues );
    }

    @Override
    protected ValidationResult doBeforeRemove( IDtoRivetInfo aEntity ) {
      // no additional checks are needed
      return super.doBeforeRemove( aEntity );
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
  public Sded2DtoRivetInfoM5Model( ISkConnection aConn ) {
    super( MODEL_ID, IDtoRivetInfo.class, ESkClassPropKind.RIVET, aConn );
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
