package org.toxsoft.uskat.s5.server.backend.addons.classes;

import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoClassInfo;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionAddonInitResult;

/**
 * Данные результата инициализации сессии расширения бекенда {@link IS5BaClassesSession}.
 *
 * @author mvk
 */
public final class S5BaClassesInitResult
    implements IS5SessionAddonInitResult, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Описания всех классов зарегистрированных в системе на момент подключения к серверу
   */
  public final IStridablesListEdit<IDtoClassInfo> classInfos = new StridablesList<>();
}
