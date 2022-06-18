package org.toxsoft.uskat.core;

import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.api.clobserv.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.api.gwids.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * USkat core API.
 *
 * @author hazard157
 */
public interface ISkCoreApi {

  /**
   * Returns the system description service.
   *
   * @return {@link ISkSysdescr} - the system description service
   */
  ISkSysdescr sysdescr();

  /**
   * Returns the objects management service.
   *
   * @return {@link ISkObjectService} - the objects service
   */
  ISkObjectService objService();

  /**
   * Returns the CLOB service.
   *
   * @return {@link ISkClobService} - the CLOB service
   */
  ISkClobService clobService();

  /**
   * Returns the command service.
   *
   * @return {@link ISkCommandService} - the command service
   */
  ISkCommandService cmdService();

  /**
   * Reurns the event service.
   *
   * @return {@link ISkEventService} - the event service
   */
  ISkEventService eventService();

  /**
   * Returns the link service.
   *
   * @return {@link ISkLinkService} - the link service
   */
  ISkLinkService linkService();

  /**
   * Returns the RTdata service.
   *
   * @return {@link ISkRtdataService} - the RTdata service
   */
  ISkRtdataService rtdService();

  /**
   * Returns the user service.
   *
   * @return {@link ISkUserService} - the user service
   */
  ISkUserService userService();

  /**
   * Returns the GWID service - helper methods collection to work with GWIDs in context of this system.
   *
   * @return {@link ISkGwidService} - the GWID service
   */
  ISkGwidService gwidService();

  // ------------------------------------------------------------------------------------
  // all services

  /**
   * Returns all services.
   *
   * @return {@link IStringMap}&lt;{@link ISkService}&gt; - map of all services "service ID" - "service"
   */
  IStringMap<ISkService> services();

  /**
   * Returns the service.
   *
   * @param <S> - expected type of the service
   * @param aServiceId String - service identifier
   * @return &lt;S&gt; - the service
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such service
   */
  <S extends ISkService> S getService( String aServiceId );

  /**
   * Adds and initializes new service to the runnning uskat.
   *
   * @param <S> - the type of the service
   * @param aCreator {@link ISkServiceCreator} - service constructor
   * @return &lt;S&gt; - the created and initialized service
   * @throws TsNullArgumentRtException argument = <code>null</code>
   * @throws TsIllegalStateRtException uskat is not running
   * @throws TsItemAlreadyExistsRtException service with the same identifier already exists
   * @throws RuntimeException service creation or initialization error
   */
  <S extends AbstractSkService> S addService( ISkServiceCreator<S> aCreator );

}
