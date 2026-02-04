package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.devapi.*;
import org.toxsoft.uskat.core.devapi.transactions.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * {@link ISkTransactionService} implementation.
 *
 * @author mvk
 */
public class SkCoreServTransaction
    extends AbstractSkCoreService
    implements ISkTransactionService {

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkCoreServTransaction::new;

  /**
   * {@link ISkTransactionService#eventer()} implementation.
   *
   * @author mvk
   */
  class Eventer
      extends AbstractTsEventer<ISkTransactionServiceListener> {

    @Override
    protected boolean doIsPendingEvents() {
      return false;
    }

    @Override
    protected void doFirePendingEvents() {
      // nop
    }

    @Override
    protected void doClearPendingEvents() {
      // nop
    }

    void fireObjCreatedEvent( IDtoObject aObjectId ) {
      for( ISkTransactionServiceListener l : listeners() ) {
        l.onObjCreated( aObjectId );
      }
    }

    void fireObjMergedEvent( IDtoObject aObjectId ) {
      for( ISkTransactionServiceListener l : listeners() ) {
        l.onObjMerged( aObjectId );
      }
    }

    void fireObjRemovedEvent( IDtoObject aObjectId ) {
      for( ISkTransactionServiceListener l : listeners() ) {
        l.onObjRemoved( aObjectId );
      }
    }

    void fireBeforeCommitEvent( IList<IDtoObject> aRemovingObjs, IList<Pair<IDtoObject, IDtoObject>> aUpdatingObjs,
        IList<IDtoObject> aCreatingObjs, IList<IDtoLinkFwd> aUpdatingLinks ) {
      for( ISkTransactionServiceListener l : listeners() ) {
        l.onBeforeCommit( aRemovingObjs, aUpdatingObjs, aCreatingObjs, aUpdatingLinks );
      }
    }

    void fireAfterCommitEvent( IList<IDtoObject> aRemovingObjs, IList<Pair<IDtoObject, IDtoObject>> aUpdatingObjs,
        IList<IDtoObject> aCreatingObjs, IList<IDtoLinkFwd> aUpdatingLinks ) {
      for( ISkTransactionServiceListener l : listeners() ) {
        l.onAfterCommit( aRemovingObjs, aUpdatingObjs, aCreatingObjs, aUpdatingLinks );
      }
    }

    void fireRollbackEvent( IList<IDtoObject> aRemovingObjs, IList<Pair<IDtoObject, IDtoObject>> aUpdatingObjs,
        IList<IDtoObject> aCreatingObjs, IList<IDtoLinkFwd> aUpdatingLinks ) {
      for( ISkTransactionServiceListener l : listeners() ) {
        l.onRollback( aRemovingObjs, aUpdatingObjs, aCreatingObjs, aUpdatingLinks );
      }
    }
  }

  final Eventer eventer = new Eventer();

  private AbstractDtoObjectManager      objectManager;
  private AbstractDtoObjectRivetManager objectRivetManager;
  private IListEdit<IDtoLinkFwd>        txUpdatingLinks = new ElemLinkedList<>();
  private boolean                       isActive        = false;

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  SkCoreServTransaction( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkCoreService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    // nop
  }

  @Override
  protected void doClose() {
    eventer.clearListenersList();
    eventer.resetPendingEvents();
  }

  @Override
  protected boolean onBackendMessage( GenericMessage aMessage ) {
    // TODO:
    return false;
  }

  // ------------------------------------------------------------------------------------
  // ISkTransactionService
  //

  @Override
  public boolean isActive() {
    return isActive;
  }

  @Override
  public void start() {
    checkThread();
    TsIllegalStateRtException.checkTrue( isActive );
    // entity manager
    objectManager = new AbstractDtoObjectManager( threadExecutor() ) {

      @Override
      protected void doPersist( IDtoObject aDtoObj ) {
        eventer.fireObjCreatedEvent( aDtoObj );
      }

      @Override
      protected void doMerge( IDtoObject aDtoObj ) {
        eventer.fireObjMergedEvent( aDtoObj );
      }

      @Override
      protected void doRemove( IDtoObject aDtoObj ) {
        eventer.fireObjRemovedEvent( aDtoObj );
      }

      @Override
      DtoObject doLoadFromDb( Skid aObjId ) {
        ISkObject sko = coreApi().objService().find( aObjId );
        if( sko != null ) {
          DtoObject retValue = SkCoreServObject.createForBackendSave( coreApi(), sko );
          // Восстановление обратных склепок
          DtoObject.setRivetRevs( retValue, sko.rivetRevs() );
          return retValue;
        }
        return null;
      }
    };
    // Object rivets editor
    objectRivetManager = new AbstractDtoObjectRivetManager( threadExecutor(), logger() ) {

      // ------------------------------------------------------------------------------------
      // abstract methods implementations
      //
      @Override
      protected ISkClassInfo doGetClassInfo( String aClassId ) {
        return coreApi().sysdescr().getClassInfo( aClassId );
      }

      @Override
      protected IDtoObject doFindObject( Skid aObjId ) {
        return objectManager.find( aObjId );
      }

      @Override
      protected void doWriteRivetRevs( IDtoObject aObj, IStringMapEdit<IMappedSkids> aRivetRevs ) {
        DtoObject.setRivetRevs( (DtoObject)aObj, aRivetRevs );
        objectManager.merge( aObj );
      }
    };

    txUpdatingLinks = new ElemLinkedList<>();
    isActive = true;
  }

  @Override
  public IDtoObjectManager objectManager() {
    checkThread();
    TsIllegalStateRtException.checkFalse( isActive );
    return objectManager;
  }

  @Override
  public IDtoObjectRivetManager rivetManager() {
    checkThread();
    TsIllegalStateRtException.checkFalse( isActive );
    return objectRivetManager;
  }

  @Override
  public void defineLinks( IList<IDtoLinkFwd> aLinks ) {
    checkThread();
    TsNullArgumentRtException.checkNull( aLinks );
    TsIllegalStateRtException.checkFalse( isActive );
    txUpdatingLinks.addAll( aLinks );
  }

  @Override
  public void commit() {
    checkThread();
    TsIllegalStateRtException.checkFalse( isActive );

    IList<IDtoObject> removingObjs = new ElemArrayList<>( objectManager.removingObjs().values() );
    IList<Pair<IDtoObject, IDtoObject>> updatingObjs = new ElemArrayList<>( objectManager.updatingObjs().values() );
    IList<IDtoObject> creatingObjs = new ElemArrayList<>( objectManager.creatingObjs().values() );
    IList<IDtoLinkFwd> updatingLinks = new ElemArrayList<>( this.txUpdatingLinks );

    // fire before commit event
    eventer.fireBeforeCommitEvent( removingObjs, updatingObjs, creatingObjs, updatingLinks );

    // refresh changed list after "before-commit-event" handle
    // matters if the riveted objects are outside of a transaction
    removingObjs = new ElemArrayList<>( objectManager.removingObjs().values() );
    updatingObjs = new ElemArrayList<>( objectManager.updatingObjs().values() );
    creatingObjs = new ElemArrayList<>( objectManager.creatingObjs().values() );
    updatingLinks = new ElemArrayList<>( this.txUpdatingLinks );

    try {
      // TODO: добавить в метод аргумент обновляемые связи
      if( removingObjs.size() > 0 || updatingObjs.size() > 0 || creatingObjs.size() > 0 ) {
        SkidList removingObjIds = new SkidList();
        for( IDtoObject removingObj : removingObjs ) {
          removingObjIds.add( removingObj.skid() );
        }
        IListEdit<IDtoObject> writingObjs = new ElemArrayList<>( updatingObjs.size() + creatingObjs.size() );
        for( Pair<IDtoObject, IDtoObject> updatingObj : updatingObjs ) {
          writingObjs.add( updatingObj.right() );
        }
        writingObjs.addAll( creatingObjs );
        ba().baObjects().writeObjects( removingObjIds, writingObjs );
      }
      if( updatingLinks.size() > 0 ) {
        ba().baLinks().writeLinksFwd( updatingLinks );
      }
      // change transaction status
      isActive = false;
      // fire after commit event
      eventer.fireAfterCommitEvent( removingObjs, updatingObjs, creatingObjs, updatingLinks );
    }
    catch( Throwable e ) {
      logger().error( e );
      rollback();
    }
  }

  @Override
  public void rollback() {
    try {
      checkThread();
      TsIllegalStateRtException.checkFalse( isActive );
      isActive = false;
      IList<IDtoObject> removingObjs = new ElemArrayList<>( objectManager.removingObjs().values() );
      IList<Pair<IDtoObject, IDtoObject>> updatingObjs = new ElemArrayList<>( objectManager.updatingObjs().values() );
      IList<IDtoObject> creatingObjs = new ElemArrayList<>( objectManager.creatingObjs().values() );
      IList<IDtoLinkFwd> updatingLinks = new ElemArrayList<>( this.txUpdatingLinks );
      // fire rollback event
      eventer.fireRollbackEvent( removingObjs, updatingObjs, creatingObjs, updatingLinks );
    }
    catch( Throwable e ) {
      logger().error( e );
    }
  }

  @Override
  public ITsEventer<ISkTransactionServiceListener> eventer() {
    return eventer;
  }

  // ------------------------------------------------------------------------------------
  // private methods
  //

}
