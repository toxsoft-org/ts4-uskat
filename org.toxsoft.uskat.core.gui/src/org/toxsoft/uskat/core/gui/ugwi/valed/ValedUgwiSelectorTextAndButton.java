package org.toxsoft.uskat.core.gui.ugwi.valed;

import static org.toxsoft.core.tsgui.valed.api.IValedControlConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.gui.ugwi.valed.ISkResources.*;
import static org.toxsoft.uskat.core.gui.ugwi.valed.ValedUgwiSelectorFactory.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.controls.helpers.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Ugwi selector VALED.
 *
 * @author hazard157
 * @author dima
 */
public class ValedUgwiSelectorTextAndButton
    extends AbstractValedTextAndButton<Ugwi> {

  /**
   * The factory name.
   */
  public static final String FACTORY_NAME = VALED_EDNAME_PREFIX + ".UgwiSelector"; //$NON-NLS-1$

  /**
   * selected value
   */
  private Ugwi value = Ugwi.NONE;

  /**
   * The factory class.
   *
   * @author dima
   */
  static class Factory
      extends AbstractValedControlFactory {

    protected Factory() {
      super( FACTORY_NAME );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected IValedControl<Ugwi> doCreateEditor( ITsGuiContext aContext ) {
      AbstractValedControl<Ugwi, ?> e = new ValedUgwiSelectorTextAndButton( aContext );
      return e;
    }

  }

  /**
   * The factory singleton.
   */
  public static final AbstractValedControlFactory FACTORY = new Factory();

  ValedUgwiSelectorTextAndButton( ITsGuiContext aContext ) {
    super( aContext );
    setParamIfNull( OPDEF_IS_WIDTH_FIXED, AV_FALSE );
    setParamIfNull( OPDEF_IS_HEIGHT_FIXED, AV_FALSE );
    // TODO if needed to increase height
    // setParamIfNull( OPDEF_VERTICAL_SPAN, new AvIntegerShortImpl( 3 ) );
  }

  @Override
  protected boolean doProcessButtonPress() {
    Ugwi selUgwi = Ugwi.NONE;
    // check what user want
    if( tsContext().params().hasValue( OPDEF_SINGLE_UGWI_KIND_ID ) ) {
      String ugwiKindId = tsContext().params().getStr( OPDEF_SINGLE_UGWI_KIND_ID );
      selUgwi = PanelUgwiSelector.selectUgwiSingleKind( tsContext(), value, ugwiKindId );
    }
    else
      if( tsContext().params().hasValue( OPDEF_UGWI_KIND_IDS_LIST ) ) {
        IStringList kindIdList = OPDEF_UGWI_KIND_IDS_LIST.getValue( tsContext().params() ).asValobj();
        selUgwi = PanelUgwiSelector.selectUgwiListKinds( tsContext(), value, kindIdList );
      }
      else {
        throw new TsIllegalStateRtException( VALED_ERR_MSG_NO_UGWI_KIND );
      }

    if( selUgwi != null && !selUgwi.equals( Ugwi.NONE ) ) {
      doSetUnvalidatedValue( selUgwi );
      value = selUgwi;
      return true;
    }
    return false;
  }

  @Override
  protected void doDoSetUnvalidatedValue( Ugwi aValue ) {
    String txt = TsLibUtils.EMPTY_STRING;
    if( aValue != null ) {
      txt = aValue.toString();
    }
    getTextControl().setText( txt );
  }

  @Override
  protected Ugwi doGetUnvalidatedValue() {
    return value;
  }

}
