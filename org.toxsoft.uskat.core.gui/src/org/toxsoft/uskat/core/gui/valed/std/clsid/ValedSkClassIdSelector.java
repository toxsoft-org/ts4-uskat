package org.toxsoft.uskat.core.gui.valed.std.clsid;

import static org.toxsoft.core.tsgui.m5.gui.mpc.IMultiPaneComponentConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.gui.valed.std.ISkStdValedControlConstants.*;

import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.bricks.filter.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.gui.km5.sded2.sysdecsr.*;
import org.toxsoft.uskat.core.gui.valed.*;
import org.toxsoft.uskat.core.gui.valed.std.*;
import org.toxsoft.uskat.core.inner.*;

/**
 * Chooses class ID from the {@link ISkSysdescr#listClasses()}.
 * <p>
 * Value is the {@link String} class ID {@link ISkClassInfo#id()}.
 * <p>
 * Respects options:
 * <ul>
 * <li>{@link ISkStdValedControlConstants#OPDEF_IS_START_MODE_TREE};</li>
 * <li>{@link ISkStdValedControlConstants#OPDEF_CLASS_ID_FILTER_PARAMS}.</li>
 * </ul>
 * <p>
 * <p>
 * Note: {@link #getValue()} may return <code>null</code>.
 *
 * @author hazard157
 */
public class ValedSkClassIdSelector
    extends AbstractSkValedControl<String> {

  /**
   * The registered factory ID.
   */
  public static final String FACTORY_NAME = ISkCoreGuiInnerSharedConstants.SKCGC_VALED_CLASS_ID_SELECTOR;

  /**
   * The factory singleton.
   */
  @SuppressWarnings( "unchecked" )
  public static final IValedControlFactory FACTORY = new AbstractValedControlFactory( FACTORY_NAME ) {

    @Override
    protected IValedControl<String> doCreateEditor( ITsGuiContext aContext ) {
      return new ValedSkClassIdSelector( aContext );
    }

  };

  private final IM5ItemsProvider<ISkClassInfo>   classesProvider;
  private final IM5CollectionPanel<ISkClassInfo> classesListPane;

  // private final ITsFilter<String> classIdFilter;

  /**
   * Constructor.
   *
   * @param aContext {@link ITsGuiContext} - the VALED context
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public ValedSkClassIdSelector( ITsGuiContext aContext ) {
    super( aContext );
    ITsCombiFilterParams fp = OPDEF_CLASS_ID_FILTER_PARAMS.getValue( tsContext().params() ).asValobj();
    // ITsFilter<String> filter = TsCombiFilter.create( fp, ITsFilterFactoriesRegistry. )

    // classesListPane
    IM5Model<ISkClassInfo> modelSk = m5().getModel( Sded2SkClassInfoM5Model.MODEL_ID, ISkClassInfo.class );
    IM5LifecycleManager<ISkClassInfo> lmSk = modelSk.getLifecycleManager( skConn() );
    classesProvider = lmSk.itemsProvider();
    ITsGuiContext ctx1 = new TsGuiContext( tsContext() );
    OPDEF_IS_FILTER_PANE.setValue( ctx1.params(), AV_TRUE );
    classesListPane = modelSk.panelCreator().createCollViewerPanel( ctx1, classesProvider );
  }

  // ------------------------------------------------------------------------------------
  // AbstractValedControl
  //

  @Override
  protected Control doCreateControl( Composite aParent ) {
    return classesListPane.createControl( aParent );
  }

  @Override
  protected void doSetEditable( boolean aEditable ) {
    classesListPane.setEditable( aEditable );
  }

  @Override
  protected String doGetUnvalidatedValue() {
    ISkClassInfo cinf = classesListPane.selectedItem();
    return cinf != null ? cinf.id() : null;
  }

  @Override
  protected void doSetUnvalidatedValue( String aValue ) {
    IStridablesList<ISkClassInfo> llClasses = new StridablesList<>( classesProvider.listItems() );
    ISkClassInfo cinf = llClasses.findByKey( aValue );
    classesListPane.setSelectedItem( cinf );
  }

  @Override
  protected void doClearValue() {
    classesListPane.setSelectedItem( null );
  }

}
