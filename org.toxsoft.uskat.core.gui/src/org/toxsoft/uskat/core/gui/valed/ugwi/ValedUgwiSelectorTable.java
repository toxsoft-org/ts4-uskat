package org.toxsoft.uskat.core.gui.valed.ugwi;

import static org.toxsoft.core.tsgui.valed.api.IValedControlConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.gui.ISkCoreGuiConstants.*;
import static org.toxsoft.uskat.core.gui.km5.sgw.ISgwM5Constants.*;
import static org.toxsoft.uskat.core.gui.ugwi.kinds.ISkResources.*;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.mpc.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.utils.layout.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tsgui.widgets.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.bricks.events.change.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
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
import org.toxsoft.uskat.core.gui.ugwi.kinds.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Ugwi selector VALED.
 *
 * @author hazard157 // API
 * @author dima // implementation
 */
public class ValedUgwiSelectorTable
    extends AbstractValedControl<Ugwi, Control> {

  /**
   * The factory name.
   */
  public static final String FACTORY_NAME = VALED_EDNAME_PREFIX + ".UgwiSelectorTable"; //$NON-NLS-1$

  private final GenericChangeEventer genericChangeEventer;

  private final ISkCoreApi coreApi; // never is null

  private final ESkClassPropKind skClassPropKind;

  private final IM5CollectionPanel<ISkClassInfo>          panelClasses;
  private final IM5CollectionPanel<ISkObject>             panelObjects;
  private final IM5CollectionPanel<IDtoClassPropInfoBase> panelProps;

  /**
   * Package-private factory singleton for {@link ValedAvUgwiSelectorTextAndButton} constructor.
   * <p>
   * This factory is <b>not</b> intended to be registered.
   */
  static final IValedControlFactory FACTORY = new AbstractValedControlFactory( FACTORY_NAME ) {

    @SuppressWarnings( "unchecked" )
    @Override
    protected IValedControl<Ugwi> doCreateEditor( ITsGuiContext aContext ) {
      return new ValedUgwiSelectorTable( aContext );
    }
  };

  ValedUgwiSelectorTable( ITsGuiContext aContext ) {
    super( aContext );
    genericChangeEventer = new GenericChangeEventer( this );

    coreApi = skCoreApi( tsContext() );
    TsInternalErrorRtException.checkNull( coreApi );
    skClassPropKind = SingleSkPropUgwiSelectPanel.OPDEF_CLASS_PROP_KIND.getValue( tsContext().params() ).asValobj();
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
    String propModelId = sgwGetClassPropModelId( skClassPropKind );
    IM5Model<IDtoClassPropInfoBase> modelProps = m5.getModel( propModelId, IDtoClassPropInfoBase.class );
    panelProps = modelProps.panelCreator().createCollViewerPanel( ctx, IM5ItemsProvider.EMPTY );
  }

  // ------------------------------------------------------------------------------------
  // IGenericChangeEventCapable
  //
  /**
   * @return {@link GenericChangeEventer}
   */
  final public GenericChangeEventer genericChangeEventer() {
    return genericChangeEventer;
  }

  // ------------------------------------------------------------------------------------
  // AbstractValedControl
  //

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

  @Override
  protected void doSetEditable( boolean aEditable ) {
    // nop
  }

  @Override
  protected ValidationResult doCanGetValue() {
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
  protected Ugwi doGetUnvalidatedValue() {
    TsValidationFailedRtException.checkError( doCanGetValue() );
    ISkObject selObj = panelObjects.selectedItem();
    IDtoClassPropInfoBase selProp = panelProps.selectedItem();
    Gwid gwid = skClassPropKind.createConcreteGwid( selObj.skid(), selProp.id() );
    String ugwiKindId = tsContext().params().getStr( SingleSkPropUgwiSelectPanel.OPDEF_SK_UGWI_KIND_ID );
    Ugwi retVal = Ugwi.of( ugwiKindId, gwid.canonicalString() );
    return retVal;
  }

  @Override
  protected void doSetUnvalidatedValue( Ugwi aValue ) {
    panelProps.setSelectedItem( null );
    panelObjects.setSelectedItem( null );
    panelClasses.setSelectedItem( null );
    if( aValue != null ) {
      Gwid gwid = Gwid.of( aValue.essence() );
      ISkClassInfo cinf = coreApi.sysdescr().findClassInfo( gwid.classId() );
      if( cinf != null ) {
        panelClasses.setSelectedItem( cinf );
        if( !gwid.isAbstract() && !gwid.isMulti() ) {
          ISkObject obj = coreApi.objService().find( gwid.skid() );
          if( obj != null && gwid.isProp() ) {
            panelObjects.setSelectedItem( obj );
            IDtoClassPropInfoBase prop = cinf.props( skClassPropKind ).list().findByKey( gwid.propId() );
            panelProps.setSelectedItem( prop );
          }
        }
      }
    }
  }

  @Override
  protected void doClearValue() {
    panelProps.setSelectedItem( null );
    panelObjects.setSelectedItem( null );
    panelClasses.setSelectedItem( null );
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
      // clear props list
      M5DefaultItemsProvider<IDtoClassPropInfoBase> propItemsProvider = new M5DefaultItemsProvider<>();
      propItemsProvider.items().setAll( IStridablesList.EMPTY );
      panelProps.setItemsProvider( propItemsProvider );
      panelProps.refresh();
    }
  }

  private void whenObjectSelectionChanges() {
    panelProps.setSelectedItem( null );
    ISkObject sel = panelObjects.selectedItem();
    if( sel != null ) {
      M5DefaultItemsProvider<IDtoClassPropInfoBase> itemsProvider = new M5DefaultItemsProvider<>();
      ISkClassInfo cinf = panelClasses.selectedItem();
      itemsProvider.items().setAll( cinf.props( skClassPropKind ).list() );
      panelProps.setItemsProvider( itemsProvider );
      panelProps.refresh();
    }
  }

  void whenPropDoubleClicked( IDtoClassPropInfoBase aSel ) {
    if( aSel != null && !doCanGetValue().isError() ) {
      // TODO
      // fireTsDoubleClickEvent( getEntity() );
    }
  }

}
