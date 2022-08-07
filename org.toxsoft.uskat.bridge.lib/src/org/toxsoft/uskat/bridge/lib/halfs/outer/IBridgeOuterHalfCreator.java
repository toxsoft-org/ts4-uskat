package org.toxsoft.uskat.bridge.lib.halfs.outer;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.uskat.bridge.lib.halfs.inner.*;

/**
 * Outer half factory.
 *
 * @author hazard157
 */
public interface IBridgeOuterHalfCreator {

  /**
   * Creates bridge outer half.
   *
   * @param aArgs {@link ITsContext} - bridge createn argu,emts
   * @param aInnerHalf {@link BridgeInnerHalf} - cretaed instance of the inner half
   * @return {@link AbstractBridgeOuterHalf} - created instance
   */
  AbstractBridgeOuterHalf cretaeOuterHalf( ITsContext aArgs, BridgeInnerHalf aInnerHalf );

}
