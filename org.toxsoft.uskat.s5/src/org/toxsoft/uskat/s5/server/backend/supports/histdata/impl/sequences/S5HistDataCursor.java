package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.time.ITemporalValue;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5HistDataSequence;
import org.toxsoft.uskat.s5.server.backend.supports.queries.IS5BackendHistDataCursor;
import org.toxsoft.uskat.s5.server.sequences.IS5Sequence;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceBlock;

/**
 * Реализация {@link IS5BackendHistDataCursor}
 *
 * @author mvk
 */
public class S5HistDataCursor
    implements IS5BackendHistDataCursor, ITemporalAtomicValue {

  private final IAtomicValue   atomicValue = new AtomicValueCursor();
  private final IS5Sequence<?> sequence;
  private IS5SequenceBlock<?>  block;
  private int                  blockIndex;
  private int                  valueIndex;
  private int                  position    = 0;

  /**
   * Создание курсора для последовательности значений
   *
   * @param aSequence {@link IS5HistDataSequence} последовательность значений
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5HistDataCursor( IS5HistDataSequence aSequence ) {
    TsNullArgumentRtException.checkNull( aSequence );
    sequence = aSequence;
    block = (sequence.blocks().size() > 0 ? (IS5HistDataBlock)sequence.blocks().get( 0 ) : null);
    setTime( aSequence.interval().startTime() );
    // blockIndex = (sequence.blocks().size() - 1);
    // valueIndex = (block != null ? block.size() - 1 : -1);
  }

  /**
   * Создание курсора для блока значений
   *
   * @param aBlock {@link IS5HistDataBlock} блок значений
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5HistDataCursor( IS5HistDataBlock aBlock ) {
    TsNullArgumentRtException.checkNull( aBlock );
    sequence = null;
    block = aBlock;
    setTime( block.startTime() );
    // blockIndex = -1;
    // valueIndex = (aBlock.size() - 1);
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendHistDataCursor
  //
  @Override
  public void setTime( long aFromTime ) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean hasNextValue() {
    if( valueIndex < block.size() ) {
      return true;
    }
    return (sequence != null ? blockIndex < sequence.blocks().size() : false);
  }

  @Override
  public ITemporalAtomicValue nextValue() {
    TsIllegalArgumentRtException.checkFalse( hasNextValue() );
    position++;
    if( valueIndex + 1 < block.size() ) {
      valueIndex++;
      return this;
    }
    if( sequence == null ) {
      throw new TsInternalErrorRtException();
    }
    valueIndex = 0;
    for( int index = blockIndex + 1, n = sequence.blocks().size(); index < n; index++ ) {
      IS5SequenceBlock<?> nextBlock = sequence.blocks().get( index );
      if( nextBlock.size() > 0 ) {
        blockIndex = index;
        return this;
      }
    }
    throw new TsInternalErrorRtException();
  }

  @Override
  public int position() {
    return position;
  }

  // ------------------------------------------------------------------------------------
  // ITemporalAtomicValue
  //
  @Override
  public long timestamp() {
    return block.timestamp( valueIndex );
  }

  @Override
  public IAtomicValue value() {
    return atomicValue;
  }

  @Override
  public int compareTo( ITemporalValue<IAtomicValue> aO ) {
    // TODO Auto-generated method stub
    return 0;
  }

  @SuppressWarnings( "unchecked" )
  protected final <T extends IS5SequenceBlock<?>> T block() {
    return (T)block;
  }

  protected final int valueIndex() {
    return valueIndex;
  }

  // ------------------------------------------------------------------------------------
  // Курсор атомарного значения
  //
  private final class AtomicValueCursor
      implements IAtomicValue {

    // ------------------------------------------------------------------------------------
    // ITemporalAtomicValue
    //
    @Override
    public int compareTo( IAtomicValue aO ) {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public boolean isAssigned() {
      return block().isAssigned( valueIndex() );
    }

    @Override
    public EAtomicType atomicType() {
      return ((IS5HistDataBlock)block()).atomicType();
    }

    @Override
    public boolean asBool() {
      return ((IS5HistDataBlock)block()).asBool( valueIndex() );
    }

    @Override
    public int asInt() {
      return ((IS5HistDataBlock)block()).asInt( valueIndex() );
    }

    @Override
    public long asLong() {
      return ((IS5HistDataBlock)block()).asLong( valueIndex() );
    }

    @Override
    public float asFloat() {
      return ((IS5HistDataBlock)block()).asFloat( valueIndex() );
    }

    @Override
    public double asDouble() {
      return ((IS5HistDataBlock)block()).asDouble( valueIndex() );
    }

    @Override
    public String asString() {
      return ((IS5HistDataBlock)block()).asString( valueIndex() );
    }

    @Override
    public <T> T asValobj() {
      return ((IS5HistDataBlock)block()).asValobj( valueIndex() );
    }
  }

  // ------------------------------------------------------------------------------------
  // Object
  //
  @Override
  public String toString() {
    // TODO Auto-generated method stub
    return super.toString();
  }

  @Override
  public boolean equals( Object aObj ) {
    // TODO Auto-generated method stub
    return super.equals( aObj );
  }

  @Override
  public int hashCode() {
    // TODO Auto-generated method stub
    return super.hashCode();
  }

}
