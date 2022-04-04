package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * {@link ISkConnection} implementation.
 *
 * @author hazard157
 */
class SkConnection
    implements ISkConnection {

  private final IListEdit<ISkConnectionListener> listeners = new ElemLinkedBundleList<>();

  private final ITsContext scope = new TsContext();

  private ESkConnState state   = ESkConnState.CLOSED;
  private SkCoreApi    coreApi = null;

  /**
   * Constructor.
   */
  public SkConnection() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  void beforeChangeState( ESkConnState aNewState ) {
    if( !listeners.isEmpty() ) {
      IList<ISkConnectionListener> ll = new ElemArrayList<>( listeners );
      for( int i = 0; i < ll.size(); i++ ) {
        ISkConnectionListener l = ll.get( i );
        try {
          l.beforeSkConnectionStateChange( this, aNewState );
        }
        catch( Exception ex ) {
          LoggerUtils.defaultLogger().error( ex );
        }
      }
    }
  }

  void changeState( ESkConnState aNewState ) {
    ESkConnState oldState = state;
    if( aNewState != oldState ) {
      state = aNewState;
      if( !listeners.isEmpty() ) {
        IList<ISkConnectionListener> ll = new ElemArrayList<>( listeners );
        for( int i = 0; i < ll.size(); i++ ) {
          ISkConnectionListener l = ll.get( i );
          try {
            l.onSkConnectionStateChanged( this, oldState );
          }
          catch( Exception ex ) {
            LoggerUtils.defaultLogger().error( ex );
          }
        }
      }
    }
  }

  private static ITsContext createContextForCoraApi( ITsContextRo aContext ) {
    ITsContext ctx = new TsContext( new IAskParent() {

      @Override
      public IAtomicValue findOp( String aId ) {
        return aContext.params().findByKey( aId );
      }

      @Override
      public Object findRef( String aKey ) {
        return aContext.find( aKey );
      }

    } );
    ctx.params().setAll( aContext.params() );
    return ctx;
  }

  // ------------------------------------------------------------------------------------
  // ICloseable
  //

  @Override
  public void close() {
    if( state() != ESkConnState.CLOSED ) {
      beforeChangeState( ESkConnState.CLOSED );
      coreApi.close();
      changeState( ESkConnState.CLOSED );
      coreApi = null;
    }
  }

  // ------------------------------------------------------------------------------------
  // ISkConnection
  //

  @Override
  public ESkConnState state() {
    return state;
  }

  @Override
  public void open( ITsContextRo aArgs ) {
    TsNullArgumentRtException.checkNull( aArgs );
    TsIllegalStateRtException.checkTrue( state().isOpen(), MSG_ERR_CONN_IS_OPEN );
    // init
    beforeChangeState( ESkConnState.ACTIVE );
    ITsContext ctx = createContextForCoraApi( aArgs );
    coreApi = new SkCoreApi( ctx, this );

    // TODO Auto-generated method stub

  }

  @Override
  public ISkCoreApi coreApi() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ISkBackendInfo serverInfo() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void addConnectionListener( ISkConnectionListener aListener ) {
    if( !listeners.hasElem( aListener ) ) {
      listeners.add( aListener );
    }
  }

  @Override
  public void removeConnectionListener( ISkConnectionListener aListener ) {
    listeners.remove( aListener );
  }

  @Override
  public ITsContext scope() {
    return scope;
  }

}
