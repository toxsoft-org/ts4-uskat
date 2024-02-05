package org.toxsoft.uskat.core.gui.km5.sded.sded.editors;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.bricks.stdevents.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.mpc.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.panels.*;
import org.toxsoft.core.tsgui.utils.layout.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.core.gui.km5.sgw.*;

/**
 * Панель просмотра списка классов ts4.<br>
 *
 * @author dima
 */
public class ClassInfoViewerPanel
    extends TsPanel {

  private final ITsSelectionChangeListener<ISkClassInfo> classChangeListener = ( aSource, aSelectedItem ) -> {
    this.selectedClass = aSelectedItem;
    if( this.skObjectCheckedListPanel != null ) {
      this.skObjectCheckedListPanel.setClass( this.selectedClass );
    }
  };

  final ISkConnection                      conn;
  private IM5CollectionPanel<ISkClassInfo> classesPanel;
  private ISkClassInfo                     selectedClass            = null;
  private ObjectCheckedListPanel           skObjectCheckedListPanel = null;

  /**
   * @return {@link ISkClassInfo} class selected by user
   */
  public ISkClassInfo getSelectedClass() {
    return selectedClass;
  }

  /**
   * Конструктор панели.
   * <p>
   * Конструктор просто запоминает ссылку на контекст, без создания копии.
   *
   * @param aParent {@link Composite} - родительская панель
   * @param aContext {@link ITsGuiContext} - контекст панели
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public ClassInfoViewerPanel( Composite aParent, ITsGuiContext aContext ) {
    super( aParent, aContext );
    this.setLayout( new BorderLayout() );
    ISkConnectionSupplier connSup = aContext.get( ISkConnectionSupplier.class );
    conn = connSup.defConn();
    // тут получаем KM5 модель ISkClassInfo
    IM5Domain m5 = conn.scope().get( IM5Domain.class );
    IM5Model<ISkClassInfo> model = m5.getModel( ISgwM5Constants.MID_SGW_CLASS_INFO, ISkClassInfo.class );
    ITsGuiContext ctx = new TsGuiContext( aContext );
    ctx.params().addAll( aContext.params() );
    IMultiPaneComponentConstants.OPDEF_IS_DETAILS_PANE.setValue( ctx.params(), AvUtils.AV_FALSE );
    IMultiPaneComponentConstants.OPDEF_DETAILS_PANE_PLACE.setValue( ctx.params(),
        avValobj( EBorderLayoutPlacement.SOUTH ) );
    // добавляем в панель фильтр
    IMultiPaneComponentConstants.OPDEF_IS_FILTER_PANE.setValue( ctx.params(), AvUtils.AV_TRUE );

    classesPanel =
        model.panelCreator().createCollViewerPanel( ctx, model.findLifecycleManager( conn ).itemsProvider() );
    // setup
    classesPanel.addTsSelectionListener( classChangeListener );
    classesPanel.createControl( this );

  }

  /**
   * Установить выбранный класс
   *
   * @param aClassId id выбранного класса
   */
  public void selectClass( String aClassId ) {
    ISkClassInfo ci = conn.coreApi().sysdescr().findClassInfo( aClassId );
    classesPanel.setSelectedItem( ci );
  }

  /**
   * @param aSkObjectCheckedListPanel {@link ObjectCheckedListPanel} panel to show checkable list objects of selected
   *          class
   */
  public void setSkObjectListPanel( ObjectCheckedListPanel aSkObjectCheckedListPanel ) {
    skObjectCheckedListPanel = aSkObjectCheckedListPanel;
  }

}
