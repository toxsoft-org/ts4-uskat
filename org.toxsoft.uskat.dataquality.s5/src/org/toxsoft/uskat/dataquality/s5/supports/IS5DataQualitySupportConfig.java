package org.toxsoft.uskat.dataquality.s5.supports;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.dataquality.s5.supports.IS5Resources.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.uskat.core.ISkHardConstants;
import org.toxsoft.uskat.dataquality.lib.ISkDataQualityService;
import org.toxsoft.uskat.s5.server.backend.supports.IS5BackendAddonConfig;

/**
 * Конфигурация поддержки расширения бекенда для службы качества данных {@link ISkDataQualityService}
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface IS5DataQualitySupportConfig
    extends IS5BackendAddonConfig {

  /**
   * Список зарегистрированных тикетов
   * <p>
   * Тип: {@link EAtomicType#VALOBJ} содержит {@link S5DataQualityTicketList}
   */
  IDataDef TICKETS =
      create( ISkHardConstants.SK_ID + ".dataquality.tickets", EAtomicType.VALOBJ, TSID_NAME, STR_N_TICKETS, //
          TSID_DESCRIPTION, STR_D_TICKETS, //
          TSID_IS_NULL_ALLOWED, AV_FALSE, //
          TSID_DEFAULT_VALUE, avValobj( new S5DataQualityTicketList() ) );

}
