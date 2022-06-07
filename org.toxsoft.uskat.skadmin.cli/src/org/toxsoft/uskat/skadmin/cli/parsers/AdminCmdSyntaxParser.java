package org.toxsoft.uskat.skadmin.cli.parsers;

import static org.toxsoft.core.tslib.bricks.validator.ValidationResult.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.skadmin.cli.parsers.AdminCmdLexicalParser.*;
import static org.toxsoft.uskat.skadmin.cli.parsers.AdminCmdParserUtils.*;
import static org.toxsoft.uskat.skadmin.cli.parsers.IAdminResources.*;
import static org.toxsoft.uskat.skadmin.core.impl.AdminCmdLibraryUtils.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringLinkedBundleList;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.legacy.plexy.*;
import org.toxsoft.uskat.skadmin.cli.AdminConsoleUtils;
import org.toxsoft.uskat.skadmin.core.*;

/**
 * Синтаксический анализатор командной строки
 *
 * @author mvk
 */
public class AdminCmdSyntaxParser
    implements IAdminCmdSyntaxParser {

  /**
   * Лексический анализатор
   */
  private final IAdminCmdSyntaxParser lexicalParser = new AdminCmdLexicalParser();

  /**
   * Библиотека команд
   */
  private final IAdminCmdLibrary library;

  /**
   * Текущий раздел команд
   */
  private String sectionId = EMPTY_STRING;

  /**
   * Список имен входных параметров контекста найденных во время последнего анализа.
   */
  private IStringListEdit contexInputNames = new StringLinkedBundleList();

  /**
   * Список имен выходных параметров контекста найденных во время последнего анализа для сохранения результата.
   */
  private IStringListEdit contexOutputNames = new StringLinkedBundleList();

  /**
   * Правое значение при вводе в форме: lvalue = rvalue
   */
  private IPlexyValue rvalue = IPlexyValue.NULL;

  /**
   * Возвращает идентификатор команды или ее алиас сформированный во время последнего анализа. Пустая строка: команда
   * недоступна
   */
  private String cmdId = EMPTY_STRING;

  /**
   * Признак того, что аргументы определены в канонической форме во время последнего анализа
   */
  private boolean canonicArgs;

  /**
   * Карты сформированных едичных значений аргументов во время последнего анализа. Ключ: идентификатор аргумента
   */
  private IStringMapEdit<IPlexyValue> argValues = new StringMap<>();

  /**
   * Список ошибок последнего анализа
   */
  private IListEdit<ValidationResult> errors = new ElemLinkedList<>();

  /**
   * Конструктор
   *
   * @param aCmdLibrary {@link IAdminCmdLibrary} - библиотека команд
   * @throws TsNullArgumentRtException аргумент = null
   */
  public AdminCmdSyntaxParser( IAdminCmdLibrary aCmdLibrary ) {
    TsNullArgumentRtException.checkNull( aCmdLibrary );
    library = aCmdLibrary;
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Установить текущий раздел команд
   *
   * @param aSectionId String - идентификатор (ИД-путь) текущего раздела команд. Пустая строка: корневой раздел
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException индентификатор не ИД-путь и не пустая строка
   */
  public void setSectionId( String aSectionId ) {
    TsNullArgumentRtException.checkNull( aSectionId );
    if( !aSectionId.equals( EMPTY_STRING ) ) {
      StridUtils.checkValidIdPath( aSectionId );
    }
    sectionId = aSectionId;
  }

  /**
   * Возвращает список имен входных параметров контекста найденных во время последнего анализа.
   *
   * @return {@link IStringList} список имен параметров контекста
   */
  public IStringList contextInputNames() {
    return contexInputNames;
  }

  /**
   * Возвращает список имен входных параметров контекста найденных во время последнего анализа.
   *
   * @return {@link IStringList} список имен параметров контекста
   */
  public IStringList contextOutputNames() {
    return contexOutputNames;
  }

  /**
   * Возвращает правое значение при вводе в форме: lvalue = rvalue.
   *
   * @return {@link IPlexyValue} значение
   */
  public IPlexyValue rvalue() {
    return rvalue;
  }

  /**
   * Возвращает идентификатор команды или ее алиас определенный во время последнего анализа
   *
   * @return String - идентификатор команды или ее алиас. Пустая строка команда недоступна
   */
  public String getCmdId() {
    return cmdId;
  }

  /**
   * Возвращает признак того, что аргументы определены в канонической форме во время последнего анализа
   *
   * @return boolean <b>true</b> аргументы определены в канонической форме <b>false</b> аргументы определены в
   *         упрощенной форме или команда без аргументов
   * @throws TsIllegalArgumentRtException команда не доступна
   */
  public boolean isCanonicArgs() {
    TsIllegalArgumentRtException.checkTrue( cmdId.equals( EMPTY_STRING ) );
    return canonicArgs;
  }

  /**
   * Возвращает карту единичных значений аргументов полученных на последнем анализе
   *
   * @return {@link IStringMap}&lt; {@link IAtomicValue}&gt; - карта прочитанных значений аргументов. Ключ:
   *         идентификатор аргумента
   */
  public IStringMap<IPlexyValue> getArgValues() {
    return argValues;
  }

  /**
   * Возвращает список ошибок последнего анализа
   *
   * @return {@link IList}&lt;{@link ValidationResult}&gt; - список ошибок последнего анализа
   */
  public IList<ValidationResult> getErrors() {
    return errors;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IAdminCmdSyntaxParser
  //
  @Override
  public IList<IAdminCmdToken> parse( String aCmdLine ) {
    TsNullArgumentRtException.checkNull( aCmdLine );
    // Очистка результатов предыдущего анализа
    contexInputNames.clear();
    contexOutputNames.clear();
    rvalue = IPlexyValue.NULL;
    cmdId = EMPTY_STRING;
    canonicArgs = false;
    argValues.clear();
    errors.clear();
    // Лексический анализ командной строки
    IList<IAdminCmdToken> tokens = lexicalParser.parse( aCmdLine );
    if( tokens.size() == 0 ) {
      // Пустая строка
      return tokens;
    }
    // Индекс лексемы в которой находится идентификтор команды. +1: с учетом оператора утверждения
    int cmdTokenIndex = 0;
    // Обрабатываем лексемы имен контекста и операторов утверждения (выходные или/и входные)
    IStringList contexNames = contextParamsParse( tokens, 0 );
    if( contexNames.size() > 0 ) {
      // Определение признака использования параметров контекста для записи
      boolean writing = contextParamsHandle( tokens, 0, contexNames, library.context(), errors );
      if( errors.size() > 0 ) {
        // Ошибки обработки параметров контекста
        return tokens;
      }
      // Индекс лексемы в которой находится идентификтор команды. +1: с учетом оператора утверждения
      cmdTokenIndex += contexNames.size() + 1;
      if( cmdTokenIndex <= tokens.size() && writing ) {
        // Были прочитаны выходные параметры контекста и есть еще лексемы для разбора
        contexOutputNames.addAll( contexNames );
        // Пробуем прочитать входные параметры контекста
        contexNames = contextParamsParse( tokens, cmdTokenIndex );
        if( contexNames.size() > 0 ) {
          // Определение признака использования параметров контекста для записи
          writing = contextParamsHandle( tokens, cmdTokenIndex, contexNames, library.context(), errors );
          if( errors.size() > 0 ) {
            // Ошибки обработки параметров контекста
            return tokens;
          }
          if( writing ) {
            // Оператор утверждения '=' встречен дважды
            ((AdminCmdToken)tokens.get( cmdTokenIndex + contexNames.size() )).setType( ETokenType.UNDEF );
            errors.add( error( ERR_MSG_DOUBLE_EQ ) );
            return tokens;
          }
          contexInputNames.addAll( contexNames );
          // Индекс лексемы в которой находится идентификтор команды. +1: с учетом оператора утверждения
          cmdTokenIndex += contexNames.size() + 1;
          if( cmdTokenIndex > tokens.size() ) {
            // Нет команды. Определены только параметры контекста.
            return tokens;
          }
        }
      }
      else {
        // Прочитаны только входные параметры контекста
        contexInputNames.addAll( contexNames );
      }
      // Проверим если нет далее команды, то пробуем прочитать "правое" значение
      if( writing && contexInputNames.size() == 0 && cmdTokenIndex < tokens.size()
          && !isCmdToken( tokens.get( cmdTokenIndex ), library, sectionId ) ) {
        String key = "rvalue"; //$NON-NLS-1$
        IStringMapEdit<IPlexyValue> values = new StringMap<>();
        argValueParse( library.context(), tokens, cmdTokenIndex, IPlexyType.NONE, IList.EMPTY, key, values, errors );
        IPlexyValue value = values.findByKey( key );
        if( value != null ) {
          rvalue = value;
          return tokens;
        }
      }
    }
    if( cmdTokenIndex >= tokens.size() ) {
      // Команда не определена
      return tokens;
    }
    // Проверка лексемы команды и получение описания команды
    // Преобразования к AdminCmdToken допустимы: AdminCmdLexicalParser агрегирован в класс
    IAdminCmdDef cmdDef = cmdParse( (AdminCmdToken)tokens.get( cmdTokenIndex ), library, sectionId, errors );
    if( cmdDef == null ) {
      // Дальнейший разбор не имеет смысла. Оставляем типы лексем определенные лексическим анализатором
      return tokens;
    }
    // Сохраняем в списке уже сформированных аргументов аргументы определеные через параметры контекста
    try {
      AdminConsoleUtils.appendContextValues( cmdDef.argumentDefs(), argValues, contexInputNames, library.context() );
    }
    catch( TsIllegalArgumentRtException e ) {
      // Ошибка дополнения аргументов
      errors.add( error( e.getLocalizedMessage() ) );
      return tokens;
    }
    // Идентификатор команды
    cmdId = cmdDef.id();
    if( tokens.size() < cmdTokenIndex + 2 ) {
      // Нет лексем определяющих аргументы команды
      return tokens;
    }
    // Переход на первую лексему аргументов
    cmdTokenIndex++;
    // Признак того, что аргументы команды представляются в канонической форме
    canonicArgs = (tokens.get( cmdTokenIndex ).type() == ETokenType.ID);

    try {
      if( canonicArgs ) {
        canonicArgsParse( tokens, cmdTokenIndex, library, cmdDef, argValues, errors );
        return tokens;
      }
      // Разбор аргментов в упрощенной форме
      simpleArgsParse( tokens, cmdTokenIndex, library, cmdDef, argValues, errors );
      return tokens;
    }
    catch( Throwable e ) {
      // Неожиданная ошибка разбора командной строки
      errors.add( error( e.getLocalizedMessage() ) );
      return tokens;
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренняя реализация
  //
  /**
   * Проводит обработку параметров контекста
   *
   * @param aTokens {@link IList}&lt;{@link IAdminCmdContext}&gt; список всех лексем командной строки
   * @param aStartIndex int индекс в списке лексем с которой разбираются лексемы параметров контекста
   * @param aContextNames {@link IStringList} список имен параметров контекста
   * @param aContext {@link IAdminCmdContext} текущий контекст выполнения команд
   * @param aErrors {@link IListEdit}&lt;{@link ValidationResult}&gt; список ошибок обработки параметров контекста
   * @return boolean <b>true</b> параметры используются для записи в контекст. <b>false</b> параметры используются для
   *         чтения.
   */
  private static boolean contextParamsHandle( IList<IAdminCmdToken> aTokens, int aStartIndex, IStringList aContextNames,
      IAdminCmdContext aContext, IListEdit<ValidationResult> aErrors ) {
    int contextParamSize = aContextNames.size();
    if( contextParamSize + aStartIndex == aTokens.size() ) {
      // В строке указаны только параметры контекста
      return false;
    }
    // За определением параметров контекста должен следовать оператор утверждения
    AdminCmdToken statementToken = (AdminCmdToken)aTokens.get( contextParamSize + aStartIndex );
    Long tokenStartIndex = Long.valueOf( statementToken.startIndex() );
    if( statementToken.type() != ETokenType.STATEMENT ) {
      statementToken.setType( ETokenType.UNDEF );
      aErrors.add( error( ERR_MSG_CTX_STATEMENT_EXPECTED, tokenStartIndex ) );
      return false;
    }
    String statement = statementToken.data();
    if( !statement.equals( STATEMENT_WRITE ) && !statement.equals( STATEMENT_APPLY ) ) {
      statementToken.setType( ETokenType.UNDEF );
      aErrors.add( error( ERR_MSG_CTX_WRONG_STATEMENT, tokenStartIndex, statement ) );
      return false;
    }
    // Признак записи параметра в контекст (в противном случае чтения из контекста)
    boolean writing = statement.equals( STATEMENT_WRITE );
    // Проверяем существование используемых для чтения параметров контекста
    for( int index = 0, n = aContextNames.size(); index < n; index++ ) {
      String name = aContextNames.get( index );
      AdminCmdToken paramToken = (AdminCmdToken)aTokens.get( aStartIndex + index );
      if( !writing && !aContext.hasParam( name ) ) {
        // Читаемый параметр не найден в контексте
        paramToken.setType( ETokenType.UNDEF );
        aErrors.add( error( ERR_MSG_NOT_FOUND_CONTEXT, name ) );
        continue;
      }
      if( writing && aContext.hasParam( name ) && aContext.readOnlyParam( name ) ) {
        // Записываемый параметр существует в контексте с признаком только чтение
        paramToken.setType( ETokenType.UNDEF );
        aErrors.add( error( ERR_MSG_CTX_READONLY, name ) );
        continue;
      }
    }
    return writing;
  }

  // ------------------------------------------------------------------------------------
  // Тесты
  //
  @SuppressWarnings( { "nls", "javadoc" } )
  public static void main( String[] aArgs ) {
    IListEdit<IAdminCmdLibrary> libraries = new ElemArrayList<>();
    AdminCmdSyntaxParser parser = new AdminCmdSyntaxParser( createAdminLibrary( libraries ) );

    // String text = "$a = \"testString\"";
    // String text = "$a = name1=1,name2=2";
    String text = "$a = name1 =1,name2  = \"test\"";
    parser.parse( text );

    IList<ValidationResult> errors = parser.getErrors();
    for( int index = 0, n = errors.size(); index < n; index++ ) {
      System.out.println( errors.get( index ).message() );
    }
    IPlexyValue rvalue = parser.rvalue();
    System.out.println( "rvalue = " + rvalue );
    if( rvalue.type().kind() == EPlexyKind.OPSET ) {
      IOptionSet os = rvalue.getOpset();
      for( String name : os.keys() ) {
        IAtomicValue value = os.getValue( name );
        System.out.println( name + "=" + value + ", type: " + value.atomicType() );
      }
    }
  }
}
