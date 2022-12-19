package org.toxsoft.uskat.s5.server.backend.addons.gwiddb;

import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.uskat.core.ISkServiceCreator;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaGwidDb;
import org.toxsoft.uskat.core.impl.AbstractSkService;
import org.toxsoft.uskat.core.impl.SkCoreServGwidDb;
import org.toxsoft.uskat.s5.server.backend.addons.*;
import org.toxsoft.uskat.s5.server.backend.supports.gwiddb.S5BackendGwidDbSingleton;

/**
 * Построитель расширения бекенда {@link IBaGwidDb} для s5
 *
 * @author mvk
 */
public class S5BaGwidDbCreator
    extends S5AbstractBackendAddonCreator {

  /**
   * Конструктор
   */
  public S5BaGwidDbCreator() {
    super( ISkBackendHardConstant.BAINF_GWID_DB );
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendAddonCreator
  //
  @Override
  protected ISkServiceCreator<? extends AbstractSkService> doGetServiceCreator() {
    return SkCoreServGwidDb.CREATOR;
  }

  @Override
  protected Pair<Class<? extends IS5BackendAddonSession>, Class<? extends IS5BackendAddonSession>> doGetSessionClasses() {
    return new Pair<>( IS5BaGwidDbSession.class, S5BaGwidDbSession.class );
  }

  @Override
  protected IS5BackendAddonLocal doCreateLocal( IS5BackendLocal aOwner ) {
    return new S5BaGwidDbLocal( aOwner );
  }

  @Override
  protected IS5BackendAddonRemote doCreateRemote( IS5BackendRemote aOwner ) {
    return new S5BaGwidDbRemote( aOwner );
  }

  @Override
  protected IStringList doSupportSingletonIds() {
    return new StringArrayList( //
        S5BackendGwidDbSingleton.BACKEND_GWIDDB_ID//
    );
  }

}
