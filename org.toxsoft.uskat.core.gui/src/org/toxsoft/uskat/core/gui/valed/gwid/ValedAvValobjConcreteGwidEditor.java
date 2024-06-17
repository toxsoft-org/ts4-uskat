package org.toxsoft.uskat.core.gui.valed.gwid;

import static org.toxsoft.uskat.core.inner.ISkCoreGuiInnerSharedConstants.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.controls.av.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * {@link EAtomicType#VALOBJ} of type {@link Gwid} editor.
 * <p>
 * Wraps over {@link ValedConcreteGwidEditor}.
 *
 * @author hazard157
 */
public class ValedAvValobjConcreteGwidEditor
    extends AbstractAvValobjWrapperValedControl<Gwid> {

  /**
   * The factory name.
   */
  public static final String FACTORY_NAME = SKCGC_VALED_AV_VALOBJ_CONCRETE_GWID_EDITOR_NAME;

  /**
   * The factory class.
   *
   * @author hazard157
   */
  static class Factory
      extends AbstractValedControlFactory {

    protected Factory() {
      super( FACTORY_NAME );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected IValedControl<IAtomicValue> doCreateEditor( ITsGuiContext aContext ) {
      return new ValedAvValobjConcreteGwidEditor( aContext );
    }

  }

  /**
   * The factory singleton.
   */
  public static final AbstractValedControlFactory FACTORY = new Factory();

  /**
   * Constructor.
   *
   * @param aTsContext {@link ITsGuiContext} - the editor context
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public ValedAvValobjConcreteGwidEditor( ITsGuiContext aTsContext ) {
    super( aTsContext, ValedConcreteGwidEditor.FACTORY );
  }

}
