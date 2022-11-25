package org.toxsoft.uskat.alarms.s5.addons;

import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.uskat.alarms.lib.IBaAlarms;
import org.toxsoft.uskat.alarms.lib.ISkAlarmServiceHardConstants;
import org.toxsoft.uskat.alarms.lib.impl.SkAlarmService;
import org.toxsoft.uskat.alarms.s5.S5AlarmValobjUtils;
import org.toxsoft.uskat.alarms.s5.supports.S5BackendAlarmSingleton;
import org.toxsoft.uskat.core.ISkServiceCreator;
import org.toxsoft.uskat.core.impl.AbstractSkService;
import org.toxsoft.uskat.s5.server.backend.addons.*;

/**
 * Построитель расширения бекенда {@link IBaAlarms} для s5
 *
 * @author mvk
 */
public class S5BaAlarmCreator
    extends S5AbstractBackendAddonCreator {

  static {
    // Регистрация хранителей данных
    S5AlarmValobjUtils.registerS5Keepers();
  }

  /**
   * Конструктор
   */
  public S5BaAlarmCreator() {
    super( ISkAlarmServiceHardConstants.BAINF_ALARMS );
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendAddonCreator
  //
  @Override
  protected ISkServiceCreator<? extends AbstractSkService> doGetServiceCreator() {
    return SkAlarmService.CREATOR;
  }

  @Override
  protected Pair<Class<? extends IS5BackendAddonSession>, Class<? extends IS5BackendAddonSession>> doGetSessionClasses() {
    return new Pair<>( IS5BaAlarmSession.class, S5BaAlarmSession.class );
  }

  @Override
  protected IS5BackendAddonLocal doCreateLocal( IS5BackendLocal aOwner ) {
    return new S5BaAlarmLocal( aOwner );
  }

  @Override
  protected IS5BackendAddonRemote doCreateRemote( IS5BackendRemote aOwner ) {
    return new S5BaAlarmRemote( aOwner );
  }

  @Override
  protected IStringList doSupportSingletonIds() {
    return new StringArrayList( //
        S5BackendAlarmSingleton.BACKEND_ALARMS_ID//
    );
  }

}
