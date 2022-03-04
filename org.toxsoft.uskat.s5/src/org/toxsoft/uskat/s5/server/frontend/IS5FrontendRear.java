package org.toxsoft.uskat.s5.server.frontend;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.bricks.events.msg.GenericMessage;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.TsLibUtils;

import ru.uskat.backend.ISkFrontendRear;
import ru.uskat.core.api.users.ISkSession;

/**
 * Фронтенд предоставляемый s5-клиентами
 *
 * @author mvk
 */
public interface IS5FrontendRear
    extends ISkFrontendRear {

  /**
   * Несуществующий фронтенд
   */
  IS5FrontendRear NULL = new InternalNullFrontendRear();

  /**
   * Возвращает идентификатор сессии {@link ISkSession} представляющей {@link IS5FrontendRear}
   *
   * @return {@link Skid} идентификатор сессии {@link ISkSession}.
   */
  Skid sessionID();

  /**
   * Возвращает данные фронтенда
   *
   * @return {@link IS5FrontendData} данные фронтенда
   */
  IS5FrontendData frontendData();
}

/**
 * Реализация несуществующего фронтенда {@link IS5FrontendRear#NULL}.
 */
class InternalNullFrontendRear
    implements IS5FrontendRear, Serializable {

  private static final long serialVersionUID = 157157L;

  private transient ReentrantReadWriteLock lock;
  private transient S5FrontendData         frontendData;

  /**
   * Метод корректно восстанавливает сериализированный {@link IS5FrontendRear#NULL}.
   *
   * @return Object объект {@link IS5FrontendRear#NULL}
   * @throws ObjectStreamException это обявление, оно тут не выбрасывается
   */
  @SuppressWarnings( { "static-method" } )
  private Object readResolve()
      throws ObjectStreamException {
    return IS5FrontendRear.NULL;
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов IS5FrontendRear
  //
  @Override
  public Skid sessionID() {
    return Skid.NONE;
  }

  @Override
  public void onGenericMessage( GenericMessage aMessage ) {
    // nop
  }

  @Override
  public ReentrantReadWriteLock mainLock() {
    if( lock == null ) {
      lock = new ReentrantReadWriteLock();
    }
    return lock;
  }

  @Override
  public IS5FrontendData frontendData() {
    if( frontendData == null ) {
      frontendData = new S5FrontendData();
    }
    return frontendData;
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
    return IS5FrontendRear.class.getSimpleName() + ".NULL"; //$NON-NLS-1$
  }
}
