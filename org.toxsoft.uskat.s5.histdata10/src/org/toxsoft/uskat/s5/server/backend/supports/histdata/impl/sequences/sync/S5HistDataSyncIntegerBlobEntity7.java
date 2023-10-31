package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.sync;

import java.sql.ResultSet;

import javax.persistence.Entity;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceSyncBlob;

/**
 * Данные блока хранения синхронных атомарных значений типа {@link EAtomicType#INTEGER}
 *
 * @author mvk
 */
@Entity
public class S5HistDataSyncIntegerBlobEntity7
    extends S5SequenceSyncBlob<S5HistDataSyncIntegerEntity7, long[], long[]> {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5HistDataSyncIntegerBlobEntity7() {
  }

  /**
   * Конструктор blob для нового блока (идентификатор формируется автоматически)
   *
   * @param aValues long[] массив значений
   * @param aEndTime long время (мсек с начала эпохи) завершения данных (включительно)
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5HistDataSyncIntegerBlobEntity7( long[] aValues, long aEndTime ) {
    super( aValues, aEndTime );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  S5HistDataSyncIntegerBlobEntity7( ResultSet aResultSet ) {
    super( aResultSet );
  }
}
