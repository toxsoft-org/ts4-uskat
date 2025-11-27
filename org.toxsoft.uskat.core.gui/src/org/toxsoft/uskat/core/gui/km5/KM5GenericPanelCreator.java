package org.toxsoft.uskat.core.gui.km5;

import static org.toxsoft.core.tsgui.m5.gui.mpc.IMultiPaneComponentConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.mpc.impl.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.gui.panels.impl.*;
import org.toxsoft.core.tsgui.m5.gui.viewers.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * Реализация {@link IM5PanelCreator}, для KM5, создающая панели по умолчанию.
 * <p>
 * Предназначен также для переопределения для создания собственных реализации.
 *
 * @author hazard157
 * @param <T> - modeled entity type
 */
public class KM5GenericPanelCreator<T extends ISkObject>
    extends M5DefaultPanelCreator<T> {

  /**
   * Конструктор.
   */
  public KM5GenericPanelCreator() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // M5DefaultPanelCreator
  //

  @Override
  protected IM5CollectionPanel<T> doCreateCollViewerPanel( ITsGuiContext aContext,
      IM5ItemsProvider<T> aItemsProvider ) {
    aContext.params().setValueIfNull( OPDEF_IS_ACTIONS_CRUD.id(), AV_FALSE );
    aContext.params().setValueIfNull( OPDEF_IS_DETAILS_PANE.id(), AV_FALSE );
    aContext.params().setValueIfNull( OPDEF_IS_TOOLBAR.id(), AV_TRUE );
    aContext.params().setValueIfNull( OPDEF_IS_SUMMARY_PANE.id(), AV_TRUE );
    aContext.params().setValueIfNull( OPDEF_IS_FILTER_PANE.id(), AV_TRUE );
    MultiPaneComponentModown<T> mpc = new MultiPaneComponentModown<>( aContext, model(), aItemsProvider, null ) {

      @Override
      protected void doCreateTreeColumns() {
        addColumnByFieldId( tree(), AID_NAME );
        addColumnByFieldId( tree(), AID_DESCRIPTION );
      }

      private void addColumnByFieldId( IM5TreeViewer<T> aTree, String aFieldId ) {
        IM5FieldDef<T, ?> fdef = model().fieldDefs().findByKey( aFieldId );
        if( fdef != null ) {
          aTree.columnManager().add( fdef.id(), fdef.getter() );
        }
      }

    };
    return new M5CollectionPanelMpcModownWrapper<>( mpc, true );
  }

  @Override
  protected IM5CollectionPanel<T> doCreateCollEditPanel( ITsGuiContext aContext, IM5ItemsProvider<T> aItemsProvider,
      IM5LifecycleManager<T> aLifecycleManager ) {
    // при создании объекта, задать начальное значение ИД класса (берется из ИД модели)
    OPDEF_IS_ACTIONS_CRUD.setValue( aContext.params(), AV_TRUE );
    MultiPaneComponentModown<T> mpc =
        new MultiPaneComponentModown<>( aContext, model(), aItemsProvider, aLifecycleManager ) {

          @Override
          protected void doAdjustEntityCreationInitialValues( IM5BunchEdit<T> aValues ) {
            // forecefuly set hidden field value of class ID when creating objects
            aValues.set( AID_CLASS_ID, avStr( model().id() ) );
          }
        };
    return new M5CollectionPanelMpcModownWrapper<>( mpc, false );
  }

}
