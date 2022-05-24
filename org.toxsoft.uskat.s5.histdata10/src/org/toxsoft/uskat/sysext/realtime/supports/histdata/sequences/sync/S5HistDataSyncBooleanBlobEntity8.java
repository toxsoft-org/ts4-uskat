package org.toxsoft.uskat.sysext.realtime.supports.histdata.sequences.sync;

import java.sql.ResultSet;

import javax.persistence.Entity;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceSyncBlob;

/**
 * Данные блока хранения синхронных атомарных значений типа {@link EAtomicType#BOOLEAN}
 *
 * @author mvk
 */
@Entity
public class S5HistDataSyncBooleanBlobEntity8
    extends S5SequenceSyncBlob<S5HistDataSyncBooleanEntity8, byte[], byte[]> {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5HistDataSyncBooleanBlobEntity8() {
  }

  /**
   * Конструктор blob для нового блока (идентификатор формируется автоматически)
   *
   * @param aValues byte[] массив значений
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5HistDataSyncBooleanBlobEntity8( byte[] aValues ) {
    super( aValues );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  S5HistDataSyncBooleanBlobEntity8( ResultSet aResultSet ) {
    super( aResultSet );
  }
}