package org.toxsoft.uskat.core.gui.ugwi.valed;

import static org.toxsoft.core.tsgui.valed.api.IValedControlConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.controls.helpers.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.ugwis.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.conn.*;

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
    // FIXME for debug only
    TsNullArgumentRtException.checkNull( tsContext() );
    ISkConnectionSupplier connSupplier = tsContext().get( ISkConnectionSupplier.class );
    ISkConnection conn = connSupplier.defConn();
    IStringListEdit kindIdList = new StringArrayList();
    for( ISkUgwiKind kind : conn.coreApi().ugwiService().listKinds() ) {
      kindIdList.add( kind.id() );
    }

    // create and dispaly Ugwi selector
    Ugwi selUgwi = PanelUgwiSelector.selectUgwiSingleKind( tsContext(), canGetValue().isOk() ? getValue() : null,
        conn.coreApi(), kindIdList.first() );

    if( selUgwi != null ) {
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
