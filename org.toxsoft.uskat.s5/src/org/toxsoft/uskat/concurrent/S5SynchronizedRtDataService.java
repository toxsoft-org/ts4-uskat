package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.rtdserv.*;

/**
 * Синхронизация доступа к {@link ISkRtdataService} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedRtDataService
    extends S5SynchronizedService<ISkRtdataService>
    implements ISkRtdataService {

  private final S5SynchronizedEventer<ITsEventer<ISkCurrDataChangeListener>, ISkCurrDataChangeListener> eventer;

  private final IMapEdit<Gwid, S5SynchronizedReadCurrDataChannel>  readCurrdata  = new ElemMap<>();
  private final IMapEdit<Gwid, S5SynchronizedWriteCurrDataChannel> writeCurrdata = new ElemMap<>();
  private final IMapEdit<Gwid, S5SynchronizedWriteHistDataChannel> writeHistdata = new ElemMap<>();

  /**
   * Конструктор
   *
   * @param aConnection {@link S5SynchronizedConnection} защищенное соединение
   * @throws TsNullArgumentRtException аругмент = null
   * @throws TsItemNotFoundRtException в соединении не найдена служба которую необходимо защитить
   */
  public S5SynchronizedRtDataService( S5SynchronizedConnection aConnection ) {
    this( (ISkRtdataService)aConnection.getUnsynchronizedService( SERVICE_ID ), aConnection.nativeLock() );
    aConnection.addService( this );
  }

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkRtdataService} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedRtDataService( ISkRtdataService aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
    eventer = new S5SynchronizedEventer<>( target().eventer(), nativeLock() );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @SuppressWarnings( "unchecked" )
  @Override
  protected void doChangeTarget( ISkRtdataService aPrevTarget, ISkRtdataService aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    eventer.changeTarget( aNewTarget.eventer(), aNewLock );
    // Создание каналов в новом соединении и замена
    IMap<Gwid, ISkReadCurrDataChannel> readCurrdataChannels =
        target().createReadCurrDataChannels( getChannelGwids( (IList<ISkRtdataChannel>)(Object)readCurrdata ) );
    for( S5SynchronizedReadCurrDataChannel channel : readCurrdata ) {
      channel.changeTarget( readCurrdataChannels.getByKey( channel.gwid() ), aNewLock );
    }
    IMap<Gwid, ISkWriteCurrDataChannel> writeCurrdataChannels =
        target().createWriteCurrDataChannels( getChannelGwids( (IList<ISkRtdataChannel>)(Object)writeCurrdata ) );
    for( S5SynchronizedWriteCurrDataChannel channel : writeCurrdata ) {
      channel.changeTarget( writeCurrdataChannels.getByKey( channel.gwid() ), aNewLock );
    }
    IMap<Gwid, ISkWriteHistDataChannel> writeHistdataChannels =
        target().createWriteHistDataChannels( getChannelGwids( (IList<ISkRtdataChannel>)(Object)writeHistdata ) );
    for( S5SynchronizedWriteHistDataChannel channel : writeHistdata ) {
      channel.changeTarget( writeHistdataChannels.getByKey( channel.gwid() ), aNewLock );
    }
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Удаление канала из внутренних структур наблюдения службы
   *
   * @param aChannel {@link S5SynchronizedRtdataChannel} канал
   * @throws TsNullArgumentRtException аргумент = null
   */
  void removeChannel( S5SynchronizedRtdataChannel<? extends ISkRtdataChannel> aChannel ) {
    TsNullArgumentRtException.checkNull( aChannel );
    if( aChannel instanceof S5SynchronizedReadCurrDataChannel ) {
      readCurrdata.removeByKey( aChannel.gwid() );
      return;
    }
    if( aChannel instanceof S5SynchronizedWriteCurrDataChannel ) {
      writeCurrdata.removeByKey( aChannel.gwid() );
      return;
    }
    if( aChannel instanceof S5SynchronizedWriteHistDataChannel ) {
      writeHistdata.removeByKey( aChannel.gwid() );
      return;
    }
  }

  // ------------------------------------------------------------------------------------
  // ISkRtdataService
  //
  @Override
  public IMap<Gwid, ISkReadCurrDataChannel> createReadCurrDataChannels( IGwidList aGwids ) {
    lockWrite( this );
    try {
      IMap<Gwid, ISkReadCurrDataChannel> channelsByGwids = target().createReadCurrDataChannels( aGwids );
      IMapEdit<Gwid, ISkReadCurrDataChannel> retValue = new ElemMap<>();
      for( Gwid gwid : channelsByGwids.keys() ) {
        S5SynchronizedReadCurrDataChannel channel = readCurrdata.findByKey( gwid );
        if( channel == null ) {
          channel = new S5SynchronizedReadCurrDataChannel( this, channelsByGwids.getByKey( gwid ), nativeLock() );
          readCurrdata.put( gwid, channel );
        }
        else {
          channel.changeTarget( channelsByGwids.getByKey( gwid ), nativeLock() );
        }
        retValue.put( gwid, channel );
      }
      return retValue;
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IMap<Gwid, ISkWriteCurrDataChannel> createWriteCurrDataChannels( IGwidList aGwids ) {
    lockWrite( this );
    try {
      IMap<Gwid, ISkWriteCurrDataChannel> channelsByGwids = target().createWriteCurrDataChannels( aGwids );
      IMapEdit<Gwid, ISkWriteCurrDataChannel> retValue = new ElemMap<>();
      for( Gwid gwid : channelsByGwids.keys() ) {
        S5SynchronizedWriteCurrDataChannel channel = writeCurrdata.findByKey( gwid );
        if( channel == null ) {
          channel = new S5SynchronizedWriteCurrDataChannel( this, channelsByGwids.getByKey( gwid ), nativeLock() );
          writeCurrdata.put( gwid, channel );
        }
        else {
          channel.changeTarget( channelsByGwids.getByKey( gwid ), nativeLock() );
        }
        retValue.put( gwid, channel );
      }
      return retValue;
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ITsEventer<ISkCurrDataChangeListener> eventer() {
    return eventer;
  }

  @Override
  public IMap<Gwid, ISkWriteHistDataChannel> createWriteHistDataChannels( IGwidList aGwids ) {
    lockWrite( this );
    try {
      IMap<Gwid, ISkWriteHistDataChannel> channelsByGwids = target().createWriteHistDataChannels( aGwids );
      IMapEdit<Gwid, ISkWriteHistDataChannel> retValue = new ElemMap<>();
      for( Gwid gwid : channelsByGwids.keys() ) {
        S5SynchronizedWriteHistDataChannel channel = writeHistdata.findByKey( gwid );
        if( channel == null ) {
          channel = new S5SynchronizedWriteHistDataChannel( this, channelsByGwids.getByKey( gwid ), nativeLock() );
          writeHistdata.put( gwid, channel );
        }
        else {
          channel.changeTarget( channelsByGwids.getByKey( gwid ), nativeLock() );
        }
        retValue.put( gwid, channel );
      }
      return retValue;
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ITimedList<ITemporalAtomicValue> queryObjRtdata( IQueryInterval aInterval, Gwid aGwid ) {
    lockWrite( this );
    try {
      return target().queryObjRtdata( aInterval, aGwid );
    }
    finally {
      unlockWrite( this );
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Возвращает список идентификаторов представленных каналов
   *
   * @param aChannels {@link IList}&lt;{@link ISkRtdataChannel}&gt; список каналов
   * @return {@link IGwidList} список идентификаторов
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static IGwidList getChannelGwids( IList<ISkRtdataChannel> aChannels ) {
    TsNullArgumentRtException.checkNull( aChannels );
    GwidList retValue = new GwidList();
    for( ISkRtdataChannel channel : aChannels ) {
      retValue.add( channel.gwid() );
    }
    return retValue;
  }

}
