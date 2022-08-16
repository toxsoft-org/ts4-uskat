package org.toxsoft.uskat.base.gui.km5;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Поддержка M5-моделирования для Sk-сущностей.
 * <p>
 * Связыват домен моделирования с Sk-соединением так, что при открытом соединении в домене присутствуют модели всех
 * классов из {@link ISkSysdescr#listClasses()}. Часть моделей создаются специализированными модулями
 * {@link KM5AbstractModelManagementUnit}, а все остальные - генерируются по внутренним правилам {@link IKM5Support}.
 * Таким образм, жизненный цикл сущностей подсистемы KM5 по сути связан с жизненным циколм {@link ISkConnection}.
 * <p>
 * Использование подсистемы KM5:
 * <ul>
 * <li>(опционально) после создания экземпляра соединения {@link ISkConnection} зарегистрировать нужные тематические
 * модули, смотрите комментарии к {@link KM5AbstractModelManagementUnit};</li>
 * <li>создать экземпляр {@link KM5Support} методом {@link KM5Utils#createKM5Support()};</li>
 * <li>создать M5-домен и установить его к Sk-соденинению методом {@link #bind(ISkConnection, ITsGuiContext)};</li>
 * <li>использовать функциональность модуля, и наслаждаться :)</li>
 * <li>(опционально)можно по закрытии соединения очистить следы работы с подсистемой, отвязав все тематические модули
 * {@link #getModelManagementUnits()} методами {@link KM5AbstractModelManagementUnit#unbind()} и сам {@link KM5Support}
 * методом {@link #unbind()}.</li>
 * </ul>
 *
 * @author goga
 */
public interface IKM5Support {

  /**
   * Создает домен M5-моделирования и привязывает с Sk-соединением.
   * <p>
   * В качестве контекста домена устанавливаеться аргумент aDomainContext, и родительский домен берется из этого же
   * контекста.
   * <p>
   * В процессе привязки метод размещает ссылки на себя и соединение в {@link IM5Domain#tsContext()}, а ссылки на себя и
   * домен - в {@link ISkConnection#scope()}.
   *
   * @param aSkConn {@link ISkConnection} - Sk-соедниение
   * @param aDomainContext {@link ITsGuiContext} - контекст для инициализации {@link IM5Domain}
   * @return {@link IM5Domain} - созданный для соединения домен M5-моделирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException любой аргумент уже состоит в {@link IKM5Support} связи
   * @throws TsIllegalStateRtException этот экземпляр класса уже обслуживает одн M5-Sk связь
   */
  IM5Domain bind( ISkConnection aSkConn, ITsGuiContext aDomainContext );

  /**
   * Определяет, есть ли привязка M5-Sk.
   *
   * @return boolean - признак наличия привязки M5-Sk
   */
  boolean isBind();

  /**
   * Снимает привязку M5-Sk.
   * <p>
   * Если привязка не была установлена, метод ничего не делает.
   */
  void unbind();

  /**
   * Возвращает домен M5-моделирования.
   *
   * @return {@link IM5Domain} - домен M5-моделирования
   * @throws TsIllegalStateRtException не установлено или уже разорвана привязка M5-Sk
   */
  IM5Domain m5();

  /**
   * Возвращает Sk-соедниение.
   *
   * @return {@link ISkConnection} - Sk-соедниение
   */
  ISkConnection skConn();

  /**
   * Возвращает зарегистрированные тематические модули.
   *
   * @return {@link IStringMap}&lt;{@link KM5AbstractModelManagementUnit}&gt; - карта "ИД" - "модуль"
   * @throws TsIllegalStateRtException не установлено или уже разорвана привязка M5-Sk
   */
  IStringMap<KM5AbstractModelManagementUnit> getModelManagementUnits();

}
