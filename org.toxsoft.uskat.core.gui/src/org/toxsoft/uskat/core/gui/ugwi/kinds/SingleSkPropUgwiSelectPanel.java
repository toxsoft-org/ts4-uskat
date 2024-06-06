package org.toxsoft.uskat.core.gui.ugwi.kinds;

import static org.toxsoft.core.tslib.ITsHardConstants.*;
import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.gui.km5.sgw.ISgwM5Constants.*;
import static org.toxsoft.uskat.core.gui.ugwi.kinds.ISkResources.*;
import static org.toxsoft.uskat.core.gui.ugwi.valed.ValedUgwiSelectorFactory.*;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.cond.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.bricks.stdevents.*;
import org.toxsoft.core.tsgui.dialogs.datarec.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.mpc.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.panels.generic.*;
import org.toxsoft.core.tsgui.utils.layout.*;
import org.toxsoft.core.tsgui.widgets.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * {@link IGenericSelectorPanel} implementation.
 * <p>
 *
 * @author dima
 */
public class SingleSkPropUgwiSelectPanel
    extends AbstractGenericEntityEditPanel<Ugwi>
    implements IGenericSelectorPanel<Ugwi> {

  static String MPC_OP_ID = TS_ID + ".m5.gui.mpc"; //$NON-NLS-1$

  /**
   * Class of property to select {@link ESkClassPropKind}
   */
  public static IDataDef OPDEF_CLASS_PROP_KIND = DataDef.create( MPC_OP_ID + ".SkClassPropKind", VALOBJ, //$NON-NLS-1$
      TSID_NAME, STR_N_CLASS_PROP_KIND, //
      TSID_DESCRIPTION, STR_D_CLASS_PROP_KIND, //
      TSID_KEEPER_ID, ESkClassPropKind.KEEPER_ID, //
      TSID_DEFAULT_VALUE, avValobj( ESkClassPropKind.RTDATA ) //
  );

  private final ISkCoreApi coreApi; // never is null

  private final ESkClassPropKind skClassPropKind;

  private final IM5CollectionPanel<ISkClassInfo>          panelClasses;
  private final IM5CollectionPanel<ISkObject>             panelObjects;
  private final IM5CollectionPanel<IDtoClassPropInfoBase> panelProps;

  /**
   * Constructor.
   * <p>
   * Constructor stores reference to the context, does not creates copy.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @param aIsViewer boolean - viewer flag, sets {@link #isViewer()} value
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SingleSkPropUgwiSelectPanel( ITsGuiContext aContext, boolean aIsViewer ) {
    super( aContext, aIsViewer );
    coreApi = ISkCoreGuiConstants.REFDEF_SK_VALED_CORE_API.getRef( aContext );
    TsInternalErrorRtException.checkNull( coreApi );
    skClassPropKind = OPDEF_CLASS_PROP_KIND.getValue( tsContext().params() ).asValobj();
    // IM5Domain m5 = aContext.get( IM5Domain.class );
    ISkConnection conn = ((SkCoreApi)coreApi).skConn();
    IM5Domain m5 = conn.scope().get( IM5Domain.class );

    // panelClasses
    IM5Model<ISkClassInfo> modelClasses = m5.getModel( MID_SGW_CLASS_INFO, ISkClassInfo.class );
    IM5LifecycleManager<ISkClassInfo> lmClasses = modelClasses.getLifecycleManager( ((SkCoreApi)coreApi).skConn() );
    ITsGuiContext ctx = new TsGuiContext( aContext );
    ctx.params().addAll( aContext.params() );
    IMultiPaneComponentConstants.OPDEF_IS_DETAILS_PANE.setValue( ctx.params(), AvUtils.AV_FALSE );
    IMultiPaneComponentConstants.OPDEF_DETAILS_PANE_PLACE.setValue( ctx.params(),
        avValobj( EBorderLayoutPlacement.SOUTH ) );
    // добавляем в панель фильтр
    IMultiPaneComponentConstants.OPDEF_IS_FILTER_PANE.setValue( ctx.params(), AvUtils.AV_TRUE );
    panelClasses = modelClasses.panelCreator().createCollViewerPanel( ctx, lmClasses.itemsProvider() );

    // panelObjects
    IM5Model<ISkObject> modelObjects = m5.getModel( MID_SGW_SK_OBJECT, ISkObject.class );
    panelObjects = modelObjects.panelCreator().createCollViewerPanel( ctx, IM5ItemsProvider.EMPTY );
    // panelProps
    String propModelId = sgwGetClassPropModelId( getClassPropKind() );
    IM5Model<IDtoClassPropInfoBase> modelProps = m5.getModel( propModelId, IDtoClassPropInfoBase.class );
    panelProps = modelProps.panelCreator().createCollViewerPanel( ctx, IM5ItemsProvider.EMPTY );

  }

  // ------------------------------------------------------------------------------------
  // AbstractGenericEntityEditPanel
  //

  @Override
  protected ValidationResult doCanGetEntity() {
    ISkClassInfo selClass = panelClasses.selectedItem();
    if( selClass == null ) {
      return ValidationResult.error( MSG_NO_SEL_CLASS );
    }
    ISkObject selObj = panelObjects.selectedItem();
    if( selObj == null ) {
      return ValidationResult.error( MSG_NO_SEL_OBJ );
    }
    IDtoClassPropInfoBase selProp = panelProps.selectedItem();
    if( selProp == null ) {
      return ValidationResult.error( MSG_NO_SEL_PROP );
    }
    return ValidationResult.SUCCESS;
  }

  @Override
  protected Ugwi doGetEntity() {
    return selectedItem();
  }

  @Override
  protected void doProcessSetEntity() {
    if( specifiedEntity() != null ) {
      setSelectedItem( getEntity() );
    }
  }

  @Override
  protected Control doCreateControl( Composite aParent ) {
    TsComposite board = new TsComposite( aParent );
    FillLayout fillLayout = new FillLayout();
    fillLayout.marginHeight = 5;
    fillLayout.marginWidth = 5;
    board.setLayout( fillLayout );

    SashForm verticalSashForm = new SashForm( board, SWT.VERTICAL );
    SashForm horizontalSashForm = new SashForm( verticalSashForm, SWT.HORIZONTAL );

    // panels
    panelClasses.createControl( horizontalSashForm );
    panelObjects.createControl( horizontalSashForm );
    panelProps.createControl( verticalSashForm );
    // setup
    horizontalSashForm.setWeights( 1, 1 );
    horizontalSashForm.setSashWidth( 5 );
    verticalSashForm.setSashWidth( 5 );
    panelClasses.addTsSelectionListener( ( src, sel ) -> whenClassSelectionChanges() );
    panelObjects.addTsSelectionListener( ( src, sel ) -> whenObjectSelectionChanges() );
    panelProps.addTsSelectionListener( ( src, sel ) -> genericChangeEventer().fireChangeEvent() );
    panelProps.addTsDoubleClickListener( ( src, sel ) -> whenPropDoubleClicked( sel ) );
    return board;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Invokes dialog with {@link IPanelSingleCondInfo} for {@link Ugwi} editing.
   *
   * @param aDialogInfo {@link ITsDialogInfo} - the dialog window parameters
   * @param aInitVal {@link Ugwi} - initial value or <code>null</code>
   * @param aCoreApi {@link ISkCoreApi} - core API
   * @return {@link Ugwi} - edited value or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static Ugwi selectUgwi( ITsDialogInfo aDialogInfo, Ugwi aInitVal, ISkCoreApi aCoreApi ) {
    TsNullArgumentRtException.checkNulls( aDialogInfo );
    ISkCoreGuiConstants.REFDEF_SK_VALED_CORE_API.setRef( aDialogInfo.tsContext(), aCoreApi );
    IDialogPanelCreator<Ugwi, Object> creator = ( par, od ) //
    -> new TsDialogGenericEntityEditPanel<>( par, od, ( aContext, aViewer ) -> {
      SingleSkPropUgwiSelectPanel panel = new SingleSkPropUgwiSelectPanel( aContext, aViewer );
      return panel;
    } );
    TsDialog<Ugwi, Object> d = new TsDialog<>( aDialogInfo, aInitVal, null, creator );
    return d.execData();
  }

  @Override
  public Ugwi selectedItem() {
    TsValidationFailedRtException.checkError( canGetEntity() );
    ISkObject selObj = panelObjects.selectedItem();
    IDtoClassPropInfoBase selProp = panelProps.selectedItem();
    ESkClassPropKind kind = getClassPropKind();
    Gwid gwid = kind.createConcreteGwid( selObj.skid(), selProp.id() );
    String ugwiKindId = tsContext().params().getStr( OPDEF_SINGLE_UGWI_KIND_ID );
    Ugwi retVal = Ugwi.of( ugwiKindId, gwid.canonicalString() );
    return retVal;
  }

  @Override
  public void setSelectedItem( Ugwi aItem ) {
    panelProps.setSelectedItem( null );
    panelObjects.setSelectedItem( null );
    panelClasses.setSelectedItem( null );
    if( aItem != null ) {
      Gwid gwid = Gwid.of( aItem.essence() );
      ISkClassInfo cinf = coreApi.sysdescr().findClassInfo( gwid.classId() );
      if( cinf != null ) {
        panelClasses.setSelectedItem( cinf );
        if( !gwid.isAbstract() && !gwid.isMulti() ) {
          ISkObject obj = coreApi.objService().find( gwid.skid() );
          if( obj != null && gwid.isProp() ) {
            panelObjects.setSelectedItem( obj );
            IDtoClassPropInfoBase prop = cinf.props( getClassPropKind() ).list().findByKey( gwid.propId() );
            panelProps.setSelectedItem( prop );
          }
        }
      }
    }
  }

  @Override
  public void addTsSelectionListener( ITsSelectionChangeListener<Ugwi> aListener ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeTsSelectionListener( ITsSelectionChangeListener<Ugwi> aListener ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void refresh() {
    // nop
  }

  public ESkClassPropKind getClassPropKind() {
    return skClassPropKind;
  }

  public EGwidKind getGwidKind() {
    return skClassPropKind.gwidKind();
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //
  private void whenClassSelectionChanges() {
    panelProps.setSelectedItem( null );
    panelObjects.setSelectedItem( null );
    ISkClassInfo cinf = panelClasses.selectedItem();
    if( cinf != null ) {
      M5DefaultItemsProvider<ISkObject> itemsProvider = new M5DefaultItemsProvider<>();
      itemsProvider.items().setAll( coreApi.objService().listObjs( cinf.id(), true ) );
      panelObjects.setItemsProvider( itemsProvider );
      panelObjects.refresh();
    }
  }

  private void whenObjectSelectionChanges() {
    panelProps.setSelectedItem( null );
    ISkObject sel = panelObjects.selectedItem();
    if( sel != null ) {
      M5DefaultItemsProvider<IDtoClassPropInfoBase> itemsProvider = new M5DefaultItemsProvider<>();
      ISkClassInfo cinf = panelClasses.selectedItem();
      itemsProvider.items().setAll( cinf.props( getClassPropKind() ).list() );
      panelProps.setItemsProvider( itemsProvider );
      panelProps.refresh();
    }
  }

  void whenPropDoubleClicked( IDtoClassPropInfoBase aSel ) {
    if( aSel != null && !canGetEntity().isError() ) {
      // TODO
      // fireTsDoubleClickEvent( getEntity() );
    }
  }

}
