package org.toxsoft.uskat.s5.server;

/**
 * Локализуемые ресурсы реализации службы системного описания.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  String STR_N_OBJECT_IMPL_CLASS = Messages.getString( "IS5Resources.STR_N_OBJECT_IMPL_CLASS" ); //$NON-NLS-1$
  String STR_D_OBJECT_IMPL_CLASS = Messages.getString( "IS5Resources.STR_D_OBJECT_IMPL_CLASS" ); //$NON-NLS-1$

  String STR_N_FWD_LINK_IMPL_CLASS = Messages.getString( "IS5Resources.STR_N_FWD_LINK_IMPL_CLASS" ); //$NON-NLS-1$
  String STR_D_FWD_LINK_IMPL_CLASS = Messages.getString( "IS5Resources.STR_D_FWD_LINK_IMPL_CLASS" ); //$NON-NLS-1$

  String STR_N_REV_LINK_IMPL_CLASS = Messages.getString( "IS5Resources.STR_N_REV_LINK_IMPL_CLASS" ); //$NON-NLS-1$
  String STR_D_REV_LINK_IMPL_CLASS = Messages.getString( "IS5Resources.STR_D_REV_LINK_IMPL_CLASS" ); //$NON-NLS-1$

  String STR_N_BACKEND_SESSION_ID = "Сессия";                       //$NON-NLS-1$
  String STR_D_BACKEND_SESSION_ID = "Сессия текущего пользователя"; //$NON-NLS-1$

  String STR_N_BACKEND_SERVER_ID = "Сервер";                                                //$NON-NLS-1$
  String STR_D_BACKEND_SERVER_ID = "Идентификатор сервера, объекта {@link ISkServer}"; //$NON-NLS-1$

  String STR_N_BACKEND_NODE_ID = "Узел";                                                     //$NON-NLS-1$
  String STR_D_BACKEND_NODE_ID = "Идентификатор узла сервера, объекта {@link ISkServerNode}"; //$NON-NLS-1$

  String STR_N_BACKEND_VERSION = Messages.getString( "IS5Resources.STR_N_BACKEND_VERSION" ); //$NON-NLS-1$
  String STR_D_BACKEND_VERSION = Messages.getString( "IS5Resources.STR_D_BACKEND_VERSION" ); //$NON-NLS-1$

  String STR_N_BACKEND_MODULE = Messages.getString( "IS5Resources.STR_N_BACKEND_MODULE" ); //$NON-NLS-1$
  String STR_D_BACKEND_MODULE = Messages.getString( "IS5Resources.STR_D_BACKEND_MODULE" ); //$NON-NLS-1$

  String STR_N_BACKEND_ZONE_ID = "Зона времени";
  String STR_D_BACKEND_ZONE_ID = "Идентификатор зоны времени, по которому работает сервер";

  String STR_N_BACKEND_START_TIME = Messages.getString( "IS5Resources.STR_N_BACKEND_START_TIME" ); //$NON-NLS-1$
  String STR_D_BACKEND_START_TIME = Messages.getString( "IS5Resources.STR_D_BACKEND_START_TIME" ); //$NON-NLS-1$

  String STR_N_BACKEND_CURRENT_TIME = Messages.getString( "IS5Resources.STR_N_BACKEND_CURRENT_TIME" ); //$NON-NLS-1$
  String STR_D_BACKEND_CURRENT_TIME = Messages.getString( "IS5Resources.STR_D_BACKEND_CURRENT_TIME" ); //$NON-NLS-1$

  String STR_N_BACKEND_DATA_WRITE_DISABLE = Messages.getString( "IS5Resources.STR_N_BACKEND_DATA_WRITE_DISABLE" ); //$NON-NLS-1$
  String STR_D_BACKEND_DATA_WRITE_DISABLE = Messages.getString( "IS5Resources.STR_D_BACKEND_DATA_WRITE_DISABLE" ); //$NON-NLS-1$

  String STR_N_BACKEND_SESSION_KEEP_DAYS = Messages.getString( "IS5Resources.STR_N_BACKEND_SESSION_KEEP_DAYS" ); //$NON-NLS-1$
  String STR_D_BACKEND_SESSION_KEEP_DAYS = Messages.getString( "IS5Resources.STR_D_BACKEND_SESSION_KEEP_DAYS" ); //$NON-NLS-1$

  String STR_N_BACKEND_SESSION_INFO = Messages.getString( "IS5Resources.STR_N_BACKEND_SESSION_INFO" ); //$NON-NLS-1$
  String STR_D_BACKEND_SESSION_INFO = Messages.getString( "IS5Resources.STR_D_BACKEND_SESSION_INFO" ); //$NON-NLS-1$

  String STR_N_BACKEND_SESSIONS_INFOS = Messages.getString( "IS5Resources.STR_N_BACKEND_SESSIONS_INFOS" ); //$NON-NLS-1$
  String STR_D_BACKEND_SESSIONS_INFOS = Messages.getString( "IS5Resources.STR_D_BACKEND_SESSIONS_INFOS" ); //$NON-NLS-1$

  String STR_N_BACKEND_TRANSACTIONS_INFOS = Messages.getString( "IS5Resources.STR_N_BACKEND_TRANSACTIONS_INFOS" ); //$NON-NLS-1$
  String STR_D_BACKEND_TRANSACTIONS_INFOS = Messages.getString( "IS5Resources.STR_D_BACKEND_TRANSACTIONS_INFOS" ); //$NON-NLS-1$

  String STR_N_BACKEND_HEAP_MEMORY_USAGE = Messages.getString( "IS5Resources.STR_N_BACKEND_HEAP_MEMORY_USAGE" ); //$NON-NLS-1$
  String STR_D_BACKEND_HEAP_MEMORY_USAGE = Messages.getString( "IS5Resources.STR_D_BACKEND_HEAP_MEMORY_USAGE" ); //$NON-NLS-1$

  String STR_N_BACKEND_NON_HEAP_MEMORY_USAGE = Messages.getString( "IS5Resources.STR_N_BACKEND_NON_HEAP_MEMORY_USAGE" ); //$NON-NLS-1$
  String STR_D_BACKEND_NON_HEAP_MEMORY_USAGE = Messages.getString( "IS5Resources.STR_D_BACKEND_NON_HEAP_MEMORY_USAGE" ); //$NON-NLS-1$

  String STR_N_BACKEND_PLATFORM_INFO = Messages.getString( "IS5Resources.STR_N_BACKEND_PLATFORM_INFO" ); //$NON-NLS-1$
  String STR_D_BACKEND_PLATFORM_INFO = Messages.getString( "IS5Resources.STR_D_BACKEND_PLATFORM_INFO" ); //$NON-NLS-1$

  String STR_N_SESSION_ADDRESS = Messages.getString( "IS5Resources.STR_N_SESSION_ADDRESS" ); //$NON-NLS-1$
  String STR_D_SESSION_ADDRESS = Messages.getString( "IS5Resources.STR_D_SESSION_ADDRESS" ); //$NON-NLS-1$

  String STR_N_SESSION_PORT = Messages.getString( "IS5Resources.STR_N_SESSION_PORT" ); //$NON-NLS-1$
  String STR_D_SESSION_PORT = Messages.getString( "IS5Resources.STR_D_SESSION_PORT" ); //$NON-NLS-1$

  String STR_N_SESSION_CLUSTER_TOPOLOGY = Messages.getString( "IS5Resources.STR_N_SESSION_CLUSTER_TOPOLOGY" ); //$NON-NLS-1$
  String STR_D_SESSION_CLUSTER_TOPOLOGY = Messages.getString( "IS5Resources.STR_D_SESSION_CLUSTER_TOPOLOGY" ); //$NON-NLS-1$

  String STR_N_STAT_SESSION_SENDED = Messages.getString( "IS5Resources.STR_N_STAT_SESSION_SENDED" ); //$NON-NLS-1$
  String STR_D_STAT_SESSION_SENDED = Messages.getString( "IS5Resources.STR_D_STAT_SESSION_SENDED" ); //$NON-NLS-1$

  String STR_N_STAT_SESSION_RECEVIED = Messages.getString( "IS5Resources.STR_N_STAT_SESSION_RECEVIED" ); //$NON-NLS-1$
  String STR_D_STAT_SESSION_RECEVIED = Messages.getString( "IS5Resources.STR_D_STAT_SESSION_RECEVIED" ); //$NON-NLS-1$

  String STR_N_STAT_RECEVIED_CURRDATA = "Текущие";
  String STR_D_STAT_RECEVIED_CURRDATA = "Количество сообщений со значениями текущих данных полученных от клиента";

  String STR_N_STAT_RECEVIED_HISTDATA = "Хранимые";
  String STR_D_STAT_RECEVIED_HISTDATA = "Количество сообщений со значениями хранимых данных полученных от клиента";

  String STR_N_STAT_SENDED_CURRDATA = "Текущие";
  String STR_D_STAT_SENDED_CURRDATA = "Количество сообщений со значениями текущих данных переданные клиенту";

  String STR_N_STAT_SENDED_HISTDATA = "Хранимые";
  String STR_D_STAT_SENDED_HISTDATA = "Количество сообщений со значениями хранимых данных переданные клиенту";

  String STR_N_STAT_SESSION_ERRORS = Messages.getString( "IS5Resources.STR_N_STAT_SESSION_ERRORS" ); //$NON-NLS-1$
  String STR_D_STAT_SESSION_ERRORS = Messages.getString( "IS5Resources.STR_D_STAT_SESSION_ERRORS" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Статистика узла backend
  //
  String STR_N_STAT_BACKEND_NODE_LOAD_AVERAGE = "Загрузка";

  String STR_D_STAT_BACKEND_NODE_LOAD_AVERAGE = "Средняя загрузка (%/100) операционной системы";

  String STR_N_STAT_BACKEND_NODE_LOAD_MAX = "Загрузка, макс";

  String STR_D_STAT_BACKEND_NODE_LOAD_MAX = "Максимальная загрузка (%/100) операционной системы";

  String STR_N_STAT_BACKEND_NODE_FREE_PHYSICAL_MEMORY = "Свободно";

  String STR_D_STAT_BACKEND_NODE_FREE_PHYSICAL_MEMORY = "Объем свободной памяти операционной системы (байты)";

  String STR_N_STAT_BACKEND_NODE_MAX_HEAP_MEMORY = "Максимум";

  String STR_D_STAT_BACKEND_NODE_MAX_HEAP_MEMORY = "Максимальный объем heap памяти (байты)";

  String STR_N_STAT_BACKEND_NODE_USED_HEAP_MEMORY = "Используеся";

  String STR_D_STAT_BACKEND_NODE_USED_HEAP_MEMORY = "Используемый объем heap памяти (байты)";

  String STR_N_STAT_BACKEND_NODE_MAX_NON_HEAP_MEMORY = "Максимум";

  String STR_D_STAT_BACKEND_NODE_MAX_NON_HEAP_MEMORY = "Максимальный объем non-heap памяти (байты)";

  String STR_N_STAT_BACKEND_NODE_USED_NON_HEAP_MEMORY = "Используеся";

  String STR_D_STAT_BACKEND_NODE_USED_NON_HEAP_MEMORY = "Используемый объем non-heap памяти (байты)";

  String STR_N_STAT_BACKEND_NODE_OPEN_SESSION_MAX = "Открыто";

  String STR_D_STAT_BACKEND_NODE_OPEN_SESSION_MAX = "Максимальное количество открытых сессий на интервале";

  String STR_N_STAT_BACKEND_NODE_OPEN_TX_MAX = "Открыто";

  String STR_D_STAT_BACKEND_NODE_OPEN_TX_MAX = "Максимальное количество открытых транзакций на интервале";

  String STR_N_STAT_BACKEND_NODE_COMMIT_TX = "Выполнено";

  String STR_D_STAT_BACKEND_NODE_COMMIT_TX = "Количество завершенных транзакций";

  String STR_N_STAT_BACKEND_NODE_ROLLBACK_TX = "Откаты";

  String STR_D_STAT_BACKEND_NODE_ROLLBACK_TX = "Количество откатов по транзакциям";

  String STR_N_STAT_BACKEND_NODE_PAS_RECEIVED = "Принято";

  String STR_D_STAT_BACKEND_NODE_PAS_RECEIVED = "Количество принятых pas-пакетов";

  String STR_N_STAT_BACKEND_NODE_PAS_SEND = "Отправлено";

  String STR_D_STAT_BACKEND_NODE_PAS_SEND = "Количество отправленных pas-пакетов";

  // ------------------------------------------------------------------------------------
  // Статистика поддержки backend формирующая хранимые данные
  //
  String STR_N_STAT_HISTORABLE_BACKEND_WRITE_COUNT = "Записано";

  String STR_D_STAT_HISTORABLE_BACKEND_WRITE_COUNT = "Количество записей (транзакций) в базу данных";

  String STR_N_STAT_HISTORABLE_BACKEND_LOADED_COUNT = "Загружено";

  String STR_D_STAT_HISTORABLE_BACKEND_LOADED_COUNT = "Количество загруженных блоков";

  String STR_N_STAT_HISTORABLE_BACKEND_LOADED_TIME = "Время";

  String STR_D_STAT_HISTORABLE_BACKEND_LOADED_TIME = "Общее время загрузки блоков (мсек)";

  String STR_N_STAT_HISTORABLE_BACKEND_INSERT_COUNT = "Добавлено";

  String STR_D_STAT_HISTORABLE_BACKEND_INSERT_COUNT = "Количество добавленных блоков";

  String STR_N_STAT_HISTORABLE_BACKEND_INSERT_TIME = "Время";

  String STR_D_STAT_HISTORABLE_BACKEND_INSERT_TIME = "Общее время добавления блоков (мсек)";

  String STR_N_STAT_HISTORABLE_BACKEND_MERGE_COUNT = "Обновлено";

  String STR_D_STAT_HISTORABLE_BACKEND_MERGE_COUNT = "Количество обновленных блоков";

  String STR_N_STAT_HISTORABLE_BACKEND_MERGE_TIME = "Время";

  String STR_D_STAT_HISTORABLE_BACKEND_MERGE_TIME = "Общее время обновления блоков (мсек)";

  String STR_N_STAT_HISTORABLE_BACKEND_REMOVED_COUNT = "Удалено";

  String STR_D_STAT_HISTORABLE_BACKEND_REMOVED_COUNT = "Количество удаленных блоков";

  String STR_N_STAT_HISTORABLE_BACKEND_REMOVED_TIME = "Время";

  String STR_D_STAT_HISTORABLE_BACKEND_REMOVED_TIME = "Общее время удаления блоков (мсек)";

  String STR_N_STAT_HISTORABLE_BACKEND_ERROR_COUNT = "Ошибки";

  String STR_D_STAT_HISTORABLE_BACKEND_ERROR_COUNT = "Количество ошибок записи блоков";

  String STR_N_STAT_HISTORABLE_BACKEND_DEFRAGMENT_COUNT = "Дефрагментации";

  String STR_D_STAT_HISTORABLE_BACKEND_DEFRAGMENT_COUNT = "Количество выполненных дефрагментаций";

  String STR_N_STAT_HISTORABLE_BACKEND_DEFRAGMENT_LOOKUP_COUNT = "Проанализировано";

  String STR_D_STAT_HISTORABLE_BACKEND_DEFRAGMENT_LOOKUP_COUNT =
      "Количество проанализированных данных при поиске дефрагментации";

  String STR_N_STAT_HISTORABLE_BACKEND_DEFRAGMENT_THREAD_COUNT = "Обработано";

  String STR_D_STAT_HISTORABLE_BACKEND_DEFRAGMENT_THREAD_COUNT = "Количество обработанных данных при дефрагментации";

  String STR_N_STAT_HISTORABLE_BACKEND_DEFRAGMENT_VALUE_COUNT = "Значений";

  String STR_D_STAT_HISTORABLE_BACKEND_DEFRAGMENT_VALUE_COUNT = "Количество обработанных значений при дефрагментации";

  String STR_N_STAT_HISTORABLE_BACKEND_DEFRAGMENT_MERGED_COUNT = "Обновлено";

  String STR_D_STAT_HISTORABLE_BACKEND_DEFRAGMENT_MERGED_COUNT =
      "Количество обновленных блоков (merged) при дефрагментации";

  String STR_N_STAT_HISTORABLE_BACKEND_DEFRAGMENT_REMOVED_COUNT = "Удалено";

  String STR_D_STAT_HISTORABLE_BACKEND_DEFRAGMENT_REMOVED_COUNT =
      "Количество удаленных блоков (removed) при дефрагментации";

  String STR_N_STAT_HISTORABLE_BACKEND_DEFRAGMENT_ERROR_COUNT = "Ошибки";

  String STR_D_STAT_HISTORABLE_BACKEND_DEFRAGMENT_ERROR_COUNT = "Количество ошибок дефрагментации";

  String STR_N_STAT_HISTORABLE_BACKEND_PARTITIONS_TASKS_COUNT = "Обработок";

  String STR_D_STAT_HISTORABLE_BACKEND_PARTITIONS_TASKS_COUNT = "Количество выполненных обработок разделов таблиц";

  String STR_N_STAT_HISTORABLE_BACKEND_PARTITIONS_LOOKUP_COUNT = "Проверено";

  String STR_D_STAT_HISTORABLE_BACKEND_PARTITIONS_LOOKUP_COUNT = "Количество проверенных таблиц";

  String STR_N_STAT_HISTORABLE_BACKEND_PARTITIONS_THREAD_COUNT = "Операций";

  String STR_D_STAT_HISTORABLE_BACKEND_PARTITIONS_THREAD_COUNT = "Количество операций обработки разделов таблиц";

  String STR_N_STAT_HISTORABLE_BACKEND_PARTITIONS_ADDED_COUNT = "Добавлено";

  String STR_D_STAT_HISTORABLE_BACKEND_PARTITIONS_ADDED_COUNT = "Количество добавленных разделов таблиц";

  String STR_N_STAT_HISTORABLE_BACKEND_PARTITIONS_REMOVED_COUNT = "Удалено разделов";

  String STR_D_STAT_HISTORABLE_BACKEND_PARTITIONS_REMOVED_COUNT = "Количество удаленных разделов таблиц";

  String STR_N_STAT_HISTORABLE_BACKEND_PARTITIONS_BLOCKS_REMOVED_COUNT = "Удалено блоков";

  String STR_D_STAT_HISTORABLE_BACKEND_PARTITIONS_BLOCKS_REMOVED_COUNT =
      "Количество удаленных блоков при удалении разделов таблиц";

  String STR_N_STAT_HISTORABLE_BACKEND_PARTITIONS_ERROR_COUNT = "Ошибки";

  String STR_D_STAT_HISTORABLE_BACKEND_PARTITIONS_ERROR_COUNT = "Количество ошибок обработки разделов таблиц";

  // ------------------------------------------------------------------------------------
  // Константы
  //
  String STR_N_S5_SERVER_INFO = Messages.getString( "IS5Resources.STR_N_S5_SERVER_INFO" ); //$NON-NLS-1$
  String STR_D_S5_SERVER_INFO = Messages.getString( "IS5Resources.STR_D_S5_SERVER_INFO" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты сообщений
  //
  // String MSG_CANT_INIT_JMS = "Невозможно инициализировать Java Message Service.";

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  // String MSG_ERR_CANT_INIT_JMS = "Невозможно инициализировать Java Message Service.";

}
