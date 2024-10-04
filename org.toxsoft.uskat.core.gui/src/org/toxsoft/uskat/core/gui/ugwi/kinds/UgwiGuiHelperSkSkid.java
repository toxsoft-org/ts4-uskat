package org.toxsoft.uskat.core.gui.ugwi.kinds;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.panels.generic.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.ugwis.*;
import org.toxsoft.uskat.core.api.ugwis.kinds.*;
import org.toxsoft.uskat.core.gui.ugwi.gui.*;

/**
 * {@link IUgwiKindGuiHelper} implementation for {@link UgwiKindSkAttr}.
 *
 * @author hazard157
 * @author dima
 */
public class UgwiGuiHelperSkSkid
    extends UgwiKindGuiHelperBase<Skid> {

  /**
   * Constructor.
   *
   * @param aKind {@link AbstractSkUgwiKind}&lt;T&gt; - the UGWI kind
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException kind of the specified ID is not registered
   */
  public UgwiGuiHelperSkSkid( AbstractSkUgwiKind<Skid> aKind ) {
    super( aKind );
  }

  // ------------------------------------------------------------------------------------
  // UgwiKindGuiHelper
  //

  @Override
  protected IGenericEntityEditPanel<Ugwi> doCreateEntityPanel( ITsGuiContext aTsContext, boolean aViewer ) {
    return new SingleSkidUgwiSelectPanel( aTsContext, aViewer );
  }

  @Override
  protected IGenericSelectorPanel<Ugwi> doCreateSelectorPanel( ITsGuiContext aTsContext, boolean aViewer ) {
    // Sol-- return new SingleSkidUgwiSelectPanel( aTsContext, aViewer );
    return new SingleSkidUgwiSelectPanel2( aTsContext, aViewer ); // Sol++
  }

}
