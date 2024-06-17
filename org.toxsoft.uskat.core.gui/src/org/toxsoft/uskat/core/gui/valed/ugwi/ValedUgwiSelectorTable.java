package org.toxsoft.uskat.core.gui.valed.ugwi;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;

import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.gw.ugwi.*;

/**
 * Ugwi selector VALED.
 *
 * @author hazard157
 */
public class ValedUgwiSelectorTable
    extends AbstractValedControl<Ugwi, Control> {

  /**
   * Package-private factory singleton for {@link ValedAvUgwiSelectorTextAndButton} constructor.
   * <p>
   * This factory is <b>not</b> intended to be registered.
   */
  static final IValedControlFactory FACTORY = new AbstractValedControlFactory( EMPTY_STRING ) {

    @SuppressWarnings( "unchecked" )
    @Override
    protected IValedControl<Ugwi> doCreateEditor( ITsGuiContext aContext ) {
      return new ValedUgwiSelectorTable( aContext );
    }
  };

  ValedUgwiSelectorTable( ITsGuiContext aContext ) {
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
  protected ValidationResult doCanGetValue() {
    // TODO Auto-generated method stub
    return super.doCanGetValue();
  }

  @Override
  protected Ugwi doGetUnvalidatedValue() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void doSetUnvalidatedValue( Ugwi aValue ) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void doClearValue() {
    // TODO Auto-generated method stub

  }

}
