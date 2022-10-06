package org.toxsoft.uskat.s5.server.backend.addons.queries;

import java.io.Serializable;

import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.uskat.core.api.hqserv.ISkAsynchronousQuery;
import org.toxsoft.uskat.core.backend.api.IBaQueries;
import org.toxsoft.uskat.s5.server.frontend.IS5BackendAddonData;

/**
 * Данные конфигурации фронтенда для {@link IBaQueries}.
 *
 * @author mvk
 */
public class S5BaQueriesData
    implements IS5BackendAddonData, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Карта выполняемых запросов
   * <p>
   * Ключ карты: идентификатор запросов {@link ISkAsynchronousQuery#queryId()};<br>
   * Значение карты: конвой объект выполняемого запроса.
   */
  public final IStringMapEdit<S5BaQueriesConvoy> openQueries = new StringMap<>();

}
