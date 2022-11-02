package org.toxsoft.uskat.s5.server.backend.supports.commands.impl;

import java.sql.ResultSet;

import javax.persistence.Entity;

import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.cmdserv.IDtoCompletedCommand;
import org.toxsoft.uskat.core.impl.dto.DtoCompletedCommand;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceAsyncBlob;

/**
 * Данные блока хранения истории выполненных команд s5-объекта.
 *
 * @author mvk
 */
@Entity
public class S5CommandBlob
    extends S5SequenceAsyncBlob<S5CommandBlock, IDtoCompletedCommand[], String[]> {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5CommandBlob() {
  }

  /**
   * Конструктор blob для нового блока (идентификатор формируется автоматически)
   *
   * @param aTimestamps long[] массив меток времени
   * @param aValues IDtoCompletedCommand[] массив выполненных команд
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5CommandBlob( long[] aTimestamps, IDtoCompletedCommand[] aValues ) {
    super( aTimestamps, aValues );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  S5CommandBlob( ResultSet aResultSet ) {
    super( aResultSet );
  }

  // ------------------------------------------------------------------------------------
  // S5SequenceBlob
  //
  @Override
  protected String[] doSerialize( IDtoCompletedCommand[] aValues ) {
    String[] retValue = new String[aValues.length];
    for( int index = 0, n = aValues.length; index < n; index++ ) {
      retValue[index] = DtoCompletedCommand.KEEPER.ent2str( aValues[index] );
    }
    return retValue;
  }

  @Override
  protected IDtoCompletedCommand[] doDeserialize( String[] aValues ) {
    IDtoCompletedCommand[] retValue = new IDtoCompletedCommand[aValues.length];
    for( int index = 0, n = aValues.length; index < n; index++ ) {
      retValue[index] = DtoCompletedCommand.KEEPER.str2ent( aValues[index] );
    }
    return retValue;
  }
}
