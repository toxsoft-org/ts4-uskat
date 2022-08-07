package org.toxsoft.uskat.bridge.lib.halfs.outer;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.uskat.bridge.lib.halfs.*;
import org.toxsoft.uskat.bridge.lib.halfs.inner.*;

/**
 * Base class to implement outer half of the bridge.
 *
 * @author hazard157
 */
public class AbstractBridgeOuterHalf
    extends AbstractBridgeHalf {

  private final BridgeInnerHalf innerHalf;

  protected AbstractBridgeOuterHalf( ITsContext aArgs, BridgeInnerHalf aInnerHalf ) {
    super( aArgs );
    innerHalf = aInnerHalf;
  }

}
