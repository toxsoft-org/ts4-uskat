package org.toxsoft.uskat.core.utils;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.evserv.*;

/**
 * Build an {@link SkEvent} instance.
 *
 * @author hazard157
 */
public final class SkEventBuilder {

  private final IOptionSetEdit eventParams = new OptionSet();

  private Gwid eventGwid = null;

  /**
   * Constructor.
   */
  public SkEventBuilder() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Sets the {@link SkEvent#eventGwid()} of event to be created.
   *
   * @param aEventGwid {@link Gwid} - concrete GWID of kine {@link EGwidKind#GW_EVENT}
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException argument is asbtract, not concrete GWID
   * @throws TsIllegalArgumentRtException argument kind is not {@link EGwidKind#GW_EVENT}
   */
  public void setEventGwid( Gwid aEventGwid ) {
    TsNullArgumentRtException.checkNull( aEventGwid );
    TsIllegalArgumentRtException.checkTrue( aEventGwid.isAbstract() );
    TsIllegalArgumentRtException.checkTrue( aEventGwid.kind() != EGwidKind.GW_EVENT );
    eventGwid = aEventGwid;
  }

  /**
   * Returns an editable paraneters of event to be created.
   *
   * @return {@link IOptionSetEdit} - editable paremeter values
   */
  public IOptionSetEdit eventParams() {
    return eventParams;
  }

  /**
   * Creates and returns new instance of the {@link SkEvent}.
   * <p>
   * Builder internals remain unchanged so subsequent <code>getEvent()</code> will create the similat event with
   * different timestamp.
   *
   * @return {@link SkEvent} - event with timestamp of now
   */
  public SkEvent getEvent() {
    TsIllegalStateRtException.checkNull( eventGwid );
    return new SkEvent( System.currentTimeMillis(), eventGwid, eventParams );
  }

  /**
   * Creates and returns new instance of the {@link SkEvent} and resets builder for new build.
   * <p>
   * Builder internals will be reset so subsequent <code>getEvent()</code> will throw an exception.
   *
   * @return {@link SkEvent} - event with timestamp of now
   */
  public SkEvent getEventAndReset() {
    TsIllegalStateRtException.checkNull( eventGwid );
    SkEvent event = new SkEvent( System.currentTimeMillis(), eventGwid, eventParams );
    reset();
    return event;
  }

  /**
   * Resets builder internals so subsequent <code>getEvent()</code> will throw an exception.
   */
  public void reset() {
    eventGwid = null;
    eventParams.clear();
  }

}
