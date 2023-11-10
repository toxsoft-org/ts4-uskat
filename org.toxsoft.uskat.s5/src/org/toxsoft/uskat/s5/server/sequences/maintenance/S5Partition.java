package org.toxsoft.uskat.s5.server.sequences.maintenance;

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
 * Информация о разделе (partition) таблицы
 */
public final class S5Partition
    implements ITimestampable {

  private final String        name;
  private final ITimeInterval interval;

  /**
   * Конструктор
   *
   * @param aPartitionName String имя раздела
   * @param aEndTime long метка времени завершения интервал времени значений хранимых в разделе
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5Partition( String aPartitionName, long aEndTime ) {
    TsNullArgumentRtException.checkNull( aPartitionName );
    name = aPartitionName;
    interval = new TimeInterval( stringToPartitionTime( aPartitionName ), aEndTime );
  }

  /**
   * Конструктор
   *
   * @param aInterval {@link ITimeInterval} интервал времени значений хранимых в разделе
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5Partition( ITimeInterval aInterval ) {
    TsNullArgumentRtException.checkNull( aInterval );
    name = partitionTimeToString( aInterval.startTime() );
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
  public String name() {
    return name;
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
    return format( "%s [%s]", name, interval ); //$NON-NLS-1$
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + name.hashCode();
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
    S5Partition other = (S5Partition)aObject;
    if( !name.equals( other.name() ) ) {
      return false;
    }
    if( interval.equals( other.interval() ) ) {
      return false;
    }
    return true;
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Проводит расчет времени начала раздела для значений с указанной меткой времени
   *
   * @param aTime long метка времени (мсек с начала эпохи) значения
   * @param aDepth int глубина(в сутках) хранения значений (размер разделов)
   * @return long время (мсек с начала эпохи) начала раздела
   */
  public static long calcPartitionStartTime( long aTime, int aDepth ) {
    if( needMonthPartition( aDepth ) ) {
      // Разделы с глубиной хранения один месяц
      return allignByMonth( aTime );
    }
    // Разделы с глубиной хранения одни сутки
    return allignByDay( aTime );
  }

  /**
   * Проводит расчет времени завершения раздела для значений с указанной меткой времени
   *
   * @param aTime long метка времени (мсек с начала эпохи) значения
   * @param aDepth int глубина(в сутках) хранения значений (размер разделов)
   * @return long время (мсек с начала эпохи) завершения раздела
   */
  public static long calcPartitionEndTime( long aTime, int aDepth ) {
    long startTime = calcPartitionStartTime( aTime, aDepth );
    if( needMonthPartition( aDepth ) ) {
      // Глубина хранения в разделе 1 месяц.
      return allignByNextMonth( aTime );
    }
    // Количество мсек в сутках
    long msecInDay = 1000 * 60 * 60 * 24;
    // Глубина хранения в разделе 1 сутки
    return calcPartitionStartTime( startTime + msecInDay, aDepth );
  }

  /**
   * Возвращает список разделов которые необходимо сохранить в БД чтобы указанный интервал значений находился в
   * указанных разделах
   *
   * @param aPartitions {@link ITimedList}&lt;{@link S5Partition}&gt; список существующих разделов
   * @param aInterval {@link ITimeInterval} интервал значений
   * @param aDepth int глубина(в сутках) хранения значений (размер разделов)
   * @return {@link IList} список новых разделов.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static IList<S5Partition> getNewPartitionsForInterval( ITimedList<S5Partition> aPartitions,
      ITimeInterval aInterval, int aDepth ) {
    TsNullArgumentRtException.checkNulls( aPartitions, aInterval );
    if( intervalWithinPartitions( aPartitions, aInterval ) ) {
      return IList.EMPTY;
    }
    // Результат
    IListEdit<S5Partition> retValue = new ElemLinkedList<>();
    if( aPartitions.size() == 0 ) {
      // Частный случай, еще нет разделов
      return createPartitionInfosForInterval( aInterval.startTime(), aInterval.endTime(), aDepth );
    }
    // Опреределние текущего интервала разделов
    ITimeInterval partitionInterval =
        new TimeInterval( aPartitions.first().interval().startTime(), aPartitions.last().interval().endTime() );
    // Формирование новых интервалов разделов
    Pair<ITimeInterval, ITimeInterval> newIntervals = TimeUtils.subtract( aInterval, partitionInterval );
    // 2023-10-04: mvk: нельзя создавать разделы "слева" ДО уже существующих в dbms разделов таблицы (ограничение SQL)
    // if( newIntervals.left() != null ) {
    // ITimeInterval interval = newIntervals.left();
    // long startTime = interval.startTime();
    // long endTime = interval.endTime() + 1;
    // retValue.addAll( createPartitionInfosForInterval( startTime, endTime, aDepth ) );
    // }
    if( newIntervals.right() != null ) {
      ITimeInterval interval = newIntervals.right();
      long startTime = interval.startTime() - 1;
      long endTime = interval.endTime();
      retValue.addAll( createPartitionInfosForInterval( startTime, endTime, aDepth ) );
    }
    return retValue;
  }

  // ------------------------------------------------------------------------------------
  // Внутренняя реализация
  //
  /**
   * Возвращает признак того, что для указанной глубины хранения требуются разделы с глубиной хранения один месяц, в
   * противном случае одни сутки.
   *
   * @param aDepth int глубина(в сутках) хранения значений (размер разделов)
   * @return boolean <b>true</b> требуется разделы с месячным хранением; <b>false</b> требуются разделы с суточным
   *         хранением.
   */
  private static boolean needMonthPartition( int aDepth ) {
    return (aDepth > 30);
  }

  /**
   * Возвращает признак того, что указанный интервал значений находится в указанных разделах
   *
   * @param aInfos {@link ITimedList}&lt;{@link S5Partition}&gt; список описаний разделов
   * @param aInterval {@link ITimeInterval} интервал значений
   * @return boolean <b>true</b> интервал значений попадает в разделы;<b>false</b> интервал значений не попадает в
   *         разделы.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static boolean intervalWithinPartitions( ITimedList<S5Partition> aInfos, ITimeInterval aInterval ) {
    TsNullArgumentRtException.checkNulls( aInfos, aInterval );
    if( aInfos.size() == 0 ) {
      return false;
    }
    if( TimeUtils.contains( aInfos.last().interval(), aInterval ) ) {
      // Частный случай (наиболее вероятный) - интервал попадает в последний раздел
      return true;
    }
    // Составляем совокупный интервал разделов и делаем проверку для него
    long startTime = aInfos.first().interval().startTime();
    long endTime = aInfos.last().interval().endTime();
    return TimeUtils.contains( new TimeInterval( startTime, endTime ), aInterval );
  }

  /**
   * Создает описания разделов для указанного интервала
   *
   * @param aStartTime long метка времени (мсек с начала эпохи) начала интервала
   * @param aEndTime long метка времени (мсек с начала эпохи) завершения интервала
   * @param aDepth int глубина(в сутках) хранения значений (размер разделов)
   * @return {@link IList}&lt; {@link S5Partition}&gt; список интервалов
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static IList<S5Partition> createPartitionInfosForInterval( long aStartTime, long aEndTime, int aDepth ) {
    IListEdit<S5Partition> retValue = new ElemLinkedList<>();
    long startTime = calcPartitionStartTime( aStartTime, aDepth );
    long endTime;
    do {
      endTime = calcPartitionEndTime( startTime, aDepth );
      retValue.add( new S5Partition( new TimeInterval( startTime, endTime ) ) );
      startTime = endTime;
    } while( endTime < aEndTime );
    return retValue;
  }

  /**
   * Выравнивание метки времени по началу суток
   *
   * @param aTime long метка времени (мсек с начала эпохи)
   * @return long метка времени выравненная по началу суток путем отброса часов, минут, секунд и миллисекунд.
   */
  private static long allignByDay( long aTime ) {
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis( aTime );
    // c.set( Calendar.DATE, c.getActualMinimum( Calendar.DAY_OF_YEAR ) );

    c.set( Calendar.AM_PM, 0 );
    c.set( Calendar.HOUR, 0 );
    c.set( Calendar.MINUTE, 0 );
    c.set( Calendar.SECOND, 0 );
    c.set( Calendar.MILLISECOND, 0 );
    return c.getTimeInMillis();
  }

  /**
   * Выравнивание метки времени по началу месяца
   *
   * @param aTime long метка времени (мсек с начала эпохи)
   * @return long метка времени выравненная по началу месяца путем отброса часов, минут, секунд и миллисекунд.
   */
  private static long allignByMonth( long aTime ) {
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis( aTime );
    c.set( Calendar.AM_PM, 0 );
    c.set( Calendar.DAY_OF_MONTH, 1 );
    c.set( Calendar.HOUR, 0 );
    c.set( Calendar.MINUTE, 0 );
    c.set( Calendar.SECOND, 0 );
    c.set( Calendar.MILLISECOND, 0 );
    return c.getTimeInMillis();
  }

  /**
   * Выравнивание метки времени по началу следующего месяца
   *
   * @param aTime long метка времени (мсек с начала эпохи)
   * @return long метка времени выравненная по началу следующего месяца путем добавления месяца к указанному времени и
   *         отброса часов, минут, секунд и миллисекунд.
   */
  private static long allignByNextMonth( long aTime ) {
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis( aTime );
    c.add( Calendar.MONTH, 1 );
    c.set( Calendar.AM_PM, 0 );
    c.set( Calendar.DAY_OF_MONTH, 1 );
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
    testCalcStartEndTimes( "2021-02-03_13:05:06", 7 );
    testCalcStartEndTimes( "2021-03-31_13:05:06", 7 );
    testCalcStartEndTimes( "2021-02-28_13:05:06", 7 );
    testCalcStartEndTimes( "2021-03-31_13:05:06", 60 );
    testCalcStartEndTimes( "2021-03-01_13:05:06", 60 );

    testPartitionForInterval( "2021-03-01_13:05:06", "2021-03-11_20:05:06", 7 );
    testPartitionForInterval( "2021-03-01_13:05:06", "2021-05-11_20:05:06", 365 * 5 );
  }

  @SuppressWarnings( "nls" )
  private static void testCalcStartEndTimes( String aTime, int aDepth ) {
    long time = TimeUtils.readTimestamp( aTime );
    long startTime = calcPartitionStartTime( time, aDepth );
    long endTime = calcPartitionEndTime( time, aDepth );
    System.out.println( "time = " + TimeUtils.timestampToString( time ) + ", aDepth = " + aDepth + //
        ", startTime = " + partitionTimeToString( startTime ) + //
        ", endTime = " + partitionTimeToString( endTime ) //
    );
  }

  @SuppressWarnings( "nls" )
  private static void testPartitionForInterval( String aStartTime, String aEndTime, int aDepth ) {
    System.out.println( "aStartTime = " + aStartTime + ", aEndTime = " + aEndTime + ", aDepth = " + aDepth );
    long startTime = TimeUtils.readTimestamp( aStartTime );
    long endTime = TimeUtils.readTimestamp( aEndTime );
    IList<S5Partition> partitions = createPartitionInfosForInterval( startTime, endTime, aDepth );
    for( int index = 0, n = partitions.size(); index < n; index++ ) {
      S5Partition partition = partitions.get( index );
      System.out.println( "partitions[" + index + "] = " + partition ); //$NON-NLS-1$
    }
    System.out.println( "==============================================" );
  }
}
