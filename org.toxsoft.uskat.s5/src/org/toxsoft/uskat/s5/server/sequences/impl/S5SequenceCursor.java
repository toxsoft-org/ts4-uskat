package org.toxsoft.uskat.s5.server.sequences.impl;

import org.toxsoft.core.tslib.bricks.time.ITemporal;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.IS5HistDataBlock;
import org.toxsoft.uskat.s5.server.sequences.*;

/**
 * Базовая реализация {@link IS5SequenceCursor}
 *
 * @author mvk
 * @param <T> тип значений последовательности
 */
public class S5SequenceCursor<T extends ITemporal<?>>
    implements IS5SequenceCursor<T> {

  private final IS5Sequence<?>      sequence;
  private final IS5SequenceBlock<?> block;
  private int                       nextBlockIndex;
  private int                       nextValueIndex;
  private IS5SequenceBlock<?>       nextBlock;
  private IS5SequenceBlock<?>       currentBlock;
  private int                       currentValueIndex;
  private int                       position = 0;

  /**
   * Создание курсора для последовательности значений
   *
   * @param aSequence {@link IS5Sequence} последовательность значений
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5SequenceCursor( IS5Sequence<?> aSequence ) {
    TsNullArgumentRtException.checkNull( aSequence );
    sequence = aSequence;
    block = (sequence.blocks().size() > 0 ? (IS5HistDataBlock)sequence.blocks().get( 0 ) : null);
    setTime( aSequence.interval().startTime() );
  }

  /**
   * Создание курсора для блока значений
   *
   * @param aBlock {@link IS5SequenceBlock} блок значений
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5SequenceCursor( IS5SequenceBlock<?> aBlock ) {
    TsNullArgumentRtException.checkNull( aBlock );
    sequence = null;
    block = aBlock;
    setTime( block.startTime() );
  }

  // ------------------------------------------------------------------------------------
  // Открытое API (для наследников)
  //
  /**
   * Возвращает текущий блок
   *
   * @param <B> тип блока
   * @return {@link IS5SequenceBlock} текущий блок. null: нет текущего блока
   */
  @SuppressWarnings( "unchecked" )
  public final <B extends IS5SequenceBlock<?>> B currentBlock() {
    return (B)currentBlock;
  }

  /**
   * Возвращает индекс текущего значения в текущем блоке
   *
   * @return int индекс значения. < 0: нет значения
   */
  public final int currentValueIndex() {
    return currentValueIndex;
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendHistDataCursor
  //
  @Override
  public final void setTime( long aFromTime ) {
    nextBlock = block;
    nextBlockIndex = -1;
    nextValueIndex = -1;
    if( sequence != null ) {
      for( int index = 0, n = sequence.blocks().size(); index < n; index++ ) {
        IS5SequenceBlock<?> b = sequence.blocks().get( index );
        if( aFromTime <= b.endTime() ) {
          nextBlock = block;
          nextBlockIndex = index;
          break;
        }
      }
    }
    if( nextBlock != null ) {
      long time = aFromTime;
      if( time < nextBlock.startTime() ) {
        time = nextBlock.startTime();
      }
      if( nextBlock.endTime() < time ) {
        time = nextBlock.endTime();
      }
      nextValueIndex = nextBlock.firstByTime( time );
    }
    position = 0;
  }

  @Override
  public final boolean hasNextValue() {
    return (nextBlock != null && nextValueIndex >= 0);
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public final T nextValue() {
    TsIllegalArgumentRtException.checkFalse( hasNextValue() );
    currentBlock = nextBlock;
    currentValueIndex = nextValueIndex;
    position++;
    if( nextValueIndex + 1 < nextBlock.size() ) {
      // Перемещение по текущему блоку
      nextValueIndex++;
      return (T)this;
    }
    if( sequence != null ) {
      // Попытка найти следующий блок в последовательности со значениями
      for( int index = nextBlockIndex + 1, n = sequence.blocks().size(); index < n; index++ ) {
        IS5SequenceBlock<?> b = sequence.blocks().get( index );
        if( b.size() > 0 ) {
          nextBlock = b;
          nextBlockIndex = index;
          nextValueIndex = 0;
          return (T)this;
        }
      }
    }
    // Больше нет значений
    nextBlock = null;
    nextBlockIndex = -1;
    nextValueIndex = -1;
    return (T)this;
  }

  @Override
  public final int position() {
    return position;
  }

}
