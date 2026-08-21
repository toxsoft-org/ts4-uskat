package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.sync;

import java.sql.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.sequences.impl.*;

import jakarta.persistence.*;

/**
 * Данные блока хранения синхронных атомарных значений типа {@link EAtomicType#TIMESTAMP}
 *
 * @author mvk
 */
@Entity
public class S5HistDataSyncTimestampBlobEntity9
    extends S5SequenceSyncBlob<S5HistDataSyncTimestampEntity9, long[], long[]> {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5HistDataSyncTimestampBlobEntity9() {
  }

  /**
   * Конструктор blob для нового блока (идентификатор формируется автоматически)
   *
   * @param aValues long[] массив значений
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5HistDataSyncTimestampBlobEntity9( long[] aValues ) {
    super( aValues );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  S5HistDataSyncTimestampBlobEntity9( ResultSet aResultSet ) {
    super( aResultSet );
  }
}
