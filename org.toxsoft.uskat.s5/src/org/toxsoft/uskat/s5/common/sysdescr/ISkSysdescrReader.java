package org.toxsoft.uskat.s5.common.sysdescr;

import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;

/**
 * Читатель данных системного описания
 *
 * @author mvk
 */
public interface ISkSysdescrReader
    extends ISkSysdescrDtoReader {

  /**
   * Возвращает описание класса объектов по его идентификатору
   *
   * @param aClassId String идентификатор класса объектов
   * @return {@link ISkClassInfo} описание класса объекта. null: класс не найден
   * @throws TsNullArgumentRtException аргумент = null
   */
  ISkClassInfo findClassInfo( String aClassId );

  /**
   * Возвращает описание класса объектов по его идентификатору
   *
   * @param aClassId String идентификатор класса объектов
   * @return {@link ISkClassInfo} описание класса объектов
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException класс не найден
   */
  ISkClassInfo getClassInfo( String aClassId );

  /**
   * Возвращает карту описаний всех классов
   *
   * @return {@link IStringMap}&lt;{@link ISkClassInfo}&gt;&gt; карта описаний классов.
   *         <p>
   *         Ключ: идентификатор класса<br>
   *         Значение: описание класса.
   * @throws TsNullArgumentRtException аргумент = null
   */
  IStringMap<ISkClassInfo> getClassInfos();

  /**
   * Возвращает список описаний самого класса и его родительских классов
   *
   * @param aClassId String идентификатор класса объектов
   * @return {@link IStringMap}&lt;{@link ISkClassInfo}&gt;&gt; карта описаний классов. Пустой карта: класс не
   *         существует.
   *         <p>
   *         Ключ: идентификатор класса<br>
   *         Значение: описание класса.
   * @throws TsNullArgumentRtException аргумент = null
   */
  IStringMap<ISkClassInfo> getClassInfos( String aClassId );

  /**
   * Определяет, является ли aParentClassId предком класса aClassId.
   * <p>
   * Класс aParentClassId считается предком, если среди цепочки родителей класса aClassId есть aParentClassId.
   *
   * @param aParentClassId String - идентификатор родительского класса
   * @param aClassId String - идентификатор проверяемого класса
   * @return boolean - признак, что класс aParentClassId является предком aClassId<br>
   *         <b>true</b> - aParentClassId является предком aClassId;<br>
   *         <b>false</b> - среди потомков aParentClassId нет класса aClassId.
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsItemNotFoundRtException нет такого класса
   */
  boolean isAncestor( String aParentClassId, String aClassId );

}
