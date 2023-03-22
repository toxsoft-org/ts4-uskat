package org.toxsoft.uskat.base.gui.conn.cfg.m5;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.uskat.base.gui.conn.cfg.m5.IConnectionConfigM5Constants.*;
import static org.toxsoft.uskat.base.gui.conn.cfg.m5.ISkResources.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.m5.std.fields.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.controls.basic.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.base.gui.conn.cfg.*;

/**
 * M5-model of {@link IConnectionConfig}.
 *
 * @author hazard157
 */
public class ConnectionConfigM5Model
    extends M5Model<IConnectionConfig> {

  /**
   * Attribute {@link IConnectionConfig#id()}
   */
  public final IM5AttributeFieldDef<IConnectionConfig> ID = new M5StdFieldDefId<>() {

    @Override
    protected void doInit() {
      super.doInit();
      setFlags( M5FF_HIDDEN );
    }

  };

  /**
   * Attribute {@link IConnectionConfig#nmName()}
   */
  public final IM5AttributeFieldDef<IConnectionConfig> NAME = new M5StdFieldDefName<>();

  /**
   * Attribute {@link IConnectionConfig#description()}
   */
  public final IM5AttributeFieldDef<IConnectionConfig> DESCRIPTION = new M5StdFieldDefDescription<>() {

    @Override
    protected void doInit() {
      setFlags( M5FF_COLUMN );
    }
  };

  /**
   * Field {@link IConnectionConfig#providerId()}
   */
  public final IM5FieldDef<IConnectionConfig, String> PROVIDER_ID = new M5FieldDef<>( FID_PROVIDER_ID, String.class ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_PROVIDER_ID, STR_D_PROVIDER_ID );
      setFlags( M5FF_DETAIL );
      setValedEditor( ValedProviderIdCombo.FACTORY_NAME );
    }

    protected String doGetFieldValue( IConnectionConfig aEntity ) {
      return aEntity.providerId();
    }

  };

  /**
   * Field {@link IConnectionConfig#opValues()}
   */
  public final IM5FieldDef<IConnectionConfig, IOptionSet> VALUES = new M5FieldDef<>( FID_VALUES, IOptionSet.class ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_VALUES, STR_D_VALUES );
      setFlags( 0 );
      setValedEditor( ValedOptionSet.FACTORY_NAME );
      setDefaultValue( IOptionSet.NULL );
      params().setBool( IValedControlConstants.OPDEF_IS_WIDTH_FIXED, false );
    }

    protected IOptionSet doGetFieldValue( IConnectionConfig aEntity ) {
      return aEntity.opValues();
    }

  };

  /**
   * Field {@link IConnectionConfig#params()}
   */
  public final IM5FieldDef<IConnectionConfig, IOptionSet> PARAMS = new M5FieldDef<>( FID_PARAMS, IOptionSet.class ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_PARAMS, STR_D_PARAMS );
      setFlags( M5FF_HIDDEN );
      setValedEditor( ValedOptionSet.FACTORY_NAME );
      setDefaultValue( IOptionSet.NULL );
    }

    protected IOptionSet doGetFieldValue( IConnectionConfig aEntity ) {
      return aEntity.params();
    }

  };

  /**
   * Constructor.
   */
  public ConnectionConfigM5Model() {
    super( MID_SK_CONN_CFG, IConnectionConfig.class );
    setNameAndDescription( STR_N_M5M_CFG_NAME, STR_D_M5M_CFG_NAME );
    addFieldDefs( ID, NAME, PROVIDER_ID, DESCRIPTION, VALUES, PARAMS );
    setPanelCreator( new ConnectionConfigM5PanelCreator() );
  }

  @Override
  protected IM5LifecycleManager<IConnectionConfig> doCreateDefaultLifecycleManager() {
    IConnectionConfigService ccs = tsContext().get( IConnectionConfigService.class );
    TsInternalErrorRtException.checkNull( ccs );
    return new ConnectionConfigM5LifecycleManager( this, ccs );
  }

  @Override
  protected IM5LifecycleManager<IConnectionConfig> doCreateLifecycleManager( Object aMaster ) {
    return new ConnectionConfigM5LifecycleManager( this, IConnectionConfigService.class.cast( aMaster ) );
  }

}
