package org.toxsoft.uskat.skadmin.cli.parsers;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.bricks.validator.ValidationResult.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.skadmin.cli.parsers.AdminCmdLexicalParser.*;
import static org.toxsoft.uskat.skadmin.cli.parsers.IAdminResources.*;
import static org.toxsoft.uskat.skadmin.core.impl.AdminCmdLibraryUtils.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.chario.impl.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.legacy.plexy.*;
import org.toxsoft.uskat.legacy.plexy.impl.*;
import org.toxsoft.uskat.skadmin.core.*;

/**
 * Вспомогательные методы парсирования строки консоли
 *
 * @author mvk
 */
public class AdminCmdParserUtils {

  private static final char CHAR_APOSTROPHE = '\'';

  /**
   * Суффиксы для неполного определения меток времени
   */
  private static String[] TIMESTAMP_SUFFIX = //
      { //
          "", //$NON-NLS-1$
          "-01-01_00:00:00.000", //$NON-NLS-1$
          "-01_00:00:00.000", //$NON-NLS-1$
          "_00:00:00.000", //$NON-NLS-1$
          ":00:00.000", //$NON-NLS-1$
          ":00.000", //$NON-NLS-1$
          ".000" //$NON-NLS-1$
      };

  /**
   * Разделители. Отличие от {@link IStrioHardConstants#DEFAULT_DELIMITER_CHARS} - добавлен пробел
   */
  private static final String READ_ATOMIC_VALUE_DELIMITER_CHARS =
      IStrioHardConstants.DEFAULT_DELIMITER_CHARS + IStrioHardConstants.CHAR_SPACE;

  /**
   * Определяет имена параметров контекста из потока лексем
   * <p>
   * Определение имен идет с указанного индекса списка лексем и заканчивается на любой лексеме не имеющей тип
   * {@link ETokenType#CONTEXT} или {@link ETokenType#LIST_CONTEXT}.
   *
   * @param aTokens {@link IList} &lt;{@link IAdminCmdToken}&gt; - список лексем командной строки
   * @param aStartIndex int индекс в aTokens с которой проводится разбор параметров контекста
   * @return {@link IStringList} список имен параметров контекста. null: не найден один из параметров в контексте.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static IStringList contextParamsParse( IList<IAdminCmdToken> aTokens, int aStartIndex ) {
    TsNullArgumentRtException.checkNulls( aTokens );
    IStringListEdit paramNames = new StringLinkedBundleList();
    for( int index = aStartIndex, n = aTokens.size(); index < n; index++ ) {
      AdminCmdToken token = (AdminCmdToken)aTokens.get( index );
      if( token.type() != ETokenType.CONTEXT && token.type() != ETokenType.LIST_CONTEXT ) {
        // Поступают лексемы не имен контекста
        break;
      }
      String paramName = token.data().trim();
      if( paramName.length() == 0 ) {
        // Нет имени параметра, пустая строка
        continue;
      }
      if( paramName.charAt( 0 ) != CHAR_CONTEXT_PREFIX ) {
        // Не параметр контекста
        break;
      }
      paramName = paramName.substring( 1 );
      paramNames.add( paramName );
    }
    return paramNames;
  }

  /**
   * Определяет описание команды из представленной лексемы
   *
   * @param aCmdToken {@link AdminCmdToken} - лексема представляющая идентификатор команды или ее алиас
   * @param aLibrary {@link IAdminCmdLibrary} - библиотека команд
   * @param aSectionId String - текущий раздел команд
   * @param aErrors {@link IListEdit}&lt;{@link ValidationResult}&gt; - список ошибок анализа
   * @return {@link IAdminCmdDef} описание команды. null: ошибка описания команды
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static IAdminCmdDef cmdParse( AdminCmdToken aCmdToken, IAdminCmdLibrary aLibrary, String aSectionId,
      IListEdit<ValidationResult> aErrors ) {
    TsNullArgumentRtException.checkNulls( aCmdToken, aLibrary, aSectionId, aErrors );
    ETokenType type = aCmdToken.type();
    // 2024-10-23 mvk
    // if( type != ETokenType.VALUE ) {
    if( type != ETokenType.ID ) {
      // Лексема команды должна представлять "значение", так как пользователь вводит ее без префиксов "--"
      aCmdToken.setType( ETokenType.UNDEF );
      aErrors.add( error( ERR_MSG_INVALID_CMD_ID ) );
      return null;
    }
    // Определяем существование команды в библиотеке
    String cmdId = aCmdToken.data();
    if( !StridUtils.isValidIdPath( cmdId ) ) {
      // Идентификатор команды должен представлять ИД-путь
      aCmdToken.setType( ETokenType.UNDEF );
      aErrors.add( error( ERR_MSG_INVALID_CMD_ID ) );
      return null;
    }
    IAdminCmdDef cmdDef = null;
    if( !aSectionId.equals( EMPTY_STRING ) ) {
      // Попробуем найти команд через текущий раздел
      cmdDef = aLibrary.findCommand( aSectionId + StridUtils.CHAR_ID_PATH_DELIMITER + cmdId );
    }
    if( cmdDef == null ) {
      // Пробуем найти команду через полный идентификатор
      cmdDef = aLibrary.findCommand( cmdId );
    }
    if( cmdDef == null ) {
      // Команда не существует
      aCmdToken.setType( ETokenType.UNDEF );
      aErrors.add( error( ERR_MSG_CMD_NOT_FOUND, cmdId ) );
      return null;
    }
    // Команда представляется как идентификатор
    aCmdToken.setType( ETokenType.ID );
    return cmdDef;
  }

  /**
   * Проверяет указанную лексемму на предмет того, что она представляет идентификатор или алиас команды
   *
   * @param aCmdToken {@link IAdminCmdToken} - лексема представляющая идентификатор команды или ее алиас
   * @param aLibrary {@link IAdminCmdLibrary} - библиотека команд
   * @param aSectionId String - текущий раздел команд
   * @return boolean <b>true</b> лексемма представляет идентификатор команды;<b>false</b> лексемма не представляет
   *         идентификатор команды.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static boolean isCmdToken( IAdminCmdToken aCmdToken, IAdminCmdLibrary aLibrary, String aSectionId ) {
    TsNullArgumentRtException.checkNulls( aCmdToken, aLibrary, aSectionId );
    ETokenType type = aCmdToken.type();
    if( type != ETokenType.VALUE ) {
      // Лексема команды должна представлять "значение", так как пользователь вводит ее без префиксов "--"
      return false;
    }
    // Определяем существование команды в библиотеке
    String cmdId = aCmdToken.data();
    if( !StridUtils.isValidIdPath( cmdId ) ) {
      // Идентификатор команды должен представлять ИД-путь
      return false;
    }
    IAdminCmdDef cmdDef = null;
    if( !aSectionId.equals( EMPTY_STRING ) ) {
      // Попробуем найти команд через текущий раздел
      cmdDef = aLibrary.findCommand( aSectionId + StridUtils.CHAR_ID_PATH_DELIMITER + cmdId );
    }
    if( cmdDef == null ) {
      // Пробуем найти команду через полный идентификатор
      cmdDef = aLibrary.findCommand( cmdId );
    }
    if( cmdDef == null ) {
      // Команда не существует
      return false;
    }
    // Команда представляется как идентификатор
    return true;
  }

  /**
   * Разбор аргументов команды определенных в упрощенной форме
   *
   * @param aTokens {@link IList} &lt;{@link IAdminCmdToken}&gt; - список лексем командной строки
   * @param aStartIndex индекс в списке лексем с которого проводить разбор аргументов
   * @param aLibrary {@link IAdminCmdLibrary} - библиотека команд
   * @param aCmdDef {@link IAdminCmdDef} - описание команды
   * @param aValues {@link IStringMapEdit}&lt;{@link IPlexyValue}&gt; - карта прочитанных значений аргументов. Ключ:
   *          идентификатор аргумента
   * @param aErrors {@link IListEdit}&lt;{@link ValidationResult}&gt; - список ошибок анализа
   * @throws TsNullArgumentRtException любой аргумент null
   */
  public static void simpleArgsParse( IList<IAdminCmdToken> aTokens, int aStartIndex, IAdminCmdLibrary aLibrary,
      IAdminCmdDef aCmdDef, IStringMapEdit<IPlexyValue> aValues, IListEdit<ValidationResult> aErrors ) {
    TsNullArgumentRtException.checkNulls( aTokens, aLibrary, aCmdDef );
    // Список аргументов с атомарными значениями
    IList<IAdminCmdArgDef> argDefs = getArgDefs( aCmdDef.argumentDefs(), false );
    int argDefQtty = argDefs.size();
    for( int index = aStartIndex; index < aTokens.size(); index++ ) {
      // Индекс текущего аргумента
      int argIndex = index - aStartIndex;
      // Преобразования к AdminCmdToken допустимы: AdminCmdLexicalParser агрегирован в класс
      AdminCmdToken argToken = (AdminCmdToken)aTokens.get( index );
      if( argIndex >= argDefQtty ) {
        Integer tokenIndex = Integer.valueOf( argToken.startIndex() );
        // Команда имеет меньше аргументов чем указано в командной строке
        argToken.setType( ETokenType.UNDEF );
        aErrors.add( error( ERR_MSG_VALUE_UNEXPECTED, tokenIndex, argToken.data() ) );
        continue;
      }
      // Описание аргумента
      IAdminCmdArgDef argDef = argDefs.get( argIndex );
      // Получаем список возможных значений аргумента
      IList<IPlexyValue> possibleValues = aLibrary.getPossibleValues( aCmdDef.id(), argDef.id(), aValues );
      // Считываем значения аргумента
      IAdminCmdContext context = aLibrary.context();
      index = argValueParse( context, aTokens, index, argDef.type(), possibleValues, argDef.id(), aValues, aErrors );
    }
  }

  /**
   * Разбор аргументов команды определенных в канонической форме
   *
   * @param aTokens {@link IList} &lt;{@link IAdminCmdToken}&gt; - список лексем командной строки
   * @param aStartIndex индекс в списке лексем с которого проводить разбор аргументов
   * @param aLibrary {@link IAdminCmdLibrary} - библиотека команд
   * @param aCmdDef {@link IAdminCmdDef} - описание команды
   * @param aValues {@link IStringMapEdit}&lt;{@link IPlexyValue}&gt; - карта прочитанных значений аргументов. Ключ:
   *          идентификатор аргумента
   * @param aErrors {@link IListEdit}&lt;{@link ValidationResult}&gt; - список ошибок анализа
   * @throws TsNullArgumentRtException любой аргумент null
   */
  public static void canonicArgsParse( IList<IAdminCmdToken> aTokens, int aStartIndex, IAdminCmdLibrary aLibrary,
      IAdminCmdDef aCmdDef, IStringMapEdit<IPlexyValue> aValues, IListEdit<ValidationResult> aErrors ) {
    TsNullArgumentRtException.checkNulls( aTokens, aLibrary, aCmdDef );
    // 1: первая лексема это команда
    for( int index = aStartIndex, n = aTokens.size(); index < n; index++ ) {
      // Преобразования к AdminCmdToken допустимы: AdminCmdLexicalParser агрегирован в класс
      AdminCmdToken argToken = (AdminCmdToken)aTokens.get( index );
      Integer tokenIndex = Integer.valueOf( argToken.startIndex() );
      if( argToken.type() != ETokenType.ID ) {
        // Ожидали идентификатор аргумента или его алиас
        argToken.setType( ETokenType.UNDEF );
        aErrors.add( error( ERR_MSG_ARG_ID_EXPECTED, tokenIndex ) );
        continue;
      }
      String argId = argToken.data();
      if( !StridUtils.isValidIdName( argId ) ) {
        // Невалидный идентификатор(ИД-имя) аргумента
        argToken.setType( ETokenType.UNDEF );
        aErrors.add( error( ERR_MSG_INVALID_ARG_ID, tokenIndex ) );
        continue;
      }
      // Список аргументов с атомарными значениями
      IList<IAdminCmdArgDef> argDefs = getArgDefs( aCmdDef.argumentDefs(), false );
      // Поиск аргумента по идентификатору или его алиасу
      IAdminCmdArgDef argDef = findArgDef( argDefs, argId );
      if( argDef == null ) {
        // Аргумент не существует
        argToken.setType( ETokenType.UNDEF );
        aErrors.add( error( ERR_MSG_ARG_NOT_FOUND, argId ) );
        continue;
      }
      if( aValues.findByKey( argId ) != null ) {
        // Аргумент уже был указан
        argToken.setType( ETokenType.UNDEF );
        aErrors.add( error( ERR_MSG_ARG_ALREADY_EXIST, argId ) );
        continue;
      }
      // Получаем список возможных значений аргумента
      IList<IPlexyValue> possibleValues = aLibrary.getPossibleValues( aCmdDef.id(), argDef.id(), aValues );
      // Следующая лексема. null: больше лексем нет
      AdminCmdToken nextArgToken = (index < n - 1 ? (AdminCmdToken)aTokens.get( index + 1 ) : null);
      // Признак того, что за ИД-именем аргумента следует значение
      boolean nextValueToken = (nextArgToken != null
          && (nextArgToken.type() == ETokenType.VALUE || nextArgToken.type() == ETokenType.LIST_VALUE));
      // Попытка прочитать значение аргумента как флаг
      IPlexyType type = argDef.type();
      if( !nextValueToken && //
          type.kind() == EPlexyKind.SINGLE_VALUE && //
          type.dataType().atomicType().equals( EAtomicType.BOOLEAN ) ) {
        if( !tryReadFlagArgValue( argDef, possibleValues, aValues, aErrors ) ) {
          // Недопустимое использование флага
          argToken.setType( ETokenType.UNDEF );
          aErrors.add( error( ERR_MSG_ARG_FLAG_INPOSSIBLE, argDef.id() ) );
          return;
        }
        // Значение аргумента считано как флаг
        continue;
      }
      if( index < n - 1 ) {
        // Считываем значение аргумента
        IAdminCmdContext context = aLibrary.context();
        index =
            argValueParse( context, aTokens, ++index, argDef.type(), possibleValues, argDef.id(), aValues, aErrors );
      }
    }
  }

  /**
   * Обработка аргумента представляющего флаг в канонической форме представления аргументов. Считается, что была найдена
   * лексема представляющая ИД-имя аргумента и в методе
   * {@link #tryReadFlagArgValue(IAdminCmdArgDef, IList, IStringMapEdit, IListEdit)} проверяется возможность
   * использования аргумента в качестве флага и если это допустимо, то добавляет в карту значений аргументов значение
   * аргумента как 'true'.
   *
   * @param aArgDef {@link IAdminCmdDef} описание аргумента
   * @param aPossibleValues {@link IList} &lt;{@link IPlexyValue}&gt; - список возможных значений. Пустой список без
   *          ограничений
   * @param aValueMap {@link IStringMapEdit}&lt;{@link IAtomicValue}&gt; - карта прочитанных значений. Ключ:
   *          идентификатор aValueKey
   * @param aErrors {@link IListEdit}&lt;{@link ValidationResult}&gt; - список ошибок анализа
   * @return boolean <b>true</b> считано значение флага; <b>false</b> ошибка чтения значения флага
   * @throws TsNullArgumentRtException любой аргумент null
   */
  private static boolean tryReadFlagArgValue( IAdminCmdArgDef aArgDef, IList<IPlexyValue> aPossibleValues,
      IStringMapEdit<IPlexyValue> aValueMap, IListEdit<ValidationResult> aErrors ) {
    TsNullArgumentRtException.checkNulls( aArgDef, aPossibleValues, aValueMap, aErrors );

    // 2022-09-10 mvk
    // IPlexyType type = aArgDef.type();
    // IDataType dataType = type.dataType();
    // // Получаем логическое значение флага через значение по умолчанию(если оно есть)
    // boolean value = false;
    // IOptionSet constraints = dataType.params();
    // if( constraints.hasValue( IAvMetaConstants.DDEF_DEFAULT_VALUE ) ) {
    // value = constraints.getBool( IAvMetaConstants.DDEF_DEFAULT_VALUE );
    // }
    // // Инвертируем значение флага
    // IPlexyValue cmdValue = PlexyValueUtils.pvSingleValue( avBool( !value ) );
    IPlexyValue cmdValue = PlexyValueUtils.pvSingleValue( avBool( true ) );

    // Проверяем использования значения флага с другими значениями аргументов
    if( aPossibleValues.size() > 0 ) {
      for( int index = 0, n = aPossibleValues.size(); index < n; index++ ) {
        if( cmdValue.equals( aPossibleValues.get( index ) ) ) {
          aValueMap.put( aArgDef.id(), cmdValue );
          return true;
        }
      }
      // Несмотря на то, что аргумент может оцениваться как флаг, текущее состояние команды(состав уже определенных
      // аргументов) не позволяют установить значение аргумента флага
      return false;
    }
    // Добавляем значение аргумента-флага
    aValueMap.put( aArgDef.id(), cmdValue );
    return true;
  }

  /**
   * Считывает и интерпретирует из потока лексем лексемы-значения указанного аргумента указанной команды
   *
   * @param aContext {@link IAdminCmdContext} контекст выполнения команд
   * @param aTokens {@link IList}&lt;{@link IAdminCmdToken}&gt; - поток лексем
   * @param aIndex int - индекс текущей лексемы в потоке
   * @param aType {@link IPlexyType} - тип читаемого значения
   * @param aPossibleValues {@link IList} &lt;{@link IPlexyValue}&gt; - список возможных значений. Пустой список без
   *          ограничений
   * @param aKey String - ключ значения под которым будет размещено значение в карте aValues
   * @param aValueMap {@link IStringMapEdit}&lt;{@link IAtomicValue}&gt; - карта прочитанных значений. Ключ:
   *          идентификатор aValueKey
   * @param aErrors {@link IListEdit}&lt;{@link ValidationResult}&gt; - список ошибок анализа
   * @return int индекс лексемы в потоке лексем на котором было завершено чтение значений аргумента
   */
  public static int argValueParse( IAdminCmdContext aContext, IList<IAdminCmdToken> aTokens, int aIndex,
      IPlexyType aType, IList<IPlexyValue> aPossibleValues, String aKey, IStringMapEdit<IPlexyValue> aValueMap,
      IListEdit<ValidationResult> aErrors ) {
    TsNullArgumentRtException.checkNulls( aContext, aTokens, aType, aPossibleValues, aKey, aValueMap );
    int index = aIndex;
    AdminCmdToken argToken = (AdminCmdToken)aTokens.get( index );
    Integer tokenIndex = Integer.valueOf( argToken.startIndex() );
    IPlexyValue value = null;
    switch( argToken.type() ) {
      case UNDEF:
        // Неизвестный тип лексемы не может представлять значение
        return index;
      case STATEMENT:
      case ID:
        // Ожидалось значение аргумента
        argToken.setType( ETokenType.UNDEF );
        aErrors.add( error( ERR_MSG_VALUE_EXPECTED, tokenIndex, aKey ) );
        return index;
      case CONTEXT:
      case VALUE:
        if( aType.dataType().atomicType() == EAtomicType.NONE ) {
          // Тип читаемого значения не установлен. Читаем что получится
          value = argSingleValueParse( aContext, argToken, aType, aKey, aPossibleValues, aErrors );
          break;
        }
        switch( aType.kind() ) {
          case SINGLE_VALUE:
            value = argSingleValueParse( aContext, argToken, aType, aKey, aPossibleValues, aErrors );
            break;
          case VALUE_LIST:
            // Аргумент представлен одиночным значением или это одно (в смысле больше не будет) значение массива
            IListEdit<IAdminCmdToken> argTokens = new ElemLinkedList<>( argToken );
            value = argListValueParse( aContext, argTokens, aType, aKey, aPossibleValues, aErrors );
            break;
          case OPSET:
            // Значение аргумента должно быть именованным значением
            argToken.setType( ETokenType.UNDEF );
            aErrors.add( error( ERR_MSG_VALUE_MUST_BE_NAMED, aKey ) );
            return index;
          case REF_LIST:
          case SINGLE_REF:
            // Не ожидаем появления аргументов вида объектных ссылок
            throw new TsInternalErrorRtException( ERR_MSG_REF_ARG_UNEXPECTED, Long.valueOf( argToken.startIndex() ) );
          default:
            throw new TsNotAllEnumsUsedRtException();
        }
        break;
      case LIST_CONTEXT:
      case LIST_VALUE:
        if( aType == IPlexyType.NONE ) {
          // Тип читаемого значения не установлен. Читаем что получится
          // Собираем лексемы представляющие значения списка
          IListEdit<IAdminCmdToken> argTokens = new ElemLinkedList<>( argToken );
          while( index + 1 < aTokens.size() ) {
            argToken = (AdminCmdToken)aTokens.get( ++index );
            if( (argToken.type() != ETokenType.LIST_CONTEXT && argToken.type() != ETokenType.LIST_VALUE)
                || argToken.listIndex() == 0 ) {
              index--;
              break;
            }
            argTokens.add( argToken );
          }
          value = argListValueParse( aContext, argTokens, aType, aKey, aPossibleValues, aErrors );
          break;
        }
        switch( aType.kind() ) {
          case SINGLE_VALUE:
            // Значение аргумента должно быть одиночным значением
            argToken.setType( ETokenType.UNDEF );
            aErrors.add( error( ERR_MSG_VALUE_MUST_BE_SINGLE, aKey ) );
            return index;
          case VALUE_LIST:
            // Собираем лексемы представляющие значения списка
            IListEdit<IAdminCmdToken> argTokens = new ElemLinkedList<>( argToken );
            while( index + 1 < aTokens.size() ) {
              argToken = (AdminCmdToken)aTokens.get( ++index );
              if( (argToken.type() != ETokenType.LIST_CONTEXT && argToken.type() != ETokenType.LIST_VALUE)
                  || argToken.listIndex() == 0 ) {
                index--;
                break;
              }
              argTokens.add( argToken );
            }
            value = argListValueParse( aContext, argTokens, aType, aKey, aPossibleValues, aErrors );
            break;
          case OPSET:
            // Значение аргумента должно быть именованным значением
            argToken.setType( ETokenType.UNDEF );
            aErrors.add( error( ERR_MSG_VALUE_MUST_BE_NAMED, aKey ) );
            return index;
          case REF_LIST:
          case SINGLE_REF:
            // Не ожидаем появления аргументов вида объектных ссылок
            throw new TsInternalErrorRtException( ERR_MSG_REF_ARG_UNEXPECTED, Long.valueOf( argToken.startIndex() ) );
          default:
            throw new TsNotAllEnumsUsedRtException();
        }
        break;
      case NAMED_VALUE:
        if( aType == IPlexyType.NONE ) {
          // Тип читаемого значения не установлен. Читаем что получится
          // Собираем лексемы представляющие все именованные значения
          IListEdit<IAdminCmdToken> argTokens = new ElemLinkedList<>( argToken );
          while( index + 1 < aTokens.size() ) {
            argToken = (AdminCmdToken)aTokens.get( ++index );
            if( argToken.type() == ETokenType.UNDEF ) {
              // Не ожидаем появления лексем с неизвестным типом
              aErrors.add( error( ERR_MSG_NAMED_ARG_UNEXPECTED, Long.valueOf( argToken.startIndex() ) ) );
              return index;
            }
            if( argToken.type() != ETokenType.NAMED_VALUE ) {
              index--;
              break;
            }
            argTokens.add( argToken );
          }
          value = argOptionSetParse( aContext, argTokens, aKey, aPossibleValues, aErrors );
          break;
        }
        switch( aType.kind() ) {
          case SINGLE_VALUE:
            // Значение аргумента должно быть одиночным значением
            argToken.setType( ETokenType.UNDEF );
            aErrors.add( error( ERR_MSG_VALUE_MUST_BE_SINGLE, aKey ) );
            return index;
          case VALUE_LIST:
            // Значение аргумента должно быть списком значений
            argToken.setType( ETokenType.UNDEF );
            aErrors.add( error( ERR_MSG_VALUE_MUST_BE_LIST, aKey ) );
            return index;
          case OPSET:
            // Собираем лексемы представляющие все именованные значения
            IListEdit<IAdminCmdToken> argTokens = new ElemLinkedList<>( argToken );
            while( index + 1 < aTokens.size() ) {
              argToken = (AdminCmdToken)aTokens.get( ++index );
              if( argToken.type() == ETokenType.UNDEF ) {
                // Не ожидаем появления лексем с неизвестным типом
                aErrors.add( error( ERR_MSG_NAMED_ARG_UNEXPECTED, Long.valueOf( argToken.startIndex() ) ) );
                return index;
              }
              if( argToken.type() != ETokenType.NAMED_VALUE ) {
                index--;
                break;
              }
              argTokens.add( argToken );
            }
            value = argOptionSetParse( aContext, argTokens, aKey, aPossibleValues, aErrors );
            break;
          case REF_LIST:
          case SINGLE_REF:
            // Не ожидаем появления аргументов вида объектных ссылок
            throw new TsInternalErrorRtException( ERR_MSG_REF_ARG_UNEXPECTED, Long.valueOf( argToken.startIndex() ) );
          default:
            throw new TsNotAllEnumsUsedRtException();
        }
        break;
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
    if( value != null ) {
      aValueMap.put( aKey, value );
    }
    return index;
  }

  /**
   * Разбор лексемы одиночного значения аргумента команды
   *
   * @param aContext {@link IAdminCmdContext} контекст выполнения команд
   * @param aToken {@link IAdminCmdToken} - лексема представляющая значение
   * @param aType {@link IPlexyType} - тип читаемого значения
   * @param aName String - удобочитаемое имя читаемого значения
   * @param aPossibleValues {@link IList} &lt;{@link IPlexyValue}&gt; - список возможных значений. Пустой список без
   *          ограничений
   * @param aErrors {@link IListEdit}&lt;{@link ValidationResult}&gt; - список ошибок анализа
   * @return {@link IPlexyValue} значение аргумента. null: ошибка получения значения
   * @throws TsNullArgumentRtException любой аргумент null
   */
  private static IPlexyValue argSingleValueParse( IAdminCmdContext aContext, IAdminCmdToken aToken, IPlexyType aType,
      String aName, IList<IPlexyValue> aPossibleValues, IListEdit<ValidationResult> aErrors ) {
    TsNullArgumentRtException.checkNulls( aContext, aToken, aType, aPossibleValues, aName, aErrors );
    // Чтение значения
    IAtomicValue datavalue = valueParse( aContext, aToken, aType.dataType(), aName, aErrors );
    if( datavalue == null ) {
      // Ошибка чтения значения
      return null;
    }
    // mvk new:
    IPlexyType type = aType;
    if( type == IPlexyType.NONE ) {
      type = PlexyValueUtils.ptSingleValue( new DataType( datavalue.atomicType(), IOptionSet.NULL ) );
    }
    IPlexyValue value = PlexyValueUtils.pvSingleValue( datavalue );
    if( aPossibleValues.size() > 0 ) {
      // Преобразования к AdminCmdToken допустимы: AdminCmdLexicalParser агрегирован в класс
      AdminCmdToken token = (AdminCmdToken)aToken;
      for( int index = 0, n = aPossibleValues.size(); index < n; index++ ) {
        if( value.equals( aPossibleValues.get( index ) ) ) {
          // Значение найдено в списке возможных значений
          return value;
        }
      }
      // Значение не найдено в списке возможных значений
      token.setType( ETokenType.UNDEF );
      // Формирование строкового представления списка возможных значений
      StringBuilder sbPossible = new StringBuilder();
      for( int index = 0, n = aPossibleValues.size(); index < n; index++ ) {
        sbPossible.append( CHAR_APOSTROPHE );
        sbPossible.append( aPossibleValues.get( index ).toString() );
        sbPossible.append( CHAR_APOSTROPHE );
        if( index < n - 1 ) {
          sbPossible.append( IStrioHardConstants.CHAR_ITEM_SEPARATOR );
        }
      }
      aErrors.add( error( ERR_MSG_UNPOSSIBLE_VALUE, value, aName, sbPossible.toString() ) );
      return null;
    }
    return value;
  }

  /**
   * Разбор лексем списка атомарных значений аргумента команды
   *
   * @param aContext {@link IAdminCmdContext} контекст выполнения команд
   * @param aArgTokens {@link IList}&lt;{@link IAdminCmdToken}&gt; - лексемы представляющие значения аргумента
   * @param aType {@link IPlexyType} - тип читаемого значения
   * @param aName String - удобочитаемое имя читаемого значения
   * @param aPossibleValues {@link IList} &lt;{@link IPlexyValue}&gt; - список возможных значений. Пустой список без
   *          ограничений
   * @param aErrors {@link IListEdit}&lt;{@link ValidationResult}&gt; - список ошибок анализа
   * @return {@link IPlexyValue} значение аргумента. null: ошибка получения значений
   * @throws TsNullArgumentRtException любой аргумент null
   */
  private static IPlexyValue argListValueParse( IAdminCmdContext aContext, IList<IAdminCmdToken> aArgTokens,
      IPlexyType aType, String aName, IList<IPlexyValue> aPossibleValues, IListEdit<ValidationResult> aErrors ) {
    TsNullArgumentRtException.checkNulls( aContext, aArgTokens, aType, aPossibleValues, aName, aErrors );
    IDataType dataType = aType.dataType();
    // Чтение значения
    int tokenQtty = aArgTokens.size();
    IAtomicValue[] datavalues = new IAtomicValue[tokenQtty];
    // Признак появлений ошибок при чтении элементов значения
    boolean readValueError = false;
    for( int index = 0; index < tokenQtty; index++ ) {
      datavalues[index] = valueParse( aContext, aArgTokens.get( index ), dataType, aName, aErrors );
      readValueError = (readValueError | datavalues[index] == null);
    }
    if( readValueError ) {
      // Были ошибки чтения единичных значений. Дальнейший разбор не имеет смысла
      return null;
    }
    IPlexyType type = aType;
    if( aType == IPlexyType.NONE && datavalues.length > 0 ) {
      // Тип читаемого значения не установлен. Устанавливаем тип из первого элемента (если он есть)
      type = PlexyValueUtils.ptValueList( dataType );
    }
    IPlexyValue value = PlexyValueUtils.pvValueList( type, datavalues );
    // Список возможных значений. Пустой список: без ограничений
    if( aPossibleValues.size() > 0 ) {
      for( int index = 0, n = aPossibleValues.size(); index < n; index++ ) {
        IPlexyValue possibleValue = aPossibleValues.get( index );
        if( value.equals( possibleValue ) ) {
          // Есть совпадение с возможным значением
          return value;
        }
      }
      // Совпадений не найдено
      for( int index = 0, n = aArgTokens.size(); index < n; index++ ) {
        // Преобразования к AdminCmdToken допустимы: AdminCmdLexicalParser агрегирован в класс
        AdminCmdToken token = (AdminCmdToken)aArgTokens.get( index );
        token.setType( ETokenType.UNDEF );
      }
      // Формирование строкового представления списка возможных значений
      StringBuilder sbPossible = new StringBuilder();
      for( int index = 0, n = aPossibleValues.size(); index < n; index++ ) {
        sbPossible.append( CHAR_APOSTROPHE );
        sbPossible.append( IStrioHardConstants.CHAR_ARRAY_BEGIN );
        IList<IAtomicValue> possibleValue = aPossibleValues.get( index ).valueList();
        for( int index2 = 0, n2 = possibleValue.size(); index2 < n2; index2++ ) {
          sbPossible.append( CHAR_APOSTROPHE );
          sbPossible.append( possibleValue.get( index2 ).toString() );
          sbPossible.append( CHAR_APOSTROPHE );
          if( index2 < n2 - 1 ) {
            sbPossible.append( IStrioHardConstants.CHAR_ITEM_SEPARATOR );
          }
        }
        sbPossible.append( IStrioHardConstants.CHAR_ARRAY_END );
        sbPossible.append( CHAR_APOSTROPHE );
        if( index < n - 1 ) {
          sbPossible.append( IStrioHardConstants.CHAR_ITEM_SEPARATOR );
        }
      }
      aErrors.add( error( ERR_MSG_UNPOSSIBLE_VALUE, aName, sbPossible.toString() ) );
      return null;
    }
    return value;
  }

  /**
   * Разбор лексем списка именованных атомарных значений аргумента команды
   *
   * @param aContext {@link IAdminCmdContext} контекст выполнения команд
   * @param aArgTokens {@link IList}&lt;{@link IAdminCmdToken}&gt; - лексемы представляющие значения аргумента
   * @param aName String - удобочитаемое имя читаемого значения
   * @param aPossibleValues {@link IList} &lt;{@link IPlexyValue}&gt; - список возможных значений. Пустой список без
   *          ограничений
   * @param aErrors {@link IListEdit}&lt;{@link ValidationResult}&gt; - список ошибок анализа
   * @return {@link IPlexyValue} значение аргумента. null: ошибка получения значений
   * @throws TsNullArgumentRtException любой аргумент null
   */
  private static IPlexyValue argOptionSetParse( IAdminCmdContext aContext, IList<IAdminCmdToken> aArgTokens,
      String aName, IList<IPlexyValue> aPossibleValues, IListEdit<ValidationResult> aErrors ) {
    TsNullArgumentRtException.checkNulls( aContext, aArgTokens, aPossibleValues, aName, aErrors );
    // IDataType dataType = aType.dataType();
    // Чтение значения
    int tokenQtty = aArgTokens.size();
    // IDataValue[] datavalues = new IDataValue[tokenQtty];
    IOptionSetEdit optionSet = new OptionSet();
    // Признак появлений ошибок при чтении элементов значения
    boolean readValueError = false;
    for( int index = 0; index < tokenQtty; index++ ) {
      // Лексема
      IAdminCmdToken token = aArgTokens.get( index );
      // Данные лексемы
      String data = token.data();
      // Чтение имени параметра
      String valueName = data.substring( 0, data.indexOf( IStrioHardConstants.CHAR_EQUAL ) ).trim();
      // Чтение значения с автоопределением типа (IDataType.NULL)
      IAtomicValue datavalue =
          valueParse( aContext, token, new DataType( EAtomicType.NONE, IOptionSet.NULL ), aName, aErrors );
      readValueError = (readValueError | datavalue == null);
      if( datavalue == null ) {
        continue;
      }
      // Размещение именованного параметра в карте параметров
      optionSet.setValue( valueName, datavalue );
    }
    if( readValueError ) {
      // Были ошибки чтения единичных значений. Дальнейший разбор не имеет смысла
      return null;
    }
    IPlexyValue value = PlexyValueUtils.pvOpset( optionSet );

    // Список возможных значений. Пустой список: без ограничений
    if( aPossibleValues.size() > 0 ) {
      for( int index = 0, n = aPossibleValues.size(); index < n; index++ ) {
        IPlexyValue possibleValue = aPossibleValues.get( index );
        if( value.equals( possibleValue ) ) {
          // Есть совпадение с возможным значением
          return value;
        }
      }
      // Совпадений не найдено
      for( int index = 0, n = aArgTokens.size(); index < n; index++ ) {
        // Преобразования к AdminCmdToken допустимы: AdminCmdLexicalParser агрегирован в класс
        AdminCmdToken token = (AdminCmdToken)aArgTokens.get( index );
        token.setType( ETokenType.UNDEF );
      }
      // Формирование строкового представления списка возможных значений
      StringBuilder sbPossible = new StringBuilder();
      for( int index = 0, n = aPossibleValues.size(); index < n; index++ ) {
        sbPossible.append( CHAR_APOSTROPHE );
        sbPossible.append( IStrioHardConstants.CHAR_ARRAY_BEGIN );
        IList<IAtomicValue> possibleValue = aPossibleValues.get( index ).valueList();
        for( int index2 = 0, n2 = possibleValue.size(); index2 < n2; index2++ ) {
          sbPossible.append( CHAR_APOSTROPHE );
          sbPossible.append( possibleValue.get( index2 ).toString() );
          sbPossible.append( CHAR_APOSTROPHE );
          if( index2 < n2 - 1 ) {
            sbPossible.append( IStrioHardConstants.CHAR_ITEM_SEPARATOR );
          }
        }
        sbPossible.append( IStrioHardConstants.CHAR_ARRAY_END );
        sbPossible.append( CHAR_APOSTROPHE );
        if( index < n - 1 ) {
          sbPossible.append( IStrioHardConstants.CHAR_ITEM_SEPARATOR );
        }
      }
      aErrors.add( error( ERR_MSG_UNPOSSIBLE_VALUE, aName, sbPossible.toString() ) );
      return null;
    }
    return value;
  }

  /**
   * Чтение значения аргумента из лексемы
   *
   * @param aContext {@link IAdminCmdContext} контекст выполнения команд
   * @param aArgToken {@link IAdminCmdToken} лексема представляющая значение
   * @param aType {@link IDataType} - тип значения
   * @param aName String - удобочитаемое имя читаемого значения
   * @param aErrors {@link IListEdit}&lt;{@link ValidationResult}&gt; - список ошибок анализа
   * @return значение. null: ошибка чтения
   * @throws TsNullArgumentRtException любой аргумент null
   */
  @SuppressWarnings( "unused" )
  public static IAtomicValue valueParse( IAdminCmdContext aContext, IAdminCmdToken aArgToken, IDataType aType,
      String aName, IListEdit<ValidationResult> aErrors ) {
    TsNullArgumentRtException.checkNulls( aContext, aArgToken, aType );
    // Преобразования к AdminCmdToken допустимы: AdminCmdLexicalParser агрегирован в класс
    AdminCmdToken token = (AdminCmdToken)aArgToken;
    String data = token.data();
    // Признак того, что значение было в кавычках, но в представленной лексеме их нет
    boolean quoted = aArgToken.quoted();
    if( aArgToken.type() == ETokenType.CONTEXT || aArgToken.type() == ETokenType.LIST_CONTEXT ) {
      // Подстановка значения параметра контекста. 1: без символа $
      String paramName = data.substring( 1 );
      Object contextValue = aContext.paramValueOrNull( paramName );
      if( contextValue == null ) {
        token.setType( ETokenType.UNDEF );
        aErrors.add( error( ERR_MSG_NOT_FOUND_CONTEXT, paramName ) );
        return null;
      }
      data = contextValue.toString();
    }
    // Именованное значение определяется через параметр контекста. Пустая строка: чтение неименнованного значения
    String paramName = EMPTY_STRING;
    if( aArgToken.type() == ETokenType.NAMED_VALUE ) {
      // Лексема представляет именованное значение
      int valueIndex = data.indexOf( IStrioHardConstants.CHAR_EQUAL );
      if( valueIndex < 0 || valueIndex >= data.length() - 1 ) {
        token.setType( ETokenType.UNDEF );
        aErrors.add( error( ERR_MSG_NOT_FOUND_NAMED_VALUE, aName ) );
        return null;
      }
      // Имя параметра
      paramName = data.substring( 0, valueIndex );
      // Выделение подстроки значения
      data = data.substring( valueIndex + 1 ).trim();
      // Все кавычки используемые в значении именованного параметра сохраняются
      quoted = false;
      if( data.startsWith( EMPTY_STRING + AdminCmdLexicalParser.CHAR_CONTEXT_PREFIX ) ) {
        // Именованное значение определяется через параметр контекста
        Object contextValue = aContext.paramValueOrNull( paramName );
        if( contextValue == null ) {
          token.setType( ETokenType.UNDEF );
          aErrors.add( error( ERR_MSG_NOT_FOUND_CONTEXT, paramName ) );
          return null;
        }
        data = contextValue.toString();
      }
    }
    try {
      switch( aType.atomicType() ) {
        case BOOLEAN:
          return avBool( Boolean.parseBoolean( data ) );
        case INTEGER:
          return avInt( Long.parseLong( data ) );
        case TIMESTAMP:
          return readTimestamp( data );
        case FLOATING:
          return avFloat( Double.parseDouble( data ) );
        case STRING:
          return avStr( data );
        case VALOBJ:
        case NONE:
          if( quoted ) {
            // Лексема была в кавычка;
            data = String.format( "\"%s\"", data ); //$NON-NLS-1$
          }
          try {
            // Тип значения определяется через формат ввода.
            // ";" - терминатор/разделитель для правильного завершения разбора timestamp (StrioReader.readTimestamp())
            return AtomicValueKeeper.KEEPER.str2ent( data + ";" ); //$NON-NLS-1$
          }
          catch( StrioRtException e ) {
            String err = (paramName.length() == 0 ? String.format( ERR_MSG_INVALID_VALUE, aName, data )
                : String.format( ERR_MSG_INVALID_PARAM_VALUE, aName, paramName, data ));
            if( StridUtils.isValidIdPath( data ) ) {
              // Строковые значения должны быть указанны в кавычках
              err += ERR_MSG_NEED_STRING;
            }
            // Ошибка чтения значения
            token.setType( ETokenType.UNDEF );
            aErrors.add( error( err ) );
            return null;
          }
        default:
          throw new TsNotAllEnumsUsedRtException();
      }
    }
    catch( RuntimeException e ) {
      // Ошибка чтения значения
      token.setType( ETokenType.UNDEF );
      aErrors.add( error( ERR_MSG_INVALID_TYPE, aName, aType.atomicType().id() ) );
      return null;
    }
  }

  /**
   * Читает из представленной строки метку времени
   * <p>
   *
   * @param aTimestamp String строковое представление метки времени
   * @return {@link IAtomicValue} значение метки времени. {@link IAtomicValue#NULL}: ошибка формата
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException неверный формат метки времени
   */
  private static IAtomicValue readTimestamp( String aTimestamp ) {
    TsNullArgumentRtException.checkNull( aTimestamp );
    for( int index = 0, n = TIMESTAMP_SUFFIX.length; index < n; index++ ) {
      IAtomicValue value = tryReadTimestamp( aTimestamp + TIMESTAMP_SUFFIX[index] );
      if( value != IAtomicValue.NULL ) {
        return value;
      }
    }
    throw new TsIllegalArgumentRtException();
  }

  /**
   * Осуществляет попытку чтения метки времени
   * <p>
   *
   * @param aTimestamp String строковое представление метки времени
   * @return {@link IAtomicValue} значение метки времени. {@link IAtomicValue#NULL}: ошибка формата
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static IAtomicValue tryReadTimestamp( String aTimestamp ) {
    TsNullArgumentRtException.checkNull( aTimestamp );
    IStrioReader sr = new StrioReader( new CharInputStreamString( aTimestamp ) );
    try {
      long timestamp = sr.readTimestamp();
      return avTimestamp( timestamp );
    }
    catch( @SuppressWarnings( "unused" ) TsRuntimeException e ) {
      return IAtomicValue.NULL;
    }
  }
}
