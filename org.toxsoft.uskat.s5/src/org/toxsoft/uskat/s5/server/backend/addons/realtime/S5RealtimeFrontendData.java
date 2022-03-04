package org.toxsoft.uskat.s5.server.backend.addons.realtime;

import java.io.Serializable;

import org.toxsoft.uskat.core.impl.S5CommandSupport;
import org.toxsoft.uskat.core.impl.S5EventSupport;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendAddonData;
import org.toxsoft.uskat.s5.utils.datasets.S5DatasetSupport;

import ru.uskat.backend.addons.realtime.ISkBackendAddonRealtime;

/**
 * Данные конфигурации frontend расширения backend {@link ISkBackendAddonRealtime}.
 *
 * @author mvk
 */
public class S5RealtimeFrontendData
    implements IS5FrontendAddonData, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Конфигурация набора текущих данных для чтения
   */
  public final S5DatasetSupport readCurrdata = new S5DatasetSupport();

  /**
   * Конфигурация набора текущих данных для записи
   */
  public final S5DatasetSupport writeCurrdata = new S5DatasetSupport();

  /**
   * Конфигурация обработки событий
   */
  public final S5EventSupport events = new S5EventSupport();

  /**
   * Конфигурация обработки команд (список исполняемых команд)
   */
  public final S5CommandSupport commands = new S5CommandSupport();

}
