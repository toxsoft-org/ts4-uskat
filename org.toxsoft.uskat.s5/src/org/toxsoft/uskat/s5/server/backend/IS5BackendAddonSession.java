package org.toxsoft.uskat.s5.server.backend;

import javax.ejb.Local;

import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionInitData;
import org.toxsoft.uskat.s5.server.sessions.init.S5SessionInitResult;
import org.toxsoft.uskat.s5.server.sessions.pas.S5SessionCallbackWriter;

/**
 * Локальный доступ к расширению backend предоставляемый s5-сервером
 *
 * @author mvk
 */
@Local
public interface IS5BackendAddonSession
    extends IStridable, IS5Verifiable, ICloseable {

  /**
   * Локальный доступ к расширению backend
   *
   * @return {@link IS5BackendAddonSession} локальный доступ
   */
  IS5BackendAddonSession selfLocal();

  /**
   * Удаленный доступ к расширению backend
   *
   * @return {@link IS5BackendAddonRemote} удаленный доступ
   */
  IS5BackendAddonRemote selfRemote();

  /**
   * Инициализация расширения
   *
   * @param aBackend {@link IS5BackendLocal} сессия предоставляющая backend ядра s5-сервера
   * @param aCallbackWriter {@link S5SessionCallbackWriter} передатчик обратных вызовов
   * @param aInitData {@link IS5SessionInitData} данные для инициализации сессии
   * @param aInitResult {@link S5SessionInitResult} результаты инициализации сессии
   * @throws TsNullArgumentRtException аргумент = null
   */
  void init( IS5BackendLocal aBackend, S5SessionCallbackWriter aCallbackWriter, IS5SessionInitData aInitData,
      S5SessionInitResult aInitResult );

  /**
   * Возвращает имя узла кластера на котором работает служба
   *
   * @return String имя узла кластера на котором работает служба
   */
  String nodeName();

  /**
   * Асинхронное удаление сессии пользователя.
   * <p>
   * После вызова этого метода сессионный бин представляющий API сессии будет удален (смотри аннотацию @Remove).
   */
  void removeAsync();
}
