package org.toxsoft.uskat.core.gui.valed.std.skid;

import static org.toxsoft.uskat.core.gui.valed.std.skid.ISkResources.*;

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
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.gui.km5.sded2.skobj.*;
import org.toxsoft.uskat.core.gui.valed.*;
import org.toxsoft.uskat.core.gui.valed.std.clsid.*;

/**
 * Package-private class of the simple SKID selector.
 *
 * @author hazard157, vs
 */
public class ValedSkidSelectorSimple
    extends AbstractSkValedControl<Skid>
    implements IM5ItemsProvider<ISkObject> {

  private final ValedSkClassIdSelector classSelector;

  private String classId = TsLibUtils.EMPTY_STRING;

  IM5CollectionPanel<ISkObject> objectsPanel;

  boolean includeSubclasses = true;

  /**
   * Constructor.
   *
   * @param aContext {@link ITsGuiContext} - the VALED context
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public ValedSkidSelectorSimple( ITsGuiContext aContext ) {
    super( aContext );
    ITsGuiContext cisCtx = new TsGuiContext( aContext );
    classSelector = new ValedSkClassIdSelector( cisCtx );
    classSelector.eventer().addListener( ( aSource, aEditFinished ) -> {
      if( classSelector.canGetValue().isOk() ) {
        classId = classSelector.getValue();
        objectsPanel.refresh();
      }
    } );

    // objectsListPane
    IM5Model<ISkObject> objModel = m5().getModel( Sded2SkObjectM5Model.MODEL_ID, ISkObject.class );
    objectsPanel = objModel.panelCreator().createCollViewerPanel( aContext, IM5ItemsProvider.EMPTY );
    objectsPanel.setItemsProvider( this );
    objectsPanel.addTsSelectionListener( ( src, sel ) -> fireModifyEvent( true ) );
  }

  // ------------------------------------------------------------------------------------
  // AbstractValedControl
  //

  @Override
  protected Control doCreateControl( Composite aParent ) {
    SashForm sash = new SashForm( aParent, SWT.HORIZONTAL );
    classSelector.createControl( sash );
    objectsPanel.createControl( sash );
    sash.setWeights( 1, 1 );
    return sash;
  }

  @Override
  protected void doSetEditable( boolean aEditable ) {
    classSelector.setEditable( aEditable );
    objectsPanel.setEditable( aEditable );
  }

  @Override
  protected ValidationResult doCanGetValue() {
    if( classSelector.canGetValue().isOk() ) {
      if( objectsPanel.selectedItem() != null ) {
        return ValidationResult.SUCCESS;
      }
    }
    return ValidationResult.error( STR_ERR_NO_SELECTED_SKID );
  }

  @Override
  protected Skid doGetUnvalidatedValue() {
    return objectsPanel.selectedItem().skid();
  }

  @Override
  protected void doSetUnvalidatedValue( Skid aValue ) {
    classSelector.setValue( aValue.classId() );
    ISkObject skObj = skConn().coreApi().objService().find( aValue );
    objectsPanel.setSelectedItem( skObj );
  }

  @Override
  protected void doClearValue() {
    classSelector.setValue( null );
  }

  // ------------------------------------------------------------------------------------
  // IM5ItemsProvider
  //

  @Override
  public IList<ISkObject> listItems() {
    if( StridUtils.isValidIdPath( classId ) ) {
      ISkidList skids = skConn().coreApi().objService().listSkids( classId, includeSubclasses );
      return skConn().coreApi().objService().getObjs( skids );
    }
    return IList.EMPTY;
  }

}
