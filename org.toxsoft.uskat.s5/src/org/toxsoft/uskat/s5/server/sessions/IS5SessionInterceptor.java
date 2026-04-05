package org.toxsoft.uskat.s5.server.sessions;

import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.s5.server.interceptors.*;

import jakarta.ejb.*;

/**
 * Перехватчик операций (создание, удаление, изменение) сессий пользователей подключенных к системе.
 * <p>
 * В отличии от событий {@link SkEvent}, события об операциях {@link IS5SessionInterceptor} передаются в режиме раннего
 * оповещения (в рамках выполняемой транзакции) и позвляют перехватчику повлиять на конечный результат проводимой
 * операции над объектами. Например, клиент(перехватчик) может запретить/отменить выполнение операции.
 * <p>
 * Все методы реализации интерфейса {@link IS5SessionInterceptor} должны быть иметь аннатоцию:
 * &#064;TransactionAttribute( TransactionAttributeType.MANDATORY ).
 *
 * @author mvk
 */
@Local
public interface IS5SessionInterceptor
    extends IS5Interceptor {

  /**
   * Вызывается ДО создания сессии на сервере
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}.
   * @throws TsIllegalStateRtException запретить создание сессии
   */
  void beforeCreateSession( Skid aSessionID );

  /**
   * Вызывается ПОСЛЕ создания сессии, но ДО завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции, но все попытки ее отмены (через поднятие исключения в
   * {@link #afterCloseSession(Skid)}) будут игнорироваться.
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}.
   */
  void afterCreateSession( Skid aSessionID );

  /**
   * Вызывается ДО завершения сессии на сервере
   * <p>
   * Событие формируется в открытой транзакции, но все попытки ее отмены (через поднятие исключения в
   * {@link #beforeCloseSession(Skid)}) будут игнорироваться.
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}.
   */
  void beforeCloseSession( Skid aSessionID );

  /**
   * Вызывается ПОСЛЕ завершения сессии, но ДО завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции, но все попытки ее отмены (через поднятие исключения в
   * {@link #afterCloseSession(Skid)}) будут игнорироваться.
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}.
   */
  void afterCloseSession( Skid aSessionID );

}
