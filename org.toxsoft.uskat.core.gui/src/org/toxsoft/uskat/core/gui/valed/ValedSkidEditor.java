package org.toxsoft.uskat.core.gui.valed;

import static org.toxsoft.core.tsgui.valed.api.IValedControlConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.gui.valed.ITsResources.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.dialogs.datarec.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.controls.helpers.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.core.gui.km5.sded.*;

/**
 * Allows to select {@link Skid} by accessing {@link ISkObjectService}.
 *
 * @author hazard157
 * @author dima
 */
public class ValedSkidEditor
    extends AbstractValedTextAndButton<Skid> {

  /**
   * The factory name.
   */
  public static final String FACTORY_NAME = VALED_EDNAME_PREFIX + ".SkidEditor"; //$NON-NLS-1$

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
    protected IValedControl<Skid> doCreateEditor( ITsGuiContext aContext ) {
      AbstractValedControl<Skid, ?> e = new ValedSkidEditor( aContext );
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
   * @param aContext {@link ITsGuiContext} - the valed context
   * @throws TsNullArgumentRtException аргумент = null
   */
  public ValedSkidEditor( ITsGuiContext aContext ) {
    super( aContext );
    setParamIfNull( OPDEF_IS_WIDTH_FIXED, AV_FALSE );
    setParamIfNull( OPDEF_IS_HEIGHT_FIXED, AV_TRUE );
    setParamIfNull( OPDEF_VERTICAL_SPAN, AV_1 );

  }

  @Override
  protected boolean doProcessButtonPress() {
    // create and dispaly Skid selector
    Skid initVal = canGetValue().isOk() ? Gwid.createObj( getValue() ).skid() : null;
    // dima 17.04.24
    // Gwid gwid = PanelGwidSelector.selectGwid( initVal, tsContext(), ESkClassPropKind.RTDATA, null );
    Skid selSkid = selectSkid( initVal, tsContext() );
    if( selSkid != null ) {
      doSetUnvalidatedValue( selSkid );
      return true;
    }
    return false;
  }

  /**
   * Выводит диалог выбора Skid.
   * <p>
   *
   * @param aInitSkid {@link Skid} для инициализации
   * @param aContext {@link ITsGuiContext} - контекст
   * @return Skid - выбранный объект или <b>null</b> в случает отказа от выбора
   */
  Skid selectSkid( Skid aInitSkid, ITsGuiContext aContext ) {
    TsNullArgumentRtException.checkNull( aContext );
    ISkConnectionSupplier connSupplier = aContext.get( ISkConnectionSupplier.class );
    ISkConnection conn = connSupplier.defConn();
    IM5Domain m5 = conn.scope().get( IM5Domain.class );

    IM5Model<ISkObject> modelSk = m5.getModel( IKM5SdedConstants.MID_SDED_SK_OBJECT, ISkObject.class );
    IM5LifecycleManager<ISkObject> lmSk = modelSk.getLifecycleManager( conn );
    ITsGuiContext ctx = new TsGuiContext( aContext );
    TsDialogInfo di = new TsDialogInfo( ctx, DLG_T_SKID_SEL, STR_MSG_SKID_SELECTION );
    ISkObject initObj = aInitSkid == null ? null : conn.coreApi().objService().get( aInitSkid );
    ISkObject selObj = M5GuiUtils.askSelectItem( di, modelSk, initObj, lmSk.itemsProvider(), lmSk );
    if( selObj != null ) {
      return selObj.skid();
    }
    return Skid.NONE;
  }

  @Override
  public ValidationResult canGetValue() {
    try {
      Gwid g = Gwid.of( getTextControl().getText() );
      if( g.kind() == EGwidKind.GW_CLASS && !g.isAbstract() ) {
        return ValidationResult.SUCCESS;
      }
      return ValidationResult.error( MSG_ERR_INV_SKID_FORMAT );
    }
    catch( @SuppressWarnings( "unused" ) Exception ex ) {
      return ValidationResult.error( MSG_ERR_INV_SKID_FORMAT );
    }
  }

  @Override
  protected void doUpdateTextControl() {
    // nop
  }

  @Override
  protected Skid doGetUnvalidatedValue() {
    return Gwid.of( getTextControl().getText() ).skid();
  }

  @Override
  protected void doDoSetUnvalidatedValue( Skid aValue ) {
    String txt = TsLibUtils.EMPTY_STRING;
    if( aValue != null ) {
      txt = aValue.toString();
    }
    getTextControl().setText( txt );
  }

}
