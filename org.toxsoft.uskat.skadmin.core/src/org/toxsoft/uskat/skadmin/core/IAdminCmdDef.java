package org.toxsoft.uskat.skadmin.core;

import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;

/**
 * Описание команды библиотеки {@link IAdminCmdLibrary}.
 * <p>
 * Этот интерфейс реализует {@link IStridable}, поля которого имеют следующий смысл:
 * <ul>
 * <li>{@link #id()} - уникальный, в рамках библиотеки,идентификатор команды (ИД-путь);</li>
 * <li>{@link #nmName()} - краткое, НЕуникальное, удобочитаемое имя команды.;</li>
 * <li>{@link #description()} - удобочитаемое описание команды.</li>
 * </ul>
 *
 * @author mvk
 */
public interface IAdminCmdDef
    extends IStridable {

  /**
   * Альтернативный идентификатор(ИД-путь) команды в краткой форме, уникальный, в рамках библиотеки команд.
   *
   * @return String идентификатор команды в краткой форме. Пустая строка - алиас не существует
   */
  String alias();

  /**
   * Возвращает описание аргумента по его идентификатору или алиасу
   *
   * @param aArgId String идентификатор (ИД-имя) аргумента или его алиас
   * @return {@link IAdminCmdArgDef} описание аргумента. null: аргумент не найден
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   */
  IAdminCmdArgDef findArgument( String aArgId );

  /**
   * Возвращает список аргументов необходимых для выполнения команды
   *
   * @return {@link IList}&lt;{@link IAdminCmdArgDef}&gt; - список описаний аргументов команды.
   */
  IList<IAdminCmdArgDef> argumentDefs();

  /**
   * Возвращает тип результата выполнения команды
   *
   * @return {@link IPlexyType} - тип результата. {@link IPlexyType#NONE} - команда не имеет результата
   */
  IPlexyType resultType();

  /**
   * Возвращает описание результата выполнения команды
   *
   * @return String описание результата
   */
  String resultDescription();

  /**
   * Список параметров контекста формируемых командой
   *
   * @return {@link IList}&lt;{@link IAdminCmdContextParam}&gt; список параметров контекста
   */
  IStridablesList<IAdminCmdContextParam> resultContextParams();

  /**
   * Список ролей пользователей которым разрешено выполнение команды
   *
   * @return {@link IStringList} - список ролей пользователей. Пустой список - команда доступна для всех пользователей.
   */
  IStringList roles();

}
