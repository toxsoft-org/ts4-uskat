package org.toxsoft.uskat.core.api.linkserv;

import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.api.sysdescr.*;

/**
 * Core service: manage links between objects.
 *
 * @author hazard157
 */
public interface ISkLinkService
    extends ISkService {

  /**
   * Service ID.
   */
  String SERVICE_ID = ISkHardConstants.SK_CORE_SERVICE_ID_PREFIX + ".Links"; //$NON-NLS-1$

  /**
   * Returns the forward link.
   *
   * @param aLeftSkid {@link Skid} - left object SKID of the link
   * @param aLinkId String - the link ID
   * @return {@link IDtoLinkFwd} - forward link (never is <code>null</code>)
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such link or object exists
   */
  IDtoLinkFwd getLinkFwd( Skid aLeftSkid, String aLinkId );

  /**
   * Returns all forward links of the specified object.
   * <p>
   * The returned map alwayes contains all links even if no objects are linked. Keys in returned map are the the same as
   * keys in {@link ISkClassInfo#links()} of the left object class.
   *
   * @param aLeftSkid {@link Skid} - the object SKID
   * @return {@link IStringMap}&lt;{@link IDtoLinkFwd}&lt; - the map "link ID" - "forward link"
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such object exists
   */
  IStringMap<IDtoLinkFwd> getAllLinksFwd( Skid aLeftSkid );

  /**
   * Returns the reverse link that is finds left objects by specifying right object and link ID.
   *
   * @param aClassId String - link declaring class ID
   * @param aLinkId String - link ID
   * @param aRightSkid {@link Skid} - right object SKID
   * @return {@link IDtoLinkRev} - found reverse link may have an empty {@link IDtoLinkRev#leftSkids()}
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such link or no such left right object exists
   */
  IDtoLinkRev getLinkRev( String aClassId, String aLinkId, Skid aRightSkid );

  /**
   * Returns all reverse links of the specified object.
   *
   * @param aRightSkid {@link Skid} - the object SKID
   * @return {@link IMap}&lt;{@link Gwid},{@link ISkidList}&gt; - the map "abstract link GWID" - "left objects SKIDs"
   */
  // FIXME IMap<Gwid, ISkidList> getAllLinksRev( Skid aRightSkid );

  /**
   * Changes the right objects of the specified link.
   * <p>
   * First objects listed in <code>aRemovedSkids</code> arguments are removed from the link and then objects listed in
   * <code>aAddedSkids</code> added to the link.
   * <p>
   * Warning: there is big difference specifying two values of the <b><code>aRemovedSkids</code></b> argument:
   * <ul>
   * <li>value <code>null</code> means deletion of <b>the all linked objects</b>;</li>
   * <li>value {@link ISkidList#EMPTY} does not changes link at all.</li>
   * </ul>
   * <p>
   * This sinle method allow to create link, edit or remove objects in the right objects list. Below there are some
   * inline methods for convinience.
   *
   * @param aLeftSkid {@link Skid} - the left object SKID
   * @param aLinkId String - the link ID
   * @param aRemovedSkids {@link ISkidList} - SKIDs to remove or <code>null</code> to remove <b>all objects</b>
   * @param aAddedSkids {@link ISkidList} - objects to be added to the link
   * @return {@link IDtoLinkFwd} - created link
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed {@link ISkLinkServiceValidator} validation
   */
  IDtoLinkFwd defineLink( Skid aLeftSkid, String aLinkId, ISkidList aRemovedSkids, ISkidList aAddedSkids );

  /**
   * Removes all links of the specified left object.
   * <p>
   * Removing link is not the same as setting link to 0 objects though both actions lead to links with empty list of the
   * right objects. For example it is impossible to set 0 right objects for the link which must have exactly 1 object
   * while clearing (removing) such link is allowed. Clearing links means to reset the object state as it was at the
   * time of creation.
   *
   * @param aLeftSkid {@link Skid} - the left object SKID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed {@link ISkLinkServiceValidator} validation
   */
  void removeLinks( Skid aLeftSkid );

  // ------------------------------------------------------------------------------------
  // Service support

  /**
   * Returns the service validator.
   *
   * @return {@link ITsValidationSupport}&lt;{@link ISkLinkServiceValidator}&gt; - the service validator
   */
  ITsValidationSupport<ISkLinkServiceValidator> svs();

  /**
   * Returns the service eventer.
   *
   * @return {@link ITsEventer}&lt;{@link ISkLinkServiceListener}&gt; - the service eventer
   */
  ITsEventer<ISkLinkServiceListener> eventer();

  // ------------------------------------------------------------------------------------
  // inline methods for convinience

  /**
   * Returns the forward link by the concrete GWID.
   *
   * @param aLinkConcreteGwid {@link Gwid} - concrete GWID of the link
   * @return {@link IDtoLinkFwd} - forward link (never is <code>null</code>)
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException argument is not a concrete GWID of the link
   * @throws TsItemNotFoundRtException no such link or object exists
   */
  default IDtoLinkFwd getLinkFwd( Gwid aLinkConcreteGwid ) {
    TsNullArgumentRtException.checkNull( aLinkConcreteGwid );
    TsIllegalArgumentRtException.checkTrue( aLinkConcreteGwid.isAbstract() );
    TsIllegalArgumentRtException.checkTrue( aLinkConcreteGwid.isMulti() );
    TsIllegalArgumentRtException.checkTrue( aLinkConcreteGwid.kind() != EGwidKind.GW_LINK );
    return getLinkFwd( aLinkConcreteGwid.skid(), aLinkConcreteGwid.propId() );
  }

  /**
   * Sets the linked right objects.
   *
   * @param aLeftSkid {@link Skid} - left object SKID
   * @param aLinkId String - the link ID
   * @param aNewSkids {@link ISkidList} - right objects to be set as link
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed {@link ISkLinkServiceValidator} validation
   */
  default void setLink( Skid aLeftSkid, String aLinkId, ISkidList aNewSkids ) {
    defineLink( aLeftSkid, aLinkId, null, aNewSkids );
  }

  /**
   * Sets the specified link.
   *
   * @param aLink {@link IDtoLinkFwd} - the link to set
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed {@link ISkLinkServiceValidator} validation
   */
  default void setLink( IDtoLinkFwd aLink ) {
    TsNullArgumentRtException.checkNull( aLink );
    defineLink( aLink.leftSkid(), aLink.linkId(), null, aLink.rightSkids() );
  }

}
