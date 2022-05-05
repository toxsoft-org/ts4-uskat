package org.toxsoft.uskat.core.utils;

import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Mixin interface for entities that work with the USkat connection.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface IUSkatConnected {

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

  // TODO default ISkClobService skClobServ() {
  // return coreApi().clobService();
  // }

}
