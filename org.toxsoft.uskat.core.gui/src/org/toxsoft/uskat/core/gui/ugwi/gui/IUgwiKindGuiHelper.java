package org.toxsoft.uskat.core.gui.ugwi.gui;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.panels.generic.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.ugwis.*;

/**
 * The UGWI kind GUI helper methods.
 * <p>
 * Implemented {@link IStridableParameterized} methods wraps over {@link #kind()}.
 *
 * @author hazard157
 */
public sealed interface IUgwiKindGuiHelper
    extends IStridableParameterized
    permits UgwiKindGuiHelperBase {

  /**
   * Returns the UGWI kind this helper is designed for.
   *
   * @return {@link AbstractSkUgwiKind} - the UGWI kind
   */
  AbstractSkUgwiKind<?> kind();

  /**
   * Creates the kind-specific UGWI editor/viewer panel.
   *
   * @param aTsContext {@link ITsGuiContext} - the context
   * @param aViewer boolean - <code>true</code> to create editor, <code>false</code> - the viewer
   * @return {@link IGenericEntityEditPanel}&lt;{@link Ugwi}&lt; - created panel
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  IGenericEntityEditPanel<Ugwi> createUgwiEntityPanel( ITsGuiContext aTsContext, boolean aViewer );

  /**
   * Returns the panel to select the existing UGWI of this kind.
   *
   * @param aTsContext {@link ITsGuiContext} - the context
   * @return {@link IGenericSelectorPanel}&lt;{@link Ugwi}&lt; - created panel
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  IGenericSelectorPanel<Ugwi> createUgwiSelectorPanel( ITsGuiContext aTsContext );

}
