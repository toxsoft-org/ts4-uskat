package org.toxsoft.uskat.s5.schedules.addons;

import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.uskat.core.ISkServiceCreator;
import org.toxsoft.uskat.core.impl.AbstractSkService;
import org.toxsoft.uskat.s5.schedules.lib.IBaSchedules;
import org.toxsoft.uskat.s5.schedules.lib.ISkSchedulesHardConstants;
import org.toxsoft.uskat.s5.schedules.lib.impl.SkScheduleService;
import org.toxsoft.uskat.s5.schedules.supports.S5BackendSchedulesSingleton;
import org.toxsoft.uskat.s5.server.backend.addons.*;

/**
 * Построитель расширения бекенда {@link IBaSchedules} для s5
 *
 * @author mvk
 */
public class S5BaSchedulesCreator
    extends S5AbstractBackendAddonCreator {

  static {
    // Регистрация хранителей данных
    // SkSchedulesValobjUtils.registerS5Keepers();
  }

  /**
   * Конструктор
   */
  public S5BaSchedulesCreator() {
    super( ISkSchedulesHardConstants.BAINF_SCHEDULES );
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendAddonCreator
  //
  @Override
  protected ISkServiceCreator<? extends AbstractSkService> doGetServiceCreator() {
    return SkScheduleService.CREATOR;
  }

  @Override
  protected Pair<Class<? extends IS5BackendAddonSession>, Class<? extends IS5BackendAddonSession>> doGetSessionClasses() {
    return new Pair<>( IS5BaSchedulesSession.class, S5BaSchedulesSession.class );
  }

  @Override
  protected IS5BackendAddonLocal doCreateLocal( IS5BackendLocal aOwner ) {
    return new S5BaSchedulesLocal( aOwner );
  }

  @Override
  protected IS5BackendAddonRemote doCreateRemote( IS5BackendRemote aOwner ) {
    return new S5BaSchedulesRemote( aOwner );
  }

  @Override
  protected IStringList doSupportSingletonIds() {
    return new StringArrayList( //
        S5BackendSchedulesSingleton.BACKEND_SCHEDULES_ID//
    );
  }

}
