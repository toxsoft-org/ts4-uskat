package org.toxsoft.uskat.core.gui.utils.ugwi;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.panels.generic.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.utils.ugwi.*;

/**
 * The UGWI kind GUI helper methods.
 * <p>
 * Implemented {@link IStridableParameterized} methods wraps over {@link #kind()}.
 * <p>
 * User defined UGWI kinds must be registered by {@link UgwiGuiUtils#registerKind(IUgwiKindGuiHelper)}for users of the
 * UGWI to handle UGWIs of different kinds. Note that {@link Ugwi} as a syntactical wrapper over canonical textual
 * representation does not uses {@link IUgwiKindGuiHelper}.
 *
 * @author hazard157
 */
public sealed interface IUgwiKindGuiHelper
    extends IStridableParameterized permits UgwiKindGuiHelper {

  /**
   * Returns the registered kind this helper is designed for.
   *
   * @param <T> - expected type of the UGWI kind
   * @return {@link IUgwiKind} - the registered UGWI kind
   */
  <T extends IUgwiKind> T kind();

  /**
   * Creates the kind-specific UGWI editor or viewer panel.
   *
   * @param aTsContext {@link ITsGuiContext} - the context
   * @param aViewer boolean - <code>true</code> to create editor, <code>false</code> - the viewer
   * @return {@link IGenericEntityEditPanel}&lt;{@link Ugwi}&lt; - created panel
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  IGenericEntityEditPanel<Ugwi> createEntityPanel( ITsGuiContext aTsContext, boolean aViewer );

  /**
   * Returns the panel to select the UGWI of this kind.
   *
   * @param aTsContext {@link ITsGuiContext} - the context
   * @param aViewer boolean - <code>true</code> to create editor, <code>false</code> - the viewer
   * @return {@link IGenericSelectorPanel}&lt;{@link Ugwi}&lt; - created panel
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  IGenericSelectorPanel<Ugwi> createSelectorPanel( ITsGuiContext aTsContext, boolean aViewer );

}
