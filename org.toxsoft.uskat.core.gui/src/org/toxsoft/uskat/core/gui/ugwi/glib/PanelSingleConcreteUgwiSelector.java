package org.toxsoft.uskat.core.gui.ugwi.glib;

import static org.toxsoft.uskat.core.gui.km5.sgw.ISgwM5Constants.*;
import static org.toxsoft.uskat.core.gui.ugwi.glib.IUgwiSelectorConstants.*;

import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tslib.bricks.events.change.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.core.gui.glib.*;

/**
 * {@link IPanelSingleConcreteUgwiSelector} implementation.
 *
 * @author hazard157
 */
public class PanelSingleConcreteUgwiSelector
    extends AbstractSkStdEventsProducerLazyPanel<Gwid>
    implements IPanelSingleConcreteUgwiSelector {

  // FIXME implement this class!!!

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
  public PanelSingleConcreteUgwiSelector( ITsGuiContext aContext ) {
    this( aContext, null );
  }

  /**
   * Constructor.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @param aUsedConnId {@link IdChain} - ID of connection to be used, may be <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public PanelSingleConcreteUgwiSelector( ITsGuiContext aContext, IdChain aUsedConnId ) {
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
  // AbstractSkStdEventsProducerLazyPanel
  //

  @Override
  protected Gwid doGetSelectedItem() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void doSetSelectedItem( Gwid aItem ) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void doInitGui( Composite aParent ) {
    // TODO Auto-generated method stub

  }

  // ------------------------------------------------------------------------------------
  // IGenericEntityPanel
  //

  @Override
  public void setEntity( Ugwi aEntity ) {
    // TODO Auto-generated method stub

  }

  // ------------------------------------------------------------------------------------
  // IGenericEntityEditPanel
  //

  @Override
  public Ugwi getEntity() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult canGetEntity() {
    // TODO Auto-generated method stub
    return null;
  }

  // ------------------------------------------------------------------------------------
  // IGenericContentPanel
  //

  @Override
  public boolean isViewer() {
    return false;
  }

  // ------------------------------------------------------------------------------------
  // IGenericChangeEventCapable
  //

  @Override
  public GenericChangeEventer genericChangeEventer() {
    return genericChangeEventer;
  }

  // ------------------------------------------------------------------------------------
  // IPanelSingleConcreteUgwiSelector
  //

  @Override
  public ESkClassPropKind getClassPropKind() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getUgwiKindId() {
    // TODO Auto-generated method stub
    return null;
  }

}
