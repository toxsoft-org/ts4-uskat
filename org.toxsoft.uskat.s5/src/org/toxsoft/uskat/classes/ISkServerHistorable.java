package org.toxsoft.uskat.classes;

import org.toxsoft.uskat.core.*;

/**
 * Класс: бекенд службы работающий в рамках узла кластера и формирующий хранимые данные.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface ISkServerHistorable
    extends ISkServerBackend {

  /**
   * Идентификатор класса.
   */
  String CLASS_ID = ISkHardConstants.SK_ID + ".ServerHistorable";

  // ------------------------------------------------------------------------------------
  // Данные
  //
  //
  // /**
  // * Данное: количество выполненных операций дефрагментации.
  // * <p>
  // * Тип: {@link EAtomicType#INTEGER}
  // */
  // String RTDID_DEFRAGMENT_COUNT = "defragmentCount";
  //
  // /**
  // * Данное: количество загруженных блоков при дефрагментации.
  // * <p>
  // * Тип: {@link EAtomicType#INTEGER}
  // */
  // String RTDID_FRAGMENT_LOOKUP_COUNT = "fragmentLookupCount";
  //
  // /**
  // * Данное: количество обработанных блоков при дефрагментации.
  // * <p>
  // * Тип: {@link EAtomicType#INTEGER}
  // */
  // String RTDID_FRAGMENT_THREAD_COUNT = "fragmentThreadCount";
  //
  // /**
  // * Данное: количество удаленных блоков при дефрагментации.
  // * <p>
  // * Тип: {@link EAtomicType#INTEGER}
  // */
  // String RTDID_FRAGMENT_REMOVED_COUNT = "fragmentRemovedCount";
  //
  // /**
  // * Данное: количество ошибок дефрагментации.
  // * <p>
  // * Тип: {@link EAtomicType#INTEGER}
  // */
  // String RTDID_FRAGMENT_ERROR_COUNT = "fragmentErrorCount";
}
