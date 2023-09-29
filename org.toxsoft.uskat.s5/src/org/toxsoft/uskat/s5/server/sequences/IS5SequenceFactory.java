package org.toxsoft.uskat.s5.server.sequences;

import java.sql.ResultSet;

import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceBlock;

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
   * @return {@link IList}&lt;{@link Pair}&gt; список пар определяющих хранение блока ({@link Pair#left()}) и его blob
   *         (({@link Pair#right()}))
   */
  IList<Pair<String, String>> tableNames();

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
