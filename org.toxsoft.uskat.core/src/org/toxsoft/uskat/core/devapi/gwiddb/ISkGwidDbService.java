package org.toxsoft.uskat.core.devapi.gwiddb;

import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.strid.more.IdChain;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.ISkHardConstants;
import org.toxsoft.uskat.core.api.ISkService;

/**
 * TODO WTF?
 *
 * @author hazard157
 */
public interface ISkGwidDbService
    extends ISkService {

  /**
   * Service identifier.
   */
  String SERVICE_ID = ISkHardConstants.SK_CORE_SERVICE_ID_PREFIX + ".GwidDb"; //$NON-NLS-1$

  /**
   * Returns existing section IDs.
   *
   * @return {@link IList}&lt;{@link IdChain}&gt; - the list of section IDs
   */
  IList<IdChain> listSectionIds();

  /**
   * Creates new or returns existig section.
   * <p>
   * Sections are created by the Sk-services. It is recommended to use {@link IdChain}s with at least two branches:
   * first bracnh is the creator service ID, and the second is meaningful name of the section.
   *
   * @param aSectionId {@link IdChain} - the section ID
   * @return {@link ISkGwidDbSection} - newly created or existing section
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException argument is not an IDpath
   */
  ISkGwidDbSection defineSection( IdChain aSectionId );

  /**
   * Removes the section.
   *
   * @param aSectionId {@link IdChain} - the section ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void removeSection( IdChain aSectionId );

  /**
   * Returns the service eventer.
   *
   * @return {@link ITsEventer}&lt;{@link ISkGwidDbServiceListener}&gt; - the service eventer
   */
  ITsEventer<ISkGwidDbServiceListener> eventer();
}
