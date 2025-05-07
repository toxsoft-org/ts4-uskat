package org.toxsoft.uskat.core.gui.conn;

import static org.toxsoft.uskat.core.gui.conn.l10n.ISkCoreGuiConnSharedResources.*;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.km5.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * {@link ISkConnectionSupplier} implementation.
 *
 * @author hazard157
 */
public class SkConnectionSupplier
    implements ISkConnectionSupplier {

  /**
   * {@link ISkConnectionSupplier#svs()} implementation.
   *
   * @author hazard157
   */
  static class Svs
      extends AbstractTsValidationSupport<ISkConnectionSupplierValidator>
      implements ISkConnectionSupplierValidator {

    @Override
    public ISkConnectionSupplierValidator validator() {
      return this;
    }

    @Override
    public ValidationResult canCreateConnection( IdChain aKey, ITsGuiContext aContext ) {
      TsNullArgumentRtException.checkNulls( aKey, aContext );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkConnectionSupplierValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canCreateConnection( aKey, aContext ) );
      }
      return vr;
    }

    @Override
    public ValidationResult canRemoveConnection( IdChain aKey ) {
      TsNullArgumentRtException.checkNull( aKey );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkConnectionSupplierValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canRemoveConnection( aKey ) );
      }
      return vr;
    }

  }

  /**
   * {@link ISkConnectionSupplier#eventer()} implementation.
   *
   * @author hazard157
   */
  class Eventer
      extends AbstractTsEventer<ISkConnectionSupplierListener> {

    private ECrudOp op     = null;
    private IdChain connId = null;

    @Override
    protected boolean doIsPendingEvents() {
      return op != null;
    }

    @Override
    protected void doFirePendingEvents() {
      reallyFireEvent( op, connId );
    }

    @Override
    protected void doClearPendingEvents() {
      op = null;
      connId = null;
    }

    private void reallyFireEvent( ECrudOp aOp, IdChain aConnId ) {
      for( ISkConnectionSupplierListener l : listeners() ) {
        l.onConnectionsListChanged( SkConnectionSupplier.this, aOp, aConnId );
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

  /**
   * Builtin validator.
   */
  private final ISkConnectionSupplierValidator builtinValidator = new ISkConnectionSupplierValidator() {

    @Override
    public ValidationResult canRemoveConnection( IdChain aKey ) {
      if( aKey == DEF_CONN_ID ) {
        return ValidationResult.error( MSG_ERR_CANT_REMOVE_NULL_ID );
      }
      if( !connsMap.hasKey( aKey ) ) {
        return ValidationResult.warn( FMT_WARN_NO_SUCH_CONN_ID, aKey.canonicalString() );
      }
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canCreateConnection( IdChain aKey, ITsGuiContext aContext ) {
      if( connsMap.hasKey( aKey ) ) {
        return ValidationResult.error( FMT_ERR_CONN_ID_EXISTS, aKey.canonicalString() );
      }
      return ValidationResult.SUCCESS;
    }

  };

  private final Svs     svs     = new Svs();
  private final Eventer eventer = new Eventer();

  private final IMapEdit<IdChain, ISkConnection> connsMap = new ElemMap<>();

  /**
   * Constructor.
   *
   * @param aWinContext {@link IEclipseContext} - windows level context
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SkConnectionSupplier( IEclipseContext aWinContext ) {
    internalReallyCreateConnectionInstance( DEF_CONN_ID, new TsGuiContext( aWinContext ) );
    svs.addValidator( builtinValidator );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private ISkConnection internalReallyCreateConnectionInstance( IdChain aKey, ITsGuiContext aContext ) {
    ISkConnection conn = SkCoreUtils.createConnection();
    conn.scope().put( ITsGuiContext.class, aContext );
    // initialize connection-specific M5 domain
    @SuppressWarnings( { "unused" } )
    KM5Support km5 = new KM5Support( conn, aContext.get( IM5Domain.class ) );
    connsMap.put( aKey, conn );
    return conn;
  }

  // ------------------------------------------------------------------------------------
  // ISkConnectionSupplier
  //

  @Override
  public ISkConnection defConn() {
    return connsMap.getByKey( DEF_CONN_ID );
  }

  @Override
  public ISkConnection createConnection( IdChain aKey, ITsGuiContext aContext ) {
    TsValidationFailedRtException.checkError( svs.canCreateConnection( aKey, aContext ) );
    ISkConnection conn = internalReallyCreateConnectionInstance( aKey, aContext );
    eventer.fireEvent( ECrudOp.CREATE, aKey );
    return conn;
  }

  @Override
  public IMap<IdChain, ISkConnection> allConns() {
    return connsMap;
  }

  @Override
  public void removeConnection( IdChain aKey ) {
    TsValidationFailedRtException.checkError( svs.canRemoveConnection( aKey ) );
    ISkConnection conn = connsMap.findByKey( aKey );
    if( conn == null ) {
      return;
    }
    TsIllegalStateRtException.checkTrue( conn.state().isOpen() );
    connsMap.removeByKey( aKey );
    eventer.fireEvent( ECrudOp.CREATE, aKey );
  }

  @Override
  public ITsValidationSupport<ISkConnectionSupplierValidator> svs() {
    return svs;
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
    for( ISkConnection conn : connsMap.values() ) {
      try {
        if( conn.state().isOpen() ) {
          conn.close();
        }
      }
      catch( Exception ex ) {
        LoggerUtils.errorLogger().error( ex );
      }
    }
  }

}
