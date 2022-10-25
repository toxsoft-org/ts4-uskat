package org.toxsoft.uskat.skadmin.cli.parsers;

import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;
import static org.toxsoft.core.tslib.bricks.strio.impl.StrioUtils.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.skadmin.cli.parsers.IAdminResources.*;

import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.chario.impl.CharInputStreamString;
import org.toxsoft.core.tslib.bricks.strio.impl.StrioReader;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.utils.errors.TsNotAllEnumsUsedRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Лексический анализатор командной строки
 *
 * @author mvk
 */
public class AdminCmdLexicalParser
    implements IAdminCmdSyntaxParser {

  /**
   * Лексемный читатель
   */
  private final StrioReader strioReader = new StrioReader( new CharInputStreamString() );

  /**
   * Поправка текущей позиции возвращаемой: strioReader.getInput().currentPosition().charNo()
   */
  private int strioReaderPositionDelta;

  /**
   * Разделители. Отличие от {@link IStrioHardConstants#DEFAULT_DELIMITER_CHARS} - нет символа ':'
   */
  private static final String DELIMITER_CHARS = DEFAULT_BYPASSED_CHARS + CHAR_ITEM_SEPARATOR + CHAR_SET_BEGIN
      + CHAR_SET_END + CHAR_ARRAY_BEGIN + CHAR_ARRAY_END + ";=()<>"; //$NON-NLS-1$

  /**
   * Разделители для чтения имен параметров контекста
   */
  private static final String CONTEXT_DELIMITER_CHARS = DELIMITER_CHARS + "-"; //$NON-NLS-1$

  /**
   * Сигнальный символ параметра контекста
   */
  public static final char CHAR_CONTEXT_PREFIX = '$';

  /**
   * Сигнальный символ идентификатора
   */
  public static final char CHAR_ID_PREFIX = '-';

  /**
   * Строка супер кавычек
   */
  public static final String SUPER_QUOTE_STRING = "'''"; //$NON-NLS-1$

  /**
   * Операторы утверждения
   */
  static final String           STATEMENT_WRITE = "=";                                                           //$NON-NLS-1$
  static final String           STATEMENT_APPLY = "->";                                                          //$NON-NLS-1$
  static final String           STATEMENT_GT    = ">";                                                           //$NON-NLS-1$
  static final String           STATEMENT_GE    = ">=";                                                          //$NON-NLS-1$
  static final String           STATEMENT_LS    = "<";                                                           //$NON-NLS-1$
  static final String           STATEMENT_LE    = "<=";                                                          //$NON-NLS-1$
  static final String           STATEMENT_EQ    = "==";                                                          //$NON-NLS-1$
  static final String           STATEMENT_NOT   = "!=";                                                          //$NON-NLS-1$
  private static final String[] STATEMENTS      = { STATEMENT_WRITE, STATEMENT_APPLY, STATEMENT_GT, STATEMENT_GE,
      STATEMENT_LS, STATEMENT_LE, STATEMENT_EQ, STATEMENT_NOT };

  /**
   * Конструктор по умолчанию
   */
  public AdminCmdLexicalParser() {
    strioReader.setSkipMode( EStrioSkipMode.SKIP_BYPASSED );
    strioReader.setDelimiterChars( DELIMITER_CHARS );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IAdminCmdSyntaxParser
  //
  @Override
  public IList<IAdminCmdToken> parse( String aCmdLine ) {
    TsNullArgumentRtException.checkNull( aCmdLine );
    strioReader.setInput( new CharInputStreamString( aCmdLine ) );
    strioReaderPositionDelta = 0;
    IListEdit<IAdminCmdToken> tokens = new ElemLinkedList<>();
    char c = strioReader.peekChar( EStrioSkipMode.SKIP_BYPASSED );
    while( c != CHAR_EOF ) {
      // Предыдущая обработанная лексемма или null если такой не было
      AdminCmdToken lastToken = (AdminCmdToken)(tokens.size() > 0 ? tokens.get( tokens.size() - 1 ) : null);
      // Проверяем чтение оператора утверждения (=, >, <, =>, =<, ==, !=, ->)
      if( isStatement( aCmdLine, currentPosition() ) ) {
        // Чтение лексемы утверждения
        c = readToken( tokens, ETokenType.STATEMENT, aCmdLine );
        // Прочитанная лексема (предположительно оператор утверждение)
        AdminCmdToken nextToken = (AdminCmdToken)(tokens.get( tokens.size() - 1 ));
        // Проверим, возможно, что производится чтение именованного значения (первое значение)
        if( nextToken.data().equals( STATEMENT_WRITE ) && //
            lastToken != null && lastToken.type() == ETokenType.VALUE ) {
          // Найден оператор утверждения "запись", при этом до этого была найдена лексема с типом "значение". Пробуем
          // прочитать именованное значение.
          tokens.removeByIndex( tokens.size() - 1 );
          tokens.removeByIndex( tokens.size() - 1 );
          strioReader.setInput( new CharInputStreamString( aCmdLine, lastToken.startIndex() ) );
          // Поправка позиции текущего символа
          strioReaderPositionDelta = lastToken.startIndex();
          // Чтение лексемы именованного значения
          c = readToken( tokens, ETokenType.NAMED_VALUE, aCmdLine );
        }
        continue;
      }
      switch( c ) {
        case CHAR_CONTEXT_PREFIX:
          // Параметр контекста (или список)
          c = readToken( tokens, ETokenType.CONTEXT, aCmdLine );
          break;
        case CHAR_ID_PREFIX:
          // Префикс идентификатора или оператор применения параметров контекста (->)
          strioReader.nextChar();
          c = strioReader.nextChar();
          if( isAsciiDigit( c ) ) {
            // Нашли отрицательное число. Возвращаем в поток знак и первый символ числа
            strioReader.putCharBack();
            strioReader.putCharBack();
            c = readToken( tokens, ETokenType.VALUE, aCmdLine );
            continue;
          }
          if( c == CHAR_ID_PREFIX ) {
            // Чтение идентификатора (каноническая форма)
            c = readToken( tokens, ETokenType.ID, aCmdLine );
            continue;
          }
          // Возвращаем в поток символ и читаем идентификатор
          strioReader.putCharBack();
          c = readToken( tokens, ETokenType.ID, aCmdLine );
          continue;
        default:
          if( strioReader.isDelimiterChar( c ) ) {
            // Чтение списка. Пропускаем символ разделителя.
            strioReader.nextChar();
            // Пропуск пробелов
            c = strioReader.peekChar( EStrioSkipMode.SKIP_BYPASSED );
            if( lastToken != null ) {
              // Тип предыдущей лексемы
              ETokenType lastTokenType = lastToken.type();
              if( lastTokenType == ETokenType.CONTEXT || lastTokenType == ETokenType.LIST_CONTEXT
                  || lastTokenType == ETokenType.VALUE || lastTokenType == ETokenType.LIST_VALUE ) {
                ETokenType prevType = ETokenType.LIST_VALUE;
                if( lastTokenType == ETokenType.CONTEXT || lastTokenType == ETokenType.LIST_CONTEXT ) {
                  prevType = ETokenType.LIST_CONTEXT;
                }
                lastToken.setType( prevType );
                // Чтение значения (элемента списка)
                ETokenType nextType = (c == CHAR_CONTEXT_PREFIX ? ETokenType.LIST_CONTEXT : ETokenType.LIST_VALUE);
                c = readToken( tokens, nextType, aCmdLine );
                // Устанавливаем индекс элемента в списке
                AdminCmdToken addedToken = (AdminCmdToken)tokens.get( tokens.size() - 1 );
                if( addedToken.type() != ETokenType.UNDEF ) {
                  addedToken.setListIndex( lastToken.listIndex() + 1 );
                }
                continue;
              }
              if( lastTokenType == ETokenType.NAMED_VALUE ) {
                // Считывание именованных параметров
                c = readToken( tokens, ETokenType.NAMED_VALUE, aCmdLine );
                continue;
              }
            }
            // Символ ',' но до этого не было токена-значения
            c = readToken( tokens, ETokenType.UNDEF, aCmdLine );
            continue;
          }
          // Чтение значения
          c = readToken( tokens, ETokenType.VALUE, aCmdLine );
          continue;
      }
    }
    return tokens;
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  //
  /**
   * Читает токен
   *
   * @param aTokens {@link IListEdit} список токенов
   * @param aTokenType {@link ETokenType} - тип читаемого токена
   * @param aSource String строка ввода
   * @return char - первый символ следующей лексемы после разделителя
   */
  @SuppressWarnings( "unused" )
  private char readToken( IListEdit<IAdminCmdToken> aTokens, ETokenType aTokenType, String aSource ) {
    ETokenType tokenType = aTokenType;
    int startPosition = currentPosition();
    char nextChar = strioReader.peekChar( EStrioSkipMode.SKIP_NONE );
    if( nextChar == CHAR_EOF ) {
      if( aTokenType == ETokenType.NAMED_VALUE ) {
        // Лексема именованного значения не может завершится таким образом
        tokenType = ETokenType.UNDEF;
      }
      // Неожиданное завершение строки (пустая лексема, без кавычек)
      aTokens.add( new AdminCmdToken( tokenType, startPosition, startPosition, EMPTY_STRING, false ) );
      return strioReader.nextChar();
    }
    boolean quoted = (nextChar == CHAR_QUOTE);
    boolean superQuoted = hasSuperQuote( strioReader );
    // Чтение данных лексемы
    String data = EMPTY_STRING;
    strioReader.setDelimiterChars( DELIMITER_CHARS );
    switch( tokenType ) {
      case STATEMENT:
        // Чтение первого символа оператора утверждения
        data += strioReader.nextChar();
        // Читаем следующий символ, для двухсимвольных операторов
        if( isStatement( aSource, currentPosition() ) ) {
          // Второй символ тоже преставляет оператор - чтение двухсимвольного оператора
          data += strioReader.nextChar();
        }
        break;
      case CONTEXT:
      case LIST_CONTEXT:
        strioReader.setDelimiterChars( CONTEXT_DELIMITER_CHARS );
        //$FALL-THROUGH$
      case ID:
      case VALUE:
      case LIST_VALUE:
      case UNDEF:
        if( quoted && !superQuoted ) {
          // Чтение текста в обычных кавычках
          try {
            data = strioReader.readQuotedString();
          }
          catch( StrioRtException e ) {
            // Нет завершающих кавычек
            // 2022-09-23 mvk чтение строки с последнего апострофа до конца строки
            // tokenType = ETokenType.UNDEF;
            data = aSource.substring( aSource.lastIndexOf( '"' ) + 1 );
            tokenType = ETokenType.UNDEF;
            quoted = false;
          }
          break;
        }
        if( !quoted && superQuoted ) {
          // Чтение текста в супер кавычках
          try {
            data = readSuperQuotedString( strioReader );
          }
          catch( StrioRtException e ) {
            // Нет завершающих кавычек
            tokenType = ETokenType.UNDEF;
          }
          break;
        }
        data = strioReader.readUntilDelimiter();
        break;
      case NAMED_VALUE:
        // Имя параметра должно преставлять ИД-путь
        data = strioReader.readUntilDelimiter();
        String valueName = data;
        if( !StridUtils.isValidIdPath( valueName ) ) {
          // Имя не ИД-путь
          tokenType = ETokenType.UNDEF;
          break;
        }
        // Чтение символа '='
        nextChar = strioReader.nextChar( EStrioSkipMode.SKIP_SPACES );
        if( nextChar != STATEMENT_WRITE.charAt( 0 ) ) {
          // Нет символа '='
          tokenType = ETokenType.UNDEF;
          break;
        }
        // Чтение значения
        nextChar = strioReader.peekChar( EStrioSkipMode.SKIP_SPACES );
        quoted = (nextChar == CHAR_QUOTE);
        superQuoted = hasSuperQuote( strioReader );
        if( quoted && !superQuoted ) {
          // Чтение текста в обычных кавычках
          try {
            data = strioReader.readQuotedString();
          }
          catch( StrioRtException e ) {
            // Нет завершающих кавычек
            tokenType = ETokenType.UNDEF;
          }
        }
        if( !quoted && superQuoted ) {
          // Чтение текста в супер кавычках
          try {
            data = readSuperQuotedString( strioReader );
          }
          catch( StrioRtException e ) {
            // Нет завершающих кавычек
            tokenType = ETokenType.UNDEF;
          }
        }
        if( !quoted && !superQuoted ) {
          data = strioReader.readUntilDelimiter();
        }
        int finishPosition = Math.min( aSource.length(), currentPosition() );
        data = aSource.substring( startPosition, finishPosition );
        aTokens.add( new AdminCmdToken( tokenType, startPosition, finishPosition - 1, data, quoted || superQuoted ) );
        return strioReader.peekChar( EStrioSkipMode.SKIP_BYPASSED );
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
    // Фиксация позиции лексемы
    // 2022-09-10 mvk
    // int finishPosition = currentPosition() - 1;
    int finishPosition = currentPosition();
    if( finishPosition < startPosition ) {
      // Пустая лексема
      startPosition = -1;
      finishPosition = -1;
    }
    if( quoted ) {
      // Для текста в кавычках позиции ковычек не учитываются
      startPosition++;
      finishPosition--;
    }
    // 2022-09-10 mvk
    aTokens.add( new AdminCmdToken( tokenType, startPosition, finishPosition, data, quoted || superQuoted ) );
    // aTokens.add( new AdminCmdToken( tokenType, startPosition - 1, finishPosition, data, quoted || superQuoted ) );
    return strioReader.peekChar( EStrioSkipMode.SKIP_BYPASSED );
  }

  /**
   * Возвращает признак того, что лексемный читатель установлен на чтение супер кавычках.
   *
   * @param aStrioReader {@link IStrioReader} лексемный читатель входного потока символов
   * @return <b>true</b> следующий символ первая из супер кавычек; <b>false</b> следующий символ не супер кавычки.
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static boolean hasSuperQuote( IStrioReader aStrioReader ) {
    TsNullArgumentRtException.checkNull( aStrioReader );
    // Количество прочитанных символов из потока читателя
    int readCount = 0;
    // Количество проверенных символов супер кавычек
    int checkCount = 0;
    try {
      // Проверка наличия в потоке строке суперкавычек
      for( int index = 0, n = SUPER_QUOTE_STRING.length(); index < n; index++ ) {
        char ch = aStrioReader.nextChar();
        readCount++;
        if( ch == CHAR_EOF || ch != SUPER_QUOTE_STRING.charAt( index ) ) {
          break;
        }
        checkCount++;
      }
    }
    finally {
      // Возвращение символов назад в поток
      for( int index = 0; index < readCount; index++ ) {
        aStrioReader.putCharBack();
      }
    }
    return (checkCount == SUPER_QUOTE_STRING.length());
  }

  /**
   * Считывает из входного потока читателя супер кавычки
   *
   * @param aStrioReader {@link IStrioReader} лексемный читатель входного потока символов
   * @throws TsNullArgumentRtException аргумент = null
   * @throws StrioRtException ошибка чтения суперкавычек
   */
  private static void readSuperQuote( IStrioReader aStrioReader ) {
    TsNullArgumentRtException.checkNull( aStrioReader );
    // Проверка наличия в потоке строке суперкавычек
    for( int index = 0, n = SUPER_QUOTE_STRING.length(); index < n; index++ ) {
      char ch = aStrioReader.nextChar();
      char ec = SUPER_QUOTE_STRING.charAt( index );
      if( ch == CHAR_EOF || ch != ec ) {
        // Ошибка чтения супер кавычек
        throw new StrioRtException( MSG_ERR_READ_SUPER_QUOTE, SUPER_QUOTE_STRING, Character.valueOf( ch ),
            Character.valueOf( ec ) );
      }
    }
  }

  /**
   * Считывает строку в супер кавычках.
   * <p>
   * При успехе, считываение останавливается перед первым сиволом после последней закрывающей кавычки. Не-успех возможен
   * если первый символ не кавычка, перед которым останалвивается чтение. также возможно, что во время чтения строки
   * встретиться {@link IStrioHardConstants#CHAR_EOF}.
   *
   * @param aStrioReader {@link IStrioReader} лексемный читатель входного потока символов
   * @return string - считанная строка
   * @throws TsNullArgumentRtException аргумент = null
   * @throws StrioRtException первый символ не кавычка или внутри строки оказался {@link IStrioHardConstants#CHAR_EOF}
   */
  private static String readSuperQuotedString( IStrioReader aStrioReader ) {
    TsNullArgumentRtException.checkNull( aStrioReader );
    StringBuilder sb = new StringBuilder();
    if( !hasSuperQuote( aStrioReader ) ) {
      char ch = aStrioReader.peekChar( EStrioSkipMode.SKIP_NONE );
      // Не найдена открывающая супер кавычка
      throw new StrioRtException( MSG_ERR_SUPER_QUOTE_EXPECTED, Character.valueOf( ch ), SUPER_QUOTE_STRING );
    }
    readSuperQuote( aStrioReader ); // считаем открывающие супер кавычки
    while( true ) {
      if( hasSuperQuote( aStrioReader ) ) {
        readSuperQuote( aStrioReader ); // считаем закрывающие супер кавычки
        break;
      }
      char ch = aStrioReader.nextChar();
      if( ch == CHAR_EOF ) {
        // Не найдены закрывающие супер кавычки
        throw new StrioRtException( MSG_ERR_SUPER_QUOTE_EXPECTED, Character.valueOf( ch ), SUPER_QUOTE_STRING );
      }
      sb.append( ch );
    }
    String retValue = sb.toString();
    return retValue;
  }

  /**
   * Проверяет, что в тексте на указанной позиции находится оператор утверждения
   *
   * @param aText String текст строки
   * @param aPosition проверяемая позиция
   * @return boolean <b>true</b>по указанной позиции находится оператор.<b>false</b>на указанной позиции нет оператора
   */
  private static boolean isStatement( String aText, int aPosition ) {
    for( int index = 0, n = STATEMENTS.length; index < n; index++ ) {
      if( aText.startsWith( STATEMENTS[index], aPosition ) ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Возвращает текущую позицию символа который будет считан из командной строки
   *
   * @return int текущая позиция символа
   */
  private int currentPosition() {
    // 2022-09-20 mvk
    // return strioReader.currentPosition() + strioReaderPositionDelta;
    return strioReader.getInput().currentPosition().charNo() + strioReaderPositionDelta - 1;
  }

  // // ------------------------------------------------------------------------------------
  // // Тесты
  // //
  // /**
  // * @param args
  // */
  // @SuppressWarnings( "nls" )
  @SuppressWarnings( { "javadoc", "nls" } )
  public static void main( String[] args ) {
    // String cmdLine =
    // "b=1, d = \" abc3\" --ru.toxsoft.test1 $a \"проверка текста в кавычках\" -test2 b,f -23.324, 3 ";
    // // String cmdLine = "-test --ru.toxsoft b,f --test -23.324, 3,,";
    // String cmdLine = "a ; ,b, c, ;d testID 1;2;3 \"раз два ";
    // String cmdLine = "one two 2013-12-01_00:00:00.000 ";
    // String cmdLine = "if value1 == value2";
    // String cmdLine = "$ru.toxsoft.test1, org.linux.test2 -> login root $root2, test";
    // String cmdLine = "echo test,\"";
    // String cmdLine = "abc t=13, b=\"afd\" value one = 1";
    String cmdLine = "$d= \"строка1\", \"строка2\"";
    AdminCmdLexicalParser parser = new AdminCmdLexicalParser();
    System.out.println( cmdLine );
    System.out.println( "******************************************************************************************" );
    for( int index = 0; index < cmdLine.length(); index++ ) {
      // System.out.println( index + "'" + cmdLine.charAt( index ) + "'" );
    }
    IList<IAdminCmdToken> tokens = parser.parse( cmdLine );
    System.out.println( "******************************************************************************************" );
    for( IAdminCmdToken token : tokens ) {
      System.out.println( "type: " + token.type() + ", start: " + token.startIndex() + ", finish: "
          + token.finishIndex() + (token.type() == ETokenType.LIST_VALUE ? ", list: " + token.listIndex() : "")
          + ", data: '" + token.data() + "'" );
    }

    // printColor( "COLOR_GRAY", IAdminAnsiConstants.COLOR_GRAY );
    // printColor( "COLOR_WHITE", IAdminAnsiConstants.COLOR_WHITE );
    // printColor( "COLOR_LIGHT_CYAN", IAdminAnsiConstants.COLOR_LIGHT_CYAN );
    // printColor( "COLOR_CYAN", IAdminAnsiConstants.COLOR_CYAN );
    // printColor( "COLOR_VIOLET", IAdminAnsiConstants.COLOR_VIOLET );
    // printColor( "COLOR_GRAY_BG_DARK_GRAY ", IAdminAnsiConstants.COLOR_GRAY_BG_DARK_GRAY );
    // printColor( "COLOR_DARK_RED ", IAdminAnsiConstants.COLOR_DARK_RED );
    // printColor( "COLOR_DARK_BLUE ", IAdminAnsiConstants.COLOR_DARK_BLUE );
    // printColor( "COLOR_BROWN ", IAdminAnsiConstants.COLOR_BROWN );
    // printColor( "COLOR_LIGHT_BROWN ", IAdminAnsiConstants.COLOR_LIGHT_BROWN );
    // printColor( "COLOR_GREEN ", IAdminAnsiConstants.COLOR_GREEN );
    // printColor( "COLOR_BG_GRAY ", IAdminAnsiConstants.COLOR_BG_GRAY );
    // printColor( "COLOR_BG_CYAN ", IAdminAnsiConstants.COLOR_BG_CYAN );
    // printColor( "COLOR_BG_VIOLET ", IAdminAnsiConstants.COLOR_BG_VIOLET );
    // printColor( "COLOR_BG_BLUE ", IAdminAnsiConstants.COLOR_BG_BLUE );
    // printColor( "COLOR_BG_BROWN ", IAdminAnsiConstants.COLOR_BG_BROWN );
    // printColor( "COLOR_BG_GREEN ", IAdminAnsiConstants.COLOR_BG_GREEN );
    // printColor( "COLOR_BG_LIGHT_GRAY ", IAdminAnsiConstants.COLOR_BG_LIGHT_GRAY );
    // }
    //
    // @SuppressWarnings( "nls" )
    // private static void printColor( String aName, String aColor ) {
    // System.out.println( "color name: " + aName + ", lengh: " + aColor.length() );
    // for( int index = 0, n = aColor.length(); index < n; index++ ) {
    // System.out.println( "[" + index + "] = " + (int)aColor.charAt( index ) + ", char = " + aColor.charAt( index ) );
    // }
  }
}
