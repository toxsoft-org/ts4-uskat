package org.toxsoft.uskat.core.gui.ugwi.glib;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.panels.generic.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.gui.ugwi.gui.*;

/**
 * Panel selects UGWI (with single concrete class property) of the specified specified property kind.
 * <p>
 * The kind of the property to select is defined at the moment of the panel creation. The class property and
 * corresponding UGWI kind are returned by the methods {@link #getClassPropKind()} and {@link #getUgwiKindId()}
 * respectively.
 * <p>
 * Designed to be used as a panel returned by {@link IUgwiKindGuiHelper#createUgwiSelectorPanel(ITsGuiContext)}.
 *
 * @author hazard157
 */
public interface IPanelSingleConcreteUgwiSelector
    extends IGenericEntityEditPanel<Ugwi> {

  /**
   * Returns the property type of the class that this panel selects.
   * <p>
   * Corresponds with Ugwi kind returned by {@link #getUgwiKindId()}.
   *
   * @return {@link ESkClassPropKind} - the class property kind
   */
  ESkClassPropKind getClassPropKind();

  /**
   * Returns the UGWI kind ID that this panel selects.
   * <p>
   * Corresponds with class property kind returned by {@link #getClassPropKind()}.
   *
   * @return String - the UGWI kind ID
   */
  String getUgwiKindId();

}
