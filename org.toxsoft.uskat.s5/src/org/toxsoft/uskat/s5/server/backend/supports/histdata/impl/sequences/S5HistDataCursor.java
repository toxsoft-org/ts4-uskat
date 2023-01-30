package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences;

import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.time.ITemporalValue;
import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNotAllEnumsUsedRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5BackendHistDataCursor;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5HistDataSequence;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceCursor;

/**
 * Реализация {@link IS5BackendHistDataCursor}
 *
 * @author mvk
 */
public class S5HistDataCursor
    extends S5SequenceCursor<ITemporalAtomicValue>
    implements IS5BackendHistDataCursor, ITemporalAtomicValue {

  /**
   * Textual representation of the constant is the string "@{}".
   */
  static final String KTOR = "" + CHAR_VALOBJ_PREFIX + CHAR_SET_BEGIN + CHAR_SET_END; //$NON-NLS-1$

  private final IAtomicValue atomicValue = new AtomicValueCursor();

  /**
   * Создание курсора для последовательности значений
   *
   * @param aSequence {@link IS5HistDataSequence} последовательность значений
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5HistDataCursor( IS5HistDataSequence aSequence ) {
    super( aSequence );
  }

  /**
   * Создание курсора для блока значений
   *
   * @param aBlock {@link IS5HistDataBlock} блок значений
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5HistDataCursor( IS5HistDataBlock aBlock ) {
    super( aBlock );
  }

  // ------------------------------------------------------------------------------------
  // ITemporalAtomicValue
  //
  @Override
  public long timestamp() {
    return currentBlock().timestamp( currentValueIndex() );
  }

  @Override
  public IAtomicValue value() {
    return atomicValue;
  }

  // ------------------------------------------------------------------------------------
  // Курсор атомарного значения
  //
  private final class AtomicValueCursor
      implements IAtomicValue {

    // ------------------------------------------------------------------------------------
    // IAtomicValue
    //
    @Override
    public boolean isAssigned() {
      return currentBlock().isAssigned( currentValueIndex() );
    }

    @Override
    public EAtomicType atomicType() {
      return ((IS5HistDataBlock)currentBlock()).atomicType();
    }

    @Override
    public boolean asBool() {
      return ((IS5HistDataBlock)currentBlock()).asBool( currentValueIndex() );
    }

    @Override
    public int asInt() {
      return ((IS5HistDataBlock)currentBlock()).asInt( currentValueIndex() );
    }

    @Override
    public long asLong() {
      return ((IS5HistDataBlock)currentBlock()).asLong( currentValueIndex() );
    }

    @Override
    public float asFloat() {
      return ((IS5HistDataBlock)currentBlock()).asFloat( currentValueIndex() );
    }

    @Override
    public double asDouble() {
      return ((IS5HistDataBlock)currentBlock()).asDouble( currentValueIndex() );
    }

    @Override
    public String asString() {
      return ((IS5HistDataBlock)currentBlock()).asString( currentValueIndex() );
    }

    @Override
    public <T> T asValobj() {
      return ((IS5HistDataBlock)currentBlock()).asValobj( currentValueIndex() );
    }

    // ------------------------------------------------------------------------------------
    // Object
    //
    @Override
    public String toString() {
      if( !isAssigned() ) {
        return "UNASSIGNED"; //$NON-NLS-1$
      }
      switch( atomicType() ) {
        case NONE:
          return KTOR;
        case BOOLEAN:
          return Boolean.toString( asBool() );
        case FLOATING:
          return Double.toString( asFloat() );
        case INTEGER:
          return Integer.toString( asInt() );
        case TIMESTAMP:
          return Long.toString( asLong() );
        case STRING:
          return asString();
        case VALOBJ:
          return asValobj().toString();
        default:
          throw new TsNotAllEnumsUsedRtException();
      }
    }

    @Override
    public boolean equals( Object aObj ) {
      if( aObj == this ) {
        return true;
      }
      if( aObj instanceof IAtomicValue that ) {
        EAtomicType atomicType = atomicType();
        if( atomicType != that.atomicType() ) {
          return false;
        }
        switch( atomicType ) {
          case NONE:
            return true;
          case BOOLEAN:
            return asBool() == that.asBool();
          case FLOATING:
            return asFloat() == that.asFloat();
          case INTEGER:
            return asInt() == that.asInt();
          case TIMESTAMP:
            return asLong() == that.asLong();
          case STRING:
            return asString().equals( that.asString() );
          case VALOBJ:
            return asValobj().equals( that.asValobj() );
          default:
            throw new TsNotAllEnumsUsedRtException();
        }
      }
      return false;
    }

    @Override
    public int hashCode() {
      switch( atomicType() ) {
        case NONE:
          return 0;
        case BOOLEAN:
          return (asBool() ? 1 : 0);
        case FLOATING:
          long dblval = Double.doubleToRawLongBits( asFloat() );
          return (int)(dblval ^ (dblval >>> 32));
        case INTEGER:
          return asInt();
        case TIMESTAMP:
          return (int)(asLong() ^ (asLong() >>> 32));
        case STRING:
          return asString().hashCode();
        case VALOBJ:
          return asValobj().hashCode();
        default:
          throw new TsNotAllEnumsUsedRtException();
      }
    }

    // ------------------------------------------------------------------------------------
    // Comparable
    //
    @SuppressWarnings( { "rawtypes", "unchecked" } )
    @Override
    public int compareTo( IAtomicValue aThat ) {
      if( aThat == null ) {
        throw new NullPointerException();
      }
      if( aThat == this ) {
        return 0;
      }
      // different types will be sorted in the order of constants declaration in EAtomicType
      if( atomicType() != aThat.atomicType() ) {
        return atomicType().ordinal() - aThat.atomicType().ordinal();
      }
      switch( atomicType() ) {
        case NONE:
          return -1;
        case BOOLEAN:
          return Boolean.compare( asBool(), aThat.asBool() );
        case FLOATING:
          return Double.compare( asFloat(), aThat.asFloat() );
        case INTEGER:
          return Integer.compare( asInt(), aThat.asInt() );
        case TIMESTAMP:
          return Long.compare( asLong(), aThat.asLong() );
        case STRING:
          return asString().compareTo( aThat.asString() );
        case VALOBJ:
          Object o1 = asValobj();
          Object o2 = aThat.asValobj();
          boolean c1 = o1 instanceof Comparable<?>;
          boolean c2 = o2 instanceof Comparable<?>;
          if( c1 && c2 ) {
            if( o1.getClass().equals( o2.getClass() ) ) {
              return ((Comparable)o1).compareTo( o2 );
            }
            // valobjs of different classes are considered as equals (as uncomparable values)
            return 0;
          }
          // both nulls are considered as equals (includes uncomparable values)
          if( o1 == null && o2 == null ) {
            return 0;
          }
          // null is considered less than any non-null valobj
          return (o1 == null) ? -1 : 1;
        default:
          throw new TsNotAllEnumsUsedRtException();
      }
    }

  }

  // ------------------------------------------------------------------------------------
  // Object
  //
  @Override
  public String toString() {
    return TimeUtils.timestampToString( timestamp() ) + ' '
        + (atomicValue == null ? "<<null>>" : atomicValue.toString()); //$NON-NLS-1$
  }

  @Override
  public boolean equals( Object aObj ) {
    if( aObj == this ) {
      return true;
    }
    if( aObj instanceof ITemporalValue ) {
      ITemporalValue<?> that = (ITemporalValue<?>)aObj;
      if( timestamp() == that.timestamp() ) {
        return this.atomicValue.equals( that.value() );
      }
      return false;
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + (int)(timestamp() ^ (timestamp() >>> 32));
    result = TsLibUtils.PRIME * result + atomicValue.hashCode();
    return result;
  }

  // ------------------------------------------------------------------------------------
  // Comparable
  //
  @Override
  public int compareTo( ITemporalValue<IAtomicValue> aThat ) {
    if( aThat == null ) {
      throw new NullPointerException();
    }
    return Long.compare( timestamp(), aThat.timestamp() );
  }

}
