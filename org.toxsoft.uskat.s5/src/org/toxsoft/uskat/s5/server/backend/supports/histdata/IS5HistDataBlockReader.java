package org.toxsoft.uskat.s5.server.backend.supports.histdata;

import static org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5Resources.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.errors.AvTypeCastRtException;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceBlockReader;

/**
 * Читатель значений блока исторического данного
 *
 * @author mvk
 */
public interface IS5HistDataBlockReader
    extends IS5SequenceBlockReader {

  /**
   * Возвращает значение по индексу
   *
   * @param aIndex индекс значения в блоке
   * @return boolean значение
   * @throws ArrayIndexOutOfBoundsException индекс значения за границами блока
   * @throws AvTypeCastRtException значение не может быть приведено к запрошенному типу
   */
  default boolean asBool( int aIndex ) {
    throw new AvTypeCastRtException( ERR_CAST_VALUE, this, EAtomicType.BOOLEAN );
  }

  /**
   * Возвращает значение по индексу
   *
   * @param aIndex индекс значения в блоке
   * @return int значение
   * @throws ArrayIndexOutOfBoundsException индекс значения за границами блока
   * @throws AvTypeCastRtException значение не может быть приведено к запрошенному типу
   */
  default int asInt( int aIndex ) {
    throw new AvTypeCastRtException( ERR_CAST_VALUE, this, EAtomicType.INTEGER );
  }

  /**
   * Возвращает значение по индексу
   *
   * @param aIndex индекс значения в блоке
   * @return long значение
   * @throws ArrayIndexOutOfBoundsException индекс значения за границами блока
   * @throws AvTypeCastRtException значение не может быть приведено к запрошенному типу
   */
  default long asLong( int aIndex ) {
    throw new AvTypeCastRtException( ERR_CAST_VALUE, this, EAtomicType.INTEGER );
  }

  /**
   * Возвращает значение по индексу
   *
   * @param aIndex индекс значения в блоке
   * @return float значение
   * @throws ArrayIndexOutOfBoundsException индекс значения за границами блока
   * @throws AvTypeCastRtException значение не может быть приведено к запрошенному типу
   */
  default float asFloat( int aIndex ) {
    throw new AvTypeCastRtException( ERR_CAST_VALUE, this, EAtomicType.FLOATING );
  }

  /**
   * Возвращает значение по индексу
   *
   * @param aIndex индекс значения в блоке
   * @return double значение
   * @throws ArrayIndexOutOfBoundsException индекс значения за границами блока
   * @throws AvTypeCastRtException значение не может быть приведено к запрошенному типу
   */
  default double asDouble( int aIndex ) {
    throw new AvTypeCastRtException( ERR_CAST_VALUE, this, EAtomicType.FLOATING );
  }

  /**
   * Возвращает значение по индексу
   *
   * @param aIndex индекс значения в блоке
   * @return String значение
   * @throws ArrayIndexOutOfBoundsException индекс значения за границами блока
   * @throws AvTypeCastRtException значение не может быть приведено к запрошенному типу
   */
  default String asString( int aIndex ) {
    throw new AvTypeCastRtException( ERR_CAST_VALUE, this, EAtomicType.STRING );
  }

  /**
   * Возвращает значение по индексу
   *
   * @param aIndex индекс значения в блоке
   * @return {@link Object} значение
   * @param <T> тип возвращаемого значения
   * @throws ArrayIndexOutOfBoundsException индекс значения за границами блока
   * @throws AvTypeCastRtException значение не может быть приведено к запрошенному типу
   */
  default <T> T asValobj( int aIndex ) {
    throw new AvTypeCastRtException( ERR_CAST_VALUE, this, EAtomicType.VALOBJ );
  }

}
