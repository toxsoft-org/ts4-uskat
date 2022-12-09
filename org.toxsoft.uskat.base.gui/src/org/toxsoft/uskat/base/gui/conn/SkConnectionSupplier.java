package org.toxsoft.uskat.base.gui.conn;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.base.gui.km5.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * {@link ISkConnectionSupplier} implementation.
 *
 * @author hazard157
 */
public class SkConnectionSupplier
    implements ISkConnectionSupplier {

  class Eventer
      extends AbstractTsEventer<ISkConnectionSupplierListener> {

    private ECrudOp op           = null;
    private IdChain connId       = null;
    private IdChain oldDefConnId = null;

    @Override
    protected boolean doIsPendingEvents() {
      return op != null || oldDefConnId != null;
    }

    @Override
    protected void doFirePendingEvents() {
      reallyFireEvent( op, connId );
      reallyFireDefChanged( oldDefConnId );
    }

    @Override
    protected void doClearPendingEvents() {
      op = null;
      connId = null;
      oldDefConnId = null;
    }

    private void reallyFireEvent( ECrudOp aOp, IdChain aConnId ) {
      for( ISkConnectionSupplierListener l : listeners() ) {
        l.onConnectionsListChanged( SkConnectionSupplier.this, aOp, aConnId );
      }
    }

    private void reallyFireDefChanged( IdChain aOldDefConnId ) {
      for( ISkConnectionSupplierListener l : listeners() ) {
        l.onDefaulConnectionChanged( SkConnectionSupplier.this, aOldDefConnId );
      }
    }

    void fireDefChanged( IdChain aOldDefConnId ) {
      if( isFiringPaused() ) {
        if( oldDefConnId == null ) { // remember only firs change of def connection change
          oldDefConnId = aOldDefConnId;
        }
      }
      else {
        reallyFireDefChanged( aOldDefConnId );
      }
    }

    void fireEvent( ECrudOp aOp, IdChain aConnId ) {
      if( isFiringPaused() ) {
        if( op == null ) { // first event to remember
          op = aOp;
          connId = aConnId;
        }
        else { // second and next events are remembered as LIST change
          op = ECrudOp.LIST;
          connId = null;
        }
      }
      else {
        reallyFireEvent( aOp, aConnId );
      }
    }

  }

  private final Eventer eventer = new Eventer();

  private final IMapEdit<IdChain, ISkConnection> connsMap = new ElemMap<>();

  private IdChain defKey = IdChain.NULL;

  /**
   * Constructor.
   */
  public SkConnectionSupplier() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // ISkConnectionSupplier
  //

  @Override
  public ISkConnection defConn() {
    return connsMap.findByKey( defKey );
  }

  @Override
  public ISkConnection setDefaultConnection( IdChain aKey ) {
    TsNullArgumentRtException.checkNull( aKey );
    TsItemNotFoundRtException.checkFalse( connsMap.hasKey( aKey ) );
    if( !defKey.equals( aKey ) ) {
      IdChain oldId = defKey;
      defKey = aKey;
      eventer.fireDefChanged( oldId );
    }
    return defConn();
  }

  @Override
  public IdChain getDefaultConnectionKey() {
    return defKey;
  }

  @Override
  public ISkConnection createConnection( IdChain aKey, ITsGuiContext aContext ) {
    TsNullArgumentRtException.checkNull( aKey );
    TsIllegalArgumentRtException.checkTrue( aKey == IdChain.NULL );
    TsItemAlreadyExistsRtException.checkTrue( connsMap.hasKey( aKey ) );
    ISkConnection conn = SkCoreUtils.createConnection();
    @SuppressWarnings( "unused" )
    KM5Support km5 = new KM5Support( conn, aContext.get( IM5Domain.class ) );
    connsMap.put( aKey, conn );
    eventer.fireEvent( ECrudOp.CREATE, aKey );
    return conn;
  }

  @Override
  public IMap<IdChain, ISkConnection> allConns() {
    return connsMap;
  }

  @Override
  public void removeConnection( IdChain aKey ) {
    ISkConnection conn = connsMap.findByKey( aKey );
    if( conn == null ) {
      return;
    }
    connsMap.removeByKey( aKey );
    if( defKey.equals( aKey ) ) {
      IdChain oldId = defKey;
      defKey = IdChain.NULL;
      eventer.fireDefChanged( oldId );
    }
    eventer.fireEvent( ECrudOp.CREATE, aKey );
  }

  @Override
  public ITsEventer<ISkConnectionSupplierListener> eventer() {
    return eventer;
  }

  // ------------------------------------------------------------------------------------
  // ICloseable
  //

  @Override
  public void close() {
    while( !connsMap.isEmpty() ) {
      ISkConnection conn = connsMap.removeByKey( connsMap.keys().first() );
      try {
        conn.close();
      }
      catch( Exception ex ) {
        LoggerUtils.errorLogger().error( ex );
      }
    }
    eventer.fireEvent( ECrudOp.LIST, null );
  }

}
