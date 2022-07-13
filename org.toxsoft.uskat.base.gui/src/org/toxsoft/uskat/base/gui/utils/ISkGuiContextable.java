package org.toxsoft.uskat.base.gui.utils;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.uskat.base.gui.conn.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.api.clobserv.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.api.gwids.*;
import org.toxsoft.uskat.core.api.hqserv.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Mixin interface of entities havind access to ISkConnection.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface ISkGuiContextable
    extends ITsGuiContextable {

  /**
   * Returns {@link ISkConnection} - connection to the USkat system.
   * <p>
   * This is the only method to be implementaed by subclass.
   *
   * @return {@link ISkConnection} - connection to USkat.
   */
  ISkConnection skConn();

  default ISkConnectionSupplier connectionSupplier() {
    return tsContext().get( ISkConnectionSupplier.class );
  }

  default ISkCoreApi coreApi() {
    return skConn().coreApi();
  }

  default <S extends ISkService> S skService( String aServiceId ) {
    return coreApi().getService( aServiceId );
  }

  default ISkSysdescr skSysdescr() {
    return coreApi().sysdescr();
  }

  default ISkGwidService skGwidMan() {
    return coreApi().gwidService();
  }

  default ISkObjectService skOs() {
    return coreApi().objService();
  }

  default ISkLinkService skLs() {
    return coreApi().linkService();
  }

  default ISkClobService skLobServ() {
    return coreApi().clobService();
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

  // HERE add more convinience methods

}
