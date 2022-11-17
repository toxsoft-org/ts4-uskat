package org.toxsoft.uskat.dataquality.s5.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.metainfo.IDataType;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.concurrent.*;
import org.toxsoft.uskat.dataquality.lib.*;

/**
 * Синхронизация доступа к {@link ISkDataQualityService} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedDataQualityService
    extends S5SynchronizedService<ISkDataQualityService>
    implements ISkDataQualityService {

  private final S5SynchronizedEventer<ISkDataQualityChangeListener> eventer;

  /**
   * Конструктор
   *
   * @param aCoreApi {@link S5SynchronizedConnection} защищенное API сервера
   * @throws TsNullArgumentRtException аругмент = null
   * @throws TsItemNotFoundRtException в соединении не найдена служба которую необходимо защитить
   */
  public S5SynchronizedDataQualityService( S5SynchronizedCoreApi aCoreApi ) {
    super( (ISkDataQualityService)aCoreApi.target().services().getByKey( ISkDataQualityService.SERVICE_ID ),
        aCoreApi.nativeLock() );
    eventer = new S5SynchronizedEventer<>( target().eventer(), nativeLock() );
    aCoreApi.services().put( ISkDataQualityService.SERVICE_ID, this );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkDataQualityService aPrevTarget, ISkDataQualityService aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    eventer.changeTarget( aNewTarget.eventer(), aNewLock );
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISkDataQualityService
  //
  @Override
  public IOptionSet getResourceMarks( Gwid aResource ) {
    lockWrite( this );
    try {
      return target().getResourceMarks( aResource );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IMap<Gwid, IOptionSet> getResourcesMarks( IGwidList aResources ) {
    lockWrite( this );
    try {
      return target().getResourcesMarks( aResources );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IMap<Gwid, IOptionSet> queryMarkedUgwies( ITsCombiFilterParams aQueryParams ) {
    lockWrite( this );
    try {
      return target().queryMarkedUgwies( aQueryParams );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IGwidList getConnectedResources() {
    lockWrite( this );
    try {
      return target().getConnectedResources();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void addConnectedResources( IGwidList aResources ) {
    lockWrite( this );
    try {
      target().addConnectedResources( aResources );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void removeConnectedResources( IGwidList aResources ) {
    lockWrite( this );
    try {
      target().removeConnectedResources( aResources );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IGwidList setConnectedResources( IGwidList aResources ) {
    lockWrite( this );
    try {
      return target().setConnectedResources( aResources );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void setMarkValue( String aTicketId, IAtomicValue aValue, IGwidList aResources ) {
    lockWrite( this );
    try {
      target().setMarkValue( aTicketId, aValue, aResources );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IStridablesList<ISkDataQualityTicket> listTickets() {
    lockWrite( this );
    try {
      return target().listTickets();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkDataQualityTicket defineTicket( String aTicketId, String aName, String aDescription, IDataType aDataType ) {
    lockWrite( this );
    try {
      return target().defineTicket( aTicketId, aName, aDescription, aDataType );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void removeTicket( String aTicketId ) {
    lockWrite( this );
    try {
      target().removeTicket( aTicketId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ITsEventer<ISkDataQualityChangeListener> eventer() {
    return eventer;
  }
}
