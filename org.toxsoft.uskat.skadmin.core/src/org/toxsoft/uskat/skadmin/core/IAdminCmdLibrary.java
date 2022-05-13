package org.toxsoft.uskat.skadmin.core;

import org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.legacy.plexy.EPlexyKind;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.core.impl.AdminCmdResult;

/**
 * Библиотека команд {@link IAdminCmdDef}
 *
 * @author mvk
 */
public interface IAdminCmdLibrary
    extends ICloseable {

  /**
   * Возвращает краткое удобочитаемое имя библиотеки
   *
   * @return String
   */
  String getName();

  /**
   * Инициализировать библиотеку
   */
  void init();

  /**
   * Возвращает признак того, что библиотека завершила свою работу
   *
   * @return <b>true</b> библиотека завершила работу; <b>false</b> библиотека не завершила работу
   */
  boolean isClosed();

  /**
   * Возвращает описание команды по ее идентификатору или алиасу
   *
   * @param aCmdId String идентификатор (ИД-путь) команды или ее алиас
   * @return {@link IAdminCmdDef} описание команды. null: команда не найдена
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентификатора не ИД-путь
   */
  IAdminCmdDef findCommand( String aCmdId );

  /**
   * Возвращает список описаний команд библиотеки доступных для исполнения
   *
   * @return {@link IList}&lt;{@link IAdminCmdDef}&gt; - список описаний команд.
   */
  IList<IAdminCmdDef> availableCmds();

  /**
   * Возвращает текущий контекст выполнения команд
   *
   * @return {@link IAdminCmdContext} контекст выполнения команд
   */
  IAdminCmdContext context();

  /**
   * Установка контекста выполнения команд
   *
   * @param aContext {@link IAdminCmdContext} контекст выполнения команд
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setContext( IAdminCmdContext aContext );

  /**
   * Выполняет команду.
   * <p>
   * Для выполнения команды необходимо указать значения аргументов согласно описанию команды. При этом, аргументы для
   * которых не были указаны значения обрабатываются по следующим правилам:
   * <li>Значения вида {@link EPlexyKind#SINGLE_VALUE} и имеющие ограничение
   * {@link IAvMetaConstants#TSID_DEFAULT_VALUE}. Для аргументов формируются значения по-умолчанию</li>;
   * <li>Значения вида {@link EPlexyKind#VALUE_LIST}, {@link EPlexyKind#REF_LIST} и {@link EPlexyKind#OPSET}. Для
   * аргументов формируются соответственно пустые списки атомарных значений, объектных ссылок, наборов именнованных
   * параметров;</li>;
   * <li>Значения вида {@link EPlexyKind#SINGLE_VALUE} и не имеющие ограничение
   * {@link IAvMetaConstants#TSID_DEFAULT_VALUE} или значения вида {@link EPlexyKind#SINGLE_REF}. В текущем контексте
   * команд проводится поиск параметров с именем и типом совпадающим соответственно с идентификатором аргумента
   * {@link IAdminCmdArgDef#id()} и типом его значения {@link IAdminCmdArgDef#type()}. Для значений параметров контекста
   * вида {@link EPlexyKind#SINGLE_REF} проверка типа осуществляется по типу объектной ссылки которая должна быть такой
   * же или быть типом-наследником типа значений аргумента.</li>;
   *
   * @param aCmdId String идентификатор команды (ИД-путь) или ее алиас
   * @param aArgValues {@link IStringMap} - карта значений аргументов. Ключ карты: идентификатор аргумента
   *          {@link IAdminCmdArgDef#id()} или его алиас.
   * @param aCallback {@link IAdminCmdCallback} - обратный вызов для слежения над процессом выполнения
   * @return {@link IAdminCmdResult} - результат выполнения. Команда не имеющая результата: {@link AdminCmdResult#EMPTY}
   *         .
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException библиотека завершила работу
   * @throws TsItemNotFoundRtException команда не существует
   * @throws TsIllegalArgumentRtException список фактических значений входных параметров контекста не соответствует
   *           требованиям команды
   * @throws TsIllegalArgumentRtException в списке выходных параметров есть параметры с признаком readonly
   * @throws TsItemNotFoundRtException не найден аргумент для выполнения команды
   * @throws TsIllegalArgumentRtException тип аргумента не соответствует описанию аргументов команды
   */
  IAdminCmdResult exec( String aCmdId, IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback );

  /**
   * Возвращает список возможных значений для аргумента при выполнения команды с указанными аргументами.
   * <p>
   * В отличии от метода {@link #exec(String, IStringMap, IAdminCmdCallback)} в карте значений аргументов могут быть
   * представлены значения НЕ ВСЕX аргументов команды, а только доступные на текущий момент времени
   *
   * @param aCmdId String идентификатор команды (ИД-путь) или ее алиас
   * @param aArgId String - идентификатор аргумента {@link IAdminCmdArgDef#id()}
   * @param aArgValues {@link IStringMap} - карта значений аргументов. Ключ карты: идентификатор аргумента
   *          {@link IAdminCmdArgDef#id()} или его алиас.
   * @return {@link IList}&lt;{@link IPlexyValue}&gt; - список возможных значений. Пустой список: нет ограничения по
   *         значению
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException библиотека завершила работу
   * @throws TsItemNotFoundRtException не найден аргумент для выполнения команды
   * @throws TsIllegalArgumentRtException тип аргумента не соответствует описанию аргументов команды
   */
  IList<IPlexyValue> getPossibleValues( String aCmdId, String aArgId, IStringMap<IPlexyValue> aArgValues );
}
