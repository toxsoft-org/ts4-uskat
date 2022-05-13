package org.toxsoft.uskat.skadmin.core;

import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.legacy.plexy.*;

/**
 * Контекст выполнения команды библиотеки {@link IAdminCmdLibrary}.
 *
 * @author mvk
 */
public interface IAdminCmdContext {

  /**
   * Возвращает имена всех параметров которые есть в контексте
   *
   * @return {@link IStringList} список имен параметров контекста
   */
  IStringList paramNames();

  /**
   * Возвращает признак того, параметр с указанным именем есть в контексте
   *
   * @param aParamName String имя параметра
   * @return boolean <b>true</b> параметр есть в контексте; <b>false</b> параметра нет в контексте.
   * @throws TsNullArgumentRtException аргумент = null
   */
  boolean hasParam( String aParamName );

  /**
   * Возвращает признак того, что параметр с указанным идентификатором есть в контексте
   *
   * @param aParamId {@link IStridable} идентификатор параметра
   * @return boolean <b>true</b> параметр есть в контексте; <b>false</b> параметра нет в контексте.
   * @throws TsNullArgumentRtException аргумент = null
   */
  boolean hasParam( IStridable aParamId );

  /**
   * Возвращает признак того, значение параметра доступно только для чтения
   *
   * @param aParamName String имя параметра
   * @return boolean <b>true</b> параметр только для чтения; <b>false</b> параметра для чтения и записи.
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException параметр не найден
   */
  boolean readOnlyParam( String aParamName );

  /**
   * Возвращает признак того, что параметр с указанным идентификатором есть в контексте
   *
   * @param aParamId {@link IStridable} идентификатор параметра
   * @return boolean <b>true</b> параметр только для чтения; <b>false</b> параметра для чтения и записи.
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException параметр не найден
   */
  boolean readOnlyParam( IStridable aParamId );

  /**
   * Возвращает значение параметра из контекста
   *
   * @param aParamName String имя параметра
   * @return IPlexyValue значение параметра
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException параметр не найден
   */
  IPlexyValue paramValue( String aParamName );

  /**
   * Возвращает значение параметра из контекста или null если такого параметра нет в контексте
   *
   * @param aParamName String имя параметра
   * @return {@link IPlexyValue} значение параметра. null: параметра нет в контексте
   * @throws TsNullArgumentRtException аргумент = null
   */
  IPlexyValue paramValueOrNull( String aParamName );

  /**
   * Возвращает значение параметра из контекста
   *
   * @param aParamId {@link IStridable} идентификатор параметра
   * @return {@link IPlexyValue} значение параметра
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException параметр не найден
   */
  IPlexyValue paramValue( IStridable aParamId );

  /**
   * Возвращает значение параметра из контекста или null если такого параметра нет в контексте
   *
   * @param aParamId {@link IStridable} идентификатор параметра
   * @return {@link IPlexyValue} значение параметра. null: параметра нет в контексте
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException параметр не найден
   */
  IPlexyValue paramValueOrNull( IStridable aParamId );

  /**
   * Возвращает список значений параметров контекста
   *
   * @param aParamNames {@link IStringList} список имен параметров
   * @return {@link IList}&lt;IPlexyValue&gt; список значений запрошенных параметров
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException нет параметра в контексте
   */
  IList<IPlexyValue> getContextParams( IStringList aParamNames );

  /**
   * Возвращает список значений параметров контекста
   *
   * @param aParamIds {@link IStridablesList} список идентификаторов параметров
   * @return {@link IList}&lt;IPlexyValue&gt; список значений запрошенных параметров
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException нет параметра в контексте
   */
  IList<IPlexyValue> getContextParams( IStridablesList<?> aParamIds );

  /**
   * Возвращает значения всех параметров контекста имеющих указанный тип
   * <p>
   * Для типов {@link EPlexyKind#SINGLE_REF}, {@link EPlexyKind#REF_LIST} выбираются значения с учетом, что значения
   * могут иметь тот же самый {@link IPlexyType#refClass()} или его наследника .
   * <p>
   * Если в контексте находятся два и более параметра имеющих эквивалентное значение (проверка по
   * {@link Object#equals(Object)}), то возвращаемый список добавляется только одно значение.
   *
   * @param aParamType {@link IPlexyType} тип значения параметра
   * @return {@link IList}&lt;{@link IPlexyValue}&gt; список значений.
   * @throws TsNullArgumentRtException аргумент = null
   */
  IList<IPlexyValue> getContextParamByType( IPlexyType aParamType );

  /**
   * Возвращает значения объектных ссылок всех параметров контекста имеющих указанный тип объектной ссылки или его
   * наследника
   * <p>
   * Если в контексте находятся два и более параметра имеющих эквивалентное значение (проверка по
   * {@link Object#equals(Object)}), то возвращаемый список добавляется только одно значение.
   * <p>
   * Если в контексте есть параметры вида {@link EPlexyKind#REF_LIST} имеющие соответствующий
   * {@link IPlexyType#refClass()}, то каждая объектная ссылка таких списков обрабатывается отдельно.
   *
   * @param aRefClass {@link Class}&lt;?&gt; тип объектной ссылки
   * @return {@link IList}&lt;{@link Object}&gt; список объектных ссылок.
   * @throws TsNullArgumentRtException аргумент = null
   */
  IList<Object> getContextReferences( Class<?> aRefClass );

  /**
   * Проверяет список указанных параметров на предмет того, что все они доступны для записи
   *
   * @param aContextNames {@link IStringList} список имен параметров контекста
   * @param aCheckExist boolean <b>true</b> проверять существование параметра в контексте; <b>false</b> не проверять
   *          существование параметра
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException параметр не существует (при aCheckExist = true)
   * @throws TsIllegalArgumentRtException в списке есть параметры с признаком readonly
   */
  void checkWritable( IStringList aContextNames, boolean aCheckExist );

  /**
   * Проверяет список указанных параметров на предмет того, что все они доступны для записи
   *
   * @param aContextNames {@link IStringList} список имен параметров контекста
   * @param aCheckExist boolean <b>true</b> проверять существование параметра в контексте; <b>false</b> не проверять
   *          существование параметра
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException параметр не существует (при aCheckExist = true)
   * @throws TsIllegalArgumentRtException в списке есть параметры с признаком readonly
   */
  void checkWritable( IStridablesList<?> aContextNames, boolean aCheckExist );

  /**
   * Возвращает количество ссылок на значение параметра контекста
   * <p>
   * Под разными ссылками понимается размещение одного и того же значения в контексте под разными именами
   *
   * @param aParam {@link IAdminCmdContextParam} параметр контекста
   * @return int количество ссылок на значение параметра. 0: параметра нет в контексте
   * @throws TsNullArgumentRtException аргумент = null
   */
  int referenceCount( IAdminCmdContextParam aParam );

  /**
   * Возвращает количество ссылок на значение параметра контекста
   * <p>
   * Под разными ссылками понимается размещение одного и того же значения в контексте под разными именами
   *
   * @param aParamId String идентификатор параметра контекста
   * @return int количество ссылок на значение параметра. 0: параметра нет в контексте
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   */
  int referenceCount( String aParamId );

  /**
   * Устанавливает значение параметра в контексте
   * <p>
   * Если в контексте уже существует параметр с указанным имененем, то его значение замещается
   * <p>
   * Если параметра не было в контексте, то формируется сообщение:
   * {@link IAdminCmdContextListener#onAddParam(IAdminCmdContext, String)}.
   * <p>
   * После установки значения параметра формируется сообщение:
   * {@link IAdminCmdContextListener#onSetParamValue(IAdminCmdContext, String)}
   *
   * @param aParamName String имя параметра
   * @param aParamValue {@link IPlexyValue} значение параметра
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException параметр уже есть в контесте и он доступен только чтения
   */
  void setParamValue( String aParamName, IPlexyValue aParamValue );

  /**
   * Тоже что и {@link #setParamValue(String, IPlexyValue)} с управлением доступа к значению параметра
   *
   * @param aParamName String имя параметра
   * @param aParamValue {@link IPlexyValue} значение параметра
   * @param aReadOnly для параметра устанавливается доступ только чтения
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException параметр уже есть в контесте и он доступен только чтения
   */
  void setParamValue( String aParamName, IPlexyValue aParamValue, boolean aReadOnly );

  /**
   * Устанавливает значение параметра в контексте
   * <p>
   * Если в контексте уже существует параметр с указанным имененем, то его значение замещается
   * <p>
   * Если параметра не было в контексте, то формируется сообщение:
   * {@link IAdminCmdContextListener#onAddParam(IAdminCmdContext, String)}.
   * <p>
   * После установки значения параметра формируется сообщение:
   * {@link IAdminCmdContextListener#onSetParamValue(IAdminCmdContext, String)}
   *
   * @param aParamId {@link IStridable} идентификатор параметра
   * @param aParamValue {@link IPlexyValue} значение параметра
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException параметр уже есть в контесте и он доступен только чтения
   */
  void setParamValue( IStridable aParamId, IPlexyValue aParamValue );

  /**
   * Тоже что и {@link #setParamValue(IStridable, IPlexyValue)} с управлением доступа к значению параметра
   *
   * @param aParamId {@link IStridable} идентификатор параметра
   * @param aParamValue {@link IPlexyValue} значение параметра
   * @param aReadOnly для параметра устанавливается доступ только чтения
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException параметр уже есть в контесте и он доступен только чтения
   */
  void setParamValue( IStridable aParamId, IPlexyValue aParamValue, boolean aReadOnly );

  /**
   * Удаляет параметр из контекста
   * <p>
   * Если в контексте не существует параметр с указанным имененем, то ничего не делает
   * <p>
   * ПЕРЕД удалением параметра формируется сообщение:
   * {@link IAdminCmdContextListener#onRemovingParam(IAdminCmdContext, String)}
   *
   * @param aParamName String имя параметра
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void removeParam( String aParamName );

  /**
   * Удаляет параметр из контекста
   * <p>
   * Если в контексте не существует параметр с указанным имененем, то ничего не делает
   * <p>
   * ПЕРЕД удалением параметра формируется сообщение:
   * {@link IAdminCmdContextListener#onRemovingParam(IAdminCmdContext, String)}
   *
   * @param aParamId {@link IStridable} идентификатор параметра
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void removeParam( IStridable aParamId );

  // ------------------------------------------------------------------------------------
  // Извещения
  //
  /**
   * Добавляет слушателя событий изменений контекста.
   * <p>
   * Если этот слушатель уже был зарегистрирован, метод ничего не делает.
   *
   * @param aListener {@link IAdminCmdContextListener} добавляемый слушатель
   * @throws TsNullArgumentRtException аргумент = null
   */
  void addContextListener( IAdminCmdContextListener aListener );

  /**
   * Удаляет слушателя событий изменений справочников и его элементов.
   * <p>
   * Если этот слушатель не был ранее зарегистрирован, метод ничего не делает.
   *
   * @param aListener {@link IAdminCmdContextListener} - удаляемый слушатель
   * @throws TsNullArgumentRtException аргумент = null
   */
  void removeContextListener( IAdminCmdContextListener aListener );

  /**
   * Возвращает список зарегистрированных слушателей контекста
   *
   * @return {@link IList}&{@link IAdminCmdContextListener}&gt; список слушателей
   */
  IList<IAdminCmdContextListener> listeners();

}
