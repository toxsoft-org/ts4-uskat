package org.toxsoft.uskat.core.gui.glib;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.utils.layout.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.uskat.core.api.objserv.*;
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

  private final IM5CollectionPanel<ISkObject> objectListPane;
  private IM5EntityPanel<IDtoFullObject>      objEditPane;
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
    objectListPane.addTsSelectionListener( ( s, i ) -> whenObjectListSelectionChnages() );

  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private void whenObjectListSelectionChnages() {
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
    String tabTitle = String.format( "%s (%s)", aSelection.nmName(), aSelection.skid().toString() ); //$NON-NLS-1$
    // поищем в существующих
    for( CTabItem ti : tabFolder.getItems() ) {
      if( ti.getText().compareTo( tabTitle ) == 0 ) {
        return ti;
      }
    }
    IM5LifecycleManager<IDtoFullObject> lmObj = modelObj.getLifecycleManager( skConn() );
    ITsGuiContext ctxObj = new TsGuiContext( tsContext() );
    objEditPane = modelObj.panelCreator().createEntityEditorPanel( ctxObj, lmObj );
    // objEditPane = modelObj.panelCreator().createEntityViewerPanel( ctxObj );

    CTabItem tabItem = new CTabItem( tabFolder, SWT.CLOSE );
    tabItem.setText( tabTitle );
    tabItem.setControl( objEditPane.createControl( tabFolder ) );
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
  }

}
