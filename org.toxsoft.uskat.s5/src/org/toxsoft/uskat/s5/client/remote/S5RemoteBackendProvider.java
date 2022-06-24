package org.toxsoft.uskat.s5.client.remote;

import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.*;

/**
 * Поставщик удаленного s5-backend
 * <p>
 * Предоставляет минимальный backend необходимый для подключения к серверу
 *
 * @author mvk
 */
public class S5RemoteBackendProvider
    implements ISkBackendProvider {

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
  public final ISkBackend createBackend( ISkFrontendRear aFrontend, ITsContextRo aArgs ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aArgs );
    return new S5BackendRemote( aArgs, aFrontend );
  }
}
