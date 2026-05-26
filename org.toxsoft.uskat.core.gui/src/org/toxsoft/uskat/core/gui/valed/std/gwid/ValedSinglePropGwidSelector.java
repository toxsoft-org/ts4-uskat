package org.toxsoft.uskat.core.gui.valed.std.gwid;

import static org.toxsoft.core.tsgui.m5.gui.mpc.IMultiPaneComponentConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.gui.valed.std.gwid.ISkResources.*;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.gui.km5.sded2.skobj.*;
import org.toxsoft.uskat.core.gui.km5.sded2.sysdecsr.*;
import org.toxsoft.uskat.core.gui.valed.*;

/**
 * Package-private class of the single class proprty GWID selector implementation.
 *
 * @author hazard157
 */
class ValedSinglePropGwidSelector
    extends AbstractSkValedControl<Gwid>
    implements IM5ItemsProvider<IDtoClassPropInfoBase> {

  private final IM5ItemsProvider<ISkClassInfo>   classesProvider;
  private final IM5CollectionPanel<ISkClassInfo> classesListPane;

  // private Skid skid = Skid.NONE;
  private String  classId           = TsLibUtils.EMPTY_STRING;
  private boolean includeSubclasses = true;

  IM5CollectionPanel<ISkObject> objectsPanel    = null;
  IM5ItemsProvider<ISkObject>   objectsProvider = () -> {
                                                  if( StridUtils.isValidIdPath( classId ) ) {
                                                    ISkidList skids = skConn().coreApi().objService()
                                                        .listSkids( classId, includeSubclasses );
                                                    return skConn().coreApi().objService().getObjs( skids );
                                                  }
                                                  return IList.EMPTY;
                                                };

  private final IM5CollectionPanel<IDtoClassPropInfoBase> propsPanel;

  boolean isAbstract = false;

  ESkClassPropKind propKind = ESkClassPropKind.ATTR;

  /**
   * Constructor.
   *
   * @param aContext {@link ITsGuiContext} - the VALED context
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public ValedSinglePropGwidSelector( ITsGuiContext aContext ) {
    super( aContext );
    if( aContext.params().hasKey( ValedGwidSelector.PARAMID_ABSTRACT ) ) {
      isAbstract = aContext.params().getBool( ValedGwidSelector.PARAMID_ABSTRACT );
    }
    if( aContext.params().hasKey( ValedGwidSelector.PARAMID_PROP_KIND ) ) {
      propKind = aContext.params().getValobj( ValedGwidSelector.PARAMID_PROP_KIND );
    }

    // classesListPane
    IM5Model<ISkClassInfo> modelSk = m5().getModel( Sded2SkClassInfoM5Model.MODEL_ID, ISkClassInfo.class );
    IM5LifecycleManager<ISkClassInfo> lmSk = modelSk.getLifecycleManager( skConn() );
    classesProvider = lmSk.itemsProvider();
    ITsGuiContext ctx1 = new TsGuiContext( tsContext() );
    OPDEF_IS_FILTER_PANE.setValue( ctx1.params(), AV_TRUE );
    OPDEF_IS_DETAILS_PANE_HIDDEN.setValue( ctx1.params(), AV_TRUE );
    classesListPane = modelSk.panelCreator().createCollViewerPanel( ctx1, classesProvider );
    classesListPane.addTsSelectionListener( ( src, sel ) -> fireModifyEvent( true ) );

    if( !isAbstract ) {
      // objectsListPane
      IM5Model<ISkObject> objModel = m5().getModel( Sded2SkObjectM5Model.MODEL_ID, ISkObject.class );
      ITsGuiContext ctx2 = new TsGuiContext( aContext );
      OPDEF_IS_DETAILS_PANE_HIDDEN.setValue( ctx2.params(), AV_TRUE );
      objectsPanel = objModel.panelCreator().createCollViewerPanel( ctx2, IM5ItemsProvider.EMPTY );
      objectsPanel.setItemsProvider( objectsProvider );
      objectsPanel.addTsSelectionListener( ( src, sel ) -> fireModifyEvent( true ) );
    }

    // panelProps
    String propModelId = classPropM5ModelId( propKind );
    IM5Model<IDtoClassPropInfoBase> modelProps = m5().getModel( propModelId, IDtoClassPropInfoBase.class );
    ITsGuiContext ctx3 = new TsGuiContext( tsContext() );
    OPDEF_IS_DETAILS_PANE_HIDDEN.setValue( ctx3.params(), AV_TRUE );
    propsPanel = modelProps.panelCreator().createCollViewerPanel( ctx3, IM5ItemsProvider.EMPTY );
    propsPanel.setItemsProvider( this );
    propsPanel.addTsSelectionListener( ( src, sel ) -> fireModifyEvent( true ) );

    classesListPane.addTsSelectionListener( ( aSource, aSelectedItem ) -> {
      if( aSelectedItem != null ) {
        classId = aSelectedItem.id();
      }
      else {
        classId = TsLibUtils.EMPTY_STRING;
      }
      if( objectsPanel != null ) {
        objectsPanel.refresh();
      }
      propsPanel.refresh();
    } );

  }

  // ------------------------------------------------------------------------------------
  // AbstractValedControl
  //

  @Override
  protected Control doCreateControl( Composite aParent ) {
    SashForm sash;
    if( isAbstract ) {
      sash = new SashForm( aParent, SWT.HORIZONTAL );
      classesListPane.createControl( sash );
    }
    else {
      sash = new SashForm( aParent, SWT.VERTICAL );
      SashForm topSash = new SashForm( sash, SWT.HORIZONTAL );
      classesListPane.createControl( topSash );
      objectsPanel.createControl( topSash );
      topSash.setWeights( 2, 1 );
    }
    propsPanel.createControl( sash );
    sash.setWeights( 2, 1 );
    return sash;
  }

  @Override
  protected void doSetEditable( boolean aEditable ) {
    classesListPane.setEditable( aEditable );
    propsPanel.setEditable( aEditable );
  }

  @Override
  public ValidationResult canGetValue() {
    if( classesListPane.selectedItem() == null ) {
      return ValidationResult.error( STR_ERR_NO_SELECTED_CLASS );
    }

    if( propsPanel.selectedItem() == null ) {
      return ValidationResult.error( STR_ERR_NO_SELECTED_PROPERTY );
    }

    if( !isAbstract ) {
      if( objectsPanel.selectedItem() == null ) {
        return ValidationResult.error( STR_ERR_NO_SELECTED_OBJECT );
      }
    }

    return ValidationResult.SUCCESS;
  }

  @Override
  protected Gwid doGetUnvalidatedValue() {
    String clsId = classesListPane.selectedItem().id();
    String propId = propsPanel.selectedItem().id();
    if( isAbstract ) {
      return createAbstractGwid( propKind, clsId, propId );
    }
    String strid = objectsPanel.selectedItem().id();
    return createGwid( propKind, clsId, strid, propId );
  }

  @Override
  protected void doSetUnvalidatedValue( Gwid aValue ) {
    ISkClassInfo clsInfo = skConn().coreApi().sysdescr().findClassInfo( aValue.classId() );
    IDtoClassPropInfoBase propInfo = clsInfo.props( propKind ).list().getByKey( aValue.propId() );
    propsPanel.setSelectedItem( propInfo );
    if( !isAbstract ) {
      Skid skid = new Skid( aValue.classId(), aValue.strid() );
      ISkObject skObj = skConn().coreApi().objService().get( skid );
      objectsPanel.setSelectedItem( skObj );
    }
  }

  @Override
  protected void doClearValue() {
    classesListPane.setSelectedItem( null );
    objectsPanel.setSelectedItem( null );
    propsPanel.setSelectedItem( null );
  }
  // ------------------------------------------------------------------------------------
  // IM5ItemsProvider
  //

  @Override
  public IList<IDtoClassPropInfoBase> listItems() {
    if( StridUtils.isValidIdPath( classId ) ) {
      ISkClassInfo clsInfo = skConn().coreApi().sysdescr().findClassInfo( classId );
      if( clsInfo != null ) {
        return clsInfo.props( propKind ).list();
      }
    }
    return IList.EMPTY;
  }

  // ------------------------------------------------------------------------------------
  // Static methods
  //

  static String classPropM5ModelId( ESkClassPropKind aKind ) {
    return switch( aKind ) {
      case ATTR -> Sded2DtoAttrInfoM5Model.MODEL_ID;
      case CLOB -> Sded2DtoClobInfoM5Model.MODEL_ID;
      case CMD -> Sded2DtoCmdInfoM5Model.MODEL_ID;
      case EVENT -> Sded2DtoEventInfoM5Model.MODEL_ID;
      case LINK -> Sded2DtoLinkInfoM5Model.MODEL_ID;
      case RIVET -> Sded2DtoRivetInfoM5Model.MODEL_ID;
      case RTDATA -> Sded2DtoRtdataInfoM5Model.MODEL_ID;
      default -> throw new TsNotAllEnumsUsedRtException( aKind.name() );
    };
  }

  static Gwid createAbstractGwid( ESkClassPropKind aKind, String aClassId, String aPropId ) {
    return switch( aKind ) {
      case ATTR -> Gwid.createAttr( aClassId, aPropId );
      case CLOB -> Gwid.createClob( aClassId, aPropId );
      case CMD -> Gwid.createCmd( aClassId, aPropId );
      case EVENT -> Gwid.createEvent( aClassId, aPropId );
      case LINK -> Gwid.createLink( aClassId, aPropId );
      case RIVET -> Gwid.createRivet( aClassId, aPropId );
      case RTDATA -> Gwid.createRtdata( aClassId, aPropId );
      default -> throw new TsNotAllEnumsUsedRtException( aKind.name() );
    };
  }

  static Gwid createGwid( ESkClassPropKind aKind, String aClassId, String aStrid, String aPropId ) {
    return switch( aKind ) {
      case ATTR -> Gwid.createAttr( aClassId, aStrid, aPropId );
      case CLOB -> Gwid.createClob( aClassId, aStrid, aPropId );
      case CMD -> Gwid.createCmd( aClassId, aStrid, aPropId );
      case EVENT -> Gwid.createEvent( aClassId, aStrid, aPropId );
      case LINK -> Gwid.createLink( aClassId, aStrid, aPropId );
      case RIVET -> Gwid.createRivet( aClassId, aStrid, aPropId );
      case RTDATA -> Gwid.createRtdata( aClassId, aStrid, aPropId );
      default -> throw new TsNotAllEnumsUsedRtException( aKind.name() );
    };
  }

}
