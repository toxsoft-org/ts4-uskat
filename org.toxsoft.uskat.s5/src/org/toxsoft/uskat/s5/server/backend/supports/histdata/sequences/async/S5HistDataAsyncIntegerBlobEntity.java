package org.toxsoft.uskat.s5.server.backend.supports.histdata.sequences.async;

import java.sql.ResultSet;

import javax.persistence.Entity;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceAsyncBlob;

/**
 * Данные блока хранения асинхронных атомарных значений типа {@link EAtomicType#INTEGER}
 *
 * @author mvk
 */
@Entity
public class S5HistDataAsyncIntegerBlobEntity
    extends S5SequenceAsyncBlob<S5HistDataAsyncIntegerEntity, long[], long[]> {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5HistDataAsyncIntegerBlobEntity() {
  }

  /**
   * Конструктор blob для нового блока (идентификатор формируется автоматически)
   *
   * @param aTimestamps long[] массив меток времени
   * @param aValues long[] массив значений
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5HistDataAsyncIntegerBlobEntity( long[] aTimestamps, long[] aValues ) {
    super( aTimestamps, aValues );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  S5HistDataAsyncIntegerBlobEntity( ResultSet aResultSet ) {
    super( aResultSet );
  }
}
