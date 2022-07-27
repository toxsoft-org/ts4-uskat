package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences;

import static org.toxsoft.core.tslib.bricks.time.impl.TimeUtils.*;
import static org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceBlock.*;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5HistDataSequenceEdit;
import org.toxsoft.uskat.s5.server.sequences.ISequenceBlock;
import org.toxsoft.uskat.s5.server.sequences.ISequenceBlockEdit;
import org.toxsoft.uskat.s5.server.sequences.impl.S5Sequence;

/**
 * Реализация последовательности исторических данных объекта
 *
 * @author mvk
 */
public class S5HistDataSequence
    extends S5Sequence<ITemporalAtomicValue>
    implements IS5HistDataSequenceEdit {

  private static final long serialVersionUID = 157157L;

  /**
   * Блок на котором находится курсор импорта данных. null: в последовательности нет блоков
   */
  private transient IHistDataBlock importBlock;

  /**
   * Индекс блока на котором находится курсор импорта данных. < 0: в последовательности нет блоков
   */
  private transient int importBlockIndex;

  /**
   * Конструктор (используется для при загрузке блоков из dbms)
   *
   * @param aFactory {@link S5HistDataSequenceFactory} фабрика последовательностей значений
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aInterval {@link IQueryInterval} интервал времени последовательности, подробности в {@link #interval()}
   * @param aBlocks {@link IList}&lt;{@link ISequenceBlock}&gt; список блоков представляющих последовательность
   * @throw {@link TsNullArgumentRtException} любой аргумент = null
   */
  public S5HistDataSequence( S5HistDataSequenceFactory aFactory, Gwid aGwid, IQueryInterval aInterval,
      Iterable<ISequenceBlockEdit<ITemporalAtomicValue>> aBlocks ) {
    super( aFactory, aGwid, aInterval, aBlocks );
    // Устанавливаем курсор импорта на начало последовательности
    setImportTime( aInterval.startTime() );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5HistDataSequence
  //
  @Override
  public void setImportTime( long aTimestamp ) {
    IParameterized info = typeInfo();
    ITimeInterval interval = interval();
    long timestamp = alignByDDT( info, aTimestamp );
    long startTime = alignByDDT( info, interval.startTime() );
    long endTime = alignByDDT( info, interval.endTime() );
    if( timestamp < startTime || endTime < timestamp ) {
      // Курсор импорта не может быть установлен за границами последовательности
      String st = timestampToString( startTime );
      String et = timestampToString( endTime );
      String ct = timestampToString( timestamp );
      throw new TsIllegalArgumentRtException( ERR_WRONG_IMPORT_CURSOR, st, et, ct );
    }
    // Инициализация курсора
    importBlockIndex = -1;
    importBlock = null;
    // Индекс ближайщего блока в зоне aTimestamp
    int nearest = findBlockIndex( timestamp );
    IHistDataBlock block = (nearest < 0 ? null : (IHistDataBlock)blocks().get( nearest ));
    if( block == null ) {
      // Блоков нет
      return;
    }
    if( timestamp >= block.startTime() && timestamp <= block.endTime() && block.size() > 0 ) {
      // Точное попадание в блок
      importBlock = block;
      importBlockIndex = nearest;
      importBlock.setImportTime( aTimestamp );
      return;
    }
    // Сдвиг к началу последовательности пока метка завершения текущего блока меньше метки курсора
    for( int index = nearest; index >= 0; index-- ) {
      IHistDataBlock prevBlock = (IHistDataBlock)blocks().get( index );
      if( aTimestamp > prevBlock.endTime() ) {
        break;
      }
      nearest = index;
      block = prevBlock;
    }
    if( timestamp >= block.startTime() && timestamp <= block.endTime() && block.size() > 0 ) {
      // Попадание в блок после "доводки"
      importBlock = block;
      importBlockIndex = nearest;
      importBlock.setImportTime( aTimestamp );
      return;
    }
    if( timestamp < block.startTime() || block.size() == 0 ) {
      // Курсор оказался "слева" от блока или на пустом блоке. Пробуем получить первое значение в любом следующем,
      // непустом блоке
      for( int index = nearest, n = blocks().size(); index < n; index++ ) {
        block = (IHistDataBlock)blocks().get( index );
        if( block.size() == 0 ) {
          continue;
        }
        importBlock = block;
        importBlockIndex = index;
        // Установка курсора импорта на начало блока!!!
        importBlock.setImportTime( block.startTime() );
        return;
      }
      // Нет больше значений (все блоки пустые)
      return;
    }
    // Невозможно установить курсор импорта значений - в последовательности нет данных
    // logger().error( ERR_MSG_NOT_IMPORT_DATA, this, TimeUtils.timestampToString( aTimestamp ) );
  }

  @Override
  public boolean hasImport() {
    return (importBlock != null && importBlock.hasImport());
  }

  @Override
  public ITemporalValueImporter nextImport() {
    if( !hasImport() ) {
      // Курсор невалиден. Движение невозможно
      throw new TsIllegalStateRtException( ERR_NOT_CURSOR_IMPORT_DATA, this );
    }
    ITemporalValueImporter retValue = importBlock.nextImport();
    // Проверяем есть ли еще значения в блоке. Если нет, то пытаемся переместится на следующий блок
    if( !hasImport() ) {
      // В текущем блоке больше нет значений
      importBlock = null;
      for( int index = importBlockIndex + 1, n = blocks().size(); index < n; index++ ) {
        IHistDataBlock block = (IHistDataBlock)blocks().get( index );
        if( block.size() == 0 ) {
          continue;
        }
        block.setImportTime( block.startTime() );
        if( block.hasImport() ) {
          // Есть возможность перехода на первое значение следующего блока
          importBlock = block;
          importBlockIndex = index;
          break;
        }
      }
    }
    if( importBlock == null ) {
      // Значений больше нет. Устраняем противоречивость
      importBlockIndex = -1;
    }
    return retValue;
  }
}
