package org.toxsoft.uskat.core.gui.ugwi.kinds;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.panels.generic.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.ugwis.*;
import org.toxsoft.uskat.core.api.ugwis.kinds.*;
import org.toxsoft.uskat.core.gui.ugwi.gui.*;

/**
 * {@link IUgwiKindGuiHelper} implementation for {@link UgwiKindSkAttr}.
 *
 * @author hazard157
 * @author dima
 */
public class UgwiGuiHelperSkAttr
    extends UgwiKindGuiHelperBase<IAtomicValue> {

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
    // set kind of prop (attr)
    SingleSkPropUgwiSelectPanel.OPDEF_CLASS_PROP_KIND.setValue( aTsContext.params(),
        avValobj( ESkClassPropKind.ATTR ) );
    SingleSkPropUgwiSelectPanel.OPDEF_SK_UGWI_KIND_ID.setValue( aTsContext.params(), avStr( UgwiKindSkAttr.KIND_ID ) );
    return new SingleSkPropUgwiSelectPanel( aTsContext, aViewer );
  }

  @Override
  protected IGenericSelectorPanel<Ugwi> doCreateSelectorPanel( ITsGuiContext aTsContext, boolean aViewer ) {

    // set kind of prop (attr)
    SingleSkPropUgwiSelectPanel.OPDEF_CLASS_PROP_KIND.setValue( aTsContext.params(),
        avValobj( ESkClassPropKind.ATTR ) );
    SingleSkPropUgwiSelectPanel.OPDEF_SK_UGWI_KIND_ID.setValue( aTsContext.params(), avStr( UgwiKindSkAttr.KIND_ID ) );
    return new SingleSkPropUgwiSelectPanel( aTsContext, aViewer );
  }

}
