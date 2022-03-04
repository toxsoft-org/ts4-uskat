package org.toxsoft.uskat.s5.client.remote.connection;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullObjectErrorRtException;
import org.toxsoft.uskat.s5.common.S5HostList;

/**
 * Адрес и другая информация для подлючения к серверу s5.
 *
 * @author mvk
 */
public interface IS5ConnectionInfo {

  /**
   * Несуществующее описание соединения
   */
  IS5ConnectionInfo NULL = new InternalNullConnectionInfo();

  /**
   * Возвращает список узлов s5-сервера.
   *
   * @return {@link S5HostList}&gt; список узлов сервера
   */
  S5HostList hosts();

  /**
   * Возвращает таймаут ожидания соединения с сервером по истечении которого принимается решение, что соединение
   * невозможно
   * <p>
   * Значение по умолчанию: <b>3000</b>
   *
   * @return long время (мсек)
   */
  long connectTimeout();

  /**
   * Возвращает таймаут ожидания ответов сервера по истечении которого принимается решение, что произошел обрыв связи с
   * сервером
   * <p>
   * Значение по умолчанию: <b>3000</b>
   *
   * @return long время (мсек)
   */
  long failureTimeout();

  /**
   * Рекомендуемый таймаут передачи текущих данных от сервера удаленным клиентам
   * <p>
   * Значение по умолчанию: <b>1000</b>
   *
   * @return long (мсек) таймаут между передачами значений текущих данных. <=0: отправлять немедленно
   */
  long currDataTimeout();

}

/**
 * Реализация несуществующего описания соединения {@link IS5ConnectionInfo#NULL}.
 */
class InternalNullConnectionInfo
    implements IS5ConnectionInfo, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Метод корректно восстанавливает сериализированный {@link IS5ConnectionInfo#NULL}.
   *
   * @return Object объект {@link IS5ConnectionInfo#NULL}
   * @throws ObjectStreamException это обявление, оно тут не выбрасывается
   */
  @SuppressWarnings( { "static-method" } )
  private Object readResolve()
      throws ObjectStreamException {
    return IS5ConnectionInfo.NULL;
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов IS5ConnectionInfo
  //
  @Override
  public S5HostList hosts() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public long connectTimeout() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public long failureTimeout() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public long currDataTimeout() {
    throw new TsNullObjectErrorRtException();
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
    return IS5ConnectionInfo.class.getSimpleName() + ".NULL"; //$NON-NLS-1$
  }
}
