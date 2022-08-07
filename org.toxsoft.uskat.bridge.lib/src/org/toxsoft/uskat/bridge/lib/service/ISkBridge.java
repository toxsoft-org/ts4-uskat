package org.toxsoft.uskat.bridge.lib.service;

import static org.toxsoft.uskat.bridge.lib.ISkBridgesHardConstants.*;

import org.toxsoft.uskat.core.api.objserv.*;

/**
 * The bridge object.
 *
 * @author hazard157
 */
public interface ISkBridge
    extends ISkObject {

  /**
   * Class ID.
   */
  String CLASS_ID = CLSID_BRIDGE;

  // TODO bridge attributes: type, protocol, etc.

  // TODO bridge RTdata: isOnline, metrics, etc.

  // TODO bridge config: maybe BLOB string (or several BLOBs)?

}
