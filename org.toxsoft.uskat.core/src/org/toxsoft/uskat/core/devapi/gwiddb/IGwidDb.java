package org.toxsoft.uskat.core.devapi.gwiddb;

import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * TODO WTF?
 *
 * @author hazard157
 */
public interface IGwidDb {

  /**
   * Returns existing section IDs.
   *
   * @return {@link IStringList} - the list of section IDs
   */
  IStringList listSectionIds();

  /**
   * Creates new or returns existig section.
   *
   * @param aSectionId String - the section ID
   * @return {@link IGwidDbSection} - newly created or existing section
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException argument is not an IDpath
   */
  IGwidDbSection defineSection( String aSectionId );

  /**
   * Removes the section.
   *
   * @param aSectionId String - the section ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void removeSection( String aSectionId );

}
