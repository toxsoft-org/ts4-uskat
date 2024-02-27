package org.toxsoft.uskat.core.gui.km5.sded.sded.editors;

import static org.toxsoft.uskat.core.gui.km5.sded.sded.editors.ITsResources.*;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.dialogs.datarec.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.core.gui.km5.sded.*;

/**
 * Панель для выбора {@link SkidList} wrapped by {@link AbstractTsDialogPanel}.
 *
 * @author dima
 */
public class PanelSkidListSelector
    extends AbstractTsDialogPanel<ISkidList, ITsGuiContext> {

  private ClassInfoViewerPanel   classesPanel;
  private ObjectCheckedListPanel skObjectCheckedListPanel;

  /**
   * Конструктор панели, предназначенной для вставки в диалог {@link TsDialog}.
   *
   * @param aParent Composite - родительская компонента
   * @param aOwnerDialog TsDialog - родительский диалог
   */
  public PanelSkidListSelector( Composite aParent, TsDialog<ISkidList, ITsGuiContext> aOwnerDialog ) {
    super( aParent, aOwnerDialog );

    // this.setLayout( new GridLayout( 2, true ) );
    FillLayout fillLayout = new FillLayout();
    fillLayout.marginHeight = 5;
    fillLayout.marginWidth = 5;
    this.setLayout( fillLayout );
    init();
  }

  void init() {
    // create vertical sash
    SashForm verticalSashForm = new SashForm( this, SWT.VERTICAL );
    SashForm horizontalSashForm = new SashForm( verticalSashForm, SWT.HORIZONTAL );
    // panel for class selection
    classesPanel = new ClassInfoViewerPanel( horizontalSashForm, tsContext() );

    // panel for skObjects selection
    skObjectCheckedListPanel = new ObjectCheckedListPanel( horizontalSashForm, tsContext(), this );
    classesPanel.setSkObjectListPanel( skObjectCheckedListPanel );
    horizontalSashForm.setWeights( 1, 1 );
    horizontalSashForm.setSashWidth( 5 );
    verticalSashForm.setSashWidth( 5 );

  }

  @Override
  protected void doSetDataRecord( ISkidList aSkidList ) {
    if( aSkidList != null && !aSkidList.isEmpty() ) {
      classesPanel.selectClass( aSkidList.first().classId() );
      skObjectCheckedListPanel.select( aSkidList );
    }
  }

  @Override
  protected ISkidList doGetDataRecord() {
    SkidList retVal = new SkidList();
    // создаем список
    for( ISkObject skObject : skObjectCheckedListPanel.getSelectedObjs() ) {
      Skid skid = new Skid( classesPanel.getSelectedClass().id(), skObject.strid() );
      retVal.add( skid );
    }
    return retVal;
  }

  /**
   * Выводит диалог выбора списка Skid'ов.
   * <p>
   *
   * @param aSkidList {@link ISkidList} для инициализации
   * @param aContext {@link ITsGuiContext} - контекст
   * @return ISkidList - выбранные объекты или <b>null</b> в случает отказа от выбора
   */
  public static final ISkidList selectSkids( ISkidList aSkidList, ITsGuiContext aContext ) {
    TsNullArgumentRtException.checkNull( aContext );
    // old version
    // IDialogPanelCreator<ISkidList, ITsGuiContext> creator = PanelSkidListSelector::new;
    // ITsDialogInfo dlgInfo = new TsDialogInfo( aContext, DLG_T_SKID_LIST_SEL, STR_MSG_SKID_LIST_SELECTION );
    // TsDialog<ISkidList, ITsGuiContext> d = new TsDialog<>( dlgInfo, aSkidList, aContext, creator );
    // return d.execData();
    // new version
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
      return ISkidList.EMPTY;
    }
    SkidList retVal = new SkidList();
    for( ISkObject obj : selObjs ) {
      retVal.add( obj.skid() );
    }
    return retVal;
  }

  @Override
  protected ValidationResult doValidate() {
    // check selected objects
    IList<ISkObject> selObjList = skObjectCheckedListPanel.getSelectedObjs();
    if( selObjList == null || selObjList.isEmpty() ) {
      return ValidationResult.error( MSG_ERR_NO_OBJ_SELECTED );
    }
    return ValidationResult.SUCCESS;
  }
}
