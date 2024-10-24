package org.toxsoft.uskat.skadmin.cli.cmds;

import static java.lang.String.*;
import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.cli.AdminColors.*;
import static org.toxsoft.uskat.skadmin.cli.IAdminAnsiConstants.*;
import static org.toxsoft.uskat.skadmin.cli.cmds.IAdminResources.*;
import static org.toxsoft.uskat.skadmin.cli.parsers.AdminCmdParserUtils.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.core.impl.AdminCmdLibraryUtils.*;
import static org.toxsoft.uskat.skadmin.core.plugins.AdminPluginUtils.*;

import java.io.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.math.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.derivative.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.legacy.plexy.*;
import org.toxsoft.uskat.skadmin.cli.*;
import org.toxsoft.uskat.skadmin.cli.parsers.*;
import org.toxsoft.uskat.skadmin.core.*;

/**
 * Команда консоли: 'Выполнение команд из представленного файла'
 *
 * @author mvk
 */
public class ConsoleCmdBatch
    extends AbstractConsoleCmd {

  /**
   * Каталог размещения скриптов
   */
  private static final String SCRIPT_HOME = File.separator + "scripts" + File.separator; //$NON-NLS-1$

  /**
   * Расширение файлов сценариев
   */
  private static final String SCRIPT_EXT = ".cli"; //$NON-NLS-1$

  /**
   * Список выполняемых операторов
   */
  private final IListEdit<Operator> operators = new ElemLinkedList<>();

  /**
   * Парсер разбора синтаксических операторов
   */
  private final IAdminCmdSyntaxParser parser = new AdminCmdLexicalParser();

  /**
   * Конструктор
   *
   * @param aConsole {@link IAdminConsole} консоль
   */
  public ConsoleCmdBatch( IAdminConsole aConsole ) {
    super( aConsole );
    // Текстовый файл с командами для выполнения
    addArg( BATCH_ARG_FILE_ID, BATCH_ARG_FILE_ALIAS, BATCH_ARG_FILE_NAME, PT_SINGLE_STRING, BATCH_ARG_FILE_DESCR );
    // Значения для аргументов скрипта
    addArg( BATCH_ARG_ARGS_ID, BATCH_ARG_ARGS_ALIAS, BATCH_ARG_ARGS_NAME, PT_LIST_STRING, BATCH_ARG_ARGS_DESCR );
    // Кодировка текстового файла
    addArg( BATCH_ARG_CHARSET_ID, BATCH_ARG_CHARSET_ALIAS, BATCH_ARG_CHARSET_NAME,
        createType( STRING, avStr( BATCH_ARG_CHARSET_DEFAULT ) ), BATCH_ARG_CHARSET_DESCR );
    // Завершение работы консоли после выполнения команд
    addArg( BATCH_ARG_EXIT_ID, BATCH_ARG_EXIT_ALIAS, BATCH_ARG_EXIT_NAME,
        createType( BOOLEAN, avBool( Boolean.parseBoolean( BATCH_ARG_EXIT_DEFAULT ) ) ), BATCH_ARG_EXIT_DESCR );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return BATCH_CMD_ID;
  }

  @Override
  public String alias() {
    return BATCH_CMD_ALIAS;
  }

  @Override
  public String nmName() {
    return BATCH_CMD_NAME;
  }

  @Override
  public String description() {
    return BATCH_CMD_DESCR;
  }

  @Override
  public IPlexyType resultType() {
    return IPlexyType.NONE;
  }

  @Override
  public IStringList roles() {
    return IStringList.EMPTY;
  }

  @Override
  protected void doExec( IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback ) {
    IAdminConsole console = getConsole();
    IAdminCmdContext beforeStartContext = console.context();
    String applicationDir = beforeStartContext.paramValue( CTX_APPLICATION_DIR ).singleValue().asString();
    // Аргументы команды
    String argFilename = argSingleValue( BATCH_ARG_FILE_ID ).asString();
    IStringList args = argStrList( BATCH_ARG_ARGS_ID );
    String charset = argSingleValue( BATCH_ARG_CHARSET_ID ).asString();
    boolean exit = argSingleValue( BATCH_ARG_EXIT_ID ).asBool();
    // Определяем абсолютное имя файла сценария
    File file = getScriptFile( applicationDir, argFilename );
    while( !file.exists() ) {
      // Файл не найден. Запрашиваем пользователя другой файл
      ValidationResult message = ValidationResult.error( ERR_MSG_FILE_NOT_FOUND, argFilename );
      IPlexyValue newFile = aCallback.getValue( PT_SINGLE_STRING, IList.EMPTY, new ElemArrayList<>( message ) );
      if( newFile == IPlexyValue.NULL ) {
        // Пользователь отказался от выполнения команды
        addResultError( ERR_MSG_CMD_REJECT, id() );
        resultOk();
        return;
      }
      file = new File( newFile.singleValue().asString() );
    }
    // Текущий файл
    String filename = file.getName();
    // Текущая строка скрипта
    int lineNo = 0;
    try {
      // Читаем построчно файл и выполняем команды
      BufferedReader reader = openScriptFile( file, charset, console );
      // Стек файловых читателей.
      IStack<BufferedReader> readers = new Stack<>();
      // Стек имен файлов из которых проводится чтение сценария.
      IStack<String> readerFileNames = new Stack<>();
      // Стек линий в файле из которых проводится чтение сценария.
      IStack<Integer> readerLines = new Stack<>();
      try {
        // Строка текущей команды
        String line;
        // Стек контекстов. Корень: контекст вызова batch
        IStack<IAdminCmdContext> contextStack = new Stack<>();
        // Формирование дочернего контекста
        IAdminCmdContext childContext = createChildContext( beforeStartContext );
        // Сохранение родительского контекста
        contextStack.push( beforeStartContext );
        // Установка дочернего контекста
        console.changeContext( childContext );
        // Сохранение в значений аргументов в контексте
        for( int index = 0, n = args.size(); index < n; index++ ) {
          // index + 1: именование идет с $1
          String argName = format( BATCH_ARG_NAME_FORMAT, Integer.valueOf( index + 1 ) );
          String argValue = args.get( index );
          // Если в контексте уже есть параметр (от родительского контекста), то удаляем его
          if( childContext.hasParam( argName ) ) {
            childContext.removeParam( argName );
          }
          // Размещение в контексте в режиме readonly
          childContext.setParamValue( argName, pvsStr( argValue ), true );
        }
        try {
          while( reader != null ) {
            // Построитель многострочной команды
            StringBuilder muliLineCmd = null;
            while( (line = getNextLineOrNull( reader )) != null ) {
              lineNo++;
              // Отбрасываем лидирующие и замыкающие пробелы
              line = line.trim();
              if( line.equals( EMPTY_STRING ) ) {
                // Пустая строка
                continue;
              }
              if( line.startsWith( CHAR_LINE_COMMENT ) ) {
                // Строка начинается с символа '#': строка комментария
                continue;
              }
              try {
                // Признак завершения строки символом продолжения
                boolean muliline = line.endsWith( CHAR_MULTI_LINE );
                if( muliline ) {
                  // Выбрасываем из строки символ переноса
                  line = line.substring( 0, line.length() - 1 );
                  if( muliLineCmd == null ) {
                    muliLineCmd = new StringBuilder();
                  }
                  muliLineCmd.append( line );
                  // Переход на следующую строку команды
                  continue;
                }
                if( muliLineCmd != null ) {
                  // Ранее уже была сформирована многострочная команда, завершаем ее формирование
                  muliLineCmd.append( line );
                  line = muliLineCmd.toString();
                  muliLineCmd = null;
                }
                // Предыдущий оператор
                Operator prevOperator = getCurrentOperatorOrNull();
                // Проводим анализ на предмет синтаксических операторов команды batch
                EOperatorType operatorType = getOperator( console.context(), line );
                // Текущий оператор
                Operator operator = getCurrentOperatorOrNull();
                if( operator != null && operator.isIgnored() || //
                    operatorType == EOperatorType.END && prevOperator != null && prevOperator.isIgnored() ) {
                  // Команды текущего оператора игнорируются или получен оператор END ранее игнорировавшегося оператора
                  continue;
                }
                // Текущий контекст
                IAdminCmdContext context = console.context();
                // Родительский, дочерний контексты
                IAdminCmdContext parentContext = null;
                switch( operatorType ) {
                  case NOOP:
                    break;
                  case IF:
                    // Формирование дочернего контекста
                    childContext = createChildContext( context );
                    // Сохранение родительского контекста
                    contextStack.push( context );
                    // Установка дочернего контекста
                    console.changeContext( childContext );
                    continue;
                  case WHILE:
                    throw new TsUnderDevelopmentRtException();
                  case END:
                    // Получение родительского контекста
                    parentContext = contextStack.pop();
                    // Перемещение текущих значений параметров контекста в родительский контекст без оповещения
                    updateParentContext( parentContext, context );
                    // Установка родительского контекста
                    console.changeContext( parentContext );
                    continue;
                  case RETURN:
                    // Оператор завершения сценария.
                    contextStack.clear();
                    contextStack.push( beforeStartContext );
                    // Успешное завершение
                    resultOk();
                    return;
                  case INCLUDE:
                    // Включение команд из другого файла
                    String newFilename = line.substring( EOperatorType.INCLUDE.id().length() ).trim();
                    File script = getScriptFile( applicationDir, newFilename );
                    if( !script.exists() ) {
                      // Включаемый файл не найден
                      console.print( 0, false, ERR_MSG_INCLUDE_NOT_FOUND, script.getAbsolutePath() );
                      throw new TsIllegalArgumentRtException();
                    }
                    // Читаем построчно файл и выполняем команды
                    readers.push( reader );
                    readerFileNames.push( filename );
                    readerLines.push( Integer.valueOf( lineNo ) );
                    reader = openScriptFile( script, charset, console );
                    filename = newFilename;
                    lineNo = 0;
                    continue;
                  default:
                    throw new TsNotAllEnumsUsedRtException();
                }
                IAdminCmdResult result = console.execute( line, false );
                // Поднимаем ошибку выполнения команды - чтобы использовать ее журналирование
                if( !result.isOk() ) {
                  throw new TsException( result.toString() );
                }
              }
              catch( @SuppressWarnings( "unused" ) Exception e ) {
                Long lineNoL = Long.valueOf( lineNo );
                // Ошибка выполнения команды
                ValidationResult err = ValidationResult.error( ERR_MSG_SCRIPT, filename, lineNoL, line );
                // Уведомляем об ошибке с возможностью прерывания выполнения скрипта
                boolean toContinue = aCallback.onNextStep( new ElemArrayList<>( err ), lineNo, 0, true );
                if( !toContinue ) {
                  // Пользователь отказался от дальнейшего выполнения скрипта
                  resultFail();
                  return;
                }
                continue;
              }
            } // while( (line = getNextLineOrNull( in )) != null )
            if( muliLineCmd != null ) {
              // Не закончено формирование многострочной команды
              throw new TsIllegalArgumentRtException( ERR_MSG_UNCLOSED_MULTILINE );
            }
            reader.close();
            reader = readers.popOrNull();
            filename = readerFileNames.popOrNull();
            Integer lineNoL = readerLines.peekOrNull();
            lineNo = (lineNoL != null ? lineNoL.intValue() : 0);
          }
        }
        finally {
          // Проверка того, что все операторы завершены
          TsIllegalArgumentRtException.checkFalse( contextStack.size() == 1, ERR_MSG_UNCLOSED_OPERATOR );
          // Восстановление контекста существовашего до начала выполнения batch
          // Обновление параметров родительского контекста
          updateParentContext( beforeStartContext, console.context() );
          // Установка родительского контекста
          console.changeContext( beforeStartContext );
        }
        if( exit ) {
          console.close();
        }
      }
      finally {
        while( reader != null ) {
          reader.close();
          reader = readers.popOrNull();
        }
      }
      resultOk();
    }
    catch( Exception e ) {
      String cause = e.getLocalizedMessage();
      Long lineNoL = Long.valueOf( lineNo );
      addResultError( MSG_ERR_CMD_UNEXPECTED, filename, lineNoL, (cause != null ? cause : e.getClass().getName()) );
      addResultError( e );
      resultFail();
    }
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    if( aArgId.equals( BATCH_ARG_FILE_ID ) ) {
      IListEdit<IPlexyValue> retValue = new ElemLinkedList<>();
      String applicationDir = contextParamValue( CTX_APPLICATION_DIR ).singleValue().asString() + SCRIPT_HOME;
      loadFileNames( applicationDir, applicationDir, retValue );
      return retValue;
    }
    return IList.EMPTY;
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  //
  /**
   * Возвращает следующую строку сценария для выполнения или null если больше строк нет
   *
   * @param aExternalBuffer {@link BufferedReader} внешний буфер чтения строк сценария
   * @return String следующая строка. null: больше строк нет
   * @throws TsNullArgumentRtException аргумент = null
   * @throws IOException ошибка ввода-вывода
   */
  private String getNextLineOrNull( BufferedReader aExternalBuffer )
      throws IOException {
    if( aExternalBuffer == null ) {
      throw new TsNullArgumentRtException();
    }
    int operatorCount = operators.size();
    // Текущий выполняемый оператор
    Operator operator = (operatorCount > 0 ? operators.get( operatorCount - 1 ) : null);
    if( operator != null && operator.isCmdBlockReady() && operator.hasCmd() ) {
      // Есть текущий оператор, у него готовый блок и есть команда в блоке для выполнения
      return operator.nextCmd();
    }
    // Чтение строки из внешнего буфера
    return aExternalBuffer.readLine();
  }

  /**
   * Выполняет попытку обработки строки сценария на предмет присутствия в ней синтаксических операторов сценария команды
   * batch
   *
   * @param aContext {@link IAdminCmdContext} контекст команды
   * @param aLine String командная строка
   * @return <b>true</b> командная строка обработана; <b>false</b> командная строка не обработана (ее можно выполнить)
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException неверный синтаксис
   */
  private EOperatorType getOperator( IAdminCmdContext aContext, String aLine ) {
    TsNullArgumentRtException.checkNull( aLine );
    IList<IAdminCmdToken> tokens = parser.parse( aLine );
    if( tokens.size() == 0 ) {
      // Пустая строка
      return EOperatorType.NOOP;
    }
    // Текущий оператор или null
    Operator operator = getCurrentOperatorOrNull();
    // Признак игнорирования текущего оператора
    boolean operatorIgnore = (operator != null && operator.isIgnored());
    // Определяем тип оператора
    EOperatorType type = EOperatorType.findByIdOrNull( tokens.get( 0 ).data().toLowerCase() );
    if( type == null ) {
      // Строка команда не представляет оператора сценария batch, то добавляем строку в текущие операторы если они
      // есть...
      tryAddCmdToOperators( aLine );
      // Команда не является оператором
      return EOperatorType.NOOP;
    }
    switch( type ) {
      case NOOP:
        // Алгоритм не позволяет подобную ситуацию
        throw new TsInternalErrorRtException();
      case IF:
        // Формат оператора: if number1 op number2 или if logical
        int countValues = tokens.size();
        IListEdit<ValidationResult> errorsList = new ElemLinkedList<>();
        switch( countValues ) {
          case 2:
            // Формат 'if logicalValue'
            IAtomicValue value =
                valueParse( aContext, tokens.get( 1 ), IAvMetaConstants.DDEF_NONE, "value", errorsList ); //$NON-NLS-1$
            operatorIgnore |= !checkCondition( value );
            break;
          case 4:
            // Формат 'if numberValue1 op numberValue2'
            IAtomicValue value1 =
                valueParse( aContext, tokens.get( 1 ), IAvMetaConstants.DDEF_NONE, "value1", errorsList ); //$NON-NLS-1$
            String op = tokens.get( 2 ).data();
            IAtomicValue value2 =
                valueParse( aContext, tokens.get( 3 ), IAvMetaConstants.DDEF_NONE, "value1", errorsList ); //$NON-NLS-1$
            operatorIgnore |= !checkCondition( value1, op, value2 );
            break;
          default:
            TsIllegalArgumentRtException.checkFalse( countValues != 2 || countValues != 4, ERR_MSG_WRONG_IF_FORMAT );
        }
        break;
      case WHILE:
        throw new TsUnderDevelopmentRtException();
      case END:
        // Завершение текущего оператора
        closeOperator();
        // Попытка добавить команду END в следующий текущий оператор
        tryAddCmdToOperators( aLine );
        return EOperatorType.END;
      case RETURN:
        // Завершение работы скрипта
        return EOperatorType.RETURN;
      case INCLUDE:
        // Попытка добавить команду INCLUDE в следующий текущий оператор
        tryAddCmdToOperators( aLine );
        return EOperatorType.INCLUDE;
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
    // Формирование операторов
    Operator newOperator = new Operator( type );
    if( operatorIgnore ) {
      // Оператор блокирован условием
      newOperator.setIgnored();
    }
    operators.add( newOperator );
    return type;
  }

  /**
   * Возвращает текущий оператор или null если его нет
   *
   * @return {@link Operator} текущий оператора или null
   */
  private Operator getCurrentOperatorOrNull() {
    return (operators.size() > 0 ? operators.get( operators.size() - 1 ) : null);
  }

  /**
   * Проверяет выполнение условия
   *
   * @param aValue IAtomicValue логическое значение
   * @return <b>true</b>условие удолетворено;<b>false</b>условие не удолетворено.
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static boolean checkCondition( IAtomicValue aValue ) {
    TsNullArgumentRtException.checkNull( aValue );
    try {
      return aValue.asBool();
    }
    catch( RuntimeException e ) {
      throw new TsIllegalArgumentRtException( e, ERR_MSG_EXPECTED_LOGICAL, e.getLocalizedMessage() );
    }
  }

  /**
   * Проверяет выполнение условия
   *
   * @param aValue1 IAtomicValue первое значение
   * @param aCompareOp String операция сравнения в строковом виде
   * @param aValue2 IAtomicValue второе значение
   * @return <b>true</b>условие удолетворено;<b>false</b>условие не удолетворено.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static boolean checkCondition( IAtomicValue aValue1, String aCompareOp, IAtomicValue aValue2 ) {
    TsNullArgumentRtException.checkNulls( aValue1, aCompareOp, aValue2 );
    try {
      IAvComparator comparator = AvComparatorStrict.INSTANCE;
      EAvCompareOp op = EAvCompareOp.findById( aCompareOp.toUpperCase() );
      TsIllegalArgumentRtException.checkTrue( op == null, ERR_MSG_UNKNOW_COMPARE, aCompareOp );
      return comparator.avCompare( aValue1, op, aValue2 );
    }
    catch( RuntimeException e ) {
      throw new TsIllegalArgumentRtException( e, ERR_MSG_COMPARE_IMPOSSIBLE, aValue1, aCompareOp, aValue2,
          e.getLocalizedMessage() );
    }
  }

  /**
   * Делает попытку добавления команды в блоки команд текущих операторов
   *
   * @param aCmd String команда
   * @return <b>true</b> команда добавлена в текущий оператор; <b>false</b> команда не добавлена в текущий оператор
   * @throws TsNullArgumentRtException аргумент = null
   */
  private boolean tryAddCmdToOperators( String aCmd ) {
    TsNullArgumentRtException.checkNull( aCmd );
    // Признак того, что команда была добавлена
    boolean topAdded = false;
    for( int index = operators.size() - 1; index >= 0; index-- ) {
      Operator operator = operators.get( index );
      if( index == operators.size() - 1 && !operator.isCmdBlockReady() ) {
        topAdded = true;
      }
      if( !operator.isCmdBlockReady() ) {
        // Добавление команды в блок оператора
        operator.addBlockCmd( aCmd );
      }
    }
    return topAdded;
  }

  /**
   * Завершает текущий оператор
   */
  private void closeOperator() {
    if( operators.size() == 0 ) {
      throw new TsIllegalArgumentRtException( ERR_MSG_UNEXPECTED_OPERATOR, EOperatorType.END );
    }
    // Количество текущих выполняемых операторов
    int operatorCount = operators.size();
    // Текущий оператор
    Operator operator = operators.get( operatorCount - 1 );
    // Формирование команд оператора завершено
    operator.setCmdBlockReady();
    // TODO: работа с циклами
    // Operator operator = operators.get( operatorCount - 1 );
    // Удаление оператора из списка текущих операторов
    operators.removeByIndex( operatorCount - 1 );
  }

  /**
   * Открывает файл сценария для чтения
   *
   * @param aFile {@link File} файл сценария
   * @param aEncoding String константа кодовой страницы
   * @param aConsole {@link IAdminConsole} консоль
   * @return {@link BufferedReader} читатель сценария
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException ошибка открытия файла сценария
   */
  private static BufferedReader openScriptFile( File aFile, String aEncoding, IAdminConsole aConsole ) {
    TsNullArgumentRtException.checkNulls( aFile, aEncoding, aConsole );
    try {
      FileInputStream inputStream = new FileInputStream( aFile );
      try {
        InputStreamReader inputStreamReader = new InputStreamReader( inputStream, aEncoding );
        try {
          BufferedReader reader = new BufferedReader( inputStreamReader );
          return reader;
        }
        catch( RuntimeException e ) {
          aConsole.print( 0, false, COLOR_ERROR + ERR_MSG_OPEN_FILE_READER + COLOR_RESET + "\n", //$NON-NLS-1$
              aFile.getAbsolutePath(), e.getLocalizedMessage() );
          throw new TsIllegalArgumentRtException( ERR_MSG_OPEN_FILE_READER, aFile.getAbsolutePath(),
              e.getLocalizedMessage() );
        }
      }
      catch( UnsupportedEncodingException e ) {
        aConsole.print( 0, false, COLOR_ERROR + ERR_MSG_OPEN_FILE_UNSUPPORT_ENCODING + COLOR_RESET + "\n", //$NON-NLS-1$
            aFile.getAbsolutePath(), e.getLocalizedMessage() );
        throw new TsIllegalArgumentRtException( ERR_MSG_OPEN_FILE_UNSUPPORT_ENCODING, aFile.getAbsolutePath(),
            e.getLocalizedMessage() );
      }
      // catch( RuntimeException e ) {
      // throw new TsIllegalArgumentRtException( ERR_MSG_OPEN_FILE_STREAM_READER, aFile.getAbsolutePath(),
      // e.getLocalizedMessage() );
      // }
    }
    catch( FileNotFoundException e ) {
      aConsole.print( 0, false, COLOR_ERROR + ERR_MSG_OPEN_FILE_NOT_FOUND + COLOR_RESET + "\n", //$NON-NLS-1$
          aFile.getAbsolutePath(), e.getLocalizedMessage() );
      throw new TsIllegalArgumentRtException( ERR_MSG_OPEN_FILE_NOT_FOUND, aFile.getAbsolutePath(),
          e.getLocalizedMessage() );
    }
    // catch( RuntimeException e ) {
    // throw new TsIllegalArgumentRtException( ERR_MSG_OPEN_FILE_STREAM, aFile.getAbsolutePath(),
    // e.getLocalizedMessage() );
    // }
  }

  /**
   * Возвращает файл сценария
   *
   * @param aApplicationDir String каталог приложения s5admin
   * @param aFileName имя файла сценария
   * @return File сценария
   */
  private static File getScriptFile( String aApplicationDir, String aFileName ) {
    // Определение файла с командами для консоли
    String filename = aApplicationDir + SCRIPT_HOME + aFileName;
    // Замена файлового разделителя используемого в целевой платформе
    filename = filename.replace( CHAR_WIN_FILE_SEPARATOR, File.separatorChar );
    File file = new File( filename );
    if( !file.exists() ) {
      file = new File( aFileName );
    }
    return file;
  }

  /**
   * Загрузка списка файлов из директория
   *
   * @param aBaseDir базовый каталог
   * @param aDir String имя файла из которого производится загрузка
   * @param aFiles {@link IListEdit}&lt; {@link IPlexyValue}&gt; список значений представляющих имена файлов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void loadFileNames( String aBaseDir, String aDir, IListEdit<IPlexyValue> aFiles ) {
    TsNullArgumentRtException.checkNulls( aDir, aFiles );
    File dir = new File( aDir );
    if( !dir.exists() || !dir.isDirectory() ) {
      // Директорий не существует
      return;
    }
    File[] files = dir.listFiles();
    for( int index = 0, n = files.length; index < n; index++ ) {
      File file = files[index];
      if( file.isDirectory() ) {
        // Обработка вложенных каталогов
        loadFileNames( aBaseDir, file.getAbsolutePath(), aFiles );
        continue;
      }
      String name = file.getAbsolutePath().substring( aBaseDir.length() );
      // Замена файлового разделителя на разделить используемый в windows
      name = name.replace( File.separatorChar, CHAR_WIN_FILE_SEPARATOR );
      if( !name.endsWith( SCRIPT_EXT ) ) {
        // Не файл сценария
        continue;
      }
      // Замена файлового разделителя на разделить используемый в windows
      aFiles.add( pvsStr( name ) );
    }
  }

  /**
   * Синтаксический оператор сценария batch
   */
  static class Operator {

    private final EOperatorType   type;
    private final IStringListEdit cmds     = new StringLinkedBundleList();
    private int                   cmdIndex = -1;
    private boolean               cmdBlockReady;
    private boolean               ignored;

    /**
     * Конструктор оператора
     *
     * @param aType {@link EOperatorType} тип оператора
     * @throws TsNullArgumentRtException аргумент = null
     */
    Operator( EOperatorType aType ) {
      TsNullArgumentRtException.checkNull( aType );
      type = aType;
    }

    // ------------------------------------------------------------------------------------
    // API класса
    //
    /**
     * Тип оператора
     *
     * @return {@link EOperatorType} тип оператора
     */
    EOperatorType type() {
      return type;
    }

    /**
     * Возвращает признак того, что формирование блока команд оператора завершено
     *
     * @return <b>true</b> формирование блока завершено; <b>false</b> формирование блока еще не завершено.
     */
    boolean isCmdBlockReady() {
      return cmdBlockReady;
    }

    /**
     * Возвращает признак того, что все команды оператора должны быть игнорированы
     *
     * @return <b>true</b> команды должны быть игнорированы; <b>false</b> команды могут быть использованы
     */
    boolean isIgnored() {
      return ignored;
    }

    /**
     * Возвращает признак того, есть команда для выполнения в рамках оператора (операторных скобок)
     *
     * @return boolean <b>true</b> есть команда;<b>false</b> блок команд пустой или достигли его завершения.
     */
    boolean hasCmd() {
      return (!ignored && cmdIndex >= 0);
    }

    /**
     * Возвращает следующую команду из блока оператора
     *
     * @return {@link TsIllegalStateRtException} нет команд
     */
    String nextCmd() {
      TsIllegalStateRtException.checkFalse( hasCmd() );
      String retValue = cmds.get( cmdIndex );
      cmdIndex = (cmdIndex < cmds.size() - 1 ? cmdIndex++ : -1);
      return retValue;
    }

    /**
     * Формирование блока команд оператора. Добавляет очередную команду в блок оператора
     *
     * @param aCmd String добавляемая команда с аргументами
     * @throws TsNullArgumentRtException аргумент= null;
     * @throws TsIllegalStateRtException формирование операторного блока уже завершено;
     */
    void addBlockCmd( String aCmd ) {
      TsNullArgumentRtException.checkNull( aCmd );
      TsIllegalStateRtException.checkTrue( cmdBlockReady );
      cmds.add( aCmd );
      cmdIndex = (cmdIndex < 0 ? cmds.size() - 1 : cmdIndex);
    }

    /**
     * Устанавливает признак того, что формирование блока команд оператора завершено
     */
    void setCmdBlockReady() {
      cmdBlockReady = true;
    }

    /**
     * Устанавливает признак того, все команды блока должны быть игнорированы
     */
    void setIgnored() {
      ignored = true;
    }
  }

  /**
   * Тип синтаксического оператора сценария batch
   */
  enum EOperatorType
      implements IStridable {

    /**
     * Нет оператора.
     */
    NOOP( "noop", E_OPERATOR_N_NOOP, E_OPERATOR_D_NOOP ), //$NON-NLS-1$

    /**
     * Оператор if.
     */
    IF( "if", E_OPERATOR_N_IF, E_OPERATOR_D_IF ), //$NON-NLS-1$

    /**
     * Оператор while
     */
    WHILE( "while", E_OPERATOR_N_WHILE, E_OPERATOR_D_WHILE ), //$NON-NLS-1$

    /**
     * Оператор end.
     */
    END( "end", E_OPERATOR_N_END, E_OPERATOR_D_END ), //$NON-NLS-1$

    /**
     * Оператор return.
     */
    RETURN( "return", E_OPERATOR_N_RETURN, E_OPERATOR_D_RETURN ), //$NON-NLS-1$

    /**
     * Оператор include.
     */
    INCLUDE( "include", E_OPERATOR_N_INCLUDE, E_OPERATOR_D_INCLUDE ); //$NON-NLS-1$

    private final String id;
    private final String nmName;
    private final String description;

    /**
     * Создать константу с заданием всех инвариантов.
     *
     * @param aId String - идентифицирующее название константы
     * @param aName String - краткое удобовчитаемое название
     * @param aDescr String - отображаемое описание константы
     */
    EOperatorType( String aId, String aName, String aDescr ) {
      id = aId;
      nmName = aName;
      description = aDescr;
    }

    // --------------------------------------------------------------------------
    // Реализация интерфейса INameable
    //

    @Override
    public String id() {
      return id;
    }

    @Override
    public String description() {
      return description;
    }

    @Override
    public String nmName() {
      return nmName;
    }

    // ----------------------------------------------------------------------------------
    // Методы проверки
    //
    /**
     * Определяет, существует ли константа перечисления с заданным идентификатором.
     *
     * @param aId String - идентификатор искомой константы
     * @return boolean - признак существования константы <br>
     *         <b>true</b> - константа с заданным идентификатором существует;<br>
     *         <b>false</b> - неет константы с таким идентификатором.
     * @throws TsNullArgumentRtException аргумент = null
     */
    public static boolean isItemById( String aId ) {
      return findByIdOrNull( aId ) != null;
    }

    /**
     * Определяет, существует ли константа перечисления с заданным описанием.
     *
     * @param aDescription String - описание искомой константы
     * @return boolean - признак существования константы <br>
     *         <b>true</b> - константа с заданным описанием существует;<br>
     *         <b>false</b> - неет константы с таким описанием.
     * @throws TsNullArgumentRtException аргумент = null
     */
    public static boolean isItemByDescription( String aDescription ) {
      return findByDescriptionOrNull( aDescription ) != null;
    }

    /**
     * Определяет, существует ли константа перечисления с заданным именем.
     *
     * @param aName String - имя (название) искомой константы
     * @return boolean - признак существования константы <br>
     *         <b>true</b> - константа с заданным именем существует;<br>
     *         <b>false</b> - неет константы с таким именем.
     * @throws TsNullArgumentRtException аргумент = null
     */
    public static boolean isItemByName( String aName ) {
      return findByNameOrNull( aName ) != null;
    }

    // ----------------------------------------------------------------------------------
    // Методы поиска
    //

    /**
     * Возвращает константу по идентификатору или null.
     *
     * @param aId String - идентификатор искомой константы
     * @return ESignalCmd - найденная константа, или null если нет константы с таимк идентификатором
     * @throws TsNullArgumentRtException аргумент = null
     */
    public static EOperatorType findByIdOrNull( String aId ) {
      TsNullArgumentRtException.checkNull( aId );
      for( EOperatorType item : values() ) {
        if( item.id.equals( aId ) ) {
          return item;
        }
      }
      return null;
    }

    /**
     * Возвращает константу по идентификатору или выбрасывает исключение.
     *
     * @param aId String - идентификатор искомой константы
     * @return EAtomicType - найденная константа
     * @throws TsNullArgumentRtException аргумент = null
     * @throws TsItemNotFoundRtException нет константы с таким идентификатором
     */
    public static EOperatorType findById( String aId ) {
      return TsItemNotFoundRtException.checkNull( findByIdOrNull( aId ) );
    }

    /**
     * Возвращает константу по описанию или null.
     *
     * @param aDescription String - описание искомой константы
     * @return EAtomicType - найденная константа, или null если нет константы с таким описанием
     * @throws TsNullArgumentRtException аргумент = null
     */
    public static EOperatorType findByDescriptionOrNull( String aDescription ) {
      TsNullArgumentRtException.checkNull( aDescription );
      for( EOperatorType item : values() ) {
        if( item.description.equals( aDescription ) ) {
          return item;
        }
      }
      return null;
    }

    /**
     * Возвращает константу по описанию или выбрасывает исключение.
     *
     * @param aDescription String - описание искомой константы
     * @return EAtomicType - найденная константа
     * @throws TsNullArgumentRtException аргумент = null
     * @throws TsItemNotFoundRtException нет константы с таким описанием
     */
    public static EOperatorType findByDescription( String aDescription ) {
      return TsItemNotFoundRtException.checkNull( findByDescriptionOrNull( aDescription ) );
    }

    /**
     * Возвращает константу по имени или null.
     *
     * @param aName String - имя искомой константы
     * @return ESignalCmd - найденная константа, или null если нет константы с таким именем
     * @throws TsNullArgumentRtException аргумент = null
     */
    public static EOperatorType findByNameOrNull( String aName ) {
      TsNullArgumentRtException.checkNull( aName );
      for( EOperatorType item : values() ) {
        if( item.nmName.equals( aName ) ) {
          return item;
        }
      }
      return null;
    }

    /**
     * Возвращает константу по имени или выбрасывает исключение.
     *
     * @param aName String - имя искомой константы
     * @return ESignalCmd - найденная константа
     * @throws TsNullArgumentRtException аргумент = null
     * @throws TsItemNotFoundRtException нет константы с таким именем
     */
    public static EOperatorType findByName( String aName ) {
      return TsItemNotFoundRtException.checkNull( findByNameOrNull( aName ) );
    }

  }
}
