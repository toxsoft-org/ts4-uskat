package org.toxsoft.uskat.s5.server.backend.supports.histdata.sequences;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.toxsoft.core.tslib.av.errors.AvUnassignedValueRtException;
import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.utils.TsLibUtils;

/**
 * Способ получения значений с метками времени
 *
 * @author mvk
 */
public interface ITemporalValueImporter
    extends IValueImporter {

  /**
   * "Нулевой", несуществующий импортер значений , всместо использования null.
   * <p>
   */
  ITemporalValueImporter NULL = new InternalTemporalValueImporter();

  /**
   * Возвращает метку времени значения.
   *
   * @return long - метка времени сущности (в миллисекундах с начала эпохи)
   */
  long timestamp();

  /**
   * Определяет, установлено ли значение по метке времени {@link #timestamp()}.
   * <p>
   * Обращение к методам чтения {@link IValueImporter} неустановленного занчения будет приводить к исключению
   * {@link AvUnassignedValueRtException}.
   *
   * @return boolean - признак того, что значение установлено<br>
   *         <b>true</b> - значение установлено и которое может быть считано;<br>
   *         <b>false</b> - значение не установлено и попытка чтения приведет к ошибке.
   */
  boolean isAssigned();
}

class InternalTemporalValueImporter
    implements ITemporalValueImporter, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Метод корректно восстанавливает сериализированный {@link ITemporalValueImporter#NULL}.
   *
   * @return Object объект {@link ITemporalValueImporter#NULL}
   * @throws ObjectStreamException это обявление, оно тут не выбрасывается
   */
  @SuppressWarnings( { "static-method" } )
  private Object readResolve()
      throws ObjectStreamException {
    return ITemporalValueImporter.NULL;
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов ITemporalValueImporter
  //
  @Override
  public long timestamp() {
    return TimeUtils.MIN_TIMESTAMP;
  }

  @Override
  public boolean isAssigned() {
    return false;
  }

  @Override
  public boolean asBool() {
    throw new AvUnassignedValueRtException();
  }

  @Override
  public int asInt() {
    throw new AvUnassignedValueRtException();
  }

  @Override
  public long asLong() {
    throw new AvUnassignedValueRtException();
  }

  @Override
  public float asFloat() {
    throw new AvUnassignedValueRtException();
  }

  @Override
  public double asDouble() {
    throw new AvUnassignedValueRtException();
  }

  @Override
  public String asString() {
    return toString();
  }

  @Override
  public <T> T asValobj() {
    throw new AvUnassignedValueRtException();
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов Object
  //
  @Override
  public int hashCode() {
    return TsLibUtils.INITIAL_HASH_CODE;
  }

  @Override
  public boolean equals( Object obj ) {
    return obj == this;
  }

  @Override
  public String toString() {
    return ITemporalValueImporter.class.getSimpleName() + ".NULL"; //$NON-NLS-1$
  }

}
