package org.toxsoft.uskat.legacy.plugins;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.legacy.plugins.ISkResources.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;

/**
 * Параметры менеджера плагинов
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface IPluginManagerOps {

  /**
   * Тип плагинов, обрабатываемых компонентой.<br>
   * Тип данных: примитивный {@link EAtomicType#STRING}<br>
   * Формат: ИД-путь или пустая строка (обозначает все типы плагинов)<br>
   * Значение по умолчанию: "foo"
   */
  IDataDef PLUGIN_TYPE_ID = create( "PluginManager.pluginTypeID", STRING, //
      TSID_NAME, E_N_PLOPS_PLUGIN_TYPE_ID, //
      TSID_DESCRIPTION, E_D_PLOPS_PLUGIN_TYPE_ID, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AvUtils.AV_STR_EMPTY );

  /**
   * Интервал времени (мсек) между проверками директория на обновление плагинов.<br>
   * Тип данных: примитивный {@link EAtomicType#INTEGER}<br>
   * Формат: положительное целое числе, интервал вв миллисекундах, не менее 10000<br>
   * Значение по умолчанию: 300000 (5 минут)
   */
  IDataDef DIR_CHECK_INTERVAL = create( "PluginManager.dirCheckInterval", INTEGER, //
      TSID_NAME, E_N_PLOPS_DIR_CHECK_INTERVAL, //
      TSID_DESCRIPTION, E_D_PLOPS_DIR_CHECK_INTERVAL, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avInt( 300000 ) );

}
