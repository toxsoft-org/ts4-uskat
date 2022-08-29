package org.toxsoft.uskat.mnemo.lib;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;

/**
 * Menmoschemes service.
 *
 * @author hazard157
 */
public interface ISkMnemosService
    extends ISkService {

  /**
   * Service identifier.
   */
  String SERVICE_ID = ISkHardConstants.SK_SYSEXT_SERVICE_ID_PREFIX + ".Mnemoschemes"; //$NON-NLS-1$

  /**
   * Finds the section by the ID.
   *
   * @param aSectionId String - the section ID
   * @return {@link ISkMnemoSection} - found section or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ISkMnemoSection findSection( String aSectionId );

  /**
   * Returns all sections.
   *
   * @return {@link IStridablesList}&lt;{@link ISkMnemoSection}&gt; - list of sections
   */
  IStridablesList<ISkMnemoSection> listSections();

  /**
   * Creates new or updates an existing section.
   * <p>
   * For an existing section only parameters are changed while content (mnemos in section) remains intact.
   *
   * @param aSectionId String - the ID of section
   * @param aParams {@link IOptionSet} - section parameters including name and description
   * @return {@link ISkMnemoSection} - created or updated section
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ISkMnemoSection defineSection( String aSectionId, IOptionSet aParams );

  /**
   * Removes the secion with the given ID.
   * <p>
   * Does nothing if section does not exists.
   *
   * @param aSectionId String - ID of section to remove
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void removeSection( String aSectionId );

}
