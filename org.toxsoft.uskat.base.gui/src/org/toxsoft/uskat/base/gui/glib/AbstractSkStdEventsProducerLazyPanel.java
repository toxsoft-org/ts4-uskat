package org.toxsoft.uskat.base.gui.glib;

import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.stdevents.*;
import org.toxsoft.core.tsgui.bricks.stdevents.impl.*;
import org.toxsoft.core.tsgui.panels.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * {@link AbstractSkLazyPanel} extension to work with a list of objects of the same type.
 * <p>
 * Implements {@link ITsSelectionChangeEventProducer} and {@link ITsDoubleClickEventProducer}.
 * <p>
 * This panel may be used instead os {@link TsStdEventsProducerPanel} in USkat applications.
 *
 * @author hazard157
 * @param <E> - type of the objects
 */
public abstract class AbstractSkStdEventsProducerLazyPanel<E>
    extends AbstractSkLazyPanel
    implements ITsSelectionProvider<E>, ITsDoubleClickEventProducer<E> {

  protected final TsSelectionChangeEventHelper<E> selectionChangeEventHelper;
  protected final TsDoubleClickEventHelper<E>     doubleClickEventHelper;

  /**
   * Constructor.
   *
   * @param aParent {@link Composite} - parent component
   * @param aContext {@link ITsGuiContext} - the context
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public AbstractSkStdEventsProducerLazyPanel( Composite aParent, ITsGuiContext aContext ) {
    this( aContext, null );
  }

  /**
   * Constructor.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @param aUsedConnId {@link IdChain} - ID of connection to be used, may be <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public AbstractSkStdEventsProducerLazyPanel( ITsGuiContext aContext, IdChain aUsedConnId ) {
    super( aContext, aUsedConnId );
    selectionChangeEventHelper = new TsSelectionChangeEventHelper<>( this );
    doubleClickEventHelper = new TsDoubleClickEventHelper<>( this );
  }

  // ------------------------------------------------------------------------------------
  // API for subclasses
  //

  /**
   * Generates an event {@link ITsSelectionChangeListener#onTsSelectionChanged(Object, Object)}.
   *
   * @param aItem &lt;E&gt; - selected element, may be <code>null</code>
   */
  public void fireTsSelectionEvent( E aItem ) {
    selectionChangeEventHelper.fireTsSelectionEvent( aItem );
  }

  /**
   * Generates an event {@link ITsSelectionChangeListener#onTsSelectionChanged(Object, Object)}.
   *
   * @param aItem &lt;E&gt; - selected element, may be <code>null</code>
   */
  public void fireTsDoubleClickEvent( E aItem ) {
    doubleClickEventHelper.fireTsDoublcClickEvent( aItem );
  }

  // ------------------------------------------------------------------------------------
  // ITsSelectionChangeEventProducer
  //

  @Override
  public void addTsSelectionListener( ITsSelectionChangeListener<E> aListener ) {
    selectionChangeEventHelper.addTsSelectionListener( aListener );
  }

  @Override
  public void removeTsSelectionListener( ITsSelectionChangeListener<E> aListener ) {
    selectionChangeEventHelper.removeTsSelectionListener( aListener );
  }

  // ------------------------------------------------------------------------------------
  // ITsDoubleClickEventProducer
  //

  @Override
  public void addTsDoubleClickListener( ITsDoubleClickListener<E> aListener ) {
    doubleClickEventHelper.addTsDoubleClickListener( aListener );
  }

  @Override
  public void removeTsDoubleClickListener( ITsDoubleClickListener<E> aListener ) {
    doubleClickEventHelper.removeTsDoubleClickListener( aListener );
  }

}
