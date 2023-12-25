package org.toxsoft.uskat.core.gui.glib.gwidsel;

import static org.toxsoft.core.tsgui.valed.api.IValedControlConstants.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.core.gui.glib.gwidsel.ISkResources.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.controls.helpers.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.uskat.core.api.sysdescr.*;

/**
 * VALED to select single concrete class property GWID.
 * <p>
 * Based on {@link AbstractValedTextAndButton}.
 * <p>
 * Accepts options:
 * <ul>
 * <li>{@link IGwidSelectorConstants#OPDEF_CLASS_PROP_KIND};</li>
 * </ul>
 *
 * @author hazard157
 */
public class ValedSingleConcreteGwidSelector
    extends AbstractValedTextAndButton<Gwid> {

  /**
   * The factory name.
   */
  public static final String FACTORY_NAME = VALED_EDNAME_PREFIX + ".SingleConcreteGwidSelector"; //$NON-NLS-1$

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
    protected IValedControl<Gwid> doCreateEditor( ITsGuiContext aContext ) {
      return new ValedSingleConcreteGwidSelector( aContext );
    }

  }

  /**
   * The factory singleton.
   */
  public static final AbstractValedControlFactory FACTORY = new Factory();

  private final ESkClassPropKind propKind;

  private Gwid value = null;

  ValedSingleConcreteGwidSelector( ITsGuiContext aContext ) {
    super( aContext );
    propKind = IGwidSelectorConstants.OPDEF_CLASS_PROP_KIND.getValue( tsContext().params() ).asValobj();
  }

  // ------------------------------------------------------------------------------------
  // AbstractValedTextAndButton
  //

  @Override
  protected boolean doProcessButtonPress() {
    // Gwid g = DialogGwidSelectors.selectSinglePropGwid( tdi, value, propKind, skConn );
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  protected void doUpdateTextControl() {
    String s = value != null ? value.toString() : EMPTY_STRING;
    getTextControl().setText( s );
  }

  @Override
  protected void doDoSetUnvalidatedValue( Gwid aValue ) {
    // TODO Auto-generated method stub

  }

  @Override
  public ValidationResult canGetValue() {
    try {
      Gwid.of( getTextControl().getText() );
      return ValidationResult.SUCCESS;
    }
    catch( @SuppressWarnings( "unused" ) Exception ex ) {
      return ValidationResult.error( MSG_ERR_INV_GWID_FORMAT );
    }
  }

  @Override
  protected Gwid doGetUnvalidatedValue() {
    // TODO Auto-generated method stub
    return null;
  }

}
