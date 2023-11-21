package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5HistDataInterceptor.*;
import static org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.IS5Resources.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ejb.*;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.QueryInterval;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.GwidList;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.*;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.S5HistDataSequenceFactory;
import org.toxsoft.uskat.s5.server.interceptors.S5InterceptorSupport;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceBlockEdit;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceFactory;
import org.toxsoft.uskat.s5.server.sequences.impl.S5BackendSequenceSupportSingleton;
import org.toxsoft.uskat.s5.utils.jobs.IS5ServerJob;

/**
 * Реализация синглетона {@link IS5BackendHistDataSingleton}
 *
 * @author mvk
 */
@Startup
@Singleton
@LocalBean
@DependsOn( { //
    BACKEND_LINKS_SINGLETON //
} )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
// @ConcurrencyManagement( ConcurrencyManagementType.CONTAINER )
@ConcurrencyManagement( ConcurrencyManagementType.BEAN )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@Lock( LockType.READ )
public class S5BackendHistDataSingleton
    extends S5BackendSequenceSupportSingleton<IS5HistDataSequence, ITemporalAtomicValue>
    // extends S5BackendSupportSingleton
    implements IS5BackendHistDataSingleton, IS5ServerJob {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String BACKEND_HISTDATA_ID = "S5BackendHistDataSingleton"; //$NON-NLS-1$

  /**
   * Интервал выполнения doJob (мсек)
   */
  private static final long DOJOB_INTERVAL = 1000;

  /**
   * Таймаут запроса хранимых значений по одному данному (мсек)
   */
  private static final long ONE_READ_VALUES_TIMEOUT = 10000;

  /**
   * Поддержка интерсепторов операций проводимых над данными
   */
  private final S5InterceptorSupport<IS5HistDataInterceptor> interceptors = new S5InterceptorSupport<>();

  /**
   * Конструктор.
   */
  public S5BackendHistDataSingleton() {
    super( BACKEND_HISTDATA_ID, STR_D_BACKEND_HISTDATA );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendSupportSingleton
  //
  @Override
  protected void doInitSupport() {
    // Инициализация базового класса
    super.doInitSupport();
    // Запуск doJob
    addOwnDoJob( DOJOB_INTERVAL );
  }

  @Override
  protected void doCloseSupport() {
    // nop
  }

  @Override
  protected IS5BackendHistDataSingleton getBusinessObject() {
    return sessionContext().getBusinessObject( IS5BackendHistDataSingleton.class );
  }

  @Override
  protected IS5SequenceFactory<ITemporalAtomicValue> doCreateFactory() {
    return new S5HistDataSequenceFactory( backend().initialConfig().impl(), sysdescrReader() );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5BackendHistDataSingleton
  //
  // TODO: 2023-11-18 mvkd +++
  @Asynchronous
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void asyncWriteValues( IMap<Gwid, Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>>> aValues ) {
    syncWriteValues( aValues );
  }

  @SuppressWarnings( "unchecked" )
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void syncWriteValues( IMap<Gwid, Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>>> aValues ) {
    TsNullArgumentRtException.checkNulls( aValues );
    try {
      // Время трассировки
      long traceTime0 = System.currentTimeMillis();
      // Пред-вызов интерсепторов
      if( !callBeforeWriteHistData( interceptors, aValues ) ) {
        // Интерсепторы отклонили запись значений хранимых данных
        logger().debug( MSG_REJECT_HISTDATA_WRITE_BY_INTERCEPTORS );
        return;
      }
      // Карта последовательностей данных. Ключ: идентификатор данного; Значение: последовательность значений
      IListEdit<IS5HistDataSequence> sequences = new ElemLinkedList<>();
      for( Gwid gwid : aValues.keys() ) {
        Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>> intervalValues = aValues.getByKey( gwid );
        // Интервал записи
        ITimeInterval ti = intervalValues.left();
        ITimedList<ITemporalAtomicValue> values = intervalValues.right();
        IQueryInterval interval = new QueryInterval( EQueryIntervalType.CSCE, ti.startTime(), ti.endTime() );
        IS5SequenceBlockEdit<ITemporalAtomicValue> block = factory().createBlock( gwid, values );
        sequences.add( (IS5HistDataSequence)factory().createSequence( gwid, interval, new ElemArrayList<>( block ) ) );
      }
      writeSequences( sequences );

      // Пост-вызов интерсепторов
      callAfterWriteHistData( interceptors, aValues, logger() );

      // Вывод в журнал сообщения об изменении значений
      writeValuesToLog( logger(), aValues, System.currentTimeMillis() - traceTime0 );
    }
    catch( Throwable e ) {
      // Неожиданная ошибка записи хранимых данных
      logger().error( e, ERR_WRITE_UNEXPECTED, cause( e ) );
      throw new TsInternalErrorRtException( e, ERR_WRITE_UNEXPECTED, cause( e ) );
    }
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public ITimedList<ITemporalAtomicValue> queryObjRtdata( IQueryInterval aInterval, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aInterval, aGwid );
    IMap<Gwid, IS5HistDataSequence> sequences =
        readSequences( new GwidList( aGwid ), aInterval, ONE_READ_VALUES_TIMEOUT );
    return sequences.values().first().get( aInterval );
  }

  @Override
  public void addHistDataInterceptor( IS5HistDataInterceptor aInterceptor, int aPriority ) {
    TsNullArgumentRtException.checkNulls( aInterceptor );
    interceptors.add( aInterceptor, aPriority );
  }

  @Override
  public void removeHistDataInterceptor( IS5HistDataInterceptor aInterceptor ) {
    TsNullArgumentRtException.checkNulls( aInterceptor );
    interceptors.remove( aInterceptor );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  // private void writeValuesToLog( IMap<Gwid, Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>>> aValues ) {
  // if( logger().isSeverityOn( ELogSeverity.INFO ) ) {
  // // Вывод в лог сохраняемых данных
  // logger().info( toStr( MSG_WRITE_HISTDATA_VALUES_DEBUG, aValues ) );
  // }
  // }

  /**
   * Вывод записанных значений в журнал
   *
   * @param aLogger {@link ILogger} журнал
   * @param aValues {@link Map} карта значений
   * @param aTime long время (мсек) записи
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void writeValuesToLog( ILogger aLogger,
      IMap<Gwid, Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>>> aValues, long aTime ) {
    TsNullArgumentRtException.checkNulls( aLogger, aValues );
    if( aLogger.isSeverityOn( ELogSeverity.DEBUG ) ) {
      aLogger.debug( toStr( MSG_WRITE_HISTDATA_VALUES_DEBUG, aValues, aTime ) );
      return;
    }
    if( aLogger.isSeverityOn( ELogSeverity.INFO ) ) {
      aLogger.info( MSG_WRITE_HISTDATA_VALUES_INFO, Integer.valueOf( aValues.size() ), Long.valueOf( aTime ) );
      return;
    }
  }

  /**
   * Возвращает строку представляющую значения хранимых данных
   *
   * @param aMessage String начальная строка
   * @param aValues
   *          {@link IMap}&lt;{@link Gwid},{@link Pair}&lt;{@link ITimeInterval},{@link ITimedList}&lt;{@link ITemporalAtomicValue}&gt;&gt;&gt;
   *          - значения
   * @param aTime long время (мсек) записи
   * @return String строка представления значений данных
   */
  private static String toStr( String aMessage,
      IMap<Gwid, Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>>> aValues, long aTime ) {
    TsNullArgumentRtException.checkNull( aValues );
    StringBuilder sb = new StringBuilder();
    sb.append( String.format( aMessage, Integer.valueOf( aValues.size() ), Long.valueOf( aTime ) ) );
    for( Gwid gwid : aValues.keys() ) {
      Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>> intervalValues = aValues.getByKey( gwid );
      ITimeInterval interval = intervalValues.left();
      ITimedList<ITemporalAtomicValue> values = intervalValues.right();
      Integer count = Integer.valueOf( values.size() );
      sb.append( String.format( MSG_HISTDATA_VALUE, gwid, interval, count ) );
    }
    return sb.toString();
  }
}
