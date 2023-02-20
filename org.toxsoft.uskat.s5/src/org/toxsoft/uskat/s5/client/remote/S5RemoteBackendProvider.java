package org.toxsoft.uskat.s5.client.remote;

import static org.toxsoft.uskat.s5.client.remote.IS5Resources.*;

import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.metainf.ISkBackendMetaInfo;
import org.toxsoft.uskat.core.backend.metainf.SkBackendMetaInfo;
import org.toxsoft.uskat.s5.client.IS5ConnectionParams;

/**
 * Поставщик удаленного s5-backend
 * <p>
 * Предоставляет минимальный backend необходимый для подключения к серверу
 *
 * @author mvk
 */
public class S5RemoteBackendProvider
    implements ISkBackendProvider {

  private static final String ID = S5RemoteBackendProvider.class.getSimpleName();

  /**
   * Конструктор по умолчанию
   */
  public S5RemoteBackendProvider() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISkBackendProvider
  //
  @Override
  public ISkBackendMetaInfo getMetaInfo() {
    SkBackendMetaInfo retValue = new SkBackendMetaInfo( ID, STR_BACKEND_NAME, STR_BACKEND_DESCR );
    retValue.argOps().add( IS5ConnectionParams.OP_USERNAME );
    retValue.argOps().add( IS5ConnectionParams.OP_PASSWORD );
    retValue.argOps().add( IS5ConnectionParams.OP_HOSTS );
    retValue.argOps().add( IS5ConnectionParams.OP_CONNECT_TIMEOUT );
    retValue.argOps().add( IS5ConnectionParams.OP_FAILURE_TIMEOUT );
    retValue.argOps().add( IS5ConnectionParams.OP_CURRDATA_TIMEOUT );
    retValue.argOps().add( IS5ConnectionParams.OP_HISTDATA_TIMEOUT );
    return retValue;
  }

  @Override
  public final ISkBackend createBackend( ISkFrontendRear aFrontend, ITsContextRo aArgs ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aArgs );
    return new S5BackendRemote( aFrontend, aArgs );
  }
}
