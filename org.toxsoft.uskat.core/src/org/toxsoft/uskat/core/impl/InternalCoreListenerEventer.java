package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.*;

class InternalCoreListenerEventer
    extends AbstractTsEventer<ISkCoreListener> {

  public final SkCoreApi coreApi;

  public final IListEdit<SkCoreEvent> collectedEvents = new ElemLinkedBundleList<>();

  public InternalCoreListenerEventer( SkCoreApi aCoreApi ) {
    coreApi = aCoreApi;
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private void internalCollectEvent( SkCoreEvent aEvent ) {
    // TODO add some processing - remove meaningless events
    collectedEvents.insert( 0, aEvent ); // insert first for reverse-order by time
  }

  private void internalReallyFireEvent( IList<SkCoreEvent> aEvents ) {
    IList<ISkCoreListener> ll = listeners();
    if( aEvents.isEmpty() || ll.isEmpty() ) {
      return;
    }
    for( ISkCoreListener l : ll ) {
      try {
        l.onGreenWorldChanged( aEvents );
      }
      catch( Exception ex ) {
        LoggerUtils.errorLogger().error( ex );
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // AbstractTsEventer
  //

  @Override
  protected boolean doIsPendingEvents() {
    return !collectedEvents.isEmpty();
  }

  @Override
  protected void doFirePendingEvents() {
    for( ISkCoreListener l : listeners() ) {
      try {
        l.onGreenWorldChanged( collectedEvents );
      }
      catch( Exception ex ) {
        LoggerUtils.errorLogger().error( ex );
      }
    }
  }

  @Override
  protected void doClearPendingEvents() {
    collectedEvents.clear();
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  public void fireCoreEvent( SkCoreEvent aSkCoreEvent ) {
    if( isFiringPaused() ) {
      internalCollectEvent( aSkCoreEvent );
    }
    else {
      internalReallyFireEvent( new SingleItemList<>( aSkCoreEvent ) );
    }

  }

}
