package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.time.ITemporalValue;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5HistDataSequence;
import org.toxsoft.uskat.s5.server.backend.supports.queries.IS5BackendHistDataCursor;

/**
 * Реализация {@link IS5BackendHistDataCursor}
 *
 * @author mvk
 */
public class S5HistDataCursor
    implements IS5BackendHistDataCursor, ITemporalAtomicValue {

  private final IAtomicValue        atomicValue = new AtomicValueCursor();
  private final IS5HistDataSequence sequence;
  private IS5HistDataBlock          block;

  public S5HistDataCursor( IS5HistDataSequence aSequence ) {
    TsNullArgumentRtException.checkNull( aSequence );
    sequence = aSequence;
    block = (sequence.blocks().size() > 0 ? (IS5HistDataBlock)sequence.blocks().get( 0 ) : null);
  }

  public S5HistDataCursor( IS5HistDataBlock aBlock ) {
    TsNullArgumentRtException.checkNull( aBlock );
    sequence = null;
    block = aBlock;
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
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public ITemporalAtomicValue nextValue() {
    TsIllegalArgumentRtException.checkFalse( hasNextValue() );
    return this;
  }

  @Override
  public int position() {
    // TODO Auto-generated method stub
    return 0;
  }

  // ------------------------------------------------------------------------------------
  // ITemporalAtomicValue
  //
  @Override
  public long timestamp() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public IAtomicValue value() {
    // TODO Auto-generated method stub
    return atomicValue;
  }

  @Override
  public int compareTo( ITemporalValue<IAtomicValue> aO ) {
    // TODO Auto-generated method stub
    return 0;
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
    public EAtomicType atomicType() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public boolean isAssigned() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean asBool() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public int asInt() {
      // TODO Auto-generated method stub
      return block.asInt( 0 );
    }

    @Override
    public long asLong() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public float asFloat() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public double asDouble() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public String asString() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public <T> T asValobj() {
      // TODO Auto-generated method stub
      return null;
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
