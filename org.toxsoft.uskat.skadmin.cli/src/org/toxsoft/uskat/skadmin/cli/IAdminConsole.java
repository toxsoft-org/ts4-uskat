package org.toxsoft.uskat.skadmin.cli;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.core.*;
import org.toxsoft.uskat.skadmin.core.impl.AdminCmdResult;

/**
 * Интерфейс консоли
 *
 * @author mvk
 */
public interface IAdminConsole
    extends ICloseable {

  /**
   * Файл хранения контекста выполнения команд по умолчанию
   */
  String CONTEXT_FILENAME = "skadmin.context"; //$NON-NLS-1$

  /**
   * Каталог расположения данных команд по умолчанию
   */
  String DATA_DIR = "data/"; //$NON-NLS-1$

  /**
   * Каталог расположения сигналов по умолчанию
   */
  String SIGNALS_DIR = "signals/"; //$NON-NLS-1$

  /**
   * Возвращает текущий контекст выполняемых команд
   *
   * @return {@link IAdminCmdContext} контекст выполняемых команд
   */
  IAdminCmdContext context();

  /**
   * Выполнение командной строки
   * <p>
   * Если строка пустая, то ничего не делает
   *
   * @param aLine String - строка представляющая команду и ее аргументы
   * @param aUser - boolean <b>true</b> запрашивать у пользователя значения недостающих аргументов; <b>false</b> не
   *          запрашивать значения и выдавать ошибку.
   * @return {@link IAdminCmdResult} результат выполнения. {@link AdminCmdResult#ERROR} ошибка выполнения
   * @throws TsNullArgumentRtException аргумент = null
   */
  IAdminCmdResult execute( String aLine, boolean aUser );

  /**
   * Возвращает количество столбцов в окне терминала
   *
   * @return int количество столбцов
   */
  int getWidth();

  /**
   * Возвращает количество строк в окне терминала
   *
   * @return int количество строк
   */
  int getHeight();

  /**
   * Очистить экран консоли
   */
  void clearScreen();

  /**
   * Форматировать текст согласно указанным параметрам и вывести его на консоль
   *
   * @param aIndent int - индекс столбца с которого начинается вывод
   * @param aSpaceEol boolean <b>true</b> производить перенос строк при появлении пробелов <b>false</b> непроизводить
   *          перенос строк
   * @param aText String - выводимый текст
   * @param aParams Object[] - аргмументы для форматирования текста
   * @throws TsNullArgumentRtException любой аргумент null
   */
  void print( int aIndent, boolean aSpaceEol, String aText, Object... aParams );

  /**
   * Обновить приглашение консоли
   */
  void updatePrompt();

  /**
   * Проверяет существование указанного раздела.
   *
   * @param aSectionId идентификатор (ИД-путь) текущего раздела. Пустая строка - корневой раздел
   * @return boolean <b>true<b> раздел существует; <b>false</b> раздел не существует
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор не ИД-путь и не пустая строка
   */
  boolean isValidSectionId( String aSectionId );

  /**
   * Установить текущий раздел команд
   *
   * @return String текущий раздел команд (ИД-путь). Пустая строка - корневой раздел
   */
  String getSectionId();

  /**
   * Установить текущий раздел команд
   *
   * @param aSectionId идентификатор (ИД-путь) текущего раздела. Пустая строка - корневой раздел
   * @return boolean <b>true</b> переход осуществлен; <b>false</b> переход не был осуществлен так как раздел уже текущий
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор не ИД-путь и не пустая строка
   * @throws TsItemNotFoundRtException раздел не существует
   */
  boolean setSectionId( String aSectionId );

  /**
   * Запросить подтверждение с консоли
   *
   * @param aMessage String - сообщение поясняющее причину запроса
   * @param aDefault boolean значение по умолчанию
   * @return boolean <b>true</b> подтверждение получено; <b>false</b> подтверждение не получено
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  boolean queryConfirm( String aMessage, boolean aDefault );

  /**
   * Считывает значение из консоли
   *
   * @param aType {@link IPlexyType} - тип читаемого значения
   * @param aName String - имя читаемого значения
   * @param aPossibleValues {@link IList} &lt;{@link IPlexyValue}&gt; - список возможных значений. Пустой список без
   *          ограничений
   * @param aDefaultValue String значение в строковом виде по умолчанию. Пустая строка нет значения по умолчанию
   * @param aRetryForErrors boolean <b>true</b> если пользователь неправильное ввел значение, то предложить ему
   *          повторить ввод <b>false</b> если пользователь неправильно ввел значение то вернуть
   *          {@link IPlexyValue#NULL}.
   * @return {@link IPlexyValue} прочитанное значение. {@link IPlexyValue#NULL} - отказ пользователия от ввода значения
   */
  IPlexyValue readValue( IPlexyType aType, String aName, IList<IPlexyValue> aPossibleValues, String aDefaultValue,
      boolean aRetryForErrors );

  /**
   * Возвращает описание команды по ее идентификатору или алиасу
   *
   * @param aCmdId String идентификатор (ИД-путь) команды или ее алиас
   * @return {@link IAdminCmdDef} описание команды. null: команда не найдена
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентификатора не ИД-путь
   */
  IAdminCmdDef findCmdDef( String aCmdId );

  /**
   * Возвращает список описаний команд библиотеки доступных для исполнения
   *
   * @return {@link IList}&lt;{@link IAdminCmdDef}&gt; - список описаний команд.
   * @throws TsIllegalStateRtException библиотека завершила работу
   */
  IList<IAdminCmdDef> listCmdDefs();

  /**
   * Сменить текущий контекст выполнения команд
   *
   * @param aContext {@link IAdminCmdContext} текущий контекст
   * @throws TsNullArgumentRtException аргумент = null
   */
  void changeContext( IAdminCmdContext aContext );
}
