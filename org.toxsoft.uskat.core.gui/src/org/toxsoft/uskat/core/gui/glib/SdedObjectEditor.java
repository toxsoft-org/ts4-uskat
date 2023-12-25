package org.toxsoft.uskat.core.gui.glib;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.panels.inpled.*;
import org.toxsoft.core.tsgui.utils.layout.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.core.gui.km5.sded.*;
import org.toxsoft.uskat.core.gui.km5.sded.sded.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * Obect editor - allows to create, edit and delete objects in Sysdescr.
 * <p>
 * Panel contents:
 * <ul>
 * <li>left pane - objects tree;</li>
 * <li>right pane - selected object editor.</li>
 * </ul>
 *
 * @author dima
 */
public class SdedObjectEditor
    extends AbstractSkLazyPanel {

  private final ISkObjectServiceListener objServiceListener = ( api, op, aSkid ) -> whenObjectsChanged( op, aSkid );
  private final ISkSysdescrListener      sysdescrListener   =
      ( api, op, classId ) -> whenSysdescrChanged( op, classId );

  private final IM5CollectionPanel<ISkObject> objectListPane;
  private CTabFolder                          tabFolder;

  /**
   * Constructor.
   * <p>
   * Panel will use {@link ISkConnection} with the given ID from {@link ISkConnectionSupplier#getConn(IdChain)}. If
   * <code>aSuppliedConnectionId</code> = <code>null</code>, then {@link ISkConnectionSupplier#defConn()} will be used.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @param aSuppliedConnectionId {@link IdChain} - ID of connection or <code>null</code> for default
   */
  public SdedObjectEditor( ITsGuiContext aContext, IdChain aSuppliedConnectionId ) {
    super( aContext, aSuppliedConnectionId );
    // left pane
    IM5Model<ISkObject> modelSk = m5().getModel( IKM5SdedConstants.MID_SDED_SK_OBJECT, ISkObject.class );
    IM5LifecycleManager<ISkObject> lmSk = modelSk.getLifecycleManager( skConn() );
    IM5ItemsProvider<ISkObject> ipSk = lmSk.itemsProvider();
    ITsGuiContext ctxSk = new TsGuiContext( tsContext() );

    objectListPane = modelSk.panelCreator().createCollEditPanel( ctxSk, ipSk, lmSk );
    objectListPane.addTsSelectionListener( ( s, i ) -> whenObjectListSelectionChanges() );

  }

  @Override
  protected void doDispose() {
    skObjServ().eventer().removeListener( objServiceListener );
    skSysdescr().eventer().removeListener( sysdescrListener );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private void whenObjectListSelectionChanges() {
    ISkObject sel = objectListPane.selectedItem();

    if( sel != null ) {
      IDtoFullObject dtoFobj = DtoFullObject.createDtoFullObject( sel.skid(), skConn().coreApi() );
      tabFolder.setSelection( getSelectedTab( dtoFobj ) );
    }

  }

  private CTabItem getSelectedTab( IDtoFullObject aSelection ) {

    // IM5Model<IDtoFullObject> modelObj = m5().getModel( MID_SDED_DTO_FULL_OBJECT, IDtoFullObject.class );
    // на лету создаем новую модель объекта по описанию его класса
    SdedDtoFullObjectM5Model modelObj =
        new SdedDtoFullObjectM5Model( skConn(), skConn().coreApi().sysdescr().findClassInfo( aSelection.classId() ) );

    m5().initTemporaryModel( modelObj );
    // Внимание! В логике работы подразумевается что в названии закладки есть подстрока: skid().toString()
    String tabTitle = String.format( "%s ( %s )", aSelection.nmName(), aSelection.skid().toString() ); //$NON-NLS-1$
    // поищем в существующих
    for( CTabItem ti : tabFolder.getItems() ) {
      if( ti.getText().compareTo( tabTitle ) == 0 ) {
        return ti;
      }
    }
    IM5LifecycleManager<IDtoFullObject> lmObj = modelObj.getLifecycleManager( skConn() );
    ITsGuiContext ctxObj = new TsGuiContext( tsContext() );
    IM5EntityPanel<IDtoFullObject> objEditPane = modelObj.panelCreator().createEntityEditorPanel( ctxObj, lmObj );
    objEditPane.setEditable( false );

    // оборачиваем в специальный контейнер
    AbstractContentPanel contentPanel = new InplaceContentM5EntityPanelWrapper<>( ctxObj, objEditPane );
    IInplaceEditorPanel inplaceEditor = new InplaceEditorContainerPanel( tsContext(), contentPanel );
    Control iec = inplaceEditor.createControl( tabFolder );

    CTabItem tabItem = new CTabItem( tabFolder, SWT.CLOSE );
    tabItem.setText( tabTitle );
    tabItem.setControl( iec );
    // запоминаем ссылку на свой редактор
    tabItem.setData( objEditPane );
    objEditPane.setEntity( aSelection );
    return tabItem;
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkLazyPanel
  //

  @Override
  protected void doInitGui( Composite aParent ) {
    SashForm sfMain = new SashForm( aParent, SWT.HORIZONTAL );
    objectListPane.createControl( sfMain );

    // right pane
    tabFolder = new CTabFolder( sfMain, SWT.BORDER );
    tabFolder.setLayout( new BorderLayout() );
    sfMain.setWeights( 3000, 7000 );
    skObjServ().eventer().addListener( objServiceListener );
    skSysdescr().eventer().addListener( sysdescrListener );
  }

  private Object whenObjectsChanged( ECrudOp aOp, Skid aSkid ) {
    // обновим левый список объектов
    objectListPane.refresh();
    // теперь поищем в открытых закладках изменившийся объект
    for( CTabItem ti : tabFolder.getItems() ) {
      int skidIndex = ti.getText().indexOf( aSkid.toString() );
      if( skidIndex >= 0 ) {
        // нашли нужную закладку
        switch( aOp ) {
          case CREATE:
          case EDIT: {
            // TODO проверить!
            // при создании и редактировании объекта обновим элемент
            IDtoFullObject dtoFobj = DtoFullObject.createDtoFullObject( aSkid, skConn().coreApi() );
            IM5EntityPanel<IDtoFullObject> objEditPane = (IM5EntityPanel<IDtoFullObject>)ti.getData();
            objEditPane.setEntity( dtoFobj );
            break;
          }
          case REMOVE: {
            // при удалении объекта закроем закладку
            ti.dispose();
            break;
          }
          case LIST:
          default:
        }
      }
    }
    return null;
  }

  /**
   * Handles class(es) changes in {@link ISkSysdescr}, is called from {@link ISkSysdescrListener}.
   *
   * @param aOp {@link ECrudOp} - the kind of change
   * @param aClassId String - affected class ID or <code>null</code> for batch changes {@link ECrudOp#LIST}
   */
  private void whenSysdescrChanged( ECrudOp aOp, String aClassId ) {
    // обновим левый список объектов
    objectListPane.refresh();
    for( CTabItem ti : tabFolder.getItems() ) {
      int skidIndex = ti.getText().indexOf( aClassId );
      if( skidIndex >= 0 ) {
        // нашли нужную закладку
        switch( aOp ) {
          case CREATE:
          case EDIT:
          case REMOVE: {
            // при изменении класса объекта закроем закладку
            ti.dispose();
            break;
          }
          case LIST:
          default:
        }
      }
    }
  }

}
