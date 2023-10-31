package org.toxsoft.uskat.s5.server.sequences.impl;

import java.sql.ResultSet;

import javax.persistence.MappedSuperclass;

import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Абстрактная реализация хранения данных блоков в blob с синхронными значениями.
 *
 * @author mvk
 * @param <BLOCK> блок данных которому принадлежат значения blob
 * @param <BLOB_ARRAY> тип массива используемый для хранения значений, например double[]
 * @param <BLOB_ARRAY_HOLDER> тип объекта хранящий массив значений
 */
@MappedSuperclass
public class S5SequenceSyncBlob<BLOCK extends S5SequenceSyncBlock<?, ?, ?>, BLOB_ARRAY, BLOB_ARRAY_HOLDER>
    extends S5SequenceBlob<BLOCK, BLOB_ARRAY, BLOB_ARRAY_HOLDER> {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5SequenceSyncBlob() {
  }

  /**
   * Конструктор blob для нового блока (идентификатор формируется автоматически)
   *
   * @param aValues BLOB_ARRAY массив значений
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected S5SequenceSyncBlob( BLOB_ARRAY aValues ) {
    super( aValues );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  protected S5SequenceSyncBlob( ResultSet aResultSet ) {
    super( aResultSet );
  }

}
