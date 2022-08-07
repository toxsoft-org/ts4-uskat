package org.toxsoft.uskat.bridge.lib.halfs;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.uskat.bridge.lib.halfs.inner.*;
import org.toxsoft.uskat.bridge.lib.halfs.outer.*;

/**
 * The bridge.
 *
 * @author hazard157
 */
public class Bridge {

  private final AbstractBridgeOuterHalf outerHalf;
  private final BridgeInnerHalf         innerHalf;

  public Bridge( ITsContext aArgs, IBridgeOuterHalfCreator aOuterCreator ) {
    innerHalf = new BridgeInnerHalf( aArgs );
    outerHalf = aOuterCreator.cretaeOuterHalf( aArgs, innerHalf );
  }

}
