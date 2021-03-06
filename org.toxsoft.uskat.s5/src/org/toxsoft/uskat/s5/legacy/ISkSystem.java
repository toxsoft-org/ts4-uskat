package org.toxsoft.uskat.s5.legacy;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.uskat.core.api.objserv.ISkObject;

/**
 * Сущность представляющая систему в виде {@link ISkObject}
 *
 * @author mvk
 */
public interface ISkSystem
    extends ISkObject {

  /**
   * The {@link ISkSystem} class identifier.
   */
  String CLASS_ID = "sk.System"; //$NON-NLS-1$

  /**
   * Идентификатор объекта системы.
   */
  String THIS_SYSTEM = "ThisSystem"; //$NON-NLS-1$

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
   * Идентификатор события: "Неудачная попытка создания сессии пользователя".
   * <p>
   * Параметры:
   * <ul>
   * <li>{@link #EVPID_LOGIN}.</li>.
   * <li>{@link #EVPID_IP}.</li>.
   * </ul>
   */
  String EVID_LOGIN_FAILED = "LoginFailed"; //$NON-NLS-1$

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
