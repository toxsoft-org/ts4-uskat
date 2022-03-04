package org.toxsoft.uskat.classes;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.uskat.s5.server.IS5ServerHardConstants;

/**
 * Класс s5: бекенд службы работающий в рамках узла кластера и формирующий хранимые данные.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface IS5ClassHistorableBackend
    extends IS5ClassBackend {

  /**
   * Идентификатор класса.
   */
  String CLASS_ID = IS5ServerHardConstants.S5_ID_START + "HistorableBackend";

  /**
   * Идентификатор класса.
   */
  String CLASS_HISTORABLE_BACKEND = CLASS_ID;

  // ------------------------------------------------------------------------------------
  // Данные
  //
  //
  /**
   * Данное: количество выполненных операций дефрагментации.
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  String RTDID_DEFRAGMENT_COUNT = "defragmentCount";

  /**
   * Данное: количество загруженных блоков при дефрагментации.
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  String RTDID_FRAGMENT_LOOKUP_COUNT = "fragmentLookupCount";

  /**
   * Данное: количество обработанных блоков при дефрагментации.
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  String RTDID_FRAGMENT_THREAD_COUNT = "fragmentThreadCount";

  /**
   * Данное: количество удаленных блоков при дефрагментации.
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  String RTDID_FRAGMENT_REMOVED_COUNT = "fragmentRemovedCount";

  /**
   * Данное: количество ошибок дефрагментации.
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  String RTDID_FRAGMENT_ERROR_COUNT = "fragmentErrorCount";
}
