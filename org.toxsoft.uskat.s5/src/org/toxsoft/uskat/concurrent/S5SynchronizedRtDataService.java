package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.core.api.rtdata.*;

/**
 * Синхронизация доступа к {@link ISkRtDataService} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedRtDataService
    extends S5SynchronizedService<ISkRtDataService>
    implements ISkRtDataService {

  private final S5SynchronizedEventer<ISkCurrDataChangeListener> eventer;
  private final IListEdit<S5SynchronizedReadCurrDataChannel>     readCurrdata    = new ElemLinkedList<>();
  private final IListEdit<S5SynchronizedWriteCurrDataChannel>    writeCurrdata   = new ElemLinkedList<>();
  private final IListEdit<S5SynchronizedWriteHistDataChannel>    writeHistdata   = new ElemLinkedList<>();
  private final IListEdit<S5SynchronizedHistDataQuery>           histDataQueries = new ElemLinkedList<>();

  /**
   * Конструктор
   *
   * @param aConnection {@link S5SynchronizedConnection} защищенное соединение
   * @throws TsNullArgumentRtException аругмент = null
   * @throws TsItemNotFoundRtException в соединении не найдена служба которую необходимо защитить
   */
  public S5SynchronizedRtDataService( S5SynchronizedConnection aConnection ) {
    this( (ISkRtDataService)aConnection.getUnsynchronizedService( SERVICE_ID ), aConnection.mainLock() );
    aConnection.addService( this );
  }

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkRtDataService} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedRtDataService( ISkRtDataService aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
    eventer = new S5SynchronizedEventer<>( target().eventer(), lock() );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @SuppressWarnings( "unchecked" )
  @Override
  protected void doChangeTarget( ISkRtDataService aPrevTarget, ISkRtDataService aNewTarget,
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
    // Создание и замена запросов хранимых данных
    for( S5SynchronizedHistDataQuery query : histDataQueries ) {
      query.changeTarget( aNewTarget.createQuery( query.options() ), aNewLock );
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
      readCurrdata.remove( (S5SynchronizedReadCurrDataChannel)aChannel );
      return;
    }
    if( aChannel instanceof S5SynchronizedWriteCurrDataChannel ) {
      writeCurrdata.remove( (S5SynchronizedWriteCurrDataChannel)aChannel );
      return;
    }
    if( aChannel instanceof S5SynchronizedWriteHistDataChannel ) {
      writeHistdata.remove( (S5SynchronizedWriteHistDataChannel)aChannel );
      return;
    }
  }

  /**
   * Удаление запроса к хранимым данным из внутренних структур наблюдения службы
   *
   * @param aQuery {@link S5SynchronizedHistDataQuery} запрос
   * @throws TsNullArgumentRtException аргумент = null
   */
  void removeQuery( S5SynchronizedHistDataQuery aQuery ) {
    TsNullArgumentRtException.checkNull( aQuery );
    histDataQueries.remove( aQuery );
  }

  // ------------------------------------------------------------------------------------
  // ISkRtDataService
  //
  @Override
  public IMap<Gwid, ISkReadCurrDataChannel> createReadCurrDataChannels( IGwidList aGwids ) {
    lockWrite( this );
    try {
      IMap<Gwid, ISkReadCurrDataChannel> channelsByGwids = target().createReadCurrDataChannels( aGwids );
      IMapEdit<Gwid, ISkReadCurrDataChannel> retValue = new ElemMap<>();
      for( Gwid gwid : channelsByGwids.keys() ) {
        S5SynchronizedReadCurrDataChannel channel =
            new S5SynchronizedReadCurrDataChannel( this, channelsByGwids.getByKey( gwid ), lock() );
        retValue.put( gwid, channel );
        readCurrdata.add( channel );
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
        S5SynchronizedWriteCurrDataChannel channel =
            new S5SynchronizedWriteCurrDataChannel( this, channelsByGwids.getByKey( gwid ), lock() );
        retValue.put( gwid, channel );
        writeCurrdata.add( channel );
      }
      return retValue;
    }
    finally {
      unlockWrite( this );
    }
  }

  @SuppressWarnings( "deprecation" )
  @Override
  public void writeCurrValues() {
    lockWrite( this );
    try {
      target().writeCurrValues();
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
  public ISkHistDataQuery createQuery( IOptionSet aOptions ) {
    lockWrite( this );
    try {
      S5SynchronizedHistDataQuery retValue =
          new S5SynchronizedHistDataQuery( this, target().createQuery( aOptions ), lock() );
      histDataQueries.add( retValue );
      return retValue;
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IMap<Gwid, ISkWriteHistDataChannel> createWriteHistDataChannels( IGwidList aGwids ) {
    lockWrite( this );
    try {
      // return target().createWriteHistDataChannels( aGwids );
      IMap<Gwid, ISkWriteHistDataChannel> channelsByGwids = target().createWriteHistDataChannels( aGwids );
      IMapEdit<Gwid, ISkWriteHistDataChannel> retValue = new ElemMap<>();
      for( Gwid gwid : channelsByGwids.keys() ) {
        S5SynchronizedWriteHistDataChannel channel =
            new S5SynchronizedWriteHistDataChannel( this, channelsByGwids.getByKey( gwid ), lock() );
        retValue.put( gwid, channel );
        writeHistdata.add( channel );
      }
      return retValue;
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
