package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.sync;

import java.sql.ResultSet;

import javax.persistence.Entity;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceSyncBlob;

/**
 * Данные блока хранения синхронных атомарных значений типа {@link EAtomicType#VALOBJ}
 *
 * @author mvk
 */
@Entity
public class S5HistDataSyncValobjBlobEntity6
    extends S5SequenceSyncBlob<S5HistDataSyncValobjEntity6, String[], String[]> {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5HistDataSyncValobjBlobEntity6() {
  }

  /**
   * Конструктор blob для нового блока (идентификатор формируется автоматически)
   *
   * @param aValues String[] массив значений
   * @param aEndTime long время (мсек с начала эпохи) завершения данных (включительно)
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5HistDataSyncValobjBlobEntity6( String[] aValues, long aEndTime ) {
    super( aValues, aEndTime );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  S5HistDataSyncValobjBlobEntity6( ResultSet aResultSet ) {
    super( aResultSet );
  }
}
