package org.toxsoft.uskat.sysext.realtime.supports.histdata.sequences;

import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.gwid.EGwidKind;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;
import org.toxsoft.uskat.sysext.realtime.supports.histdata.IS5HistDataSequenceWriter;

import ru.uskat.common.dpu.rt.events.DpuWriteHistData;
import ru.uskat.common.dpu.rt.events.DpuWriteHistDataValues;
import ru.uskat.core.connection.ISkConnection;

/**
 * Поддержка записи исторических данных
 *
 * @author mvk
 */
public final class S5HistDataWriteSupport {

  /**
   * Карта значений хранимых данных подготовленных для записи
   * <p>
   * Ключ: идентификатор данного;<br>
   * Значение: последовательность значения данного
   */
  private final DpuWriteHistData histdata = new DpuWriteHistData();

  /**
   * Текущий интервал (мсек) передачи данных
   */
  private long writeInterval = 10000;

  /**
   * Метка времени последней передачи данных
   */
  private long lastWriteTimestamp = System.currentTimeMillis();

  /**
   * Блокировка доступа к механизму
   */
  private S5Lockable lock = new S5Lockable();

  /**
   * Конструктор
   *
   * @param aArgs {@link ITsContextRo} параметры создания соединения {@link ISkConnection#open(ITsContextRo)}
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5HistDataWriteSupport( ITsContextRo aArgs ) {
    // TODO: обработка аргументов
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Записывает все значения данного за указанный минтервал времени.
   * <p>
   * Внимание: большое значение имеет {@link ITimedList#getInterval()}. Метод предполагает, что в аргументе за заданный
   * интервал содержатся <b>все</b> значения. Например, если интервал сутки, а список пустой, это означает что за
   * заданный интервал значение не менялось.
   * <p>
   * Идентификатором данного может быть только конкретный (с идентификатором объекта) {@link Gwid}-ы имеющий вид
   * {@link EGwidKind#GW_RTDATA}. Все другие идентификаторы молча игнорируются.
   *
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aInterval {@link ITimeInterval} интервал записи значений.
   * @param aValues {@link Gwid},{@link ITimedList}&lt;{@link ITemporalAtomicValue}&gt; - значения в интервале aInterval
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public void writeHistData( Gwid aGwid, ITimeInterval aInterval, ITimedList<ITemporalAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aGwid, aInterval, aValues );
    lockWrite( lock );
    try {
      DpuWriteHistDataValues dataValues = histdata.findByKey( aGwid );
      if( dataValues == null ) {
        histdata.put( aGwid, new DpuWriteHistDataValues( aInterval, aValues ) );
        return;
      }
      dataValues.addValues( aInterval, aValues );
    }
    finally {
      unlockWrite( lock );
    }
  }

  /**
   * Выполнить фоновую работу
   * <p>
   * Отправляет накопленные данные в бекенд
   *
   * @param aWriter {@link IS5HistDataSequenceWriter} писатель исторических данных в бекенд
   * @param aForce boolean <b>true</b> безусловная передача данных. <b>false</b> решение о передаче принимается
   *          механизмом поддержки
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void doJob( IS5HistDataSequenceWriter aWriter, boolean aForce ) {
    TsNullArgumentRtException.checkNull( aWriter );
    long currTime = System.currentTimeMillis();
    if( !aForce && currTime - lastWriteTimestamp < writeInterval ) {
      // Нет необходимости передачи данных
      return;
    }
    lastWriteTimestamp = currTime;
    lockWrite( lock );
    try {
      if( histdata.size() == 0 ) {
        // Нет данных для передачи
        return;
      }
      // if( histdata.size() > 0 ) {
      // System.err.println( "S5HistDataWriteSupport.doJob. histdata.size() = " + histdata.size() );
      // }
      aWriter.write( histdata );
      histdata.clear();
    }
    finally {
      unlockWrite( lock );
    }
  }
}
