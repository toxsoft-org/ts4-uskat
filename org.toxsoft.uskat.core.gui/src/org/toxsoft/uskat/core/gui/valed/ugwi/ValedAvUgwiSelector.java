package org.toxsoft.uskat.core.gui.valed.ugwi;

import static org.toxsoft.uskat.core.inner.ISkCoreGuiInnerSharedConstants.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.gw.ugwi.*;

/**
 * {@link EAtomicType#VALOBJ} of type {@link Ugwi} selector VALED, edits the value of type {@link Ugwi}..
 * <p>
 * Actually it is not a VALED, rather it declares VALED factory and uses options from {@link ValedUgwiSelector}.
 *
 * @author hazard157
 */
public class ValedAvUgwiSelector {

  /**
   * The factory name.
   */
  public static final String FACTORY_NAME = SKCGC_VALED_AV_UGWI_SELECTOR;

  /**
   * The factory class.
   *
   * @author dima
   */
  @SuppressWarnings( "unchecked" )
  static class Factory
      extends AbstractValedControlFactory {

    protected Factory() {
      super( FACTORY_NAME );
    }

    @Override
    protected IValedControl<IAtomicValue> doCreateEditor( ITsGuiContext aContext ) {
      return new ValedAvUgwiSelectorTable( aContext );
    }

    @Override
    protected IValedControl<IAtomicValue> doCreateSingleLine( ITsGuiContext aContext ) {
      return new ValedAvUgwiSelectorTextAndButton( aContext );
    }

  }

  /**
   * The factory singleton.
   */
  public static final AbstractValedControlFactory FACTORY = new Factory();

}
