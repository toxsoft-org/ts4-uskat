package org.toxsoft.uskat.skadmin.cli.completion;

import static org.toxsoft.core.tslib.bricks.strid.impl.StridUtils.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.skadmin.cli.AdminColors.*;
import static org.toxsoft.uskat.skadmin.cli.IAdminAnsiConstants.*;
import static org.toxsoft.uskat.skadmin.cli.parsers.AdminCmdLexicalParser.*;
import static org.toxsoft.uskat.skadmin.core.impl.AdminCmdLibraryUtils.*;

import java.util.List;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.legacy.plexy.EPlexyKind;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.cli.parsers.*;
import org.toxsoft.uskat.skadmin.core.*;

import scala.tools.jline.console.completer.Completer;

/**
 * Автодоплнение команд, (TODO: аргументов и их значений)
 *
 * @author mvk
 */
public class AdminCmdCompleter
    implements Completer {

  private final IAdminCmdLibrary     library;
  private final AdminCmdSyntaxParser parser;
  private String                     sectionId;

  /**
   * Конструктор
   *
   * @param aLibrary {@link IAdminCmdLibrary} - библиотека команд
   * @param aSectionId String - текущий раздел консоли
   */
  public AdminCmdCompleter( IAdminCmdLibrary aLibrary, String aSectionId ) {
    super();
    TsNullArgumentRtException.checkNulls( aLibrary, aSectionId );
    library = aLibrary;
    parser = new AdminCmdSyntaxParser( aLibrary );
    setSectionId( aSectionId );
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Установить текущий раздел консоли
   *
   * @param aSectionId String текущий раздел консоли. Пустая строка: корневой раздел
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор раздела не валидный ИД-путь и не пустая строка
   */
  public void setSectionId( String aSectionId ) {
    TsNullArgumentRtException.checkNull( aSectionId );
    if( !aSectionId.equals( EMPTY_STRING ) ) {
      StridUtils.checkValidIdPath( aSectionId );
    }
    parser.setSectionId( aSectionId );
    sectionId = aSectionId;
  }

  // ------------------------------------------------------------------------------------
  // Реализация Completer
  //
  @Override
  public int complete( final String aBuffer, final int aCursor, final List<CharSequence> aCandidates ) {
    // Буфер может быть aBuffer == null
    TsNullArgumentRtException.checkNull( aCandidates );
    String filter = (aBuffer == null ? EMPTY_STRING : aBuffer);
    // Все команды библиотеки
    IList<IAdminCmdDef> cmdDefs = library.availableCmds();
    // Разбор строки
    IList<IAdminCmdToken> tokens = parser.parse( aBuffer != null ? aBuffer : EMPTY_STRING );
    // Количество лексем
    int tokenQtty = tokens.size();
    if( tokenQtty == 0 ) {
      // Нет лексем. Вывод всех команд
      for( IAdminCmdDef cmdDef : cmdDefs ) {
        tryCmdAdd( sectionId, cmdDef, filter, aCandidates );
      }
      return aCursor;
    }
    if( aBuffer != null ) {
      IAdminCmdToken lastToken = tokens.get( tokens.size() - 1 );
      boolean hasCmd = !parser.getCmdId().equals( EMPTY_STRING );
      boolean isCanonic = hasCmd && parser.isCanonicArgs();
      // Определение лексемы в позиции курсора
      int cursorTokenIndex = getCursorTokenIndex( tokens, aCursor );
      // Отработка ситуации - курсор на последнем символе строки
      if( cursorTokenIndex < 0 && aCursor == aBuffer.length()
          && (aCursor == 0 || aBuffer.charAt( aCursor - 1 ) != ' ') ) {
        cursorTokenIndex = tokens.size() - 1;
      }
      IAdminCmdToken cursorToken = (cursorTokenIndex >= 0 ? tokens.get( cursorTokenIndex ) : null);
      // System.out.println( "cursorToken = " + cursorToken + ", cursorTokenIndex = " + cursorTokenIndex +
      // ", aCursor = "
      // + aCursor + ", tokenType = " + cursorToken.type() );
      if( cursorToken != null && //
          (cursorToken.type() == ETokenType.CONTEXT || cursorToken.type() == ETokenType.LIST_CONTEXT
              || isContextToken( cursorToken )) ) {
        // Добавление параметров контекста. ...substring( 1 ): без символа $
        tryContextAdd( cursorToken.data().substring( 1 ), library.context(), aCandidates );
        return cursorToken.startIndex() + 1;
      }
      if( !hasCmd ) {
        // Нет команды. Добавление доступных команд
        filter = (lastToken.type() == ETokenType.STATEMENT ? EMPTY_STRING : lastToken.data());
        for( IAdminCmdDef cmdDef : cmdDefs ) {
          tryCmdAdd( sectionId, cmdDef, filter, aCandidates );
        }
        if( cursorToken != null ) {
          return cursorToken.startIndex();
        }
        return aCursor - aBuffer.length();
      }
      if( hasCmd && isCanonic && cursorToken != null && isIdToken( aBuffer, cursorToken ) ) {
        // Добавление аргументов команды определенных в канонической форме
        IAdminCmdDef cmdDef = library.findCommand( parser.getCmdId() );
        IStringList argIds = parser.getArgValues().keys();
        return tryArgAdd( cursorToken, cmdDef, argIds, aCandidates );
      }
      if( hasCmd && isCanonic ) {
        // Добавление значений аргументов команды определенных в канонической форме
        int prevTokenIndex = getLastIdTokenIndex( tokens, aCursor );
        IAdminCmdToken prevToken = (prevTokenIndex >= 0 ? tokens.get( prevTokenIndex ) : null);
        if( prevToken != null && prevToken.type() == ETokenType.ID ) {
          IAdminCmdDef cmdDef = library.findCommand( parser.getCmdId() );
          IAdminCmdArgDef argDef = cmdDef.findArgument( prevToken.data() );
          if( cursorToken == null ) {
            // Создаем лексему по текущей позиции курсора (пустая без кавычек)
            cursorToken = new AdminCmdToken( ETokenType.VALUE, aCursor, aCursor, EMPTY_STRING, false );
          }
          return tryValueAdd( cursorToken, cmdDef.id(), argDef.id(), library, parser.getArgValues(), aCandidates );
        }
      }
      if( hasCmd && !isCanonic ) {
        // Добавление значений аргументов команды определенных в упрощенной форме
        IAdminCmdDef cmdDef = library.findCommand( parser.getCmdId() );
        // Список аргументов атомарных значений, списков, наборов
        IList<IAdminCmdArgDef> argDefs = getArgDefs( cmdDef.argumentDefs(), false );
        // Определяем сколько аргументов(значений) введено и какой требуется ввести следующим
        int nextArgIndex = 0;
        for( IPlexyValue argValue : parser.getArgValues() ) {
          if( argValue.type().kind() != EPlexyKind.SINGLE_REF && argValue.type().kind() != EPlexyKind.REF_LIST ) {
            nextArgIndex++;
          }
        }
        if( nextArgIndex < argDefs.size() ) {
          IAdminCmdArgDef argDef = argDefs.get( nextArgIndex );
          if( cursorToken == null ) {
            // Создаем лексему по текущей позиции курсора (пустая, без кавычек)
            cursorToken = new AdminCmdToken( ETokenType.VALUE, aCursor, aCursor, EMPTY_STRING, false );
          }
          return tryValueAdd( cursorToken, cmdDef.id(), argDef.id(), library, parser.getArgValues(), aCandidates );
        }
      }
    }
    return aCandidates.isEmpty() ? -1 : 0;
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  //
  /**
   * Пытается добавить параметры контекста в список кандидатов
   *
   * @param aFilter String - фильтр имен параметров
   * @param aContext {@link IAdminCmdContext} контекст выполнения команды
   * @param aCandidates List - список кандидатов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void tryContextAdd( String aFilter, IAdminCmdContext aContext, List<CharSequence> aCandidates ) {
    TsNullArgumentRtException.checkNulls( aFilter, aContext, aCandidates );
    for( String paramName : aContext.paramNames() ) {
      if( paramName.startsWith( aFilter ) ) {
        aCandidates.add( COLOR_SINGLE_REF + paramName + COLOR_RESET );
      }
    }
  }

  /**
   * Пытается добавить идентификатор команды в список кандидатов на исполнение
   *
   * @param aSectionId String - текущий раздел
   * @param aCmdDef IAdminCmdDef - описание команды
   * @param aFilter String - фильтр по последнему имени в ИД-пути команды
   * @param aCandidates List - список кандидатов
   */
  private static void tryCmdAdd( String aSectionId, IAdminCmdDef aCmdDef, String aFilter,
      final List<CharSequence> aCandidates ) {
    String cmdId = aCmdDef.id();
    if( !isIdAPath( cmdId ) ) {
      // Команда корневого раздела видна всегда
      if( cmdId.startsWith( aFilter ) ) {
        aCandidates.add( COLOR_ID + cmdId + COLOR_RESET );
        return;
      }
    }
    String idpath = StridUtils.removeTailingIdNames( cmdId, 1 );
    if( aSectionId.equals( idpath ) ) {
      // Команда в текущем разделе
      String idName = StridUtils.getLast( cmdId );
      if( idName.startsWith( aFilter ) ) {
        aCandidates.add( COLOR_ID + idName + COLOR_RESET );
        return;
      }
    }
    // Добавление полного пути команды
    if( cmdId.startsWith( aFilter ) ) {
      aCandidates.add( COLOR_ID + cmdId + COLOR_RESET );
    }
  }

  /**
   * Пытается добавить идентификатор аргументов или возможные значения в список кандидатов на исполнение
   *
   * @param aToken {@link IAdminCmdToken} лексема представляющая идентификатор аргумента
   * @param aCmdDef {@link IAdminCmdDef} - описание команд
   * @param aExistArgIds {@link IStringList} - список идентификаторов уже введенных аргументов
   * @param aCandidates List - список кандидатов
   * @return int позиция с которой ставляется значение
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static int tryArgAdd( IAdminCmdToken aToken, IAdminCmdDef aCmdDef, IStringList aExistArgIds,
      List<CharSequence> aCandidates ) {
    TsNullArgumentRtException.checkNulls( aToken, aCmdDef, aCandidates );
    aCandidates.clear();
    // Список аргументов атомарных значений, списков, наборов
    IList<IAdminCmdArgDef> argDefs = getArgDefs( aCmdDef.argumentDefs(), false );
    // Фильтр идентификаторов аргументов
    String filter = aToken.data();
    // Дополнение идентификатора аргумента
    for( int index = 0, n = argDefs.size(); index < n; index++ ) {
      IAdminCmdArgDef argDef = argDefs.get( index );
      String argId = argDef.id();
      if( (aToken.data().isEmpty() || argId.startsWith( filter )) && !aExistArgIds.hasElem( argId ) ) {
        aCandidates.add( COLOR_ID + argId + COLOR_RESET );
      }
    }
    return (aToken.data().length() == 0 ? aToken.startIndex() + 1 : aToken.startIndex());
  }

  /**
   * Пытается добавить возможные значения аргумента
   *
   * @param aToken {@link IAdminCmdToken} лексема представляющая значения аргумента
   * @param aCmdId String - идентификатор команды
   * @param aArgId String - идентификатор аргумента
   * @param aLibrary {@link IAdminCmdLibrary} библиотека команд
   * @param aArgValues карта уже введенных значений
   * @param aCandidates List - список кандидатов
   * @return int позиция с которой ставляется значение
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static int tryValueAdd( IAdminCmdToken aToken, String aCmdId, String aArgId, IAdminCmdLibrary aLibrary,
      IStringMap<IPlexyValue> aArgValues, List<CharSequence> aCandidates ) {
    TsNullArgumentRtException.checkNulls( aToken, aCmdId, aArgId, aLibrary, aCandidates );
    aCandidates.clear();
    // Фильтр значений
    String filter = aToken.data();
    // Дополнение идентификатора аргумента
    IList<IPlexyValue> possibleValues = IList.EMPTY;
    try {
      possibleValues = aLibrary.getPossibleValues( aCmdId, aArgId, aArgValues );
    }
    catch( @SuppressWarnings( "unused" ) TsIllegalArgumentRtException e ) {
      // Тип найденных значений не соответствует типу аргумента
      return aToken.startIndex();
    }
    for( IPlexyValue value : possibleValues ) {
      if( value.type().kind() != EPlexyKind.SINGLE_VALUE ) {
        // Обрабатываем только единичные атомарные значения
        continue;
      }
      String strValue = EMPTY_STRING;
      IAtomicValue atomicValue = value.singleValue();
      switch( value.type().dataType().atomicType() ) {
        case BOOLEAN:
          strValue = String.valueOf( atomicValue.asBool() );
          break;
        case INTEGER:
          strValue = String.valueOf( atomicValue.asInt() );
          break;
        case STRING:
          strValue = atomicValue.asString();
          break;
        case FLOATING:
          strValue = String.valueOf( atomicValue.asFloat() );
          break;
        case TIMESTAMP:
          strValue = TimeUtils.timestampToString( atomicValue.asLong() );
          break;
        case VALOBJ:
        case NONE:
          continue;
        default:
          throw new TsNotAllEnumsUsedRtException();
      }
      if( strValue.trim().length() == 0 ) {
        // Пустое значение
        continue;
      }
      if( (aToken.data().isEmpty() || strValue.startsWith( filter )) ) {
        aCandidates.add( COLOR_SINGLE_VALUE + strValue + COLOR_RESET );
      }
    }
    return aToken.startIndex();
  }

  /**
   * Возвращает индекс лексемы в списке лексем на который попадает позиция курсора
   *
   * @param aTokens {@link IList}&lt;{@link IAdminCmdToken}&gt; список лексем
   * @param aCursor int позиция курсора
   * @return int индекс лексемы. -1: лексема не найдена
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static int getCursorTokenIndex( IList<IAdminCmdToken> aTokens, int aCursor ) {
    for( int index = 0, n = aTokens.size(); index < n; index++ ) {
      IAdminCmdToken token = aTokens.get( index );
      // 2022-09-20 mvk
      // if( token.startIndex() <= aCursor && aCursor - 1 <= token.finishIndex() ) {
      if( token.startIndex() <= aCursor && aCursor <= token.finishIndex() ) {
        return index;
      }
    }
    return -1;
  }

  /**
   * Возвращает индекс лексемы в списке лексем перед позицией курсора и имеющей тип ID
   *
   * @param aTokens {@link IList}&lt;{@link IAdminCmdToken}&gt; список лексем
   * @param aCursor int позиция курсора
   * @return int индекс лексемы. -1: лексема не найдена
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static int getLastIdTokenIndex( IList<IAdminCmdToken> aTokens, int aCursor ) {
    int retValue = -1;
    for( int index = 0, n = aTokens.size(); index < n; index++ ) {
      IAdminCmdToken token = aTokens.get( index );
      if( token.finishIndex() < aCursor && token.type() == ETokenType.ID ) {
        retValue = index;
      }
    }
    return retValue;
  }

  /**
   * Возвращает признак того, что лексема начинается с символа контекста
   *
   * @param aToken {@link IAdminCmdToken} лексема
   * @return boolean <b>true</b> лексема начианается с символа контекста; <b>false</b> лексема не начинается с символа
   *         контекста
   */
  private static boolean isContextToken( IAdminCmdToken aToken ) {
    return aToken.data().length() > 0 && aToken.data().charAt( 0 ) == CHAR_CONTEXT_PREFIX;
  }

  /**
   * Возвращает признак того, что лексема начинается с символа идентификатора
   *
   * @param aBuffer String буфер из которого была сформирована лексема
   * @param aToken {@link IAdminCmdToken} лексема
   * @return boolean <b>true</b> лексема начианается с символа идентификатора; <b>false</b> лексема не начинается с
   *         символа идентификатора
   */
  private static boolean isIdToken( String aBuffer, IAdminCmdToken aToken ) {
    // 2022-09-20 mvk
    // Проверка символа перед лексемой
    if( aBuffer.length() > aToken.startIndex() ) {
      if( aToken.startIndex() == aToken.finishIndex() ) {
        if( aToken.data().length() == 0 ) {
          char c = aBuffer.charAt( aToken.startIndex() );
          return (c == CHAR_ID_PREFIX);
        }
      }
    }
    if( aToken.startIndex() > 0 ) {
      char c = aBuffer.charAt( aToken.startIndex() - 1 );
      return (c == CHAR_ID_PREFIX);
    }
    return false;
  }

}
