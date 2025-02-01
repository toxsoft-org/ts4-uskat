package org.toxsoft.uskat.classes;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.uskat.core.*;

/**
 * Класс: сервер.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface ISkServer
    extends ISkNetNode {

  /**
   * Идентификатор класса.
   */
  String CLASS_ID = ISkHardConstants.SK_ID + ".Server";

  /**
   * Параметр события: логин пользователя.
   * <p>
   * Параметр имеет значение {@link EAtomicType#STRING}.
   */
  String EVPID_LOGIN = "login"; //$NON-NLS-1$

  /**
   * Параметр события: IP-адрес пользователя.
   * <p>
   * Параметр имеет значение {@link EAtomicType#STRING}.
   */
  String EVPID_IP = "IP"; //$NON-NLS-1$

  /**
   * Параметр события: идентификатор сессии ({@link ISkSession#skid()}).
   * <p>
   * Параметр имеет значение {@link EAtomicType#VALOBJ} (Skid).
   */
  String EVPID_SESSION_ID = "sessionID"; //$NON-NLS-1$

  /**
   * Идентификатор события: "Неудачная попытка создания сессии пользователя".
   * <p>
   * Параметры:
   * <ul>
   * <li>{@link #EVPID_LOGIN}.</li>.
   * <li>{@link #EVPID_IP}.</li>.
   * </ul>
   */
  String EVID_LOGIN_FAILED = "LoginFailed"; //$NON-NLS-1$

  /**
   * Идентификатор события: "Создание сессии пользователя".
   * <p>
   * Параметры:
   * <ul>
   * <li>{@link #EVPID_LOGIN};</li>.
   * <li>{@link #EVPID_IP}.</li>.
   * <li>{@link #EVPID_SESSION_ID};</li>.
   * </ul>
   */
  String EVID_SESSION_CREATED = "SessionCreated"; //$NON-NLS-1$

  /**
   * Идентификатор события: "Завершение сессии пользователя".
   * <p>
   * Параметры:
   * <ul>
   * <li>{@link #EVPID_LOGIN};</li>.
   * <li>{@link #EVPID_IP}.</li>.
   * <li>{@link #EVPID_SESSION_ID};</li>.
   * </ul>
   */
  String EVID_SESSION_CLOSED = "SessionClosed"; //$NON-NLS-1$

  /**
   * Идентификатор события: "Обрыв сессии пользователя".
   * <p>
   * Параметры:
   * <ul>
   * <li>{@link #EVPID_LOGIN};</li>.
   * <li>{@link #EVPID_IP}.</li>.
   * <li>{@link #EVPID_SESSION_ID};</li>.
   * </ul>
   */
  String EVID_SESSION_BREAKED = "SessionBreaked"; //$NON-NLS-1$

  /**
   * Идентификатор события: "Восстановление сессии пользователя".
   * <p>
   * Параметры:
   * <ul>
   * <li>{@link #EVPID_LOGIN};</li>.
   * <li>{@link #EVPID_IP}.</li>.
   * <li>{@link #EVPID_SESSION_ID};</li>.
   * </ul>
   */
  String EVID_SESSION_RESTORED = "SessionRestored"; //$NON-NLS-1$

  // ------------------------
  /**
   * Параметр события: пользователь системы (login).
   * <p>
   * Параметр имеет значение {@link EAtomicType#STRING}.
   */
  String EVPID_USER = "user"; //$NON-NLS-1$

  /**
   * Параметр события: описание изменений (вводится пользователем).
   * <p>
   * Параметр имеет значение {@link EAtomicType#STRING}.
   */
  String EVPID_DESCR = "description"; //$NON-NLS-1$

  /**
   * Параметр события: описание редактора.
   * <p>
   * Параметр имеет значение {@link EAtomicType#STRING}.
   */
  String EVPID_EDITOR = "editor"; //$NON-NLS-1$

  /**
   * Идентификатор события: "Изменено системное описание сервера".
   * <p>
   * Параметры:
   * <ul>
   * <li>{@link #EVPID_USER}.</li>.
   * <li>{@link #EVPID_DESCR}.</li>.
   * <li>{@link #EVPID_EDITOR}.</li>.
   * </ul>
   */
  String EVID_SYSDESCR_CHANGED = "SysdescrChanged"; //$NON-NLS-1$

}
