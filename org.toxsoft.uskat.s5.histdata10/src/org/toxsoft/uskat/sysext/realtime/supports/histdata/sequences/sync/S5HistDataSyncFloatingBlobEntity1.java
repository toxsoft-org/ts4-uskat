package org.toxsoft.uskat.sysext.realtime.supports.histdata.sequences.sync;

import java.sql.ResultSet;

import javax.persistence.Entity;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceSyncBlob;

/**
 * Данные блока хранения синхронных атомарных значений типа {@link EAtomicType#FLOATING}
 *
 * @author mvk
 */
@Entity
public class S5HistDataSyncFloatingBlobEntity1
    extends S5SequenceSyncBlob<S5HistDataSyncFloatingEntity1, double[], double[]> {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5HistDataSyncFloatingBlobEntity1() {
  }

  /**
   * Конструктор blob для нового блока (идентификатор формируется автоматически)
   *
   * @param aValues double[] массив значений
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5HistDataSyncFloatingBlobEntity1( double[] aValues ) {
    super( aValues );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  S5HistDataSyncFloatingBlobEntity1( ResultSet aResultSet ) {
    super( aResultSet );
  }
}
