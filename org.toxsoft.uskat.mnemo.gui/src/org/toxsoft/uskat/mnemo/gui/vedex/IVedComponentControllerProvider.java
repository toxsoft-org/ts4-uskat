package org.toxsoft.uskat.mnemo.gui.vedex;

import org.toxsoft.core.tsgui.ved.core.*;
import org.toxsoft.core.tslib.av.errors.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Controller factory and definitions provider.
 *
 * @author hazard157
 */
public interface IVedComponentControllerProvider
    extends IStridableParameterized {

  // FIXME maybe use IMnemosEnvironment instead of IVedEnvironment?

  /**
   * Returns the information about controller properties.
   *
   * @return {@link IStridablesList}&lt;{@link IDataDef}&gt; - properties definitions
   */
  IStridablesList<IDataDef> propDefs();

  /**
   * Returns possible link definitions of controllers created by this provider.
   * <p>
   * Some links are mandatory for controller to work. Again, for <code>Gauge</code> example "splitBar" link to the
   * <code>SplitBar</code> component is mandatory while "textValue" link to the <code>TextLabel</code> component
   * (displaying the value) is optional. Mandatory links has the {@link IAvMetaConstants#TSID_IS_MANDATORY} option set
   * to <code>true</code> in {@link ICompLinkDef#params()}.
   * <p>
   * This is a "backend" method intended to be used by some GUI designeds.
   *
   * @return {@link IStridablesList}&lt;{@link ICompLinkDef}&gt; - links definitions
   */
  IStridablesList<ICompLinkDef> linkDefs();

  /**
   * Creates the controller.
   *
   * @param aId String - the ID of controller to be created
   * @param aVedEnv {@link IVedEnvironment} the VED environment
   * @param aProps {@link IOptionSet} - propeties initial values
   * @param aLinks {@link IStringMap}&lt;String&gt; - the links map "link ID" - "VED component ID"
   * @return {@link IVedComponent} - created component
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws AvTypeCastRtException any property value is not compatible to the property definition
   */
  IVedComponent createComponent( String aId, IVedEnvironment aVedEnv, IOptionSet aProps, IStringMap<String> aLinks );

}
