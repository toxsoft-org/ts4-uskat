package org.toxsoft.uskat.s5.server.backend.addons.queries;

import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.uskat.core.ISkServiceCreator;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaQueries;
import org.toxsoft.uskat.core.impl.AbstractSkService;
import org.toxsoft.uskat.core.impl.SkCoreServHistQuery;
import org.toxsoft.uskat.s5.server.backend.addons.*;
import org.toxsoft.uskat.s5.server.backend.supports.queries.impl.S5BackendQueriesSingleton;

/**
 * Построитель расширения бекенда {@link IBaQueries} для s5
 *
 * @author mvk
 */
public class S5BaQueriesCreator
    extends S5AbstractBackendAddonCreator {

  /**
   * Конструктор
   */
  public S5BaQueriesCreator() {
    super( ISkBackendHardConstant.BAINF_QUERIES );
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendAddonCreator
  //
  @Override
  protected ISkServiceCreator<? extends AbstractSkService> doGetServiceCreator() {
    return SkCoreServHistQuery.CREATOR;
  }

  @Override
  protected Pair<Class<? extends IS5BackendAddonSession>, Class<? extends IS5BackendAddonSession>> doGetSessionClasses() {
    return new Pair<>( IS5BaQueriesSession.class, S5BaQueriesSession.class );
  }

  @Override
  protected IS5BackendAddonLocal doCreateLocal( IS5BackendLocal aOwner ) {
    return new S5BaQueriesLocal( aOwner );
  }

  @Override
  protected IS5BackendAddonRemote doCreateRemote( IS5BackendRemote aOwner ) {
    return new S5BaQueriesRemote( aOwner );
  }

  @Override
  protected IStringList doSupportSingletonIds() {
    return new StringArrayList( //
        S5BackendQueriesSingleton.BACKEND_QUERIES_ID//
    );
  }

}
