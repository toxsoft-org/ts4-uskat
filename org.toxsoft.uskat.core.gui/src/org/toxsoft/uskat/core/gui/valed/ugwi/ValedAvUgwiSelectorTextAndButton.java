package org.toxsoft.uskat.core.gui.valed.ugwi;

import static org.toxsoft.uskat.core.inner.ISkCoreGuiInnerSharedConstants.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.controls.av.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * {@link EAtomicType#VALOBJ} of type {@link Ugwi} editor - selector.
 * <p>
 * Wraps over {@link ValedUgwiSelectorTextAndButton}.
 *
 * @author hazard157
 */
public class ValedAvUgwiSelectorTextAndButton
    extends AbstractAvValobjWrapperValedControl<Ugwi> {

  /**
   * The factory name.
   */
  public static final String FACTORY_NAME = SKCGC_VALED_AV_UGWI_SELECTOR_TEXT_AND_BUTTON;

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
      return new ValedAvUgwiSelectorTextAndButton( aContext );
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
  public ValedAvUgwiSelectorTextAndButton( ITsGuiContext aTsContext ) {
    super( aTsContext, ValedUgwiSelectorTextAndButton.FACTORY );
  }

}
