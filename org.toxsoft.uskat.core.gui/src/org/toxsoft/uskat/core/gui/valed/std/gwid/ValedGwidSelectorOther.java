package org.toxsoft.uskat.core.gui.valed.std.gwid;

import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Package-private class of the SKID selector implementation.
 *
 * @author hazard157
 */
class ValedGwidSelectorOther
    extends AbstractValedControl<Gwid, Control> {

  /**
   * Constructor.
   *
   * @param aContext {@link ITsGuiContext} - the VALED context
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public ValedGwidSelectorOther( ITsGuiContext aContext ) {
    super( aContext );
    // TODO Auto-generated constructor stub
  }

  // ------------------------------------------------------------------------------------
  // AbstractValedControl
  //

  @Override
  protected Control doCreateControl( Composite aParent ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void doSetEditable( boolean aEditable ) {
    // TODO Auto-generated method stub

  }

  @Override
  protected Gwid doGetUnvalidatedValue() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void doSetUnvalidatedValue( Gwid aValue ) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void doClearValue() {
    // TODO Auto-generated method stub

  }

}
