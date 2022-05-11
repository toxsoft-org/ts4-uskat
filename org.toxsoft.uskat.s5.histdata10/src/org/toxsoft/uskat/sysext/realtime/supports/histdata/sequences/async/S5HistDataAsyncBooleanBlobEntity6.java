package org.toxsoft.uskat.sysext.realtime.supports.histdata.sequences.async;

import java.sql.ResultSet;

import javax.persistence.Entity;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceAsyncBlob;

/**
 * Данные блока хранения асинхронных атомарных значений типа {@link EAtomicType#BOOLEAN}
 *
 * @author mvk
 */
@Entity
public class S5HistDataAsyncBooleanBlobEntity6
    extends S5SequenceAsyncBlob<S5HistDataAsyncBooleanEntity6, byte[], byte[]> {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5HistDataAsyncBooleanBlobEntity6() {
  }

  /**
   * Конструктор blob для нового блока (идентификатор формируется автоматически)
   *
   * @param aTimestamps long[] массив меток времени
   * @param aValues byte[] массив значений
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5HistDataAsyncBooleanBlobEntity6( long[] aTimestamps, byte[] aValues ) {
    super( aTimestamps, aValues );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  S5HistDataAsyncBooleanBlobEntity6( ResultSet aResultSet ) {
    super( aResultSet );
  }
}
