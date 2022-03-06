package org.toxsoft.uskat.s5.server.cluster;

import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.pas.tj.impl.TjUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Обработчик команды передаваемой узлам кластера
 *
 * @author mvk
 */
public interface IS5ClusterCommandHandler {

  /**
   * Обработать команду кластера
   *
   * @param aCommand {@link IS5ClusterCommand} обрабатываемая команда
   * @return {@link ITjValue} результат обработки команды. {@link TjUtils#NULL}: команда не обработана
   * @throws TsNullArgumentRtException аргумент = null
   */
  ITjValue handleClusterCommand( IS5ClusterCommand aCommand );
}
