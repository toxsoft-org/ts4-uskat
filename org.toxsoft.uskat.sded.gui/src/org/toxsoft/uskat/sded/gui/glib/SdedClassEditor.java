package org.toxsoft.uskat.sded.gui.glib;

import static org.toxsoft.uskat.sded.gui.km5.IKM5SdedConstants.*;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.uskat.base.gui.conn.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;

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
    extends AbstractLazySkConnectedPanel<Control> {

  private final IM5CollectionPanel<IDtoClassInfo> classesListPane;
  private final IM5EntityPanel<IDtoClassInfo>     classEditPane;
  // private final IM5CollectionPanel<IDtoClassInfo> classEditPane;

  /**
   * Constrcutor.
   * <p>
   * Panel will use {@link ISkConnection} with the given ID from {@link ISkConnectionSupplier#getConn(IdChain)}. If
   * <code>aSuppliedConnectionId</code> = <code>null</code>, then {@link ISkConnectionSupplier#defConn()} will be used.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @param aSuppliedConnectionId {@link IdChain} - ID of connection or <code>null</code> for default
   */
  public SdedClassEditor( ITsGuiContext aContext, IdChain aSuppliedConnectionId ) {
    super( aContext, aSuppliedConnectionId );
    IM5Model<IDtoClassInfo> modelDto = m5().getModel( MID_SDED_DTO_CLASS_INFO, IDtoClassInfo.class );
    // left pane
    IM5LifecycleManager<IDtoClassInfo> lmSk = modelDto.getLifecycleManager( skConn() );
    IM5ItemsProvider<IDtoClassInfo> ipSk = lmSk.itemsProvider();
    ITsGuiContext ctxSk = new TsGuiContext( tsContext() );
    classesListPane = modelDto.panelCreator().createCollEditPanel( ctxSk, ipSk, lmSk );
    classesListPane.addTsSelectionListener( ( s, i ) -> whenClassListSelectionChnages() );

    // TODO right pane
    IM5LifecycleManager<IDtoClassInfo> lmDto = modelDto.getLifecycleManager( skConn() );
    ITsGuiContext ctxDto = new TsGuiContext( tsContext() );
    classEditPane = modelDto.panelCreator().createEntityEditorPanel( ctxDto, lmDto );
    // classEditPane = modelDto.panelCreator().createCollEditPanel( ctxDto, lmDto.itemsProvider(), lmDto );

  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private void whenClassListSelectionChnages() {
    IDtoClassInfo sel = classesListPane.selectedItem();
    classEditPane.setEntity( sel );
    // classEditPane.setSelectedItem( dto );
  }

  // ------------------------------------------------------------------------------------
  // AbstractLazyPanel
  //

  @Override
  protected Control doCreateControl( Composite aParent ) {
    SashForm sfMain = new SashForm( aParent, SWT.HORIZONTAL );
    classesListPane.createControl( sfMain );
    classEditPane.createControl( sfMain );
    sfMain.setWeights( 3000, 7000 );
    return sfMain;

    // TsComposite board = new TsComposite( aParent );
    // board.setLayout( new BorderLayout() );
    // classEditPane.createControl( board );
    //
    // return board;
  }

}
