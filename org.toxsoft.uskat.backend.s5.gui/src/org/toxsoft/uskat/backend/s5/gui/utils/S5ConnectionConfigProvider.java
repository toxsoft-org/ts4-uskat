package org.toxsoft.uskat.backend.s5.gui.utils;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.backend.s5.gui.utils.ISkResources.*;

import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.uskat.core.gui.conn.cfg.*;
import org.toxsoft.uskat.s5.client.remote.*;

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

    // TODO S5ConnectionConfigProvider.doProcessArgs()

  }

}
