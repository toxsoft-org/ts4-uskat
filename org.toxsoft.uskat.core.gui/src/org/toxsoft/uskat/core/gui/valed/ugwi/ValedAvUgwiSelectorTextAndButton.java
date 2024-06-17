package org.toxsoft.uskat.core.gui.valed.ugwi;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.valed.controls.av.*;
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
   * Constructor.
   *
   * @param aTsContext {@link ITsGuiContext} - the editor context
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public ValedAvUgwiSelectorTextAndButton( ITsGuiContext aTsContext ) {
    super( aTsContext, ValedUgwiSelectorTextAndButton.FACTORY );
  }

}
