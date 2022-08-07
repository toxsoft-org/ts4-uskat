package org.toxsoft.uskat.bridge.lib.service;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * USkat core service to work with bridges.
 *
 * @author hazard157
 */
public interface ISkBridgeService {

  IStridablesList<ISkObject> listBridges();

  ISkBridge findBridgeObject( String aBridgeId );

  void createBridgeObject( IDtoFullObject aBrdgeInfo );

  // TODO bridge service API

}
