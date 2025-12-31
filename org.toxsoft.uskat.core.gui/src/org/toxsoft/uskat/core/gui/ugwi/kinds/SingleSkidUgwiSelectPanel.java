package org.toxsoft.uskat.core.gui.ugwi.kinds;

import static org.toxsoft.core.tslib.ITsHardConstants.*;
import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.gui.ISkCoreGuiConstants.*;
import static org.toxsoft.uskat.core.gui.ugwi.kinds.ISkCoreGuiUgwiKindsSharedResources.*;
import static org.toxsoft.uskat.core.gui.valed.ugwi.ValedUgwiSelector.*;

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
import org.toxsoft.core.tsgui.panels.generic.*;
import org.toxsoft.core.tsgui.utils.layout.*;
import org.toxsoft.core.tsgui.widgets.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.api.ugwis.*;
import org.toxsoft.uskat.core.api.ugwis.kinds.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.core.gui.km5.sded.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * {@link IGenericSelectorPanel} implementation to select single Skid.
 * <p>
 *
 * @author dima
 */
public class SingleSkidUgwiSelectPanel
    extends AbstractGenericEntityEditPanel<Ugwi>
    implements IGenericSelectorPanel<Ugwi> {

  /**
   * ID of option {@link #OPDEF_CLASS_IDS_LIST}.
   */
  public static final String OPID_CLASS_IDS_LIST = TS_ID + ".gui.ugwi.kinds.ClassIdsList"; //$NON-NLS-1$

  /**
   * {@link SingleSkidUgwiSelectPanel#OPDEF_CLASS_IDS_LIST} option: list ID of classes for selection from.
   */
  public static final IDataDef OPDEF_CLASS_IDS_LIST = DataDef.create( OPID_CLASS_IDS_LIST, VALOBJ, //
      TSID_NAME, STR_CLASS_IDS_LIST, //
      TSID_DESCRIPTION, STR_CLASS_IDS_LIST, //
      TSID_KEEPER_ID, StringListKeeper.KEEPER_ID, //
      TSID_DEFAULT_VALUE, avValobj( IStringList.EMPTY ) );

  private final ISkCoreApi coreApi; // never is null

  private final IM5CollectionPanel<ISkObject> panelObjects;

  /**
   * List of classes to select from
   */
  private IStringList filteredClassIdList = IStringList.EMPTY;

  /**
   * Constructor.
   * <p>
   * Constructor stores reference to the context, does not creates copy.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @param aIsViewer boolean - viewer flag, sets {@link #isViewer()} value
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SingleSkidUgwiSelectPanel( ITsGuiContext aContext, boolean aIsViewer ) {
    super( aContext, aIsViewer );
    coreApi = skCoreApi( tsContext() );
    TsInternalErrorRtException.checkNull( coreApi );
    // check class ids for selection
    if( aContext.params().hasValue( OPDEF_CLASS_IDS_LIST ) ) {
      filteredClassIdList = aContext.params().getValobj( OPDEF_UGWI_KIND_IDS_LIST );
    }

    ISkConnection conn = ((SkCoreApi)coreApi).skConn();
    IM5Domain m5 = conn.scope().get( IM5Domain.class );

    ITsGuiContext ctx = new TsGuiContext( aContext );
    ctx.params().addAll( aContext.params() );
    IMultiPaneComponentConstants.OPDEF_IS_DETAILS_PANE.setValue( ctx.params(), AvUtils.AV_FALSE );
    IMultiPaneComponentConstants.OPDEF_DETAILS_PANE_PLACE.setValue( ctx.params(),
        avValobj( EBorderLayoutPlacement.SOUTH ) );
    // добавляем в панель фильтр
    IMultiPaneComponentConstants.OPDEF_IS_FILTER_PANE.setValue( ctx.params(), AvUtils.AV_TRUE );
    // panelObjects
    IM5Model<ISkObject> modelSk = m5.getModel( IKM5SdedConstants.MID_SDED_SK_OBJECT, ISkObject.class );
    IM5LifecycleManager<ISkObject> lmSk = modelSk.getLifecycleManager( conn );
    IM5ItemsProvider<ISkObject> itemsProvider = lmSk.itemsProvider();
    if( !filteredClassIdList.isEmpty() ) {
      itemsProvider = () -> {
        IListEdit<ISkObject> retVal = new ElemArrayList<>();
        for( String classId : filteredClassIdList ) {
          retVal.addAll( coreApi.objService().listObjs( classId, true ) );
        }
        return retVal;
      };
    }
    panelObjects = modelSk.panelCreator().createCollViewerPanel( ctx, itemsProvider );

  }

  // ------------------------------------------------------------------------------------
  // AbstractGenericEntityEditPanel
  //

  @Override
  protected ValidationResult doCanGetEntity() {
    ISkObject selObj = panelObjects.selectedItem();
    if( selObj == null ) {
      return ValidationResult.error( MSG_NO_SEL_OBJ );
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

    panelObjects.createControl( board );
    // setup
    panelObjects.addTsSelectionListener( ( src, sel ) -> whenObjectSelectionChanges() );
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
   * @param aSkConnKey {@link IdChain} - key for {@link ISkConnectionSupplier} of the Sk-connection to use
   * @return {@link Ugwi} - edited value or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static Ugwi selectUgwi( ITsDialogInfo aDialogInfo, Ugwi aInitVal, IdChain aSkConnKey ) {
    TsNullArgumentRtException.checkNulls( aDialogInfo );
    setCtxSkConnKey( aDialogInfo.tsContext(), aSkConnKey );
    IDialogPanelCreator<Ugwi, Object> creator = ( par, od ) //
    -> new TsDialogGenericEntityEditPanel<>( par, od, ( aContext, aViewer ) -> {
      SingleSkidUgwiSelectPanel panel = new SingleSkidUgwiSelectPanel( aContext, aViewer );
      return panel;
    } );
    TsDialog<Ugwi, Object> d = new TsDialog<>( aDialogInfo, aInitVal, null, creator );
    return d.execData();
  }

  @Override
  public Ugwi selectedItem() {
    TsValidationFailedRtException.checkError( canGetEntity() );
    ISkObject selObj = panelObjects.selectedItem();
    Gwid gwid = Gwid.createObj( selObj.skid() );
    Ugwi retVal = Ugwi.of( UgwiKindSkSkid.KIND_ID, gwid.canonicalString() );
    return retVal;
  }

  @Override
  public void setSelectedItem( Ugwi aItem ) {
    panelObjects.setSelectedItem( null );
    if( aItem != null ) {
      return;
    }
    ISkUgwiKind ugwiKind = coreApi.ugwiService().findKind( aItem );
    if( ugwiKind == null ) {
      return;
    }
    Gwid gwid = ugwiKind.ugwiKind().getGwid( aItem );
    ISkObject obj = coreApi.objService().find( gwid.skid() );
    if( obj != null ) {
      panelObjects.setSelectedItem( obj );
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

  private void whenObjectSelectionChanges() {
    ISkObject sel = panelObjects.selectedItem();
    if( sel != null ) {
      // TODO notify
    }
  }

  void whenPropDoubleClicked( IDtoClassPropInfoBase aSel ) {
    if( aSel != null && !canGetEntity().isError() ) {
      // TODO
      // fireTsDoubleClickEvent( getEntity() );
    }
  }

}
