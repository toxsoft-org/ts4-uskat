package org.toxsoft.uskat.core.gui.km5.sded.sded.editors;

import static org.toxsoft.core.tsgui.valed.api.IValedControlConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.gui.km5.sded.sded.editors.ISkResources.*;

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
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.core.gui.km5.sded.*;

/**
 * Allows to select {@link SkidList} by accessing {@link ISkObjectService}.
 *
 * @author hazard157
 * @author dima
 */
public class ValedSkidListEditor
    extends AbstractValedTextAndButton<SkidList> {

  /**
   * The factory name.
   */
  public static final String FACTORY_NAME = VALED_EDNAME_PREFIX + ".SkidListEditor"; //$NON-NLS-1$

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
    protected IValedControl<SkidList> doCreateEditor( ITsGuiContext aContext ) {
      AbstractValedControl<SkidList, ?> e = new ValedSkidListEditor( aContext );
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
  public ValedSkidListEditor( ITsGuiContext aContext ) {
    super( aContext );
    setParamIfNull( OPDEF_IS_WIDTH_FIXED, AV_FALSE );
    setParamIfNull( OPDEF_IS_HEIGHT_FIXED, AV_TRUE );
    setParamIfNull( OPDEF_VERTICAL_SPAN, AV_1 );

  }

  @Override
  protected boolean doProcessButtonPress() {
    // create and dispaly SkidList selector
    SkidList initVal = canGetValue().isOk() ? getValue() : null;
    // old version
    // SkidList selSkids = (SkidList)PanelSkidListSelector.selectSkids( initVal, tsContext() );
    SkidList selSkids = (SkidList)selectSkids( initVal, tsContext() );

    if( selSkids != null ) {
      doSetUnvalidatedValue( selSkids );
      return true;
    }
    return false;
  }

  /**
   * Выводит диалог выбора списка Skid'ов.
   * <p>
   *
   * @param aSkidList {@link ISkidList} для инициализации
   * @param aContext {@link ITsGuiContext} - контекст
   * @return ISkidList - выбранные объекты или <b>null</b> в случает отказа от выбора
   */
  ISkidList selectSkids( ISkidList aSkidList, ITsGuiContext aContext ) {
    TsNullArgumentRtException.checkNull( aContext );
    ISkConnectionSupplier connSupplier = aContext.get( ISkConnectionSupplier.class );
    ISkConnection conn = connSupplier.defConn();
    IM5Domain m5 = conn.scope().get( IM5Domain.class );

    IM5Model<ISkObject> modelSk = m5.getModel( IKM5SdedConstants.MID_SDED_SK_OBJECT, ISkObject.class );
    IM5LifecycleManager<ISkObject> lmSk = modelSk.getLifecycleManager( conn );
    ITsGuiContext ctx = new TsGuiContext( aContext );
    TsDialogInfo di = new TsDialogInfo( ctx, DLG_T_SKID_LIST_SEL, STR_MSG_SKID_LIST_SELECTION );
    IListEdit<ISkObject> initObjs = new ElemArrayList<>();
    for( Skid skid : aSkidList ) {
      initObjs.add( conn.coreApi().objService().get( skid ) );
    }
    IList<ISkObject> selObjs = M5GuiUtils.askSelectItemsList( di, modelSk, initObjs, lmSk.itemsProvider() );
    if( selObjs == null ) {
      return null;
    }
    SkidList retVal = new SkidList();
    for( ISkObject obj : selObjs ) {
      retVal.add( obj.skid() );
    }
    return retVal;
  }

  @Override
  public ValidationResult canGetValue() {
    try {
      SkidListKeeper.KEEPER.str2ent( getTextControl().getText() );
      return ValidationResult.SUCCESS;
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
  protected SkidList doGetUnvalidatedValue() {
    return (SkidList)SkidListKeeper.KEEPER.str2ent( getTextControl().getText() );
  }

  @Override
  protected void doDoSetUnvalidatedValue( SkidList aValue ) {
    String txt = TsLibUtils.EMPTY_STRING;
    if( aValue != null ) {
      txt = SkidListKeeper.KEEPER.ent2str( aValue );
    }
    getTextControl().setText( txt );
  }

}
