package org.toxsoft.uskat.s5.server.backend.addons.rtdata;

import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.uskat.core.ISkServiceCreator;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaRtdata;
import org.toxsoft.uskat.core.impl.AbstractSkService;
import org.toxsoft.uskat.core.impl.SkCoreServRtdata;
import org.toxsoft.uskat.s5.server.backend.addons.*;
import org.toxsoft.uskat.s5.server.backend.supports.currdata.impl.S5BackendCurrDataSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.S5BackendHistDataSingleton;

/**
 * Построитель расширения бекенда {@link IBaRtdata} для s5
 *
 * @author mvk
 */
public class S5BaRtdataCreator
    extends S5AbstractBackendAddonCreator {

  /**
   * Конструктор
   */
  public S5BaRtdataCreator() {
    super( ISkBackendHardConstant.BAINF_RTDATA );
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendAddonCreator
  //
  @Override
  protected ISkServiceCreator<? extends AbstractSkService> doGetServiceCreator() {
    return SkCoreServRtdata.CREATOR;
  }

  @Override
  protected Pair<Class<? extends IS5BackendAddonSession>, Class<? extends IS5BackendAddonSession>> doGetSessionClasses() {
    return new Pair<>( IS5BaRtdataSession.class, S5BaRtdataSession.class );
  }

  @Override
  protected IS5BackendAddonLocal doCreateLocal( IS5BackendLocal aOwner ) {
    return new S5BaRtdataLocal( aOwner );
  }

  @Override
  protected IS5BackendAddonRemote doCreateRemote( IS5BackendRemote aOwner ) {
    return new S5BaRtdataRemote( aOwner );
  }

  @Override
  protected IStringList doSupportSingletonIds() {
    return new StringArrayList( //
        S5BackendCurrDataSingleton.BACKEND_CURRDATA_ID, //
        S5BackendHistDataSingleton.BACKEND_HISTDATA_ID//
    );
  }

}
