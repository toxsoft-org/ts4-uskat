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
    long msecInDay = 1000 * 60 * 60 * 24;
    IListEdit<S5Partition> retValue = new ElemLinkedList<>();
    long startTime = allignByDay( aStartTime, 0 );
    long endTime;
    // Глубина хранения значений в разделе. Если глубина хранения значений < 30 дней, то глубина 1 сутки. Иначе - месяц
    int partitionDepth = (aDepth > 30 ? 30 : 1);
    do {
      endTime = startTime + partitionDepth * msecInDay;
      retValue.add( new S5Partition( new TimeInterval( startTime, endTime ) ) );
      startTime = startTime + partitionDepth * msecInDay;
    } while( endTime < aEndTime );
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
