package org.toxsoft.uskat.s5.client.remote.connection;

import org.toxsoft.core.pas.common.IPasTxChannel;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.backend.IS5BackendRemote;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddon;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionInitResult;
import org.toxsoft.uskat.s5.server.sessions.init.S5SessionInitData;

/**
 * Содинение с сервером.
 *
 * @author mvk
 */
public interface IS5Connection {

  /**
   * Возвращает текущее состояние соединения
   *
   * @return {@link EConnectionState} состояние соединения
   */
  EConnectionState state();

  /**
   * Возвращает описания расширений {@link IS5BackendAddon} бекенда поддерживаемых сервером
   *
   * @return {@link IStringMap}&lt;String&gt; карта описания расширений.
   *         <p>
   *         Ключ: идентификатор расширения {@link IS5BackendAddon#id()};<br>
   *         Значение: полное имя java-класса реализующий расширение {@link IS5BackendAddon};<br>
   * @throws TsIllegalStateRtException нет связи с сервером или конфигурация не получена
   */
  IStringMap<String> backendAddonInfos();

  /**
   * Возвращает данные инициализации сессии
   *
   * @return {@link S5SessionInitData} данные для инициализации
   */
  S5SessionInitData sessionInitData();

  /**
   * Возвращает результаты инициализации сессии
   *
   * @return {@link IS5SessionInitResult} результаты инициализации
   */
  IS5SessionInitResult sessionInitResult();

  /**
   * Возвращает удаленную ссылку на s5-backend сервера.
   *
   * @return {@link IS5BackendRemote} - удаленная ссылка s5-backend сервера
   * @throws TsIllegalArgumentRtException соединение находится в неактивном состоянии
   */
  IS5BackendRemote backend();

  /**
   * Возвращает канал передачи сообщений серверу
   *
   * @return {@link IPasTxChannel} канал передачи
   * @throws TsIllegalArgumentRtException нет связи с сервером
   */
  IPasTxChannel callbackTxChannel();

  /**
   * Добавляет слушателя сообщении о открытии/завершении сеанса работы с сервером.
   * <p>
   * Если этот слушатель уже зарегистрирован, то метод ничего не делает.
   *
   * @param aListener {@link IS5ConnectionListener} - слушатель событий
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException соединение завершается (выполняется метод {@link S5Connection#closeSession()}
   */
  void addConnectionListener( IS5ConnectionListener aListener );

  /**
   * Удаляет слушателя сообщении о открытии/завершении сеанса работы с сервером.
   * <p>
   * Если этот слушатель не был зарегистрирован, то метод ничего не делает.
   *
   * @param aListener {@link IS5ConnectionListener} -слушатель событий
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException соединение завершается (выполняется метод {@link S5Connection#closeSession()}
   */
  void removeConnectionListener( IS5ConnectionListener aListener );

}
