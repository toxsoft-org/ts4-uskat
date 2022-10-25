package org.toxsoft.uskat.skadmin.core.impl;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.core.impl.IAdminResources.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedBundleList;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringLinkedBundleList;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.legacy.plexy.*;
import org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils;
import org.toxsoft.uskat.s5.client.remote.connection.EConnectionState;
import org.toxsoft.uskat.s5.client.remote.connection.IS5Connection;
import org.toxsoft.uskat.skadmin.core.*;

/**
 * Менеджер библиотек
 *
 * @author mvk
 */
class AdminCmdLibraryManager
    implements IAdminCmdLibrary {

  /**
   * Список подключенных библиотек
   */
  private final IListEdit<IAdminCmdLibrary> libraries = new ElemLinkedBundleList<>();

  /**
   * Карта библиотек исполнения команд. Ключ: идентификатор или алиас команды, значение библиотека ее выполнения
   */
  private final IStringMapEdit<IAdminCmdLibrary> libraryByCmds = new StringMap<>();

  /**
   * Карта описаний команд. Ключ: идентификатор или алиас команды, значение ее описание
   */
  private final IStringMapEdit<IAdminCmdDef> cmdMap  = new StringMap<>();
  /**
   * Список описаний команд.
   */
  private final IListEdit<IAdminCmdDef>      cmdList = new ElemLinkedList<>();

  /**
   * Текущий контекст выполнения команд
   */
  private IAdminCmdContext context = new AdminCmdContext();

  /**
   * Журнал
   */
  private ILogger logger = getLogger( getClass() );

  /**
   * Конструктор
   *
   * @param aInitLibraries {@link IList}&lt;{@link IAdminCmdLibrary}&gt; - список библиотек подключаемых прямым вызовом
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException ошибка конфигурирования плагинов
   * @throws TsItemAlreadyExistsRtException команда плагина уже существует в библиотеке
   */
  AdminCmdLibraryManager( IList<IAdminCmdLibrary> aInitLibraries ) {
    libraries.addAll( aInitLibraries );
    // Формирование списка команд библиотеки
    updateLibraryCmds();
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IAdminCmdLibrary
  //
  @Override
  public String getName() {
    return getClass().getName();
  }

  @Override
  public void init() {
    // nop
  }

  @Override
  public boolean isClosed() {
    return false;
  }

  @Override
  public void close() {
    libraryByCmds.clear();
    cmdMap.clear();
    cmdList.clear();
    for( IAdminCmdLibrary library : libraries ) {
      logger.debug( MSG_LIBRARY_CLOSE, library.getName() );
      library.close();
    }
  }

  @Override
  public IAdminCmdDef findCommand( String aCmdId ) {
    StridUtils.checkValidIdPath( aCmdId );
    return cmdMap.findByKey( aCmdId );
  }

  @Override
  public IList<IAdminCmdDef> availableCmds() {
    return cmdList;
  }

  @Override
  public IAdminCmdContext context() {
    return context;
  }

  @Override
  public void setContext( IAdminCmdContext aContext ) {
    TsNullArgumentRtException.checkNull( aContext );
    context = aContext;
    for( IAdminCmdLibrary library : libraries ) {
      library.setContext( aContext );
      logger.debug( MSG_LIBRARY_CONTEXT, library.getName() );
    }
  }

  @Override
  public IAdminCmdResult exec( String aCmdId, IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback ) {
    TsNullArgumentRtException.checkNulls( aCmdId, aArgValues, aCallback );
    // Получение библиотеки выполняющей команду
    IAdminCmdLibrary library = libraryByCmds.findByKey( aCmdId );
    TsItemNotFoundRtException.checkNull( library, ERR_CMD_NOT_FOUND, aCmdId );
    // Команда для выполнения
    IAdminCmdDef cmd = cmdMap.getByKey( aCmdId );
    // Проверка аргументов команды с возможным дополнением аргументами по умолчанию
    StringMap<IPlexyValue> argValues = new StringMap<>( aArgValues );
    prepareArgs( context, cmd, argValues, true );
    // Выполнение команды
    IAdminCmdResult retValue = library.exec( aCmdId, argValues, aCallback );
    return retValue;
  }

  @Override
  public IList<IPlexyValue> getPossibleValues( String aCmdId, String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    TsNullArgumentRtException.checkNulls( aCmdId, aArgId, aArgValues );
    // Получение библиотеки выполняющей команду
    IAdminCmdLibrary library = libraryByCmds.findByKey( aCmdId );
    TsItemNotFoundRtException.checkNull( library, ERR_CMD_NOT_FOUND, aCmdId );
    // Получение описания команды аргумента
    IAdminCmdDef cmdDef = cmdMap.getByKey( aCmdId );
    IAdminCmdArgDef argDef = cmdDef.findArgument( aArgId );
    // Проверка что запрашиваемый аргумент существует в описании команды
    TsItemNotFoundRtException.checkNull( argDef, ERR_ARG_NOT_FOUND, aCmdId, aArgId );
    // Проверка аргументов команды с возможным дополнением аргументами по умолчанию
    StringMap<IPlexyValue> args = new StringMap<>( aArgValues );
    prepareArgs( context, cmdDef, args, false );
    // Делегирование метода библиотеке
    return library.getPossibleValues( aCmdId, aArgId, aArgValues );
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  //
  /**
   * Формирование списка команд библиотеки
   */
  void updateLibraryCmds() {
    libraryByCmds.clear();
    cmdMap.clear();
    cmdList.clear();
    for( IAdminCmdLibrary library : libraries ) {
      // Установка контекста команд
      library.setContext( context );
      // Загрузка команд плагина
      for( IAdminCmdDef cmdDef : library.availableCmds() ) {
        addLibraryCmd( library, cmdDef, cmdDef.id() );
        String alias = cmdDef.alias();
        if( alias != null && !EMPTY_STRING.equals( alias ) ) {
          addLibraryCmd( library, cmdDef, alias );
        }
        cmdList.add( cmdDef );
      }
    }
  }

  /**
   * Добавляет команду плагина в библиотеку
   *
   * @param aLibrary {@link IAdminCmdLibrary} - библиотека в которой размещена команда
   * @param aCmdDef {@link IAdminCmdDef} - описание команды
   * @param aCmdId String - идентификатор команды или ее алиас
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsItemAlreadyExistsRtException команда уже существует
   * @throws TsIllegalArgumentRtException неполное определение команды
   */
  private void addLibraryCmd( IAdminCmdLibrary aLibrary, IAdminCmdDef aCmdDef, String aCmdId ) {
    TsNullArgumentRtException.checkNulls( aLibrary, aCmdDef, aCmdId );
    // Проверка того, что команда не регистрируется несколько раз
    IAdminCmdLibrary existLibrary = libraryByCmds.findByKey( aCmdId );
    if( existLibrary != null ) {
      String existLibraryName = existLibrary.getName();
      throw new TsItemAlreadyExistsRtException( ERR_CMD_DOUBLE_DEFINE, aCmdId, aLibrary.getName(), existLibraryName );
    }
    if( aCmdDef.resultType() != IPlexyType.NONE && aCmdDef.resultDescription().equals( EMPTY_STRING ) ) {
      throw new TsIllegalArgumentRtException( ERR_CMD_NOT_RESULT_DESCR, aCmdId, aCmdDef.resultType() );
    }
    // Проверка доступности команды для пользователя
    IStringList cmdRoles = aCmdDef.roles();
    if( cmdRoles.size() > 0 && !hasIntersection( cmdRoles, getUserRoles() ) ) {
      // Нет пересечения по ролям. Команда не добавляется в библиотеку
      return;
    }
    // Размещение команды в библиотеке
    libraryByCmds.put( aCmdId, aLibrary );
    cmdMap.put( aCmdId, aCmdDef );
  }

  /**
   * Возвращает список ролей текущего пользователя соединения
   *
   * @return {@link IStringList} список ролей пользователя соединения. Пустой список если нет соединения
   */
  private IStringList getUserRoles() {
    IStringListEdit retValue = new StringLinkedBundleList();
    if( !context.hasParam( CTX_CONNECTION ) ) {
      // В контексте нет параметра соединения с сервером
      return retValue;
    }
    IS5Connection connection = (IS5Connection)context.paramValue( CTX_CONNECTION ).singleRef();
    if( connection.state() != EConnectionState.CONNECTED ) {
      return retValue;
    }
    // TODO: 2021-01-03 mvk
    // IServerApi serverApi = connection.serverApi();
    // long userId = serverApi.sessionInfo().userId();
    // ILongList objIds = serverApi.linkService().getLinks( userId, IUser.LINK_ID_ROLES );
    // IList<IBsObject> bsList = serverApi.objectService().getObjects( objIds );
    // for( IBsObject bs : bsList ) {
    // IUserRole role = (IUserRole)bs;
    // retValue.add( role.strid() );
    // }
    return retValue;
  }

  /**
   * Определяет есть ли пересечение двух множеств состоящих из списка строк
   *
   * @param aList1 {@link IStringList} - список 1
   * @param aList2 {@link IStringList} - список 2
   * @return boolean <b>true</b> есть пересечение; <b>false</b> нет пересечения.
   * @throws TsNullArgumentRtException любой аргумент null
   */
  private static boolean hasIntersection( IStringList aList1, IStringList aList2 ) {
    TsNullArgumentRtException.checkNulls( aList1, aList2 );
    for( String item : aList1 ) {
      if( aList2.hasElem( item ) ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Подготавливает аргументы для выполнения команды
   *
   * @param aContext {@link IAdminCmdContext} текущий контекст команд
   * @param aCmdDef {@link IAdminCmdDef} - описание команды
   * @param aArgValues {@link IStringMapEdit} - аргументы команды с возможностью редактирования
   * @param aCheckPresent boolean <b>true</b> проверять существование значения аргумента; <b>false</b> не проверять
   *          существование аргумента.
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsItemNotFoundRtException аргумент не найден
   * @throws TsIllegalArgumentRtException тип аргумента не соответствует описанию аргументов команды
   */
  private static void prepareArgs( IAdminCmdContext aContext, IAdminCmdDef aCmdDef,
      IStringMapEdit<IPlexyValue> aArgValues, boolean aCheckPresent ) {
    TsNullArgumentRtException.checkNulls( aContext, aCmdDef, aArgValues );
    String cmdId = aCmdDef.id();
    // Список идентификаторов неизвестных аргументов
    IStringListEdit unknowArgs = new StringLinkedBundleList( aArgValues.keys() );
    for( IAdminCmdArgDef arg : aCmdDef.argumentDefs() ) {
      String argId = arg.id();
      // Определяем идентификатор через который фактически проводится адресация значения
      String factId = findIdOrNull( argId, arg.alias(), aArgValues.keys() );
      // Тип аргумента
      IPlexyType argType = arg.type();
      // Значение или null
      IPlexyValue argValue = (factId != null ? aArgValues.findByKey( factId ) : null);
      // Удаляем найденый иденификатор из списка неизвестных
      if( factId != null ) {
        unknowArgs.remove( factId );
      }
      // Если есть значение, проверим соответствие plexy-вида
      if( argValue != null && argValue.type().kind() != argType.kind() ) {
        throw new TsIllegalArgumentRtException( ERR_ARG_WRONG_TYPE, cmdId, argId, argType );
      }
      // Обработка значения по виду plexy-типа
      switch( arg.type().kind() ) {
        case SINGLE_VALUE:
          IOptionSet typeConstraints = arg.type().dataType().params();
          if( argValue == null && typeConstraints.hasValue( TSID_DEFAULT_VALUE ) ) {
            // Аргумента нет, но есть его значение по умолчанию
            IAtomicValue value = typeConstraints.getValue( TSID_DEFAULT_VALUE );
            argValue = PlexyValueUtils.pvSingleValue( value );
            // Размещаем аргумент в карте аргументов команды
            aArgValues.put( argId, argValue );
          }
          if( argValue == null ) {
            // Значение не представлено. Пробуем найти значение в контексте
            argValue = aContext.paramValueOrNull( argId );
            if( argValue == null ) {
              // Параметр не найден в контексте
              if( aCheckPresent ) {
                throw new TsIllegalArgumentRtException( ERR_CONTEXT_NOT_FOUND, argId );
              }
              continue;
            }
            // Проверка типа значения параметра найденого в контексте
            IPlexyType valueType = argValue.type();
            if( !argType.equals( valueType ) ) {
              throw new TsIllegalArgumentRtException( ERR_ARG_WRONG_CONTEXT_TYPE, cmdId, argId, valueType );
            }
            aArgValues.put( argId, argValue );
            continue;
          }
          // Проверка типа
          IPlexyType valueType = argValue.type();
          if( valueType.dataType().atomicType() != EAtomicType.NONE //
              && argType.dataType().atomicType() != EAtomicType.NONE //
              && !argType.dataType().atomicType().equals( valueType.dataType().atomicType() ) ) {
            throw new TsIllegalArgumentRtException( ERR_ARG_WRONG_TYPE, cmdId, argId, valueType );
          }
          continue;
        case SINGLE_REF:
          Class<?> argRefClass = arg.type().refClass();
          if( argValue == null ) {
            // Значение не представлено. Пробуем найти значение в контексте
            argValue = aContext.paramValueOrNull( argId );
            if( argValue == null ) {
              // Параметр не найден в контексте
              if( aCheckPresent ) {
                throw new TsIllegalArgumentRtException( ERR_CONTEXT_NOT_FOUND, argId );
              }
              continue;
            }
            if( argValue.type().kind() != EPlexyKind.SINGLE_REF ) {
              throw new TsIllegalArgumentRtException( ERR_ARG_WRONG_CONTEXT_TYPE, cmdId, argId, argValue.type() );
            }
            Class<?> valueRefClass = argValue.type().refClass();
            if( !argRefClass.isAssignableFrom( valueRefClass ) ) {
              throw new TsIllegalArgumentRtException( ERR_ARG_WRONG_CONTEXT_TYPE, cmdId, argId, valueRefClass );
            }
            aArgValues.put( argId, argValue );
            continue;
          }
          // Проверка типа
          Class<?> valueRefClass = argValue.type().refClass();
          if( !argRefClass.isAssignableFrom( valueRefClass ) ) {
            throw new TsIllegalArgumentRtException( ERR_ARG_WRONG_TYPE, cmdId, argId, valueRefClass );
          }
          continue;
        case VALUE_LIST:
          if( argValue == null ) {
            aArgValues.put( argId, PlexyValueUtils.pvValueList( argType ) );
            continue;
          }
          // Проверка типов элементов
          EAtomicType argItemType = arg.type().dataType().atomicType();
          for( int index = 0, n = argValue.valueList().size(); index < n; index++ ) {
            EAtomicType itemType = argValue.valueList().get( index ).atomicType();
            if( argItemType != itemType ) {
              throw new TsIllegalArgumentRtException( ERR_ARG_WRONG_ITEM_TYPE, cmdId, argId, argItemType );
            }
          }
          continue;
        case OPSET:
          if( argValue == null ) {
            aArgValues.put( argId, PlexyValueUtils.pvOpset( IOptionSet.NULL ) );
          }
          continue;
        case REF_LIST:
          if( argValue == null ) {
            aArgValues.put( argId, PlexyValueUtils.pvRefList( argType, IList.EMPTY ) );
            continue;
          }
          // Проверка типов элементов
          Class<?> argItemRefClass = arg.type().refClass();
          for( int index = 0, n = argValue.refList().size(); index < n; index++ ) {
            Class<?> itemRefClass = argValue.refList().get( index ).getClass();
            if( !argItemRefClass.isAssignableFrom( itemRefClass ) ) {
              throw new TsIllegalArgumentRtException( ERR_ARG_WRONG_ITEM_TYPE, cmdId, argId, argItemRefClass );
            }
          }
          continue;
        default:
          throw new TsNotAllEnumsUsedRtException();
      }
    }
    if( unknowArgs.size() > 0 ) {
      // Обработка неизвестных аргументов
      StringBuilder sb = new StringBuilder();
      for( int index = 0, n = unknowArgs.size(); index < n; index++ ) {
        sb.append( CHAR_APOSTROPHE );
        sb.append( unknowArgs.get( index ) );
        sb.append( CHAR_APOSTROPHE );
        if( index < n - 1 ) {
          sb.append( CHAR_COMMA );
        }
      }
      throw new TsItemNotFoundRtException( ERR_ARG_UNKNOW_ARGS, cmdId, sb.toString() );
    }
  }

  /**
   * Ищет в указанном списке идентификатор или его алиас.
   *
   * @param aId String - иденитфикатор
   * @param aAlias String - алиас
   * @param aIds {@link IStringList} список идентификаторов или алиасов.
   * @return String найденный идентификатор или alias
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException в списке найдены и идентификатор и алиас
   */
  private static String findIdOrNull( String aId, String aAlias, IStringList aIds ) {
    TsNullArgumentRtException.checkNull( aId, aAlias, aIds );
    boolean hasId = aIds.hasElem( aId );
    boolean hasAlias = aIds.hasElem( aAlias );
    if( hasId && hasAlias ) {
      throw new TsItemAlreadyExistsRtException( ERR_DOUBLE_ARG, aId, aAlias );
    }
    if( hasId ) {
      return aId;
    }
    if( hasAlias ) {
      return aAlias;
    }
    return null;
  }
}
