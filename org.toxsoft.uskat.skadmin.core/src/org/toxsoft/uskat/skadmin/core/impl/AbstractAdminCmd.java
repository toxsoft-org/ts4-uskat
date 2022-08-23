package org.toxsoft.uskat.skadmin.core.impl;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.skadmin.core.impl.IAdminResources.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.av.impl.DataType;
import org.toxsoft.core.tslib.av.metainfo.IDataType;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.legacy.plexy.*;
import org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils;
import org.toxsoft.uskat.s5.common.IS5CommonResources;
import org.toxsoft.uskat.skadmin.core.*;

/**
 * Абстрактная реализация команды
 *
 * @author mvk
 */
public abstract class AbstractAdminCmd
    implements IAdminCmdDef, IAdminCmd, Cloneable {

  private IStringMapEdit<IAdminCmdArgDef> argMap  = new StringMap<>();
  private IListEdit<IAdminCmdArgDef>      argList = new ElemLinkedList<>();

  private IStringMap<IPlexyValue> argValues;
  private AdminCmdResult          result;
  private IAdminCmdCallback       callback;
  private boolean                 needClientNotify;
  private IAdminCmdContext        context;
  private ILogger                 logger = getLogger( getClass() );

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IAdminCmdDef
  //
  @Override
  public abstract String id();

  @Override
  public abstract String alias();

  @Override
  public abstract String nmName();

  @Override
  public abstract String description();

  @Override
  public abstract IPlexyType resultType();

  @Override
  public String resultDescription() {
    return TsLibUtils.EMPTY_STRING;
  }

  @Override
  public IStridablesList<IAdminCmdContextParam> resultContextParams() {
    return IStridablesList.EMPTY;
  }

  @Override
  public abstract IStringList roles();

  @Override
  public IAdminCmdArgDef findArgument( String aArgId ) {
    StridUtils.checkValidIdName( aArgId );
    return argMap.findByKey( aArgId );
  }

  @Override
  public IList<IAdminCmdArgDef> argumentDefs() {
    return argList;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IAdminCmd
  //
  @Override
  public void setContext( IAdminCmdContext aContext ) {
    TsNullArgumentRtException.checkNull( aContext );
    context = aContext;
  }

  @Override
  public IAdminCmdDef cmdDef() {
    return this;
  }

  @Override
  public IAdminCmdResult exec( IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback ) {
    TsNullArgumentRtException.checkNulls( aArgValues, aCallback );
    // Инициализация списка аргументов, обратного вызова и результата
    argValues = aArgValues;
    callback = aCallback;
    result = new AdminCmdResult( resultType(), false );
    needClientNotify = false;
    try {
      doExec( aArgValues, aCallback );
      TsInternalErrorRtException.checkFalse( result.hasResult(), ERR_CMD_NOT_RESULT, id() );
      if( needClientNotify ) {
        // Установлен признак оповещения клиента о проводимых операциях
        callback.afterEnd( result );
      }
      return result;
    }
    finally {
      argValues = null;
      callback = null;
      result = null;
      needClientNotify = false;
    }
  }

  @Override
  public IList<IPlexyValue> getPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    TsNullArgumentRtException.checkNulls( aArgId, aArgValues );
    return doPossibleValues( aArgId, aArgValues );
  }

  // ------------------------------------------------------------------------------------
  // API для наследников
  //
  /**
   * Возвращает журнал работы
   *
   * @return {@link ILogger} журнал работы
   */
  protected final ILogger logger() {
    return logger;
  }

  /**
   * Шаблонный метод: выполнить команду
   *
   * @param aArgValues {@link IStringMap} - карта значений аргументов команды. Ключ карты: идентификатор аргумента
   *          {@link IAdminCmdDef#id()}.
   * @param aCallback {@link IAdminCmdCallback} - обратный вызов для слежения над процессом выполнения
   */
  protected abstract void doExec( IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback );

  /**
   * Шаблонный метод: вернуть список возможных значений для аргумента при выполнения команды с указанными аргументами
   * <p>
   * В отличии от метода {@link #doExec(IStringMap, IAdminCmdCallback)} в картах значениях аргументов могут быть
   * представлены значения НЕ ВСЕX аргументов команды, а только доступные на текущий момент времени
   *
   * @param aArgId String - идентификатор аргумента {@link IAdminCmdArgDef#id()}
   * @param aArgValues {@link IStringMap} - карта доступных аргументов команды. Ключ карты: идентификатор аргумента
   *          {@link IAdminCmdDef#id()}.
   * @return {@link IList}&lt; {@link IPlexyValue}&gt; - список возможных значений. Пустой список: значения неограничены
   */
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    return IList.EMPTY;
  }

  /**
   * Добавляет аргумент команды требущий параметр контекста
   *
   * @param aContextParam {@link IAdminCmdContextParam} - параметр контекста
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemAlreadyExistsRtException аргумент уже существует
   */
  protected void addArg( IAdminCmdContextParam aContextParam ) {
    TsNullArgumentRtException.checkNull( aContextParam );
    addArg( aContextParam.id(), TsLibUtils.EMPTY_STRING, aContextParam.nmName(), aContextParam.type(),
        aContextParam.description() );
  }

  /**
   * Добавляет аргумент команды
   *
   * @param aId String - иденитфикатор аргумента
   * @param aAlias String - алиас аргумента или пустая строка, если нет алиаса
   * @param aName String - краткое удобочитаемое название команды
   * @param aType {@link IPlexyType} - тип значения аргумента
   * @param aDescription String - описание аргумента
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор аргумента и его алиас пустая строка
   * @throws TsIllegalArgumentRtException идентификатор аргумента или его алиас должен быть ИД-имененем или пустой
   *           строкой
   * @throws TsItemAlreadyExistsRtException аргумент уже существует
   */
  protected void addArg( String aId, String aAlias, String aName, IPlexyType aType, String aDescription ) {
    TsNullArgumentRtException.checkNulls( aId, aAlias, aName, aType, aDescription );
    AdminCmdArgDef argDef = new AdminCmdArgDef( aId, aAlias, aName, aType, aDescription );
    addArg( argDef );
  }

  /**
   * Добавляет аргумент команды
   *
   * @param aId String - иденитфикатор аргумента
   * @param aAlias String - алиас аргумента или пустая строка, если нет алиаса
   * @param aName String - краткое удобочитаемое название команды
   * @param aType {@link IDataType} - тип значения аргумента
   * @param aDescription String - описание аргумента
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор аргумента и его алиас пустая строка
   * @throws TsIllegalArgumentRtException идентификатор аргумента или его алиас должен быть ИД-имененем или пустой
   *           строкой
   * @throws TsItemAlreadyExistsRtException аргумент уже существует
   */
  protected void addArg( String aId, String aAlias, String aName, IDataType aType, String aDescription ) {
    AdminCmdArgDef argDef = new AdminCmdArgDef( aId, aAlias, aName, aType, aDescription );
    addArg( argDef );
  }

  /**
   * Добавляет аргумент команды
   *
   * @param aArg {@link IAdminCmdArgDef} - описание аргумента команды
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор аргумента и его алиас пустая строка
   * @throws TsIllegalArgumentRtException идентификатор аргумента или его алиас должен быть ИД-имененем или пустой
   *           строкой
   * @throws TsItemAlreadyExistsRtException аргумент уже существует
   */
  protected void addArg( IAdminCmdArgDef aArg ) {
    TsNullArgumentRtException.checkNull( aArg );
    String argId = aArg.id();
    String argAlias = aArg.alias();
    checkArg( argId );
    checkArg( argAlias );
    boolean noArgId = argId.equals( EMPTY_STRING ) && argAlias.equals( EMPTY_STRING );
    TsIllegalArgumentRtException.checkTrue( noArgId, ERR_ARG_NOT_ID, id() );
    if( !argId.equals( EMPTY_STRING ) ) {
      argMap.put( argId, aArg );
    }
    if( !argAlias.equals( EMPTY_STRING ) ) {
      argMap.put( argAlias, aArg );
    }
    argList.add( aArg );
  }

  // ------------------------------------------------------------------------------------
  // Доступ к параметрам контекста
  //
  /**
   * Возвращает значение параметра контекста
   *
   * @param aParam {@link IAdminCmdContextParam} параметр контекста
   * @return {@link IPlexyValue} значение параметра
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException параметра нет в контексте
   * @throws TsIllegalArgumentRtException тип значения параметра не может быть преобразован к запрашиваемому
   */
  protected IPlexyValue contextParamValue( IAdminCmdContextParam aParam ) {
    TsNullArgumentRtException.checkNull( aParam );
    return contextParamValue( aParam.id() );
  }

  /**
   * Возвращает значение параметра контекста
   *
   * @param aParamId String идентификатор параметра контекста
   * @return {@link IPlexyValue} значение параметра
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   * @throws TsIllegalArgumentRtException параметра нет в контексте
   * @throws TsIllegalArgumentRtException тип значения параметра не может быть преобразован к запрашиваемому
   */
  protected IPlexyValue contextParamValue( String aParamId ) {
    TsNullArgumentRtException.checkNull( aParamId );
    return context.paramValue( aParamId );
  }

  /**
   * Возвращает значение параметра контекста или null
   *
   * @param aParam {@link IAdminCmdContextParam} параметр контекста
   * @return {@link IPlexyValue} значение параметра. null: параметра нет в контесте
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException тип значения параметра не может быть преобразован к запрашиваемому
   */
  protected IPlexyValue contextParamValueOrNull( IAdminCmdContextParam aParam ) {
    TsNullArgumentRtException.checkNull( aParam );
    return contextParamValueOrNull( aParam.id() );
  }

  /**
   * Возвращает значение параметра контекста или null
   *
   * @param aParamId String идентификатор параметра контекста
   * @return {@link IPlexyValue} значение параметра. null: параметра нет в контесте
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   * @throws TsIllegalArgumentRtException тип значения параметра не может быть преобразован к запрашиваемому
   */
  protected IPlexyValue contextParamValueOrNull( String aParamId ) {
    TsNullArgumentRtException.checkNull( aParamId );
    return context.paramValueOrNull( aParamId );
  }

  /**
   * Возвращает количество ссылок на значение параметра контекста
   * <p>
   * Под разными ссылками понимается размещение одного и того же значения в контексте под разными именами
   *
   * @param aParam {@link IAdminCmdContextParam} параметр контекста
   * @return int количество ссылок на значение параметра. 0: параметра нет в контесте
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected int contextParamReferenceCount( IAdminCmdContextParam aParam ) {
    TsNullArgumentRtException.checkNull( aParam );
    return contextParamReferenceCount( aParam.id() );
  }

  /**
   * Возвращает количество ссылок на значение параметра контекста
   * <p>
   * Под разными ссылками понимается размещение одного и того же значения в контексте под разными именами
   *
   * @param aParamId String идентификатор параметра контекста
   * @return int количество ссылок на значение параметра. 0: параметра нет в контесте
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected int contextParamReferenceCount( String aParamId ) {
    TsNullArgumentRtException.checkNull( aParamId );
    return context.referenceCount( aParamId );
  }

  /**
   * Устанавливает значение параметра контекста
   * <p>
   * Значения параметра устанавливаются в режиме "только чтение"
   *
   * @param aParam {@link IAdminCmdContextParam} параметр контекста
   * @param aValue {@link IPlexyValue} значение параметра
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   * @throws TsIllegalArgumentRtException команда не выполняется
   * @throws TsIllegalArgumentRtException параметр контекста не зарегистрирован как изменяемый
   *           {@link #resultContextParams()}.
   */
  protected void setContextParamValue( IAdminCmdContextParam aParam, IPlexyValue aValue ) {
    TsNullArgumentRtException.checkNulls( aParam, aValue );
    setContextParamValue( aParam.id(), aValue );
  }

  /**
   * Устанавливает значение параметра контекста
   * <p>
   * Значения параметра устанавливаются в режиме "только чтение"
   *
   * @param aParamId String идентификатор параметра контекста
   * @param aValue {@link IPlexyValue} значение параметра
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   * @throws TsIllegalArgumentRtException команда не выполняется
   * @throws TsIllegalArgumentRtException параметр контекста не зарегистрирован как изменяемый
   *           {@link #resultContextParams()}.
   */
  protected void setContextParamValue( String aParamId, IPlexyValue aValue ) {
    TsNullArgumentRtException.checkNull( aParamId );
    TsIllegalArgumentRtException.checkTrue( argValues == null, ERR_CMD_NOT_EXECUTE, id() );
    if( !resultContextParams().hasKey( aParamId ) ) {
      throw new TsIllegalArgumentRtException( ERR_CMD_CMD_WITHOUT_RESULT_CTX, id(), aParamId );
    }
    if( context.hasParam( aParamId ) ) {
      // Удаляем старое значение параметра (возможно оно только для чтения)
      context.removeParam( aParamId );
    }
    // Добавляем новое значение
    context.setParamValue( aParamId, aValue, true );
  }

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
  protected IList<IPlexyValue> getContextParamByType( IPlexyType aParamType ) {
    TsNullArgumentRtException.checkNull( aParamType );
    return context.getContextParamByType( aParamType );
  }

  // ------------------------------------------------------------------------------------
  // Доступ к значениям аргументов
  //
  /**
   * При выполнении команды возвращает значение аргмента представленного единичным значением {@link IAtomicValue}.
   *
   * @param aArg {@link IStridable} - аргумент
   * @return {@link IAtomicValue} - значение аргумента
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда не выполняется
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   * @throws TsItemNotFoundRtException аргумент не существует
   * @throws TsIllegalStateRtException значение не может быть представлено единичным значением {@link IAtomicValue}
   */
  protected IAtomicValue argSingleValue( IStridable aArg ) {
    TsNullArgumentRtException.checkNull( aArg );
    return argSingleValue( aArg.id() );
  }

  /**
   * При выполнении команды возвращает значение аргмента представленного единичным значением {@link IAtomicValue}.
   *
   * @param aArgId String - идентификатор аргумента (ИД-имя)
   * @return {@link IAtomicValue} - значение аргумента
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда не выполняется
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   * @throws TsItemNotFoundRtException аргумент не существует
   * @throws TsIllegalStateRtException значение не может быть представлено единичным значением {@link IAtomicValue}
   */
  protected IAtomicValue argSingleValue( String aArgId ) {
    StridUtils.checkValidIdName( aArgId );
    TsIllegalArgumentRtException.checkTrue( argValues == null, ERR_CMD_NOT_EXECUTE, id() );
    IPlexyValue value = argValues.findByKey( aArgId );
    TsItemNotFoundRtException.checkNull( value, ERR_ARG_NOT_FOUND, id(), aArgId );
    if( value.type().kind() != EPlexyKind.SINGLE_VALUE ) {
      throw new TsIllegalStateRtException( ERR_ARG_WRONG_TYPE, id(), aArgId, value.type() );
    }
    return value.singleValue();
  }

  /**
   * При выполнении команды возвращает значение аргмента представленного единичным значением {@link IAtomicValue}.
   *
   * @param aArg {@link IStridable} - аргумент
   * @param aDefaultValue {@link IAtomicValue} - значение по умолчанию
   * @return {@link IAtomicValue} - значение аргумента
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда не выполняется
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   * @throws TsItemNotFoundRtException аргумент не существует
   * @throws TsIllegalStateRtException значение не может быть представлено единичным значением {@link IAtomicValue}
   */
  protected IAtomicValue argSingleValue( IStridable aArg, IAtomicValue aDefaultValue ) {
    TsNullArgumentRtException.checkNulls( aArg, aDefaultValue );
    return argSingleValue( aArg.id(), aDefaultValue );
  }

  /**
   * При выполнении команды возвращает значение аргмента представленного единичным значением {@link IAtomicValue}.
   *
   * @param aArgId String - идентификатор аргумента (ИД-имя)
   * @param aDefaultValue {@link IAtomicValue} - значение по умолчанию
   * @return {@link IAtomicValue} - значение аргумента
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда не выполняется
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   * @throws TsItemNotFoundRtException аргумент не существует
   * @throws TsIllegalStateRtException значение не может быть представлено единичным значением {@link IAtomicValue}
   */
  protected IAtomicValue argSingleValue( String aArgId, IAtomicValue aDefaultValue ) {
    TsNullArgumentRtException.checkNulls( aArgId, aDefaultValue );
    StridUtils.checkValidIdName( aArgId );
    TsIllegalArgumentRtException.checkTrue( argValues == null, ERR_CMD_NOT_EXECUTE, id() );
    IPlexyValue value = argValues.findByKey( aArgId );
    if( value == null ) {
      return aDefaultValue;
    }
    TsItemNotFoundRtException.checkNull( value, ERR_ARG_NOT_FOUND, id(), aArgId );
    if( value.type().kind() != EPlexyKind.SINGLE_VALUE ) {
      throw new TsIllegalStateRtException( ERR_ARG_WRONG_TYPE, id(), aArgId, value.type() );
    }
    IAtomicValue retValue = value.singleValue();
    return (retValue.isAssigned() ? retValue : aDefaultValue);
  }

  /**
   * При выполнении команды возвращает значение аргмента представленного значением объектной ссылки.
   *
   * @param <T> тип значения объектной ссылки
   * @param aArgId {@link IStridable} - идентификатор аргумента
   * @return {@link IAtomicValue} - значение аргумента
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда не выполняется
   * @throws TsItemNotFoundRtException аргумент не существует
   * @throws TsIllegalStateRtException значение не может быть представлено объектной ссылкой
   * @throws TsIllegalArgumentRtException значение не может быть преобразовано к запрошенному типу
   */
  protected <T> T argSingleRef( IStridable aArgId ) {
    TsNullArgumentRtException.checkNull( aArgId );
    return argSingleRef( aArgId.id() );
  }

  /**
   * При выполнении команды возвращает значение аргмента представленного значением объектной ссылки.
   *
   * @param <T> тип значения объектной ссылки
   * @param aArgId String - идентификатор аргумента (ИД-путь)
   * @return {@link IAtomicValue} - значение аргумента
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда не выполняется
   * @throws TsIllegalArgumentRtException идентификатор не ИД-путь
   * @throws TsItemNotFoundRtException аргумент не существует
   * @throws TsIllegalStateRtException значение не может быть представлено объектной ссылкой
   * @throws TsIllegalArgumentRtException значение не может быть преобразовано к запрошенному типу
   */
  @SuppressWarnings( "unchecked" )
  protected <T> T argSingleRef( String aArgId ) {
    // mvk 2018-11-07
    // StridUtils.checkValidIdName( aArgId );
    StridUtils.checkValidIdPath( aArgId );
    TsIllegalArgumentRtException.checkTrue( argValues == null, ERR_CMD_NOT_EXECUTE, id() );
    IPlexyValue value = argValues.findByKey( aArgId );
    TsItemNotFoundRtException.checkNull( value, ERR_ARG_NOT_FOUND, id(), aArgId );
    if( value.type().kind() != EPlexyKind.SINGLE_REF ) {
      throw new TsIllegalStateRtException( ERR_ARG_WRONG_TYPE, id(), aArgId, value.type() );
    }
    Object refValue = value.singleRef();
    try {
      return (T)refValue;
    }
    catch( @SuppressWarnings( "unused" ) RuntimeException e ) {
      String valueTypeName = refValue.getClass().getName();
      throw new TsIllegalArgumentRtException( ERR_ARG_WRONG_REF_TYPE, id(), aArgId, valueTypeName );
    }
  }

  /**
   * При выполнении команды возвращает значение аргмента представленного значением объектной ссылки.
   *
   * @param <T> тип значения объектной ссылки
   * @param aArgId String - идентификатор аргумента (ИД-имя)
   * @return {@link IAtomicValue} - значение аргумента
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда не выполняется
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   * @throws TsItemNotFoundRtException аргумент не существует
   * @throws TsIllegalStateRtException значение не может быть представлено списком объектных ссылок
   * @throws TsIllegalArgumentRtException значение не может быть преобразовано к запрошенному типу
   */
  protected <T> T argRefList( IStridable aArgId ) {
    TsNullArgumentRtException.checkNull( aArgId );
    return argRefList( aArgId.id() );
  }

  /**
   * При выполнении команды возвращает значение аргмента представленного значением объектной ссылки.
   *
   * @param <T> тип значения объектной ссылки
   * @param aArgId String - идентификатор аргумента (ИД-имя)
   * @return {@link IAtomicValue} - значение аргумента
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда не выполняется
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   * @throws TsItemNotFoundRtException аргумент не существует
   * @throws TsIllegalStateRtException значение не может быть представлено списком объектных ссылок
   * @throws TsIllegalArgumentRtException значение не может быть преобразовано к запрошенному типу
   */
  @SuppressWarnings( "unchecked" )
  protected <T> T argRefList( String aArgId ) {
    StridUtils.checkValidIdName( aArgId );
    TsIllegalArgumentRtException.checkTrue( argValues == null, ERR_CMD_NOT_EXECUTE, id() );
    IPlexyValue value = argValues.findByKey( aArgId );
    TsItemNotFoundRtException.checkNull( value, ERR_ARG_NOT_FOUND, id(), aArgId );
    if( value.type().kind() != EPlexyKind.REF_LIST ) {
      throw new TsIllegalStateRtException( ERR_ARG_WRONG_TYPE, id(), aArgId, value.type() );
    }
    Object refValue = value.refList();
    try {
      return (T)refValue;
    }
    catch( @SuppressWarnings( "unused" ) RuntimeException e ) {
      String valueTypeName = refValue.getClass().getName();
      throw new TsIllegalArgumentRtException( ERR_ARG_WRONG_REF_TYPE, id(), aArgId, valueTypeName );
    }
  }

  /**
   * При выполнении команды возвращает значение аргмента представленного массивом значений {@link IAtomicValue}.
   *
   * @param aArg {@link IStridable} - аргумент
   * @return {@link IList}&lt;{@link IAtomicValue}&gt; - список значений определяющий значение аргумента
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда не выполняется
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   * @throws TsItemNotFoundRtException аргумент не существует
   * @throws TsIllegalStateRtException значение аргумента не может быть представлено значением-массивом
   *           {@link IAtomicValue}
   */
  protected IList<IAtomicValue> argValueList( IStridable aArg ) {
    TsNullArgumentRtException.checkNull( aArg );
    return argValueList( aArg.id() );
  }

  /**
   * При выполнении команды возвращает значение аргмента представленного массивом значений {@link IAtomicValue}.
   *
   * @param aArgId String - идентификатор аргумента (ИД-имя)
   * @return {@link IList}&lt;{@link IAtomicValue}&gt; - список значений определяющий значение аргумента
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда не выполняется
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   * @throws TsItemNotFoundRtException аргумент не существует
   * @throws TsIllegalStateRtException значение аргумента не может быть представлено значением-массивом
   *           {@link IAtomicValue}
   */
  protected IList<IAtomicValue> argValueList( String aArgId ) {
    StridUtils.checkValidIdName( aArgId );
    TsIllegalArgumentRtException.checkTrue( argValues == null, ERR_CMD_NOT_EXECUTE, id() );
    IPlexyValue value = argValues.findByKey( aArgId );
    TsItemNotFoundRtException.checkNull( value, ERR_ARG_NOT_FOUND, id(), aArgId );
    if( value.type().kind() != EPlexyKind.VALUE_LIST ) {
      throw new TsIllegalStateRtException( ERR_ARG_WRONG_TYPE, id(), aArgId, value.type() );
    }
    return value.valueList();
  }

  /**
   * При выполнении команды возвращает значение аргмента представленного списком Boolean-значений.
   *
   * @param aArg {@link IStridable} - аргумент
   * @return {@link IList}&lt;Boolean&gt; - список Boolean-значений определяющий значение аргумента
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда не выполняется
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   * @throws TsItemNotFoundRtException аргумент не существует
   * @throws TsIllegalStateRtException значение аргумента не может быть представлено единичным значением
   *           {@link IAtomicValue}
   */
  protected IList<Boolean> argBoolList( IStridable aArg ) {
    TsNullArgumentRtException.checkNull( aArg );
    return argBoolList( aArg.id() );
  }

  /**
   * При выполнении команды возвращает значение аргмента представленного списком Boolean-значений.
   *
   * @param aArgId String - идентификатор аргумента (ИД-имя)
   * @return {@link IList}&lt;Boolean&gt; - список Boolean-значений определяющий значение аргумента
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда не выполняется
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   * @throws TsItemNotFoundRtException аргумент не существует
   * @throws TsIllegalStateRtException значение аргумента не может быть представлено единичным значением
   *           {@link IAtomicValue}
   */
  protected IList<Boolean> argBoolList( String aArgId ) {
    StridUtils.checkValidIdName( aArgId );
    TsIllegalArgumentRtException.checkTrue( argValues == null, ERR_CMD_NOT_EXECUTE, id() );
    IPlexyValue value = argValues.findByKey( aArgId );
    TsItemNotFoundRtException.checkNull( value, ERR_ARG_NOT_FOUND, id(), aArgId );
    if( value.type().kind() != EPlexyKind.VALUE_LIST ) {
      throw new TsIllegalStateRtException( ERR_ARG_WRONG_TYPE, id(), aArgId, value.type() );
    }
    if( value.type().dataType().atomicType() != EAtomicType.BOOLEAN ) {
      throw new TsIllegalStateRtException( ERR_ARG_CANT_BOOL_LIST_NARROW, id(), aArgId );
    }
    IList<IAtomicValue> valueList = value.valueList();
    IListEdit<Boolean> retValue = new ElemArrayList<>( valueList.size() );
    for( int index = 0, n = valueList.size(); index < n; index++ ) {
      retValue.add( Boolean.valueOf( valueList.get( index ).asBool() ) );
    }
    return retValue;
  }

  /**
   * При выполнении команды возвращает значение аргмента представленного списком integer-значений.
   *
   * @param aArg {@link IStridable} - аргумент
   * @return {@link IIntList} - список integer-значений определяющий значение аргумента
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда не выполняется
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   * @throws TsItemNotFoundRtException аргумент не существует
   * @throws TsIllegalStateRtException значение аргумента не может быть представлено единичным значением
   *           {@link IAtomicValue}
   */
  protected IIntList argIntList( IStridable aArg ) {
    TsNullArgumentRtException.checkNull( aArg );
    return argIntList( aArg.id() );
  }

  /**
   * При выполнении команды возвращает значение аргмента представленного списком integer-значений.
   *
   * @param aArgId String - идентификатор аргумента (ИД-имя)
   * @return {@link IIntList} - список integer-значений определяющий значение аргумента
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда не выполняется
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   * @throws TsItemNotFoundRtException аргумент не существует
   * @throws TsIllegalStateRtException значение аргумента не может быть представлено единичным значением
   *           {@link IAtomicValue}
   */
  protected IIntList argIntList( String aArgId ) {
    StridUtils.checkValidIdName( aArgId );
    TsIllegalArgumentRtException.checkTrue( argValues == null, ERR_CMD_NOT_EXECUTE, id() );
    IPlexyValue value = argValues.findByKey( aArgId );
    TsItemNotFoundRtException.checkNull( value, ERR_ARG_NOT_FOUND, id(), aArgId );
    if( value.type().kind() != EPlexyKind.VALUE_LIST ) {
      throw new TsIllegalStateRtException( ERR_ARG_WRONG_TYPE, id(), aArgId, value.type() );
    }
    if( value.type().dataType().atomicType() != EAtomicType.INTEGER ) {
      throw new TsIllegalStateRtException( ERR_ARG_CANT_INT_LIST_NARROW, id(), aArgId );
    }
    IList<IAtomicValue> valueList = value.valueList();
    IIntListEdit retValue = new IntArrayList();
    for( int index = 0, n = valueList.size(); index < n; index++ ) {
      retValue.add( valueList.get( index ).asInt() );
    }
    return retValue;
  }

  /**
   * При выполнении команды возвращает значение аргмента представленного списком long-значений.
   *
   * @param aArg {@link IStridable} - аргумент
   * @return {@link ILongList} - список long-значений определяющий значение аргумента
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда не выполняется
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   * @throws TsItemNotFoundRtException аргумент не существует
   * @throws TsIllegalStateRtException значение аргумента не может быть представлено единичным значением
   *           {@link IAtomicValue}
   */
  protected ILongList argLongList( IStridable aArg ) {
    TsNullArgumentRtException.checkNull( aArg );
    return argLongList( aArg.id() );
  }

  /**
   * При выполнении команды возвращает значение аргмента представленного списком long-значений.
   *
   * @param aArgId String - идентификатор аргумента (ИД-имя)
   * @return {@link ILongList} - список long-значений определяющий значение аргумента
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда не выполняется
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   * @throws TsItemNotFoundRtException аргумент не существует
   * @throws TsIllegalStateRtException значение аргумента не может быть представлено единичным значением
   *           {@link IAtomicValue}
   */
  protected ILongList argLongList( String aArgId ) {
    StridUtils.checkValidIdName( aArgId );
    TsIllegalArgumentRtException.checkTrue( argValues == null, ERR_CMD_NOT_EXECUTE, id() );
    IPlexyValue value = argValues.findByKey( aArgId );
    TsItemNotFoundRtException.checkNull( value, ERR_ARG_NOT_FOUND, id(), aArgId );
    if( value.type().kind() != EPlexyKind.VALUE_LIST ) {
      throw new TsIllegalStateRtException( ERR_ARG_WRONG_TYPE, id(), aArgId, value.type() );
    }
    if( value.type().dataType().atomicType() != EAtomicType.INTEGER ) {
      throw new TsIllegalStateRtException( ERR_ARG_CANT_INT_LIST_NARROW, id(), aArgId );
    }
    IList<IAtomicValue> valueList = value.valueList();
    ILongListEdit retValue = new LongArrayList( valueList.size() );
    for( int index = 0, n = valueList.size(); index < n; index++ ) {
      retValue.add( valueList.get( index ).asLong() );
    }
    return retValue;
  }

  /**
   * При выполнении команды возвращает значение аргмента представленного списком float-значений.
   *
   * @param aArg {@link IStridable} - аргумент
   * @return {@link IList}&lt;Float&gt; - список float-значений определяющий значение аргумента
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда не выполняется
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   * @throws TsItemNotFoundRtException аргумент не существует
   * @throws TsIllegalStateRtException значение аргумента не может быть представлено единичным значением
   *           {@link IAtomicValue}
   */
  protected IList<Float> argFloatList( IStridable aArg ) {
    TsNullArgumentRtException.checkNull( aArg );
    return argFloatList( aArg.id() );
  }

  /**
   * При выполнении команды возвращает значение аргмента представленного списком float-значений.
   *
   * @param aArgId String - идентификатор аргумента (ИД-имя)
   * @return {@link IList}&lt;Float&gt; - список float-значений определяющий значение аргумента
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда не выполняется
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   * @throws TsItemNotFoundRtException аргумент не существует
   * @throws TsIllegalStateRtException значение аргумента не может быть представлено единичным значением
   *           {@link IAtomicValue}
   */
  protected IList<Float> argFloatList( String aArgId ) {
    StridUtils.checkValidIdName( aArgId );
    TsIllegalArgumentRtException.checkTrue( argValues == null, ERR_CMD_NOT_EXECUTE, id() );
    IPlexyValue value = argValues.findByKey( aArgId );
    TsItemNotFoundRtException.checkNull( value, ERR_ARG_NOT_FOUND, id(), aArgId );
    if( value.type().kind() != EPlexyKind.VALUE_LIST ) {
      throw new TsIllegalStateRtException( ERR_ARG_WRONG_TYPE, id(), aArgId, value.type() );
    }
    if( value.type().dataType().atomicType() != EAtomicType.FLOATING ) {
      throw new TsIllegalStateRtException( ERR_ARG_CANT_FLOAT_LIST_NARROW, id(), aArgId );
    }
    IList<IAtomicValue> valueList = value.valueList();
    IListEdit<Float> retValue = new ElemArrayList<>( valueList.size() );
    for( int index = 0, n = valueList.size(); index < n; index++ ) {
      retValue.add( Float.valueOf( valueList.get( index ).asFloat() ) );
    }
    return retValue;
  }

  /**
   * При выполнении команды возвращает значение аргмента представленного списком строк.
   *
   * @param aArg {@link IStridable} - аргумент
   * @return {@link IStringList} - список строковых значений определяющий значение аргумента
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда не выполняется
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   * @throws TsItemNotFoundRtException аргумент не существует
   * @throws TsIllegalStateRtException значение аргумента не может быть представлено единичным значением
   *           {@link IAtomicValue}
   */
  protected IStringList argStrList( IStridable aArg ) {
    TsNullArgumentRtException.checkNull( aArg );
    return argStrList( aArg.id() );
  }

  /**
   * При выполнении команды возвращает значение аргмента представленного списком строк.
   *
   * @param aArgId String - идентификатор аргумента (ИД-имя)
   * @return {@link IStringList} - список строковых значений определяющий значение аргумента
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда не выполняется
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   * @throws TsItemNotFoundRtException аргумент не существует
   * @throws TsIllegalStateRtException значение аргумента не может быть представлено единичным значением
   *           {@link IAtomicValue}
   */
  protected IStringList argStrList( String aArgId ) {
    StridUtils.checkValidIdName( aArgId );
    TsIllegalArgumentRtException.checkTrue( argValues == null, ERR_CMD_NOT_EXECUTE, id() );
    IPlexyValue value = argValues.findByKey( aArgId );
    TsItemNotFoundRtException.checkNull( value, ERR_ARG_NOT_FOUND, id(), aArgId );
    if( value.type().kind() != EPlexyKind.VALUE_LIST ) {
      throw new TsIllegalStateRtException( ERR_ARG_WRONG_TYPE, id(), aArgId, value.type() );
    }
    if( value.type().dataType().atomicType() != EAtomicType.STRING ) {
      throw new TsIllegalStateRtException( ERR_ARG_CANT_STR_LIST_NARROW, id(), aArgId );
    }
    IList<IAtomicValue> valueList = value.valueList();
    IStringListEdit retValue = new StringArrayList( valueList.size() );
    for( int index = 0, n = valueList.size(); index < n; index++ ) {
      retValue.add( valueList.get( index ).asString() );
    }
    return retValue;
  }

  /**
   * При выполнении команды возвращает значение аргмента представленного в виде набора именованных параметров.
   *
   * @param aArg {@link IStridable} - аргумент
   * @return {@link IOptionSet} - набор именованных параметров определяющий значение аргумента
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда не выполняется
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   * @throws TsItemNotFoundRtException аргумент не существует
   * @throws TsIllegalStateRtException значение аргумента не может быть представлено значением-массивом
   *           {@link IAtomicValue}
   * @throws TsIllegalArgumentRtException неверный формат элементов (должно быть: optionId=optionValue)
   */
  protected IOptionSet argOptionSet( IStridable aArg ) {
    TsNullArgumentRtException.checkNull( aArg );
    return argOptionSet( aArg.id() );
  }

  /**
   * При выполнении команды возвращает значение аргмента представленного в виде набора именованных параметров.
   *
   * @param aArgId String - идентификатор аргумента (ИД-имя)
   * @return {@link IOptionSet} - набор именованных параметров определяющий значение аргумента
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда не выполняется
   * @throws TsIllegalArgumentRtException идентификатор не ИД-имя
   * @throws TsItemNotFoundRtException аргумент не существует
   * @throws TsIllegalStateRtException значение аргумента не может быть представлено значением-массивом
   *           {@link IAtomicValue}
   * @throws TsIllegalArgumentRtException неверный формат элементов (должно быть: optionId=optionValue)
   */
  protected IOptionSet argOptionSet( String aArgId ) {
    StridUtils.checkValidIdName( aArgId );
    TsIllegalArgumentRtException.checkTrue( argValues == null, ERR_CMD_NOT_EXECUTE, id() );
    IPlexyValue value = argValues.findByKey( aArgId );
    TsItemNotFoundRtException.checkNull( value, ERR_ARG_NOT_FOUND, id(), aArgId );
    if( value.type().kind() != EPlexyKind.OPSET ) {
      throw new TsIllegalStateRtException( ERR_ARG_WRONG_TYPE, id(), aArgId, value.type() );
    }
    return value.getOpset();
  }

  // ------------------------------------------------------------------------------------
  // Формирование результата выполнения команды
  //
  /**
   * Возвращает признак того, что в данный момент выполняется команда
   *
   * @return <b>true</b> команда выполняется; <b>false</b> команда не выполняется.
   */
  protected boolean isExecuting() {
    return (result != null);
  }

  /**
   * Добавляет информацию в результат обработки команды
   *
   * @param aMsg String - форматирующее методом {@link String#format(String, Object...)} сообщение
   * @param aArgs Object[] - аргументы форматной строки
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда в данный момент не выполняется
   * @throws TsIllegalStateRtException результат уже сформирован. Коррекция запрещена
   */
  protected void addResultInfo( String aMsg, Object... aArgs ) {
    TsNullArgumentRtException.checkNulls( aMsg, aArgs );
    TsIllegalStateRtException.checkNull( result );
    result.addValidation( ValidationResult.info( aMsg, aArgs ) );
  }

  /**
   * Добавляет информацию в результат обработки команды
   *
   * @param aMsgs {@link IList}&lt;{@link ValidationResult}&gt; список сообщений
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда в данный момент не выполняется
   * @throws TsIllegalStateRtException результат уже сформирован. Коррекция запрещена
   */
  protected void addResultInfo( IList<ValidationResult> aMsgs ) {
    TsNullArgumentRtException.checkNull( aMsgs );
    TsIllegalStateRtException.checkNull( result );
    for( ValidationResult msg : aMsgs ) {
      result.addValidation( msg );
    }
  }

  /**
   * Добавляет предупреждение в результат обработки команды
   *
   * @param aMsg String - форматирующее методом {@link String#format(String, Object...)} сообщение
   * @param aArgs Object[] - аргументы форматной строки
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда в данный момент не выполняется
   * @throws TsIllegalStateRtException результат уже сформирован. Коррекция запрещена
   */
  protected void addResultWarning( String aMsg, Object... aArgs ) {
    TsNullArgumentRtException.checkNulls( aMsg, aArgs );
    TsIllegalStateRtException.checkNull( result );
    result.addValidation( ValidationResult.warn( aMsg, aArgs ) );
  }

  /**
   * Добавляет в результат информацию об ошибке
   *
   * @param aError {@link Throwable} - ошибка
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда в данный момент не выполняется
   * @throws TsIllegalStateRtException результат уже сформирован. Коррекция запрещена
   */
  protected void addResultError( Throwable aError ) {
    TsNullArgumentRtException.checkNull( aError );
    TsIllegalStateRtException.checkNull( result );
    String msg = IS5CommonResources.cause( aError );
    if( msg == null ) {
      msg = aError.getMessage();
    }
    if( msg == null ) {
      msg = aError.getClass().getName();
    }
    if( logger.isSeverityOn( ELogSeverity.DEBUG ) ) {
      logger.error( aError );
    }
    result.addValidation( ValidationResult.error( msg + IStrioHardConstants.CHAR_EOL ) );
  }

  /**
   * Добавляет в результат информацию об ошибке
   *
   * @param aMsg String - форматирующее методом {@link String#format(String, Object...)} сообщение
   * @param aArgs Object[] - аргументы форматной строки
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда в данный момент не выполняется
   * @throws TsIllegalStateRtException результат уже сформирован. Коррекция запрещена
   */
  protected void addResultError( String aMsg, Object... aArgs ) {
    TsNullArgumentRtException.checkNull( aMsg );
    TsIllegalStateRtException.checkNull( result );
    result.addValidation( ValidationResult.error( aMsg, aArgs ) );
  }

  /**
   * Завершить выполнение команды без формирования результата
   *
   * @throws TsIllegalStateRtException команда в данный момент не выполняется
   * @throws TsIllegalStateRtException результат уже сформирован. Коррекция запрещена
   * @throws TsIllegalArgumentRtException команда должна формировать результат
   */
  protected void resultOk() {
    TsIllegalStateRtException.checkNull( result, ERR_CMD_NOT_EXECUTE, id() );
    IPlexyType resultType = resultType();
    if( resultType != IPlexyType.NONE ) {
      throw new TsIllegalArgumentRtException( ERR_RESULT_MUST_BE, id() );
    }
    result.ok( IPlexyValue.NULL );
  }

  /**
   * Завершить выполнение команды с формированием атомарного результата
   *
   * @param aValue {@link IPlexyValue} - значение результата выполнения команды
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException команда в данный момент не выполняется
   * @throws TsIllegalStateRtException результат уже сформирован. Коррекция запрещена
   * @throws TsIllegalArgumentRtException тип результата не совместим с типом результата команды
   */
  protected void resultOk( IPlexyValue aValue ) {
    TsNullArgumentRtException.checkNull( aValue );
    TsIllegalStateRtException.checkNull( result, ERR_CMD_NOT_EXECUTE, id() );
    result.ok( aValue );
  }

  /**
   * Прервать выполнение команды с ошибкой выполнения (без результата)
   *
   * @throws TsIllegalStateRtException команда в данный момент не выполняется
   * @throws TsIllegalStateRtException результат уже сформирован. Коррекция запрещена
   */
  protected void resultFail() {
    TsIllegalStateRtException.checkNull( result, ERR_CMD_NOT_EXECUTE, id() );
    result.fail();
  }

  /**
   * Печать стека ошибки в результат
   *
   * @param aError {@link Throwable} - ошибка. null: ничего не делает
   */
  protected void errorToResult( Throwable aError ) {
    if( aError == null ) {
      return;
    }
    StackTraceElement[] trace = aError.getStackTrace();
    for( StackTraceElement item : trace ) {
      addResultError( item.toString() + IStrioHardConstants.CHAR_EOL );
    }
    if( aError.getCause() != null ) {
      errorToResult( aError.getCause() );
    }
  }

  // ------------------------------------------------------------------------------------
  // Взаимодействие с клиентом запросившим выполнение команды
  //
  /**
   * Вызывается перед началом выполнения задачи.
   * <p>
   *
   * @param aMessage {@link ValidationResult} - сообщение пользователю
   * @param aStepsCount long - ожидаемое количество шагов выполнения (0 означает, что количество шагов непредсказуемо)
   * @param aStartDefault boolean <b>true</b> задача предлагает начать выполнение; <b>false</b> задача предлагает
   *          отменить выполнение
   * @return boolean <b>true</b> продолжить выполнение задачи; <b>false</b> отменить выполнение задачи.
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда в данный момент не выполняется
   */
  protected boolean beforeStartConfirm( ValidationResult aMessage, long aStepsCount, boolean aStartDefault ) {
    TsNullArgumentRtException.checkNull( aMessage );
    TsIllegalStateRtException.checkNull( result );
    IListEdit<ValidationResult> messages = new ElemArrayList<>( aMessage );
    needClientNotify = callback.beforeStart( messages, aStepsCount, aStartDefault );
    return needClientNotify;
  }

  /**
   * Вызывается перед началом выполнения задачи.
   *
   * @param aMessages {@link IList}&lt;{@link ValidationResult}&gt; - сообщения пользователю
   * @param aStepsCount long - ожидаемое количество шагов выполнения (0 означает, что количество шагов непредсказуемо)
   * @param aStartDefault boolean <b>true</b> задача предлагает начать выполнение; <b>false</b> задача предлагает
   *          отменить выполнение
   * @return boolean <b>true</b> продолжить выполнение задачи; <b>false</b> отменить выполнение задачи.
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException команда в данный момент не выполняется
   */
  protected boolean beforeStartConfirm( IList<ValidationResult> aMessages, long aStepsCount, boolean aStartDefault ) {
    TsNullArgumentRtException.checkNull( aMessages );
    TsIllegalStateRtException.checkNull( result );
    needClientNotify = callback.beforeStart( aMessages, aStepsCount, aStartDefault );
    return needClientNotify;
  }

  /**
   * Запросить у клиента подтверждение с указанием типа причины
   *
   * @param aMessage {@link ValidationResult} - сообщение поясняющее причину запроса
   * @param aDefault boolean значение по умолчанию
   * @return boolean <b>true</b> клиент требует продолжить выполнение; <b>false</b> клиент требует остановить выполнение
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected boolean queryClientConfirm( ValidationResult aMessage, boolean aDefault ) {
    TsNullArgumentRtException.checkNulls( aMessage );
    IListEdit<ValidationResult> messages = new ElemArrayList<>( aMessage );
    return queryClientConfirm( messages, aDefault );
  }

  /**
   * Запросить у клиента подтверждение с указанием типа причины
   *
   * @param aMessages {@link IList}&lt;{@link ValidationResult}&gt; - сообщения пользователю
   * @param aDefault boolean значение по умолчанию
   * @return boolean <b>true</b> клиент требует продолжить выполнение; <b>false</b> клиент требует остановить выполнение
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected boolean queryClientConfirm( IList<ValidationResult> aMessages, boolean aDefault ) {
    TsNullArgumentRtException.checkNulls( aMessages );
    // Создание типа значения
    IOptionSetEdit constraints = new OptionSet();
    constraints.setStr( TSID_DEFAULT_VALUE, (aDefault ? CHAR_YES : CHAR_NO) );
    // Создание возможных значений
    IAtomicValue dataValueYes = AvUtils.avStr( CHAR_YES );
    IAtomicValue dataValueNo = AvUtils.avStr( CHAR_NO );
    IPlexyValue cmdValueYes = PlexyValueUtils.pvSingleValue( dataValueYes );
    IPlexyValue cmdValueNo = PlexyValueUtils.pvSingleValue( dataValueNo );
    IList<IPlexyValue> possibles = new ElemArrayList<>( cmdValueYes, cmdValueNo );
    // Запрос значения у клиента
    IDataType dataType = new DataType( EAtomicType.STRING, constraints );
    IPlexyType cmdValueType = PlexyValueUtils.ptSingleValue( dataType );
    IPlexyValue confirm = callback.getValue( cmdValueType, possibles, aMessages );
    if( confirm == IPlexyValue.NULL ) {
      // Клиент отказался предоставить значение
      return aDefault;
    }
    return CHAR_YES.equals( confirm.singleValue().asString() );
  }

  // ------------------------------------------------------------------------------------
  // Клонирование команды: используется библиотекой команд для поддержки вложенности вызовов при выполнении скриптов
  // (batch)
  //
  @Override
  public Object clone()
      throws CloneNotSupportedException {
    return super.clone();
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  //
  /**
   * Проверяет параметры аргумента на возможность добавления в команду
   *
   * @param aArgId String - идентификатор аргумента или его алиас
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор аргумента или его алиас должен быть ИД-путем или пустой строкой
   * @throws TsItemAlreadyExistsRtException аргумент уже существует
   */
  private void checkArg( String aArgId ) {
    TsNullArgumentRtException.checkNull( aArgId );
    if( !aArgId.equals( EMPTY_STRING ) ) {
      // mvk 2018-11-07
      // boolean valid = StridUtils.isValidIdName( aArgId );
      boolean valid = StridUtils.isValidIdPath( aArgId );
      TsIllegalArgumentRtException.checkFalse( valid, ERR_ARG_ID_MUST_NAME, id(), aArgId );
    }
    TsItemAlreadyExistsRtException.checkTrue( argMap.hasKey( aArgId ), ERR_ARG_ALREADY_EXIST, id(), aArgId );
  }
}
