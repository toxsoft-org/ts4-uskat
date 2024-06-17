package org.toxsoft.uskat.core.gui.valed.gwid;

import static org.toxsoft.core.tsgui.valed.api.IValedControlConstants.*;
import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.gui.valed.gwid.ISkResources.*;
import static org.toxsoft.uskat.core.inner.ISkCoreGuiInnerSharedConstants.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.dialogs.datarec.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.controls.helpers.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.gui.glib.gwidsel.*;

/**
 * Allows to select {@link Gwid} by accessing {@link ISkObjectService}.
 *
 * @author hazard157
 * @author dima
 */
public class ValedConcreteGwidEditor
    extends AbstractValedTextAndButton<Gwid> {

  /**
   * The factory name.
   */
  public static final String FACTORY_NAME = SKCGC_VALED_CONCRETE_GWID_EDITOR_NAME;

  /**
   * Id for gwid kind option
   */
  public static final String OPID_GWID_KIND = SKCGC_VALED_CONCRETE_GWID_EDITOR_NAME_OPID_GWID_KIND;

  /**
   * The gwid kind will be returned.
   */
  public static final IDataDef OPDEF_GWID_KIND = DataDef.create( OPID_GWID_KIND, VALOBJ, //
      TSID_NAME, STR_GWID_KIND, //
      TSID_DESCRIPTION, STR_GWID_KIND_D, //
      TSID_KEEPER_ID, EGwidKind.KEEPER_ID, //
      TSID_DEFAULT_VALUE, avValobj( EGwidKind.GW_RTDATA ) //
  );

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
      AbstractValedControl<Gwid, ?> e = new ValedConcreteGwidEditor( aContext );
      return e;
    }

  }

  /**
   * The factory singleton.
   */
  public static final AbstractValedControlFactory FACTORY = new Factory();

  /**
   * Constructor for subclasses.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public ValedConcreteGwidEditor( ITsGuiContext aContext ) {
    super( aContext );
    setParamIfNull( OPDEF_IS_WIDTH_FIXED, AV_FALSE );
    setParamIfNull( OPDEF_IS_HEIGHT_FIXED, AV_TRUE );
    setParamIfNull( OPDEF_VERTICAL_SPAN, AV_1 );
  }

  @Override
  protected boolean doProcessButtonPress() {
    EGwidKind gwidKind = params().getValobj( OPDEF_GWID_KIND );
    ESkClassPropKind propKind = ESkClassPropKind.getById( gwidKind.id() );
    IGwidSelectorConstants.OPDEF_CLASS_PROP_KIND.setValue( params(), avValobj( propKind ) );
    // create and display Gwid selector
    ITsDialogInfo di = new TsDialogInfo( tsContext(), DLG_CAPTION_SELECT_GWID, DLG_TITLE_SELECT_GWID );
    Gwid gwid = SingleSkPropGwidSelectPanel.selectGwid( di, canGetValue().isOk() ? getValue() : null );

    if( gwid != null ) {
      doSetUnvalidatedValue( gwid );
      return true;
    }
    return false;
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
  protected void doUpdateTextControl() {
    // nop
  }

  @Override
  protected Gwid doGetUnvalidatedValue() {
    return Gwid.of( getTextControl().getText() );
  }

  @Override
  protected void doDoSetUnvalidatedValue( Gwid aValue ) {
    String txt = TsLibUtils.EMPTY_STRING;
    if( aValue != null ) {
      txt = aValue.toString();
    }
    getTextControl().setText( txt );
  }

}
