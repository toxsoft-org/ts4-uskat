package org.toxsoft.uskat.core.utils;

import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.clobserv.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.api.gwids.*;
import org.toxsoft.uskat.core.api.hqserv.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Mixin interface for entities that work with the USkat connection.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface ISkConnected {

  /**
   * Returns the connection to work with.
   *
   * @return {@link ISkConnection} - the connection
   */
  ISkConnection skConn();

  // ------------------------------------------------------------------------------------
  // inline methods for convenience

  default ISkCoreApi coreApi() {
    return skConn().coreApi();
  }

  default ISkSysdescr skSysdescr() {
    return coreApi().sysdescr();
  }

  default ISkObjectService skObjServ() {
    return coreApi().objService();
  }

  default ISkClobService skClobServ() {
    return coreApi().clobService();
  }

  default ISkLinkService skLinkServ() {
    return coreApi().linkService();
  }

  default ISkGwidService skGwidServ() {
    return coreApi().gwidService();
  }

  default ISkUserService skUserServ() {
    return coreApi().userService();
  }

  default ISkRtdataService skRtdataServ() {
    return coreApi().rtdService();
  }

  default ISkHistoryQueryService skHqServ() {
    return coreApi().hqService();
  }

  default ISkCommandService skCmdServ() {
    return coreApi().cmdService();
  }

  default ISkEventService skEventServ() {
    return coreApi().eventService();
  }

  // HERE add more convinience methods

}
