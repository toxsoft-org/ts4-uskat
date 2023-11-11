package org.toxsoft.uskat.core.gui.glib.gwidsel;

import org.toxsoft.core.tsgui.panels.generic.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.uskat.core.api.sysdescr.*;

/**
 * Panel selects single concrete GWID of the specified specified property kind.
 * <p>
 * The kind of the property to select is defined at the moment of the panel creation. The class property and
 * corresponding GWID kind are returned by the methods {@link #getClassPropKind()} and {@link #getGwidKind()}
 * respectively.
 *
 * @author hazard157
 */
public interface IPanelSingleConcreteGwidSelector
    extends IGenericEntityEditPanel<Gwid> {

  /**
   * Returns the property type of the class that this panel selects.
   * <p>
   * Corresponds with GWID kind returned by {@link #getGwidKind()}.
   *
   * @return {@link ESkClassPropKind} - the class property kind
   */
  ESkClassPropKind getClassPropKind();

  /**
   * Returns the GWID kind that this panel selects.
   * <p>
   * Corresponds with class property kind returned by {@link #getClassPropKind()}.
   *
   * @return {@link EGwidKind} - the GWID kind
   */
  EGwidKind getGwidKind();

}
