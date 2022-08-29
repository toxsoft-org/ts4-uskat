package org.toxsoft.uskat.mnemo.gui.vedex;

import org.toxsoft.core.tsgui.ved.core.*;
import org.toxsoft.core.tsgui.ved.core.library.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Defines link between {@link IVedComponentController} and one {@link IVedComponent}.
 *
 * @author hazard157
 */
public interface ICompLinkDef
    extends IStridableParameterized {

  /**
   * Determines if components from the specified providers are accepted by this link.
   *
   * @param aComponentProvider {@link IVedComponentProvider} - the component provied
   * @return boolean - <code>true</code> any component from this provider may be linked
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  boolean acceptsComponentsOfProvider( IVedComponentProvider aComponentProvider );

}
