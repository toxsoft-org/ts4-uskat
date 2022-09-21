package org.toxsoft.uskat.s5.server.backend.addons;

import javax.ejb.Local;

import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSessionControl;
import org.toxsoft.uskat.s5.server.backend.IS5Verifiable;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionInitData;
import org.toxsoft.uskat.s5.server.sessions.init.S5SessionInitResult;
import org.toxsoft.uskat.s5.server.sessions.pas.S5SessionMessenger;

/**
 * Управление сессией расширения бекенда. Работает только в локальном доступе к серверу (в том же процессе что и сервер)
 *
 * @author mvk
 */
@Local
public interface IS5BackendAddonSessionControl
    extends IStridable, IS5Verifiable, ICloseable {

  /**
   * Удаленный доступ к сессии расширения backend
   *
   * @return {@link IS5BackendAddonSession} удаленный доступ к сессии
   */
  IS5BackendAddonSession session();

  /**
   * Локальный доступ к расширению backend
   *
   * @return {@link IS5BackendAddonSessionControl} локальный доступ
   */
  IS5BackendAddonSessionControl control();

  /**
   * Инициализация расширения
   *
   * @param aBackend {@link IS5BackendSessionControl} сессия предоставляющая backend ядра s5-сервера
   * @param aMessenger {@link S5SessionMessenger} приемопередатчик сообщений сессии
   * @param aInitData {@link IS5SessionInitData} данные для инициализации сессии
   * @param aInitResult {@link S5SessionInitResult} результаты инициализации сессии
   * @throws TsNullArgumentRtException аргумент = null
   */
  void init( IS5BackendSessionControl aBackend, S5SessionMessenger aMessenger, IS5SessionInitData aInitData,
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
