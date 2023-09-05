package org.toxsoft.uskat.s5.server.sequences.impl;

import static java.lang.String.*;

import java.util.Calendar;

import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.TimeInterval;
import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Информация о разделе (партиция) таблицы
 */
public final class S5SequencePartitionInfo
    implements ITimestampable {

  private final String        partitionName;
  private final ITimeInterval interval;

  /**
   * Конструктор
   *
   * @param aPartitionName String имя раздела
   * @param aEndTime long метка времени завершения интервал времени значений хранимых в разделе
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5SequencePartitionInfo( String aPartitionName, long aEndTime ) {
    TsNullArgumentRtException.checkNull( aPartitionName );
    partitionName = aPartitionName;
    interval = new TimeInterval( stringToPartitionTime( aPartitionName ), aEndTime );
  }

  /**
   * Конструктор
   *
   * @param aInterval {@link ITimeInterval} интервал времени значений хранимых в разделе
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5SequencePartitionInfo( ITimeInterval aInterval ) {
    TsNullArgumentRtException.checkNull( aInterval );
    partitionName = partitionTimeToString( aInterval.startTime() );
    interval = aInterval;
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Имя раздела
   *
   * @return String имя
   */
  public String partitionName() {
    return partitionName;
  }

  /**
   * Возвращает интервал времени значений хранимых в разделе
   *
   * @return {@link ITimeInterval} интервал
   */
  public ITimeInterval interval() {
    return interval;
  }

  // ------------------------------------------------------------------------------------
  // Реализация ITimestampable
  //
  @Override
  public long timestamp() {
    return interval.startTime();
  }

  // ------------------------------------------------------------------------------------
  // Реализация Object
  //
  @Override
  public String toString() {
    return format( "%s [%s]", partitionName, interval ); //$NON-NLS-1$
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + partitionName.hashCode();
    result = TsLibUtils.PRIME * result + interval.hashCode();
    return result;
  }

  @Override
  public boolean equals( Object aObject ) {
    if( this == aObject ) {
      return true;
    }
    if( aObject == null ) {
      return false;
    }
    if( getClass() != aObject.getClass() ) {
      return false;
    }
    S5SequencePartitionInfo other = (S5SequencePartitionInfo)aObject;
    if( !partitionName.equals( other.partitionName() ) ) {
      return false;
    }
    if( interval.equals( other.interval() ) ) {
      return false;
    }
    return true;
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Возвращает список разделов которые необходимо сохранить в БД чтобы указанный интервал значений находится в
   * указанных разделах
   *
   * @param aInfos {@link ITimedList}&lt;{@link S5SequencePartitionInfo}&gt; список описаний разделов
   * @param aInterval {@link ITimeInterval} интервал значений
   * @param aDepth int глубина(в сутках) хранения значений (размер разделов)
   * @return {@link IList} список новых разделов.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static IList<S5SequencePartitionInfo> getNewPartitionsForInterval( ITimedList<S5SequencePartitionInfo> aInfos,
      ITimeInterval aInterval, int aDepth ) {
    TsNullArgumentRtException.checkNulls( aInfos, aInterval );
    if( intervalWithinPartitions( aInfos, aInterval ) ) {
      return IList.EMPTY;
    }
    // Результат
    IListEdit<S5SequencePartitionInfo> retValue = new ElemLinkedList<>();
    if( aInfos.size() == 0 ) {
      // Частный случай, еще нет разделов
      return splitIntervals( aInterval, aDepth );
    }
    // Опреределние текущего интервала разделов
    ITimeInterval partitionInterval =
        new TimeInterval( aInfos.first().interval().startTime(), aInfos.last().interval().endTime() );
    // Формирование новых интервалов разделов
    Pair<ITimeInterval, ITimeInterval> newIntervals = TimeUtils.subtract( aInterval, partitionInterval );
    if( newIntervals.left() != null ) {
      retValue.addAll( splitIntervals( newIntervals.left(), aDepth ) );
    }
    if( newIntervals.right() != null ) {
      retValue.addAll( splitIntervals( newIntervals.right(), aDepth ) );
    }
    return retValue;
  }

  /**
   * Возвращает признак того, что указанный интервал значений находится в указанных разделах
   *
   * @param aInfos {@link ITimedList}&lt;{@link S5SequencePartitionInfo}&gt; список описаний разделов
   * @param aInterval {@link ITimeInterval} интервал значений
   * @return boolean <b>true</b> интервал значений попадает в разделы;<b>false</b> интервал значений не попадает в
   *         разделы.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static boolean intervalWithinPartitions( ITimedList<S5SequencePartitionInfo> aInfos, ITimeInterval aInterval ) {
    TsNullArgumentRtException.checkNulls( aInfos, aInterval );
    if( aInfos.size() == 0 ) {
      return false;
    }
    if( TimeUtils.contains( aInfos.last().interval(), aInterval ) ) {
      // Частный случай (наиболее вероятный) - интервал попадает в последний раздел
      return true;
    }
    // Составляем общий интервал разделов и делаем проверку для нго
    long startTime = aInfos.first().interval().startTime();
    long endTime = aInfos.last().interval().endTime();
    return TimeUtils.contains( new TimeInterval( startTime, endTime ), aInterval );
  }

  // ------------------------------------------------------------------------------------
  // Внутренняя реализация
  //
  /**
   * Разделяет указанный интервал на серию интервалов не более указанной глубины
   *
   * @param aInterval {@link ITimeInterval} исходный интервал
   * @param aDepth int глубина(в сутках) хранения значений (размер разделов)
   * @return {@link IList}&lt; {@link S5SequencePartitionInfo}&gt; список интервалов
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static IList<S5SequencePartitionInfo> splitIntervals( ITimeInterval aInterval, int aDepth ) {
    TsNullArgumentRtException.checkNull( aInterval );
    long msecInDay = 1000 * 60 * 60 * 24;
    IListEdit<S5SequencePartitionInfo> retValue = new ElemLinkedList<>();
    long startTime = allignByDay( aInterval.startTime(), 0 );
    long endTime;
    do {
      endTime = startTime + aDepth * msecInDay;
      retValue.add( new S5SequencePartitionInfo( new TimeInterval( startTime, endTime ) ) );
      startTime = startTime + aDepth * msecInDay;
    } while( endTime < aInterval.endTime() );
    return retValue;
  }

  /**
   * Выравнивание метки времени по началу суток
   *
   * @param aTime long метка времени (мсек с начала эпохи)
   * @param aAddDays int количество дней прибавляемых к результату
   * @return long метка времени выравненная по началу суток путем отброса часов, минут, секунд и миллисекунд.
   */
  private static long allignByDay( long aTime, int aAddDays ) {
    long msecInDay = 1000 * 60 * 60 * 24;
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis( aTime + aAddDays * msecInDay );
    c.set( Calendar.AM_PM, 0 );
    c.set( Calendar.HOUR, 0 );
    c.set( Calendar.MINUTE, 0 );
    c.set( Calendar.SECOND, 0 );
    c.set( Calendar.MILLISECOND, 0 );
    return c.getTimeInMillis();
  }

  /**
   * Преобразует метку времени в текстовой формат
   *
   * @param aStartTime long метка времени (мсек с начала эпохи)
   * @return String текстовый формат метки
   */
  private static String partitionTimeToString( long aStartTime ) {
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis( aStartTime );
    int year = c.get( Calendar.YEAR );
    int month = c.get( Calendar.MONTH ) + 1;
    int day = c.get( Calendar.DAY_OF_MONTH );
    return String.format( "p_%d_%d_%d", Integer.valueOf( year ), Integer.valueOf( month ), Integer.valueOf( day ) ); //$NON-NLS-1$
  }

  /**
   * Преобразует метку времени в текстовом формате в метку времени
   *
   * @param aTimeStr String текстовый формат метки
   * @return long long метка времени (мсек с начала эпохи)
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static long stringToPartitionTime( String aTimeStr ) {
    TsNullArgumentRtException.checkNull( aTimeStr );
    Calendar c = Calendar.getInstance();
    String[] t = aTimeStr.split( "_" ); //$NON-NLS-1$
    c.clear();
    c.set( Integer.parseInt( t[1] ), Integer.parseInt( t[2] ) - 1, Integer.parseInt( t[3] ) );
    return c.getTimeInMillis();
  }

  // ------------------------------------------------------------------------------------
  // Тесты
  //
  @SuppressWarnings( { "nls", "javadoc" } )
  public static void main( String[] aArgs ) {
    // long currTime = TimeUtils.readTimestamp( "2021-02-03_13:05:06" );
    long currTime = System.currentTimeMillis();
    System.out.println( "currTime = " + TimeUtils.timestampToString( currTime ) );

    String partitionTimeStr = partitionTimeToString( currTime );
    System.out.println( "partitionTimeStr = " + partitionTimeStr );

    long partitionTime = stringToPartitionTime( partitionTimeStr );
    System.out.println( "partitionTime = " + TimeUtils.timestampToString( partitionTime ) );

    long msecInDay = 1000 * 60 * 60 * 24;
    // Частный случай, еще нет разделов
    long startTime = (currTime / msecInDay) * msecInDay;
    long startTime2 = currTime - msecInDay;
    long endTime = (currTime / msecInDay + 1) * msecInDay;

    System.out.println( "startTime = " + TimeUtils.timestampToString( startTime ) );
    System.out.println( "startTime2 = " + TimeUtils.timestampToString( startTime2 ) );
    System.out.println( "endTime = " + TimeUtils.timestampToString( endTime ) );

    long startTime3 = allignByDay( currTime, 0 );
    long endTime3 = allignByDay( startTime3, 0 );
    System.out.println( "startTime3 = " + TimeUtils.timestampToString( startTime3 ) );
    System.out.println( "endTime3 = " + TimeUtils.timestampToString( endTime3 ) );

    long startTime4 = allignByDay( currTime, 1 );
    System.out.println( "startTime4 = " + TimeUtils.timestampToString( startTime4 ) );
  }
}
