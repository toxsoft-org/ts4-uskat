package org.toxsoft.uskat.s5.server.sequences;

import java.sql.*;

import org.toxsoft.core.tslib.av.utils.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.sequences.impl.*;

/**
 * Фабрика формирования последовательностей {@link IS5Sequence}
 *
 * @author mvk
 * @param <V> тип значения последовательности
 */
public interface IS5SequenceFactory<V extends ITemporal<?>>
    extends IS5SequenceValueFactory {

  /**
   * Возвращает список имен таблиц базы данных в которых возможно хранение значений данных
   *
   * @return {@link IList}&lt;{@link IS5SequenceTableNames}&gt; список пар определяющих хранение блока и его blob
   */
  IList<IS5SequenceTableNames> tableNames();

  /**
   * Возвращает имя таблицы хранения блоков по указанной таблице хранения blob.
   *
   * @param aBlobTableName String имя таблицы хранения blob
   * @return String имя таблицы хранения блоков; null: таблица хранения blob не найдена
   * @throws TsNullArgumentRtException аргумент = null;
   */
  String findBlockTableName( String aBlobTableName );

  /**
   * Возвращает имя таблицы хранения blob по указанной таблице хранения блоков.
   *
   * @param aBlockTableName String имя таблицы хранения блоков
   * @return String имя таблицы хранения blob; null: таблица хранения блоков не найдена
   * @throws TsNullArgumentRtException аргумент = null;
   */
  String findBlobTableName( String aBlockTableName );

  /**
   * Возвращает глубину хранения (в сутках) значений в таблице с указанным именем
   *
   * @param aTableName String имя таблицы хранения блоков или blob
   * @return int глубина хранения (в сутках)
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException таблица не существует
   */
  int getTableDepth( String aTableName );

  /**
   * Возвращает описание типа для указанного данного
   *
   * @param aGwid {@link Gwid} идентификатор типа
   * @return {@link IParameterized} параметризованное описание типа данного
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException несуществующее данное
   */
  IParameterized typeInfo( Gwid aGwid );

  /**
   * Создание последовательности значений данного
   *
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aInterval {@link IQueryInterval} интервал времени последовательности, подробности смотри в
   *          {@link IS5Sequence#interval()}
   * @param aBlocks {@link Iterable}&lt;{@link IS5SequenceBlockEdit}&gt; список блоков представляющих последовательность
   * @return {@link IS5SequenceEdit} последовательность с возможностью редактирования
   * @throw {@link TsNullArgumentRtException} любой аргумент = null
   */
  IS5SequenceEdit<V> createSequence( Gwid aGwid, IQueryInterval aInterval, Iterable<IS5SequenceBlockEdit<V>> aBlocks );

  /**
   * Создает блок значений данного
   *
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aValues {@link ITimedList}&lt;{@link ITemporal}&gt; список значений
   * @return {@link IS5SequenceBlockEdit} блок значений с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   */
  IS5SequenceBlockEdit<V> createBlock( Gwid aGwid, ITimedList<V> aValues );

  /**
   * Создает блок значений данного из текущей записи курсора dbms
   *
   * @param aBlockImplClassName String полное имя класса реализации блока значений, наследника {@link S5SequenceBlock}
   * @param aResultSet {@link ResultSet} курсор dbms
   * @return {@link IS5SequenceBlock} блок значений с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  IS5SequenceBlockEdit<V> createBlock( String aBlockImplClassName, ResultSet aResultSet );

}
