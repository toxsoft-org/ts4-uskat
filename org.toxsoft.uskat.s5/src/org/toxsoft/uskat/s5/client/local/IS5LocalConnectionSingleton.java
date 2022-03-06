package org.toxsoft.uskat.s5.client.local;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.ejb.Local;

import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.core.api.ISkExtServicesProvider;
import ru.uskat.core.connection.ISkConnection;

/**
 * Поставщик соединений {@link ISkConnection} с локальным сервером.
 *
 * @author mvk
 */
@Local
public interface IS5LocalConnectionSingleton {

  /**
   * Открывает новое соединение с локальным сервером
   * <p>
   * После завершения работы с соединением, клиент обязан вызвать {@link ISkConnection#close()}.
   *
   * @param aModuleName String имя модуля создающего подключение к серверу
   * @return {@link ISkConnection} соединение с сервером
   * @throws TsNullArgumentRtException аргумент = null
   */
  ISkConnection open( String aModuleName );

  /**
   * Открывает новое соединение с локальным сервером
   * <p>
   * После завершения работы с соединением, клиент обязан вызвать {@link ISkConnection#close()}.
   *
   * @param aModuleName String имя модуля создающего подключение к серверу
   * @param aExtServiceProvider String полное имя класса поставщика расширений API {@link ISkExtServicesProvider}.
   *          Пустая строка: не используется
   * @return {@link ISkConnection} соединение с сервером
   * @throws TsNullArgumentRtException аргумент = null
   */
  ISkConnection open( String aModuleName, String aExtServiceProvider );

  /**
   * Открывает новое соединение с локальным сервером
   * <p>
   * После завершения работы с соединением, клиент обязан вызвать {@link ISkConnection#close()}.
   *
   * @param aModuleName String имя модуля создающего подключение к серверу
   * @param aExtServiceProvider String полное имя класса поставщика расширений API {@link ISkExtServicesProvider}.
   *          Пустая строка: не используется
   * @param aLock {@link ReentrantReadWriteLock} внешняя блокировка используемая соединением
   * @return {@link ISkConnection} соединение с сервером
   * @throws TsNullArgumentRtException аргумент = null
   */
  ISkConnection open( String aModuleName, String aExtServiceProvider, ReentrantReadWriteLock aLock );
}
