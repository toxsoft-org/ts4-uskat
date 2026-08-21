package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.sync;

import java.sql.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.sequences.impl.*;

import jakarta.persistence.*;

/**
 * Данные блока хранения синхронных атомарных значений типа {@link EAtomicType#INTEGER}
 *
 * @author mvk
 */
@Entity
public class S5HistDataSyncIntegerBlobEntity2
    extends S5SequenceSyncBlob<S5HistDataSyncIntegerEntity2, long[], long[]> {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5HistDataSyncIntegerBlobEntity2() {
  }

  /**
   * Конструктор blob для нового блока (идентификатор формируется автоматически)
   *
   * @param aValues long[] массив значений
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5HistDataSyncIntegerBlobEntity2( long[] aValues ) {
    super( aValues );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  S5HistDataSyncIntegerBlobEntity2( ResultSet aResultSet ) {
    super( aResultSet );
  }
}
