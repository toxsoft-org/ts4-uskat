package org.toxsoft.uskat.mnemo.gui.vedex;

import org.toxsoft.core.tsgui.ved.core.*;
import org.toxsoft.core.tsgui.ved.core.library.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.props.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.coll.primtypes.*;

/**
 * Controls behaviour of the several {@link IVedComponent}.
 * <p>
 * Controller may me decaribed as having a "frontend" and a "backend". As a backend controller is connected to at least
 * one VED component and sets their properties. As a frontend controller is decared {@link IPropertable} so main API is
 * to set controllers properties.
 * <p>
 * While it seems strange setting properties of controller to set properties of components this have a sense. VED
 * components properties are simply drawing properties like coordinates, color, fillstyle, font or text string.
 * Properties of the controller re-define components as real-time visual controls.
 * <p>
 * For exmple imagine an rectanguler component <code>SplitBar</code> divided in two parts (say left and right parts).
 * Each part has it's <i>ColorLeft</i> and <i>ColorRight</i> and also <i>PartsRatio</i> property is declared as
 * percentage (0.0% - no left part, 100.0% - no right part). Let's introduce controller named <code>Gauge</code> with
 * the {@link EAtomicType#FLOATING FLOATING} properties <i>Value</i>, <i>MinVal</i> and <i>MaxVal</i>. It's easy to
 * image how rectangular component becames a measurer indicator: the <code>Gauge</code> controller changes
 * <code>SplitBar</code>.<i>PartsRatio</i> from 0.0 to 100.0 as <code>Gauge</code>.<i>Value</i> is changes from
 * <i>MinVal</i> to <i>MaxVal</i> values.
 *
 * @author hazard157
 */
public interface IVedComponentController
    extends IStridableParameterized, IPropertable {

  /**
   * Returns the provider that created this controller.
   *
   * @return {@link IVedComponentProvider} - creator
   */
  IVedComponentControllerProvider provider();

  /**
   * Returns linkes component IDs.
   * <p>
   * Keys in this map are from the {@link IVedComponentControllerProvider#linkDefs()} keys. This map may contain only
   * part of the defined links, especially at deign time.
   *
   * @return {@link IStringMap}&lt;String&gt; - the map "link ID" - "VED component ID"
   */
  IStringMap<String> links();

}
