package org.toxsoft.uskat.core.gui.glib;

import static org.toxsoft.uskat.core.gui.km5.sded.IKM5SdedConstants.*;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.panels.inpled.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * Class editor - allows to create, edit and delete classes in Sysdescr.
 * <p>
 * Panel contents:
 * <ul>
 * <li>left pane - classes list;</li>
 * <li>right pane - selected class editor.</li>
 * </ul>
 *
 * @author hazard157
 */
public class SdedClassEditor
    extends AbstractSkLazyPanel {

  private final ISkSysdescrListener sysdescrListener = ( api, op, classId ) -> whenSysdescrChanged( op, classId );

  private final IM5CollectionPanel<ISkClassInfo> classesListPane;
  private final IM5EntityPanel<IDtoClassInfo>    classEditPane;
  private final IInplaceEditorPanel              inplaceEditor;

  /**
   * When this flag is <code>true</code> selection events are ingored in the handler
   * {@link #whenClassListSelectionChanges()}.
   * <p>
   * Flag set/reset in {@link #whenSysdescrChanged(ECrudOp, String)}.
   */
  private boolean ignoreSelectionChange = false;

  /**
   * Constructor.
   * <p>
   * Panel will use {@link ISkConnection} with the given ID from {@link ISkConnectionSupplier#getConn(IdChain)}. If
   * <code>aSuppliedConnectionId</code> = <code>null</code>, then {@link ISkConnectionSupplier#defConn()} will be used.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @param aSuppliedConnectionId {@link IdChain} - ID of connection or <code>null</code> for default
   */
  public SdedClassEditor( ITsGuiContext aContext, IdChain aSuppliedConnectionId ) {
    super( aContext, aSuppliedConnectionId );
    // left pane
    IM5Model<ISkClassInfo> modelSk = m5().getModel( MID_SDED_SK_CLASS_INFO, ISkClassInfo.class );
    IM5LifecycleManager<ISkClassInfo> lmSk = modelSk.getLifecycleManager( skConn() );
    IM5ItemsProvider<ISkClassInfo> ipSk = lmSk.itemsProvider();
    ITsGuiContext ctxSk = new TsGuiContext( tsContext() );
    classesListPane = modelSk.panelCreator().createCollEditPanel( ctxSk, ipSk, lmSk );

    // right pane
    IM5Model<IDtoClassInfo> modelDto = m5().getModel( MID_SDED_DTO_CLASS_INFO, IDtoClassInfo.class );
    IM5LifecycleManager<IDtoClassInfo> lmDto = modelDto.getLifecycleManager( skConn() );
    ITsGuiContext ctxDto = new TsGuiContext( tsContext() );
    classEditPane = modelDto.panelCreator().createEntityEditorPanel( ctxDto, lmDto );
    classEditPane.setEditable( false );
    AbstractContentPanel contentPanel = new InplaceContentM5EntityPanelWrapper<>( ctxDto, classEditPane );
    inplaceEditor = new InplaceEditorContainerPanel( aContext, contentPanel );
  }

  @Override
  protected void doDispose() {
    skSysdescr().eventer().removeListener( sysdescrListener );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  /**
   * Handles selection change in the left panel {@link #classesListPane}.
   */
  private void whenClassListSelectionChanges() {
    if( ignoreSelectionChange ) {
      return;
    }
    ISkClassInfo sel = classesListPane.selectedItem();
    if( inplaceEditor.isEditing() ) {
      inplaceEditor.cancelAndFinishEditing();
    }
    if( sel != null ) {
      IDtoClassInfo dto = DtoClassInfo.createFromSk( sel, false );
      classEditPane.setEntity( dto );
    }
    else {
      classEditPane.setEntity( null );
    }
    inplaceEditor.refresh();
  }

  /**
   * Handles class(es) changes in {@link ISkSysdescr}, is called from {@link ISkSysdescrListener}.
   *
   * @param aOp {@link ECrudOp} - the kind of change
   * @param aClassId String - affected class ID or <code>null</code> for batch changes {@link ECrudOp#LIST}
   */
  private void whenSysdescrChanged( ECrudOp aOp, String aClassId ) {
    ISkClassInfo sel = classesListPane.selectedItem();
    // no selected class means that there is nothing in right panel, just refresh left panel
    if( sel == null ) {
      classesListPane.refresh();
      return;
    }
    String selClassId = sel.id();
    ignoreSelectionChange = true;
    try {
      classesListPane.refresh();
      sel = skSysdescr().findClassInfo( selClassId );
      classesListPane.setSelectedItem( sel );
      if( inplaceEditor.isEditing() ) {
        IDtoClassInfo dto = DtoClassInfo.createFromSk( sel, false );
        classEditPane.setEntity( dto );
      }
    }
    finally {
      ignoreSelectionChange = false;
    }
  }

  // ------------------------------------------------------------------------------------
  // AbstractLazyPanel
  //

  @Override
  protected void doInitGui( Composite aParent ) {
    SashForm sfMain = new SashForm( aParent, SWT.HORIZONTAL );
    classesListPane.createControl( sfMain );
    inplaceEditor.createControl( sfMain );
    sfMain.setWeights( 3000, 7000 );
    // setup
    classesListPane.addTsSelectionListener( ( s, i ) -> whenClassListSelectionChanges() );
    skSysdescr().eventer().addListener( sysdescrListener );
  }

}
