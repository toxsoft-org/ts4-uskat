package org.toxsoft.uskat.core.api.gwids;

import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.api.sysdescr.*;

/**
 * Helper service to work with GWIDs.
 *
 * @author hazard157
 */
public interface ISkGwidService
    extends ISkService {

  /**
   * Service identifier.
   */
  String SERVICE_ID = ISkHardConstants.SK_CORE_SERVICE_ID_PREFIX + ".Gwids"; //$NON-NLS-1$

  /**
   * Tests if GWID is more general (covers other) then other one when filtering out class properties.
   * <p>
   * One of the usage of the GWID is to specify it as kind of filter for class property GWID. Usually a list of the
   * multi and single (non-multi) GWIDs are used for filtering single GWIDs. If any of the GWIDs in the list is "more
   * general" than the GWID being tested, then the GWID being tested is accepted by the filter. Multi GWID corresponds
   * to many valid GWIDs existing in the system, while single GWID corresponds to the one GWID. "More general" means
   * that GWID being tested is on of the corresponding GWIDs.
   * <p>
   * Some more rule applies when GWID ised for filtering
   * <ul>
   * <li>it is assumed that {@link Gwid#classId()} corresponds the class itself and all subclasses;</li>
   * <li>filter GWID of kind {@link EGwidKind#GW_CLASS} (both concrete and abstract) is considered as "accept all
   * property IDs of the specified kind".</li>
   * </ul>
   * <p>
   * This method tests if <code>aGeneral</code> GWID is really more general when <code>aTested</code> in the context of
   * the specified Core API. Testing is done for the class property of kind <code>aKind</code>. Note that GWID being
   * tested may also be a multi GWID.
   * <p>
   * In case of any error (like non-valid GWIDs, GWIDs not of specified kind, etc) method does not throws exception
   * rather returns <code>false</code>.
   * <p>
   * Note that if GWID A is <b>not</b> more general than GWID B, it does not means that B is more general than A.
   * <p>
   * In addition to being used when implementing a filter, this method is useful when adding GWID to a list of filter
   * GWIDs. There is no need to add GWID to list if it contains more general GWID and also GWID may be removed from the
   * list when more general one is being added.
   * <p>
   * In summary, this method answers either of two questions:
   * <ul>
   * <li>is <code>aTested</code> GWID accepted by <code>aGeneral</code> filter condition?</li>
   * <li>may filter condition <code>aTested</code> GWID ignored when <code>aGeneral</code> condition exists?</li>
   * </ul>
   *
   * @param aGeneral {@link Gwid} - probably more general GWID when GWID being tested
   * @param aTested {@link Gwid} - GWID being tested if is covered by aGeneral
   * @param aKind {@link ESkClassPropKind} - kind of class property
   * @return boolean - <code>true</code> when <code>aGeneral</code> is more general than <code>aTested</code> GWID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  boolean covers( Gwid aGeneral, Gwid aTested, ESkClassPropKind aKind );

  /**
   * Tests if GWID is more general (covers other) then other one when filtering out class properties.
   * <p>
   * This is the same as {@link #covers(Gwid, Gwid, ESkClassPropKind)} when it is known that <code>aTested</code> is
   * single (not a multi) GWID.
   *
   * @param aGeneral {@link Gwid} - probably more general GWID when GWID being tested
   * @param aTested {@link Gwid} - GWID being tested if is covered by aGeneral
   * @param aKind {@link ESkClassPropKind} - kind of class property
   * @return boolean - <code>true</code> when <code>aGeneral</code> is more general than <code>aTested</code> GWID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException <code>aTested</code> is a multi GWID
   */
  boolean coversSingle( Gwid aGeneral, Gwid aTested, ESkClassPropKind aKind );

  /**
   * Update list of GWIDs used for filtering by the new GWID.
   * <p>
   * If GWID to be added is covered by any GWID from the list then method does nothing. All the GIWDs covered by GWID to
   * be added are removed from the list.
   *
   * @param aList {@link IListEdit}&lt;{@link Gwid}&gt; - list of GWIDs
   * @param aToAdd {@link Gwid} - the GWID to add
   * @param aKind {@link ESkClassPropKind} - kind of class property
   * @return boolean - indicates if list was changed
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  boolean updateGwidsOfIntereset( IListEdit<Gwid> aList, Gwid aToAdd, ESkClassPropKind aKind );

  /**
   * Determines if the specified GWID exists in the system.
   * <p>
   * For multi-GWIDs returne <code>true</code> if any of the expanded GWID exists in the system.
   *
   * @param aGwid {@link Gwid} - the GWID to check
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  boolean exists( Gwid aGwid );

  /**
   * Returns all existing single GWIDs covered by the specified GWID.
   * <p>
   * For unexisting GWID returns an empty list.
   * <p>
   * Note: method may be very resource-expensive!
   *
   * @param aGwid {@link Gwid} - the GWID to expand
   * @return {@link GwidList} - an editable list of GWIDs
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  GwidList expandGwid( Gwid aGwid );

}
