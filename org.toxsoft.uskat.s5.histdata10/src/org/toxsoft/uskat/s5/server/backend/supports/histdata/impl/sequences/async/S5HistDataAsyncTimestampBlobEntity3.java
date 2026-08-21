package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.async;

import java.sql.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.sequences.impl.*;

import jakarta.persistence.*;

/**
 * Данные блока хранения асинхронных атомарных значений типа {@link EAtomicType#TIMESTAMP}
 *
 * @author mvk
 */
@Entity
public class S5HistDataAsyncTimestampBlobEntity3
    extends S5SequenceAsyncBlob<S5HistDataAsyncTimestampEntity3, long[], long[]> {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5HistDataAsyncTimestampBlobEntity3() {
  }

  /**
   * Конструктор blob для нового блока (идентификатор формируется автоматически)
   *
   * @param aTimestamps long[] массив меток времени
   * @param aValues long[] массив значений
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5HistDataAsyncTimestampBlobEntity3( long[] aTimestamps, long[] aValues ) {
    super( aTimestamps, aValues );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  S5HistDataAsyncTimestampBlobEntity3( ResultSet aResultSet ) {
    super( aResultSet );
  }
}
