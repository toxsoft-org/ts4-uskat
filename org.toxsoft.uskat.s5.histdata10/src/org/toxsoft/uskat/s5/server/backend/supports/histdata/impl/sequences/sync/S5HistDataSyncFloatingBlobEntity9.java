package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.sync;

import java.sql.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.sequences.impl.*;

import jakarta.persistence.*;

/**
 * Данные блока хранения синхронных атомарных значений типа {@link EAtomicType#FLOATING}
 *
 * @author mvk
 */
@Entity
public class S5HistDataSyncFloatingBlobEntity9
    extends S5SequenceSyncBlob<S5HistDataSyncFloatingEntity9, double[], double[]> {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5HistDataSyncFloatingBlobEntity9() {
  }

  /**
   * Конструктор blob для нового блока (идентификатор формируется автоматически)
   *
   * @param aValues double[] массив значений
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5HistDataSyncFloatingBlobEntity9( double[] aValues ) {
    super( aValues );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  S5HistDataSyncFloatingBlobEntity9( ResultSet aResultSet ) {
    super( aResultSet );
  }
}
