package org.toxsoft.uskat.sysext.realtime.supports.histdata.sequences.async;

import java.sql.ResultSet;

import javax.persistence.Entity;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceAsyncBlob;

/**
 * Данные блока хранения асинхронных атомарных значений типа {@link EAtomicType#VALOBJ}
 *
 * @author mvk
 */
@Entity
public class S5HistDataAsyncValobjBlobEntity5
    extends S5SequenceAsyncBlob<S5HistDataAsyncValobjEntity5, String[], String[]> {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5HistDataAsyncValobjBlobEntity5() {
  }

  /**
   * Конструктор blob для нового блока (идентификатор формируется автоматически)
   *
   * @param aTimestamps long[] массив меток времени
   * @param aValues String[] массив значений в формате
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5HistDataAsyncValobjBlobEntity5( long[] aTimestamps, String[] aValues ) {
    super( aTimestamps, aValues );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  S5HistDataAsyncValobjBlobEntity5( ResultSet aResultSet ) {
    super( aResultSet );
  }
}
