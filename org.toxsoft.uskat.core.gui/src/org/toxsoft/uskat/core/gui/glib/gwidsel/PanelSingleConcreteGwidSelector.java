package org.toxsoft.uskat.core.gui.glib.gwidsel;

import static org.toxsoft.uskat.core.gui.glib.gwidsel.IGwidSelectorConstants.*;
import static org.toxsoft.uskat.core.gui.glib.gwidsel.ISkResources.*;
import static org.toxsoft.uskat.core.gui.km5.sgw.ISgwM5Constants.*;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.bricks.events.change.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.core.gui.glib.*;

/**
 * {@link IPanelSingleConcreteGwidSelector} implementation.
 * <p>
 * Any constructor uses {@link IGwidSelectorConstants#OPDEF_CLASS_PROP_KIND} context option to determine the selectable
 * property kind {@link #getClassPropKind()}.
 *
 * @author dima
 * @author hazard157
 */
public class PanelSingleConcreteGwidSelector
    extends AbstractSkStdEventsProducerLazyPanel<Gwid>
    implements IPanelSingleConcreteGwidSelector {

  private final GenericChangeEventer genericChangeEventer;

  private final ESkClassPropKind skClassPropKind;

  private final IM5CollectionPanel<ISkClassInfo>          panelClasses;
  private final IM5CollectionPanel<ISkObject>             panelObjects;
  private final IM5CollectionPanel<IDtoClassPropInfoBase> panelProps;

  /**
   * Constructor.
   * <p>
   * Used connection ID is initialized to <code>null</code> thus using {@link ISkConnectionSupplier#defConn()}.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public PanelSingleConcreteGwidSelector( ITsGuiContext aContext ) {
    this( aContext, null );
  }

  /**
   * Constructor.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @param aUsedConnId {@link IdChain} - ID of connection to be used, may be <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public PanelSingleConcreteGwidSelector( ITsGuiContext aContext, IdChain aUsedConnId ) {
    super( aContext, aUsedConnId );
    genericChangeEventer = new GenericChangeEventer( this );
    skClassPropKind = OPDEF_CLASS_PROP_KIND.getValue( tsContext().params() ).asValobj();
    IM5Domain m5 = skConn().scope().get( IM5Domain.class );
    // panelClasses
    IM5Model<ISkClassInfo> modelClasses = m5.getModel( MID_SGW_CLASS_INFO, ISkClassInfo.class );
    IM5LifecycleManager<ISkClassInfo> lmClasses = modelClasses.getLifecycleManager( skConn() );
    panelClasses = modelClasses.panelCreator().createCollViewerPanel( aContext, lmClasses.itemsProvider() );
    // panelObjects
    IM5Model<ISkObject> modelObjects = m5.getModel( MID_SGW_SK_OBJECT, ISkObject.class );
    panelObjects = modelObjects.panelCreator().createCollViewerPanel( aContext, IM5ItemsProvider.EMPTY );
    // panelProps
    String propModelId = sgwGetClassPropModelId( getClassPropKind() );
    IM5Model<IDtoClassPropInfoBase> modelProps = m5.getModel( propModelId, IDtoClassPropInfoBase.class );
    panelProps = modelProps.panelCreator().createCollViewerPanel( aContext, IM5ItemsProvider.EMPTY );
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
      itemsProvider.items().setAll( skObjServ().listObjs( cinf.id(), true ) );
      panelObjects.setItemsProvider( itemsProvider );
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
    }
  }

  void whenPropDoubleClicked( IDtoClassPropInfoBase aSel ) {
    if( aSel != null && !canGetEntity().isError() ) {
      fireTsDoubleClickEvent( getEntity() );
    }
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkLazyPanel
  //

  @Override
  protected void doInitGui( Composite aParent ) {
    SashForm verticalSashForm = new SashForm( aParent, SWT.VERTICAL );
    SashForm horizontalSashForm = new SashForm( verticalSashForm, SWT.HORIZONTAL );
    // panels
    panelClasses.createControl( horizontalSashForm );
    panelObjects.createControl( horizontalSashForm );
    panelProps.createControl( verticalSashForm );
    // setup
    horizontalSashForm.setWeights( 1000, 1000 );
    verticalSashForm.setWeights( 1000, 1000 );
    panelClasses.addTsSelectionListener( ( src, sel ) -> whenClassSelectionChanges() );
    panelObjects.addTsSelectionListener( ( src, sel ) -> whenObjectSelectionChanges() );
    panelProps.addTsSelectionListener( ( src, sel ) -> genericChangeEventer().fireChangeEvent() );
    panelProps.addTsDoubleClickListener( ( src, sel ) -> whenPropDoubleClicked( sel ) );
  }

  // ------------------------------------------------------------------------------------
  // IGenericChangeEventCapable
  //

  @Override
  public GenericChangeEventer genericChangeEventer() {
    return genericChangeEventer;
  }

  // ------------------------------------------------------------------------------------
  // IGenericContentPanel
  //

  @Override
  public boolean isViewer() {
    return false;
  }

  // ------------------------------------------------------------------------------------
  // IGenericEntityPanel
  //

  @Override
  public void setEntity( Gwid aGwid ) {
    panelProps.setSelectedItem( null );
    panelObjects.setSelectedItem( null );
    panelClasses.setSelectedItem( null );
    if( aGwid != null ) {
      ISkClassInfo cinf = skSysdescr().findClassInfo( aGwid.classId() );
      if( cinf != null ) {
        panelClasses.setSelectedItem( cinf );
        if( !aGwid.isAbstract() && !aGwid.isMulti() ) {
          ISkObject obj = skObjServ().find( aGwid.skid() );
          if( obj != null && aGwid.isProp() ) {
            panelObjects.setSelectedItem( obj );
            IDtoClassPropInfoBase prop = cinf.props( getClassPropKind() ).list().findByKey( aGwid.propId() );
            panelProps.setSelectedItem( prop );
          }
        }
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // IGenericEntityEditPanel
  //

  @Override
  public Gwid getEntity() {
    TsValidationFailedRtException.checkError( canGetEntity() );
    ISkObject selObj = panelObjects.selectedItem();
    IDtoClassPropInfoBase selProp = panelProps.selectedItem();
    ESkClassPropKind kind = getClassPropKind();
    return kind.createConcreteGwid( selObj.skid(), selProp.id() );
  }

  @Override
  public ValidationResult canGetEntity() {
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

  // ------------------------------------------------------------------------------------
  // AbstractSkStdEventsProducerLazyPanel
  //

  @Override
  protected Gwid doGetSelectedItem() {
    if( !canGetEntity().isError() ) {
      return getEntity();
    }
    return null;
  }

  @Override
  protected void doSetSelectedItem( Gwid aItem ) {
    setEntity( aItem );
  }

  // ------------------------------------------------------------------------------------
  // IPanelConcreteSingleGwidSelector
  //

  @Override
  public ESkClassPropKind getClassPropKind() {
    return skClassPropKind;
  }

  @Override
  public EGwidKind getGwidKind() {
    return skClassPropKind.gwidKind();
  }

}
