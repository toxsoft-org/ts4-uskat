package org.toxsoft.uskat.s5.server.startup;

import javax.ejb.Local;

import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.uskat.core.backend.api.ISkBackendInfo;
import org.toxsoft.uskat.s5.server.IS5ImplementConstants;
import org.toxsoft.uskat.s5.server.IS5ServerHardConstants;

/**
 * Начальная, неизменяемая, проектно-зависимая конфигурация реализации бекенда сервера
 * <p>
 * Все параметры конфигурации {@link IParameterized} устанавливаются в параметрах бекенда
 * {@link ISkBackendInfo#params()}. Перечень и описание параметров находится в {@link IS5ServerHardConstants}.
 * <p>
 * В системе (в конечном проекте) должна существовать одна реализация синглетона этого интерфейса с именем
 * {@link IS5ImplementConstants#PROJECT_INITIAL_IMPLEMENT_SINGLETON}. Для упрощения реализации синглетона
 * {@link IS5ImplementConstants#PROJECT_INITIAL_IMPLEMENT_SINGLETON} может быть использована абстрактная реализация
 * {@link S5InitialImplementSingleton}.
 *
 * @author mvk
 */
@Local
public interface IS5InitialImplementSingleton {

  /**
   * Возвращает начальную, неизменяемая, проектно-зависимая конфигурация реализации бекенда сервера
   *
   * @return {@link IS5InitialImplementation} конфигурация реализации
   */
  IS5InitialImplementation impl();
}
