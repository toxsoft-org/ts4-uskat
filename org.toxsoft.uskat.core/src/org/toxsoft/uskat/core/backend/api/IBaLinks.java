package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.linkserv.*;

/**
 * Backend addon for links storage.
 * <p>
 * This is the mandatory addon.
 *
 * @author hazard157
 */
public interface IBaLinks
    extends IBackendAddon {

  /**
   * ID of this backend addon.
   */
  String ADDON_ID = SK_ID + "ba.Links"; //$NON-NLS-1$

  /**
   * Finds the forward link.
   * <p>
   * Argument <code>aClassId</code> must be the class where the specified link is defined, not the subclass. Generally
   * backend is "dumb" and does not knows about class inheritance.
   *
   * @param aClassId String - link definition class ID
   * @param aLinkId String - the link ID
   * @param aLeftSkid {@link Skid} - the left object SKID
   * @return {@link IDtoLinkFwd} - forward link, may be <code>null</code> if no objects are linked
   */
  IDtoLinkFwd findLinkFwd( String aClassId, String aLinkId, Skid aLeftSkid );

  /**
   * Returns all forward links stored in backend.
   * <p>
   * The empty links may not be returned so not all links defined for {@link Skid#classId()} class may be returned.
   *
   * @param aLeftSkid {@link Skid} - the object SKID
   * @return {@link IList}&lt;{@link IDtoLinkFwd}&gt; - list of forward links
   */
  IList<IDtoLinkFwd> getAllLinksFwd( Skid aLeftSkid );

  /**
   * Finds the reverse link.
   * <p>
   * Argument <code>aClassId</code> must be the class where the specified link is defined, not the subclass. Generally
   * backend is "dumb" and does not knows about class inheritance.
   *
   * @param aClassId String - link definition class ID
   * @param aLinkId String - the link ID
   * @param aRightSkid {@link Skid} - right object SKID
   * @param aLeftClassIds {@link IStringList} - IDs of queried left class or an empty list for all left classes
   * @return {@link IDtoLinkRev} - the left link, may be <code>null</code> if no left objects are linkes
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  IDtoLinkRev findLinkRev( String aClassId, String aLinkId, Skid aRightSkid, IStringList aLeftClassIds );

  /**
   * Returns all reverse links of the specified object.
   * <p>
   * Returned list may contain multiple elements with the same {@link IDtoLinkRev#gwid()} values.
   *
   * @param aRightSkid {@link Skid} - the object SKID
   * @return {@link IList}&lt;{@link IDtoLinkRev}&gt; - list of reverse links
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  IList<IDtoLinkRev> getAllLinksRev( Skid aRightSkid );

  /**
   * Stores links.
   * <p>
   * If any of the {@link IDtoLinkFwd#rightSkids()} is empty such link will be removed.
   * <p>
   * Elements of argument are applied sequentaly.
   *
   * @param aLinks {@link IList}&lt;{@link IDtoLinkFwd}&gt; - list of the links
   */
  void writeLinksFwd( IList<IDtoLinkFwd> aLinks );

}
