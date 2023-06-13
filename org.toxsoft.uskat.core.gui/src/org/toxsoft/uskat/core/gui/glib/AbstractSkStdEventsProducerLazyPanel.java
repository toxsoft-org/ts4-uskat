package org.toxsoft.uskat.core.gui.glib;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.stdevents.*;
import org.toxsoft.core.tsgui.bricks.stdevents.impl.*;
import org.toxsoft.core.tsgui.panels.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.gui.conn.*;

/**
 * {@link AbstractSkLazyPanel} extension to work with a list of objects of the same type.
 * <p>
 * Implements {@link ITsSelectionChangeEventProducer} and {@link ITsDoubleClickEventProducer}.
 * <p>
 * This panel may be used instead of {@link TsStdEventsProducerPanel} in USkat applications.
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
   * <p>
   * Used connection ID is initialized to <code>null</code> thus using {@link ISkConnectionSupplier#defConn()}.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public AbstractSkStdEventsProducerLazyPanel( ITsGuiContext aContext ) {
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
  // ITsSelectionProvider
  //

  @Override
  final public void addTsSelectionListener( ITsSelectionChangeListener<E> aListener ) {
    selectionChangeEventHelper.addTsSelectionListener( aListener );
  }

  @Override
  final public void removeTsSelectionListener( ITsSelectionChangeListener<E> aListener ) {
    selectionChangeEventHelper.removeTsSelectionListener( aListener );
  }

  @Override
  public E selectedItem() {
    if( isControlValid() ) {
      return doGetSelectedItem();
    }
    return null;
  }

  @Override
  public void setSelectedItem( E aItem ) {
    if( isControlValid() ) {
      doSetSelectedItem( aItem );
    }
  }

  // ------------------------------------------------------------------------------------
  // ITsDoubleClickEventProducer
  //

  @Override
  final public void addTsDoubleClickListener( ITsDoubleClickListener<E> aListener ) {
    doubleClickEventHelper.addTsDoubleClickListener( aListener );
  }

  @Override
  final public void removeTsDoubleClickListener( ITsDoubleClickListener<E> aListener ) {
    doubleClickEventHelper.removeTsDoubleClickListener( aListener );
  }

  // ------------------------------------------------------------------------------------
  // To implement
  //

  /**
   * Implementation must return the selected item.
   * <p>
   * Called only when {@link #isControlValid()} = <code>true</code>, so there is no need to check if SWT controls of
   * panel exist.
   *
   * @return &lt;E&gt; - currently selected element in this panel
   */
  protected abstract E doGetSelectedItem();

  /**
   * Implementation must set selected item.
   * <p>
   * Called only when {@link #isControlValid()} = <code>true</code>, so there is no need to check if SWT controls of
   * panel exist.
   *
   * @param aItem &lt;E&gt; - the element to make selected
   */
  protected abstract void doSetSelectedItem( E aItem );

}
