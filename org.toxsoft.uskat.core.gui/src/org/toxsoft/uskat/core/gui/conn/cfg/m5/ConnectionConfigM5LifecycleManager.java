package org.toxsoft.uskat.core.gui.conn.cfg.m5;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.gui.conn.cfg.m5.IConnectionConfigM5Constants.*;
import static org.toxsoft.uskat.core.gui.conn.cfg.m5.ISkResources.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.strid.idgen.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.gui.conn.cfg.*;

/**
 * Lifecycle manager for model {@link ConnectionConfigM5Model}.
 *
 * @author hazard157
 */
class ConnectionConfigM5LifecycleManager
    extends M5LifecycleManager<IConnectionConfig, IConnectionConfigService> {

  private static final String CONN_CFG_ID_PREFIX = ISkHardConstants.SK_ID + ".ConnCfg"; //$NON-NLS-1$

  private final IStridGenerator idGen = new UuidStridGenerator( UuidStridGenerator.createState( CONN_CFG_ID_PREFIX ) );

  /**
   * Constructor.
   *
   * @param aModel {@link IM5Model}&lt;T&gt; - the model
   * @param aMaster {@link IConnectionConfigService} - master object
   * @throws TsNullArgumentRtException model is <code>null</code>
   */
  public ConnectionConfigM5LifecycleManager( IM5Model<IConnectionConfig> aModel, IConnectionConfigService aMaster ) {
    super( aModel, true, true, true, true, aMaster );
    TsNullArgumentRtException.checkNull( aMaster );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private static ConnectionConfig makeConfig( IM5Bunch<IConnectionConfig> aValues ) {
    String id = aValues.getAsAv( FID_ID ).asString();
    IOptionSetEdit params = new OptionSet( aValues.getAs( FID_PARAMS, IOptionSet.class ) );
    params.setStr( TSID_NAME, aValues.getAsAv( FID_NAME ).asString() );
    params.setStr( TSID_DESCRIPTION, aValues.getAsAv( FID_DESCRIPTION ).asString() );
    String providerId = aValues.getAs( FID_PROVIDER_ID, String.class );
    IOptionSet values = aValues.getAs( FID_VALUES, IOptionSet.class );
    return new ConnectionConfig( id, providerId, params, values );
  }

  // ------------------------------------------------------------------------------------
  // M5LifecycleManager
  //

  @Override
  protected void doSetupNewItemValues( IM5BunchEdit<IConnectionConfig> aValues ) {
    String id = idGen.nextId();
    aValues.set( FID_ID, avStr( id ) );
  }

  @Override
  protected ValidationResult doBeforeCreate( IM5Bunch<IConnectionConfig> aValues ) {
    String providerId = aValues.getAs( FID_PROVIDER_ID, String.class );
    if( providerId == null ) {
      return ValidationResult.error( MSG_ERR_NO_CC_PROVIDER );
    }
    ConnectionConfig cfg = makeConfig( aValues );
    return master().svs().validator().canAddConfig( master(), cfg );
  }

  @Override
  protected IConnectionConfig doCreate( IM5Bunch<IConnectionConfig> aValues ) {
    ConnectionConfig cfg = makeConfig( aValues );
    master().defineConfig( cfg );
    return cfg;
  }

  @Override
  protected ValidationResult doBeforeEdit( IM5Bunch<IConnectionConfig> aValues ) {
    String providerId = aValues.getAs( FID_PROVIDER_ID, String.class );
    if( providerId == null ) {
      return ValidationResult.error( MSG_ERR_NO_CC_PROVIDER );
    }
    ConnectionConfig cfg = makeConfig( aValues );
    return master().svs().validator().canReplaceConfig( master(), cfg, aValues.originalEntity() );
  }

  @Override
  protected IConnectionConfig doEdit( IM5Bunch<IConnectionConfig> aValues ) {
    ConnectionConfig cfg = makeConfig( aValues );
    master().defineConfig( cfg );
    return cfg;
  }

  @Override
  protected ValidationResult doBeforeRemove( IConnectionConfig aEntity ) {
    return master().svs().validator().canRemoveConfig( master(), aEntity.id() );
  }

  @Override
  protected void doRemove( IConnectionConfig aEntity ) {
    master().removeConfig( aEntity.id() );
  }

  @Override
  protected IList<IConnectionConfig> doListEntities() {
    return master().listConfigs();
  }

}
