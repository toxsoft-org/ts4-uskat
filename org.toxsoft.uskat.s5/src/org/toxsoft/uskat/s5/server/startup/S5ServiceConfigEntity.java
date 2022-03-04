package org.toxsoft.uskat.s5.server.startup;

import javax.persistence.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;

/**
 * Сущность для хранения конфигурации служб.
 * <p>
 * Сущность устроена просто - поле-идентификатор службы {@link #getServiceId()} и текстовое представление конфигурации
 * {@link IOptionSet} службы - поле {@link #getServiceConfig()}. Текстовое представление создается хранителем
 * {@link OptionSetKeeper#KEEPER}.
 * <p>
 * Эта сущность используется внутри пакета, и не предназначена для использования клиентами.
 *
 * @author goga
 */
@Entity
public class S5ServiceConfigEntity {

  private String serviceId;
  private String serviceConfig;

  /**
   * Пустой конструктор.
   */
  public S5ServiceConfigEntity() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // get/set методы EJB-свойств
  //

  /**
   * Возвращает идентификатор службы.
   *
   * @return String - идентификатор службы
   */
  @Id
  public String getServiceId() {
    return serviceId;
  }

  /**
   * Задает идентификатор службы.
   *
   * @param aServiceId String - идентификатор службы
   */
  public void setServiceId( String aServiceId ) {
    this.serviceId = aServiceId;
  }

  /**
   * Возвращает текстовое представление {@link IOptionSet} конфигурации службы.
   *
   * @return String - текстовое представление {@link IOptionSet} конфигурации службы.
   */
  @Column
  @Lob
  public String getServiceConfig() {
    return serviceConfig;
  }

  /**
   * Задает текстовое представление {@link IOptionSet} конфигурации службы.
   *
   * @param aServiceConfig String - текстовое представление {@link IOptionSet} конфигурации службы.
   */
  public void setServiceConfig( String aServiceConfig ) {
    this.serviceConfig = aServiceConfig;
  }

}
