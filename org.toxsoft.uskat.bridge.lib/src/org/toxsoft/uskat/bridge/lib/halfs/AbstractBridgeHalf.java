package org.toxsoft.uskat.bridge.lib.halfs;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.utils.*;

/**
 * Base class for both halgfs of bridge.
 *
 * @author hazard157
 */
public class AbstractBridgeHalf
    implements ICloseable {

  private final ITsContext args;

  /**
   * Constructor for descendants.
   *
   * @param aArgs {@link ITsContext} - creation arguments
   */
  protected AbstractBridgeHalf( ITsContext aArgs ) {
    args = aArgs;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * returns bridge creation arguments.
   *
   * @return {@link ITsContext} - bridge creation argumetns
   */
  public ITsContext creatrionArgs() {
    return args;
  }

  // ------------------------------------------------------------------------------------
  // Packcgae API: lifecycle management
  //

  void initialize() {
    // TODO AbstractBridgeHalf.initialize()
  }

  @Override
  public void close() {
    // TODO Auto-generated method stub

  }

}
