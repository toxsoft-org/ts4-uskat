package org.toxsoft.uskat.core.gui.km5.sded2.sysdecsr;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.uskat.core.api.sysdescr.ESkClassPropKind.*;
import static org.toxsoft.uskat.core.gui.km5.sded2.IKM5Sded2Constants.*;
import static org.toxsoft.uskat.core.gui.km5.sded2.sysdecsr.ISkResources.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.m5.std.models.av.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * M5-model of the {@link IDtoCmdInfo}.
 *
 * @author hazard157
 */
public class Sded2DtoCmdInfoM5Model
    extends Sded2DtoPropInfoM5ModelBase<IDtoCmdInfo> {

  /**
   * The model ID.
   */
  public static final String MODEL_ID = SDED2_M5_ID + ".DtoCmdInfo"; //$NON-NLS-1$

  /**
   * Field {@link IDtoCmdInfo#argDefs()}.
   */
  public final IM5MultiModownFieldDef<IDtoCmdInfo, IDataDef> ARG_DEFS =
      new M5MultiModownFieldDef<>( FID_ARG_DEFS, DataDefM5Model.MODEL_ID ) {

        @Override
        protected void doInit() {
          setNameAndDescription( STR_CMD_ARG_DEFS, STR_CMD_ARG_DEFS_D );
          setFlags( M5FF_DETAIL );
          params().setInt( IValedControlConstants.OPDEF_VERTICAL_SPAN, 5 );
        }

        protected IStridablesList<IDataDef> doGetFieldValue( IDtoCmdInfo aEntity ) {
          return aEntity.argDefs();
        }

      };

  /**
   * LM for this model.
   *
   * @author hazard157
   */
  class LifecycleManager
      extends LmBase {

    public LifecycleManager( IM5Model<IDtoCmdInfo> aModel ) {
      super( aModel );
    }

    // ------------------------------------------------------------------------------------
    // implementation
    //

    private IDtoCmdInfo makeCmdInfo( IM5Bunch<IDtoCmdInfo> aValues ) {
      String id = aValues.getAsAv( FID_ID ).asString();
      StridablesList<IDataDef> argDefs = new StridablesList<>( ARG_DEFS.getFieldValue( aValues ) );
      IOptionSetEdit params = new OptionSet();
      if( aValues.originalEntity() != null ) {
        params.setAll( aValues.originalEntity().params() );
      }
      params.setStr( FID_NAME, aValues.getAsAv( FID_NAME ).asString() );
      params.setStr( FID_DESCRIPTION, aValues.getAsAv( FID_DESCRIPTION ).asString() );
      IDtoCmdInfo inf = DtoCmdInfo.create1( id, argDefs, params );
      return inf;
    }

    // ------------------------------------------------------------------------------------
    // LmBase
    //

    @Override
    protected ValidationResult doBeforeCreate( IM5Bunch<IDtoCmdInfo> aValues ) {
      // no additional checks are needed
      return super.doBeforeCreate( aValues );
    }

    @Override
    protected IDtoCmdInfo doCreate( IM5Bunch<IDtoCmdInfo> aValues ) {
      return makeCmdInfo( aValues );
    }

    @Override
    protected ValidationResult doBeforeEdit( IM5Bunch<IDtoCmdInfo> aValues ) {
      // no additional checks are needed
      return super.doBeforeEdit( aValues );
    }

    @Override
    protected IDtoCmdInfo doEdit( IM5Bunch<IDtoCmdInfo> aValues ) {
      return makeCmdInfo( aValues );
    }

    @Override
    protected ValidationResult doBeforeRemove( IDtoCmdInfo aEntity ) {
      // no additional checks are needed
      return super.doBeforeRemove( aEntity );
    }

    @Override
    protected void doRemove( IDtoCmdInfo aEntity ) {
      // nop
    }

  }

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public Sded2DtoCmdInfoM5Model( ISkConnection aConn ) {
    super( MODEL_ID, IDtoCmdInfo.class, ESkClassPropKind.CMD, aConn );
    setNameAndDescription( CMD.nmName(), CMD.description() );
    addFieldDefs( ARG_DEFS );
  }

  @Override
  protected IM5LifecycleManager<IDtoCmdInfo> doCreateDefaultLifecycleManager() {
    return new LifecycleManager( this );
  }

  @Override
  protected IM5LifecycleManager<IDtoCmdInfo> doCreateLifecycleManager( Object aMaster ) {
    return getLifecycleManager( null );
  }

}
