package org.toxsoft.uskat.backend.s5.gui.utils;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.backend.s5.gui.utils.ISkResources.*;

import org.toxsoft.core.tslib.av.opset.impl.OptionSetUtils;
import org.toxsoft.core.tslib.bricks.ctx.ITsContext;
import org.toxsoft.uskat.core.gui.conn.cfg.ConnectionConfigProvider;
import org.toxsoft.uskat.core.gui.conn.cfg.IConnectionConfigProvider;
import org.toxsoft.uskat.s5.client.IS5ConnectionParams;
import org.toxsoft.uskat.s5.client.remote.S5RemoteBackendProvider;

/**
 * {@link IConnectionConfigProvider} implementation for connection to the S5 backend.
 *
 * @author hazard157
 */
public class S5ConnectionConfigProvider
    extends ConnectionConfigProvider {

  /**
   * The singleton instance.
   */
  public static final IConnectionConfigProvider INSTANCE = new S5ConnectionConfigProvider();

  /**
   * Constructor.
   */
  private S5ConnectionConfigProvider() {
    super( new S5RemoteBackendProvider(), OptionSetUtils.createOpSet( //
        TSID_NAME, STR_S5_CONN_CFG_PROVIDER, //
        TSID_DESCRIPTION, STR_S5_CONN_CFG_PROVIDER_D //
    ) );
  }

  @Override
  protected void doProcessArgs( ITsContext aSkConnArgs ) {
    IS5ConnectionParams.REF_CLASSLOADER.setRef( aSkConnArgs, getClass().getClassLoader() );
  }

}
