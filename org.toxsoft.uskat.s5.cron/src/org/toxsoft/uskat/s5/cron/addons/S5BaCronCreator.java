package org.toxsoft.uskat.s5.cron.addons;

import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.uskat.core.ISkServiceCreator;
import org.toxsoft.uskat.core.impl.AbstractSkService;
import org.toxsoft.uskat.s5.cron.lib.IBaCrone;
import org.toxsoft.uskat.s5.cron.lib.ISkCronHardConstants;
import org.toxsoft.uskat.s5.cron.lib.impl.SkCronService;
import org.toxsoft.uskat.s5.cron.supports.S5BackendCronSingleton;
import org.toxsoft.uskat.s5.server.backend.addons.*;

/**
 * Построитель расширения бекенда {@link IBaCrone} для s5
 *
 * @author mvk
 */
public class S5BaCronCreator
    extends S5AbstractBackendAddonCreator {

  static {
    // Регистрация хранителей данных
    // SkSchedulesValobjUtils.registerS5Keepers();
  }

  /**
   * Конструктор
   */
  public S5BaCronCreator() {
    super( ISkCronHardConstants.BAINF_CRON );
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendAddonCreator
  //
  @Override
  protected ISkServiceCreator<? extends AbstractSkService> doGetServiceCreator() {
    return SkCronService.CREATOR;
  }

  @Override
  protected Pair<Class<? extends IS5BackendAddonSession>, Class<? extends IS5BackendAddonSession>> doGetSessionClasses() {
    return new Pair<>( IS5BaCronSession.class, S5BaCronSession.class );
  }

  @Override
  protected IS5BackendAddonLocal doCreateLocal( IS5BackendLocal aOwner ) {
    return new S5BaCronLocal( aOwner );
  }

  @Override
  protected IS5BackendAddonRemote doCreateRemote( IS5BackendRemote aOwner ) {
    return new S5BaCronRemote( aOwner );
  }

  @Override
  protected IStringList doSupportSingletonIds() {
    return new StringArrayList( //
        S5BackendCronSingleton.BACKEND_SCHEDULES_ID//
    );
  }

}
