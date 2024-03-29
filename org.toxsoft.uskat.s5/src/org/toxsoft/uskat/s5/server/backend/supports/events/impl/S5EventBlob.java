package org.toxsoft.uskat.s5.server.backend.supports.events.impl;

import java.sql.ResultSet;

import javax.persistence.Entity;

import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceAsyncBlob;

/**
 * Данные блока хранения событий s5-объекта.
 *
 * @author mvk
 */
@Entity
public class S5EventBlob
    extends S5SequenceAsyncBlob<S5EventBlock, SkEvent[], String[]> {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5EventBlob() {
  }

  /**
   * Конструктор blob для нового блока (идентификатор формируется автоматически)
   *
   * @param aTimestamps long[] массив меток времени
   * @param aValues IEvent[] массив событий
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5EventBlob( long[] aTimestamps, SkEvent[] aValues ) {
    super( aTimestamps, aValues );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  S5EventBlob( ResultSet aResultSet ) {
    super( aResultSet );
  }

  // ------------------------------------------------------------------------------------
  // S5SequenceBlob
  //
  @Override
  protected String[] doSerialize( SkEvent[] aValues ) {
    String[] retValue = new String[aValues.length];
    for( int index = 0, n = aValues.length; index < n; index++ ) {
      retValue[index] = SkEvent.KEEPER.ent2str( aValues[index] );
    }
    return retValue;
  }

  @Override
  protected SkEvent[] doDeserialize( String[] aValues ) {
    SkEvent[] retValue = new SkEvent[aValues.length];
    for( int index = 0, n = aValues.length; index < n; index++ ) {
      retValue[index] = SkEvent.KEEPER.str2ent( aValues[index] );
    }
    return retValue;
  }

}
