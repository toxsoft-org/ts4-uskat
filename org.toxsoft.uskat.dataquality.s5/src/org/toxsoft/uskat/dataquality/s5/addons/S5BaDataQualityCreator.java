package org.toxsoft.uskat.dataquality.s5.addons;

import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.uskat.core.ISkServiceCreator;
import org.toxsoft.uskat.core.impl.AbstractSkService;
import org.toxsoft.uskat.dataquality.lib.IBaDataQuality;
import org.toxsoft.uskat.dataquality.lib.ISkDataQualityServiceHardConstants;
import org.toxsoft.uskat.dataquality.lib.impl.SkDataQualityService;
import org.toxsoft.uskat.dataquality.s5.S5DataQualtiyValobjUtils;
import org.toxsoft.uskat.dataquality.s5.supports.S5BackendDataQualitySingleton;
import org.toxsoft.uskat.s5.server.backend.addons.*;

/**
 * Построитель расширения бекенда {@link IBaDataQuality} для s5
 *
 * @author mvk
 */
public class S5BaDataQualityCreator
    extends S5AbstractBackendAddonCreator {

  static {
    // Регистрация хранителей данных
    S5DataQualtiyValobjUtils.registerS5Keepers();
  }

  /**
   * Конструктор
   */
  public S5BaDataQualityCreator() {
    super( ISkDataQualityServiceHardConstants.BAINF_DATA_QUALITY );
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendAddonCreator
  //
  @Override
  protected ISkServiceCreator<? extends AbstractSkService> doGetServiceCreator() {
    return SkDataQualityService.CREATOR;
  }

  @Override
  protected Pair<Class<? extends IS5BackendAddonSession>, Class<? extends IS5BackendAddonSession>> doGetSessionClasses() {
    return new Pair<>( IS5BaDataQualitySession.class, S5BaDataQualitySession.class );
  }

  @Override
  protected IS5BackendAddonLocal doCreateLocal( IS5BackendLocal aOwner ) {
    return new S5BaDataQualityLocal( aOwner );
  }

  @Override
  protected IS5BackendAddonRemote doCreateRemote( IS5BackendRemote aOwner ) {
    return new S5BaDataQualityRemote( aOwner );
  }

  @Override
  protected IStringList doSupportSingletonIds() {
    return new StringArrayList( //
        S5BackendDataQualitySingleton.BACKEND_DATA_QUALITY_ID//
    );
  }

}
