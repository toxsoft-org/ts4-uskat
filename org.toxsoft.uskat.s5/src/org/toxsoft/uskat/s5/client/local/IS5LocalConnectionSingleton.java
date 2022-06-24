package org.toxsoft.uskat.s5.client.local;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.ejb.Local;

import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.core.impl.ISkCoreConfigConstants;

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
   * @param aArgs {@link ITsContextRo} дополнительные параметры для создания соединения, например,
   *          {@link ISkCoreConfigConstants#REFDEF_USER_SERVICES}.
   * @param aLock {@link ReentrantReadWriteLock} внешняя блокировка используемая соединением
   * @return {@link ISkConnection} соединение с сервером
   * @throws TsNullArgumentRtException аргумент = null
   */
  ISkConnection open( String aModuleName, ITsContextRo aArgs, ReentrantReadWriteLock aLock );
}
