package org.toxsoft.uskat.core.gui.ugwi.kinds;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.panels.generic.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.ugwis.*;
import org.toxsoft.uskat.core.api.ugwis.kinds.*;
import org.toxsoft.uskat.core.gui.ugwi.gui.*;

/**
 * {@link IUgwiKindGuiHelper} implementation for {@link UgwiKindSkAttr}.
 *
 * @author hazard157
 */
public class UgwiGuiHelperSkAttr
    extends UgwiKindGuiHelperBase<IAtomicValue> {

  // /**
  // * The registrator singleton.
  // */
  // public static final SkUgwiGuiUtils.IRegistrator<IAtomicValue> REGISTRATOR =
  // aKind -> aKind.registerHelper( IUgwiKindGuiHelper.class, new UgwiGuiHelperSkAttr( aKind ) );

  /**
   * Constructor.
   *
   * @param aKind {@link AbstractSkUgwiKind}&lt;T&gt; - the UGWI kind
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException kind of the specified ID is not registered
   */
  public UgwiGuiHelperSkAttr( AbstractSkUgwiKind<IAtomicValue> aKind ) {
    super( aKind );
  }

  // ------------------------------------------------------------------------------------
  // UgwiKindGuiHelper
  //

  @Override
  protected IGenericEntityEditPanel<Ugwi> doCreateEntityPanel( ITsGuiContext aTsContext, boolean aViewer ) {

    // TODO UgwiGuiHelperSkAttr.doCreateEntityPanel()

    return super.doCreateEntityPanel( aTsContext, aViewer );
  }

  @Override
  protected IGenericSelectorPanel<Ugwi> doCreateSelectorPanel( ITsGuiContext aTsContext, boolean aViewer ) {

    // TODO UgwiGuiHelperSkAttr.doCreateSelectorPanel()

    return super.doCreateSelectorPanel( aTsContext, aViewer );
  }

}
