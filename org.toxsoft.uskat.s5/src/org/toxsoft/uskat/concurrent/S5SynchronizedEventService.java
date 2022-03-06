package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.coll.IMapEdit;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.gw.gwid.GwidList;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.common.dpu.rt.events.SkEvent;
import ru.uskat.core.api.events.ISkEventHandler;
import ru.uskat.core.api.events.ISkEventService;
import ru.uskat.core.common.helpers.ITemporalsHistory;

/**
 * Синхронизация доступа к {@link ISkEventService} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedEventService
    extends S5SynchronizedService<ISkEventService>
    implements ISkEventService {

  private final S5SynchronizedTemporalsHistory<SkEvent> history;
  private final IMapEdit<ISkEventHandler, GwidList>     handlers = new ElemMap<>();

  /**
   * Конструктор
   *
   * @param aConnection {@link S5SynchronizedConnection} защищенное соединение
   * @throws TsNullArgumentRtException аругмент = null
   * @throws TsItemNotFoundRtException в соединении не найдена служба которую необходимо защитить
   */
  public S5SynchronizedEventService( S5SynchronizedConnection aConnection ) {
    this( (ISkEventService)aConnection.getUnsynchronizedService( SERVICE_ID ), aConnection.mainLock() );
    aConnection.addService( this );
  }

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkEventService} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedEventService( ISkEventService aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
    history = new S5SynchronizedTemporalsHistory<>( aTarget.history(), aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkEventService aPrevTarget, ISkEventService aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    history.changeTarget( aNewTarget.history(), aNewLock );
    for( ISkEventHandler handler : handlers.keys() ) {
      aNewTarget.registerHandler( handlers.getByKey( handler ), handler );
    }
  }

  // ------------------------------------------------------------------------------------
  // ISkEventService
  //
  @Override
  public void fireEvent( SkEvent aEvent ) {
    lockWrite( this );
    try {
      target().fireEvent( aEvent );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void registerHandler( IGwidList aNeededGwids, ISkEventHandler aEventHandler ) {
    lockWrite( this );
    try {
      target().registerHandler( aNeededGwids, aEventHandler );
      GwidList gwids = handlers.findByKey( aEventHandler );
      if( gwids == null ) {
        gwids = new GwidList();
        handlers.put( aEventHandler, gwids );
      }
      gwids.addAll( aNeededGwids );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void unregisterHandler( ISkEventHandler aEventHandler ) {
    lockWrite( this );
    try {
      target().unregisterHandler( aEventHandler );
      handlers.removeByKey( aEventHandler );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ITemporalsHistory<SkEvent> history() {
    return history;
  }
}
