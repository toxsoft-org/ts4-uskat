package org.toxsoft.uskat.s5.server.frontend;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.core.backend.ISkFrontendRear;

/**
 * Фронтенд предоставляемый s5-клиентами
 * <p>
 * TODO: не попросить ли goga сделать ISkFrontendRear extends IGtMessageEventCapable, что будет означать что соообщения
 * идут не только от бекенда к фронтенду, но и в обратном направлении
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
   * Возвращает признак того, что фронтенд представляет локального клиента
   *
   * @return boolean <b>true</b> фронтенд локального клиента;<b>false</b> клиент удаленного клиента
   */
  boolean isLocal();

  /**
   * Возвращает данные фронтенда
   *
   * @return {@link IS5FrontendData} данные фронтенда
   */
  IS5FrontendData frontendData();

  /**
   * Возвращает механизм широковещательной рассылки сообщений от фронтенда к бекенду и всем фронтендам
   *
   * @return {@link IGtMessageEventer} - механизм рассылки
   */
  IGtMessageEventer broadcastEventer();

  /**
   * Возвращает механизм рассылки сообщений от фронтенда бекенду
   *
   * @return {@link IGtMessageEventer} - механизм рассылки
   */
  IGtMessageEventer frontendEventer();
}

/**
 * Реализация несуществующего фронтенда {@link IS5FrontendRear#NULL}.
 */
class InternalNullFrontendRear
    implements IS5FrontendRear, Serializable {

  private static final long serialVersionUID = 157157L;

  private transient S5FrontendData    frontendData;
  private transient IGtMessageEventer siblingsEventer;
  private transient IGtMessageEventer frontendEventer;

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
  public boolean isLocal() {
    return true;
  }

  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    // nop
  }

  @Override
  public IS5FrontendData frontendData() {
    if( frontendData == null ) {
      frontendData = new S5FrontendData();
    }
    return frontendData;
  }

  @Override
  public IGtMessageEventer broadcastEventer() {
    if( siblingsEventer == null ) {
      siblingsEventer = new GtMessageEventer();
    }
    return siblingsEventer;
  }

  @Override
  public IGtMessageEventer frontendEventer() {
    if( frontendEventer == null ) {
      frontendEventer = new GtMessageEventer();
    }
    return frontendEventer;
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
