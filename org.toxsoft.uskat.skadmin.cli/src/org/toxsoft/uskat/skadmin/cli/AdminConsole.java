package org.toxsoft.uskat.skadmin.cli;

import static java.lang.String.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.bricks.wub.IWubConstants.*;
import static org.toxsoft.core.tslib.utils.plugins.IPluginsHardConstants.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.cli.AdminColors.*;
import static org.toxsoft.uskat.skadmin.cli.AdminConsoleUtils.*;
import static org.toxsoft.uskat.skadmin.cli.IAdminAnsiConstants.*;
import static org.toxsoft.uskat.skadmin.cli.IAdminResources.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.core.impl.AdminCmdLibraryUtils.*;
import static scala.tools.jline.console.ConsoleReader.*;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.jar.*;

import org.fusesource.jansi.*;
import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.wub.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.plugins.impl.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.legacy.plexy.*;
import org.toxsoft.uskat.s5.common.*;
import org.toxsoft.uskat.s5.common.sessions.*;
import org.toxsoft.uskat.s5.server.*;
import org.toxsoft.uskat.skadmin.cli.cmds.*;
import org.toxsoft.uskat.skadmin.cli.completion.*;
import org.toxsoft.uskat.skadmin.cli.parsers.*;
import org.toxsoft.uskat.skadmin.core.*;
import org.toxsoft.uskat.skadmin.core.impl.*;
import org.toxsoft.uskat.skadmin.core.plugins.*;

import scala.tools.jline.console.history.*;

/**
 * Консоль
 * <p>
 * TODO:
 * <ol>
 * <li>Автодополнение аргументов, значений;</li>
 * <li>Перемещение курсора ctrl-left, ctrl-up;</li>
 * <li>Выделение символов в консоли (для копирования в буфер);</li>
 * <li>Вставка из буфера по комбинации shift-ins, ctrl-v;</li>
 * </ol>
 *
 * @author mvk
 */
class AdminConsole
    extends AbstractAdminCmdLibrary
    implements IAdminConsole {

  private final TsVersion version = new TsVersion( IS5ServerHardConstants.version.verMajor(),
      IS5ServerHardConstants.version.verMinor(), getBuildTime() );

  private static final String WORK_DIR             = "user.dir";    //$NON-NLS-1$
  private static final String S5ADMIN_CFG_FILENAME = "s5admin.cfg"; //$NON-NLS-1$

  /**
   * Конфигурационные параметры окружения
   */
  private static final String S5ADMIN_ENV_SYNTAX_HIGHLIGHTING = "org.toxsoft.uskat.skadmin.cli.syntax_highlighting"; //$NON-NLS-1$

  /**
   * Таймаут (мсек) опроса консолью командной строки
   */
  private static final long READ_CONSOLE_TIMEOUT = 100;

  private final TsThreadExecutor     threadExecutor;
  private final IWubBox              rootBox;
  private final IAdminCmdLibrary     library;
  private final AdminConsoleTeminal  terminal;
  private final AdminCmdSyntaxParser syntaxParser;
  private final AdminCmdSyntaxParser executeParser;
  private final AdminCmdCompleter    completer;
  private String                     sectionId = TsLibUtils.EMPTY_STRING;
  private boolean                    shutdown;
  private ISkConnection              skConnection;
  private S5Host                     skHost;
  private volatile boolean           executing = false;

  private ConsoleSkConnectionListener consoleSkConnectionListener = new ConsoleSkConnectionListener();

  private static final ILogger logger = LoggerWrapper.getLogger( AdminConsole.class );

  /**
   * Конструктор
   *
   * @param aCmdLine String командная строка (из аргументов среды запуска s5admin-cli)
   * @throws TsNullArgumentRtException аргумент = null
   */
  AdminConsole( String aCmdLine ) {
    TsNullArgumentRtException.checkNull( aCmdLine );
    // Инициализация команд консоли
    logger.debug( MSG_CONSOLE_CMD_INIT );
    init();

    // Переменные окружения
    String applicationDir = readEnvDirName( CTX_APPLICATION_DIR.id() );
    IStringList pluginPaths = readEnv( CTX_PLUGIN_PATHS.id(), new StringArrayList() );

    logger.debug( CTX_APPLICATION_DIR.id() + IStrioHardConstants.CHAR_EQUAL + applicationDir );
    logger.debug( CTX_PLUGIN_PATHS.id() + IStrioHardConstants.CHAR_EQUAL );
    for( String pluginPath : pluginPaths ) {
      logger.debug( "   " + pluginPath ); //$NON-NLS-1$
    }

    // Создание, инициализация, загрузка контейнера плагинов
    IOptionSetEdit params = new OptionSet();
    OPDEF_UNIT_STOPPING_TIMEOUT_MSECS.setValue( params, avInt( 10000 ) );
    // Контекст для инициализации контейнера скатлетов
    TsContext environ = new TsContext();
    PLUGIN_TYPE_ID.setValue( environ.params(), avStr( IAdminCmdLibraryPlugin.CMD_LIBRARY_PLUGIN_TYPE ) );
    PLUGINS_DIR.setValue( environ.params(), avValobj( pluginPaths ) );
    TMP_DIR.setValue( environ.params(), avStr( "temp" ) ); //$NON-NLS-1$
    CLEAN_TMP_DIR.setValue( environ.params(), avBool( true ) );
    // Создание контейнера плагинов
    PluginBox<PluginUnit> pluginBox = new PluginBox<>( "pluginBox", params, logger ); //$NON-NLS-1$
    // Создание корневого контейнера...
    rootBox = new WubBox( "rootBox", params ); //$NON-NLS-1$
    // Добавление в корневой контейнер контейнера плагинов
    rootBox.addUnit( pluginBox );
    // ...инициализация...
    rootBox.init( environ );
    // ...запуск
    rootBox.start();

    // Инициализация библиотеки команд
    IListEdit<IAdminCmdLibrary> libraries = new ElemArrayList<>();
    libraries.addAll( loadPluginLibraries( pluginBox.listPlugins() ) );
    libraries.add( this );
    library = createAdminLibrary( libraries );
    @SuppressWarnings( "unused" )
    RuntimeException readContextError = null;
    try {
      // Попытка загрузить контекст из файла. false: без формирования ошибки существования файла
      readContextFromFile( CONTEXT_FILENAME, library.context(), false );
    }
    catch( RuntimeException e ) {
      // Ошибка чтения файла параметров контекста
      readContextError = e;
    }
    // Исполнитель запросов в одном потоке
    threadExecutor =
        new TsThreadExecutor( AdminConsole.class.getSimpleName(), LoggerWrapper.getLogger( TsThreadExecutor.class ) );
    // Контекст выполнения команд
    IAdminCmdContext context = library.context();
    // Формирование параметров контеста связанных с окружением запуска
    context.setParamValue( CTX_APPLICATION_DIR, pvSingleValue( avStr( applicationDir ) ), true );
    context.setParamValue( CTX_PLUGIN_PATHS, pvSingleValue( avValobj( pluginPaths ) ), true );
    context.setParamValue( CTX_THREAD_EXECUTOR, pvSingleRef( threadExecutor ), true );
    // Слушаем контекст выполнения команд
    context.addContextListener( new ContextListener() );
    // Синтаксический анализатор для подсветки синтаксиса
    syntaxParser = new AdminCmdSyntaxParser( library );
    // Синтаксический анализатор для выполнения команд
    executeParser = new AdminCmdSyntaxParser( library );
    // Инициализация терминала
    AnsiConsole.systemInstall();
    terminal = createTerminal( context, syntaxParser, readEnv( S5ADMIN_ENV_SYNTAX_HIGHLIGHTING, AV_TRUE ).asBool() );
    // Автодополнение
    completer = new AdminCmdCompleter( library, sectionId );
    terminal.addCompleter( completer );
    terminal.setCompletionHandler( new AdminCmdCompletionHandler() );
    // "Звонки" не нужны - бьют по ушам )))
    terminal.setBellEnabled( false );
    // Логотип
    System.out.println( format( MSG_LOGO1, version ) );
    // if( getWidth() < RECOMMEND_SCREEN_BUFFER_WIDHT ) {
    // System.out.print( format( MSG_RCM_WIDTH, Integer.valueOf( RECOMMEND_SCREEN_BUFFER_WIDHT ) ) );
    // }
    System.out.println( MSG_LOGO2 );

    // TODO: ???
    // if( readContextError != null ) {
    // // Отображаем ошибку загрузки контекста
    // System.out.println( format( ERR_READ_CONTEXT, CONTEXT_FILENAME, readContextError.getLocalizedMessage() ) );
    // logger.error( readContextError );
    // }
    // Инициализация команд консоли
    logger.debug( MSG_CONSOLE_INIT_FINISH );
    // Пробуем запустить команду запуска консоли без возможности "довода" значений аргументов
    if( execute( aCmdLine, false ) != AdminCmdResult.EMPTY ) {
      return;
    }
    // Приглашение к вводу
    updatePrompt();
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Главный цикл приложения
   */
  public void run() {
    while( !shutdown ) {
      rootBox.doJob();
      if( executing ) {
        // В данный момент выполняется команда
        try {
          Thread.sleep( READ_CONSOLE_TIMEOUT );
          continue;
        }
        catch( InterruptedException e ) {
          logger.error( e );
        }
      }
      try {
        String line = terminal.readLine();
        if( line == null || line.trim().length() == 0 ) {
          continue;
        }
        // Сохранение истории команд в файле
        ((PersistentHistory)terminal.getHistory()).flush();
        // Выполнение командной строки с возможностью "довода" значений аргументов
        threadExecutor.syncExec( () -> {
          try {
            execute( line, true );
          }
          catch( Throwable e ) {
            logger.error( e );
          }
        } );
      }
      catch( IOException e ) {
        throw new TsInternalErrorRtException( e, e.getLocalizedMessage() );
      }
      catch( Throwable e ) {
        logger.error( e );
      }
    }
    library.close();
    // Завершаем работу конслои
    terminal.close();
    AnsiConsole.systemUninstall();
  }

  // ------------------------------------------------------------------------------------
  // Реализация IAdminCmdLibrary и шаблонных методов AbstractAdminCmdLibrary
  //
  @Override
  public String getName() {
    return getClass().getName();
  }

  @Override
  protected void doInit() {
    // Определение команд консоли
    addCmd( new ConsoleCmdHelp( this ) );
    addCmd( new ConsoleCmdClear( this ) );
    addCmd( new ConsoleCmdCd( this ) );
    addCmd( new ConsoleCmdLs( this ) );
    addCmd( new ConsoleCmdBatch( this ) );
    addCmd( new ConsoleCmdHasParam( this ) );
    addCmd( new ConsoleCmdSignal( this ) );
    addCmd( new ConsoleCmdTimeout( this ) );
    addCmd( new ConsoleCmdEcho( this ) );
    addCmd( new ConsoleCmdIsEquals( this ) );
    addCmd( new ConsoleCmdTimeToString( this ) );
    addCmd( new ConsoleCmdExit( this ) );
    addCmd( new ConsoleCmdQuit( this ) );
  }

  @Override
  protected void doClose() {
    // Завершение работы исполнителя запросов в одном потоке
    threadExecutor.close();
  }

  // ------------------------------------------------------------------------------------
  // Реализация IAdminConsole
  //
  @Override
  public IAdminCmdContext context() {
    return library.context();
  }

  @Override
  public IAdminCmdResult execute( String aLine, boolean aUser ) {
    // Установка признака того, что проводится выполнение команды
    executing = true;
    try {
      TsNullArgumentRtException.checkNull( aLine );
      if( aLine.trim().equals( TsLibUtils.EMPTY_STRING ) ) {
        // Пустая строка. Нет команды. Выход
        return AdminCmdResult.EMPTY;
      }
      // Анализ введенной командной строки
      executeParser.parse( aLine );
      // Анализ ошибок определения команды и ее аргументов
      IList<ValidationResult> errors = executeParser.getErrors();
      if( errors.size() > 0 ) {
        // Команда была введена с ошибками
        for( ValidationResult error : errors ) {
          System.out.println( COLOR_ERROR + error.message() + COLOR_RESET );
          logger.debug( error.message() );
        }
        return AdminCmdResult.ERROR;
      }
      // Используемые входные параметры контекста
      IStringList contextInputs = executeParser.contextInputNames();
      // Выходные параметры контекста для сохранения результата
      IStringList contextOutputs = executeParser.contextOutputNames();
      // Команда для выполнения
      String cmdId = executeParser.getCmdId();
      try {
        if( cmdId.equals( TsLibUtils.EMPTY_STRING ) ) {
          // Команда не была определена. Возможно это операции с контекстом
          IAdminCmdContext context = context();
          if( contextOutputs.size() > 0 && contextInputs.size() > 1 ) {
            // Входной параметр контекста при указании выходных параметров может быть только один
            throw new TsIllegalArgumentRtException( MSG_ERR_CTX_MULTY_INPUTS );
          }
          // Признак необходимости вывода значений параметров на консоль
          boolean print = (contextOutputs.size() == 0 && contextInputs.size() > 0);
          // Признак необходимости провести копирование значений параметров
          boolean assign = (contextOutputs.size() > 0);
          if( print ) {
            // Вывод значений на экран
            for( String name : contextInputs ) {
              IPlexyValue value = context.paramValue( name );
              String valueString = value.toString() + " [" + value.type() + "]"; //$NON-NLS-1$//$NON-NLS-2$
              System.out.println( getTypeNameColor( value.type() ) + valueString + COLOR_RESET );
            }
          }
          if( assign ) {
            // Копирование значений контекста, например: $a, $b = $c или $a, $b = rvalue
            IPlexyValue value =
                (contextInputs.size() > 0 ? context.paramValue( contextInputs.get( contextInputs.size() - 1 ) )
                    : executeParser.rvalue());
            for( String name : contextOutputs ) {
              context.setParamValue( name, value );
            }
          }
          return AdminCmdResult.EMPTY;
        }
        IAdminCmdDef cmdDef = library.findCommand( cmdId );
        TsInternalErrorRtException.checkNull( cmdDef );
        // Значения аргументов для выполнения
        IStringMapEdit<IPlexyValue> argValues = new StringMap<>( executeParser.getArgValues() );
        try {
          // Обработка значений передаваемых через контекст
          appendContextValues( cmdDef.argumentDefs(), argValues, contextInputs, context() );
          // Дополнение значениями аргументов неведенных пользователем, но необходимых для выполнения
          appendArgValues( cmdDef, argValues, aUser );
        }
        catch( TsIllegalArgumentRtException e ) {
          // Ошибка дополнения недостающих аргументов
          logger.error( e );
          System.out.println( format( MSG_ERR, e.getLocalizedMessage() ) );
          logger.debug( e.getLocalizedMessage() );
          return AdminCmdResult.EMPTY;
        }
        // Запуск команды. Пока обеспечиваем выполнение команды только синхронном режиме
        // (без возможности остановки выполнения)
        IAdminCmdResult result = library.exec( cmdId, argValues, new AdminCmdCallback( this ) );
        // Обработка результата
        if( result.isOk() && cmdDef.resultType() != IPlexyType.NONE ) {
          // Успешное выполнение. Сохранение результата в контексте
          IPlexyValue resultValue = result.result();
          for( String contextParamName : contextOutputs ) {
            context().setParamValue( contextParamName, resultValue );
          }
        }
        // Печать результатов выполнения для внешних команд
        printCmdResults( cmdDef, result );
        // Возвращение результата
        return result;
      }
      catch( RuntimeException e ) {
        logger.error( e );
        if( cmdId.equals( TsLibUtils.EMPTY_STRING ) ) {
          System.out.println( format( MSG_ERR, e.getLocalizedMessage() ) );
          logger.debug( e.getLocalizedMessage() );
        }
        else {
          String errText = format( MSG_ERR_CMD_UNEXPECTED, cmdId, e.getLocalizedMessage() );
          System.out.println( errText );
          logger.debug( stripAnsi( errText ) );
        }
        return AdminCmdResult.ERROR;
      }
    }
    finally {
      // Выполнение команды завершено
      executing = false;
    }
  }

  @Override
  public int getWidth() {
    return terminal.getTerminal().getWidth();
  }

  @Override
  public int getHeight() {
    return terminal.getTerminal().getHeight();
  }

  @Override
  public void clearScreen() {
    try {
      terminal.clearScreen();
    }
    catch( IOException e ) {
      throw new TsInternalErrorRtException( e, e.getLocalizedMessage() );
    }
  }

  @Override
  public void print( int aIndent, boolean aSpaceEol, String aText, Object... aParams ) {
    TsNullArgumentRtException.checkNulls( aText, aParams );
    // Форматирование текста
    String text = format( aText, aParams );
    // Количество столбцов в терминале
    int width = getWidth();
    // Текущая позиция вывода
    int position = aIndent;
    // Признак текущей ansi-последовательности
    boolean escaping = false;
    // Признак того, что проходят пробелы
    boolean spacing = true;
    // Признак пропуска пробелов после перехода строки
    boolean passSpaceAfterEol = false;
    // Построитель текста
    StringBuilder sb = new StringBuilder();
    for( int index = 0, n = text.length(); index < n; index++ ) {
      // Текущий символ
      char c = text.charAt( index );
      // Анализ escape последовательности
      if( !escaping && c == CHAR_ANSI_START ) {
        // Начало escape последовательности
        escaping = true;
        sb.append( c );
        continue;
      }
      if( escaping && c == CHAR_ANSI_FINISH ) {
        // Завершение escape последовательности
        escaping = false;
        sb.append( c );
        continue;
      }
      if( escaping ) {
        // Продолжение формирования escape последовательности
        sb.append( c );
        continue;
      }
      if( aSpaceEol && spacing && c != IStrioHardConstants.CHAR_SPACE ) {
        // После пробелов получили первый печатный символ. Анализируем строки между пробелами. Если она выходит за
        // границы терминала, то делаем упреждающий переход строки
        int endIndex = text.indexOf( IStrioHardConstants.CHAR_SPACE, index );
        if( endIndex < 0 ) {
          // Символ пробела не найден. Считаем его концом строки
          endIndex = text.length();
        }
        // Длина печатной строки. +1: обеспечиваем видимость пробела между границей и концом текста
        int length = stripAnsi( text.substring( index, endIndex ) ).length() + 1;
        if( position + length >= width ) {
          // Строка не входит в терминал. Осуществляем переход и отступ
          sb.append( IStrioHardConstants.CHAR_EOL );
          appendSpaces( sb, aIndent );
          passSpaceAfterEol = true;
          position = aIndent;
        }
      }
      spacing = (c == IStrioHardConstants.CHAR_SPACE);
      if( !spacing ) {
        passSpaceAfterEol = false;
      }
      if( !passSpaceAfterEol || !spacing ) {
        position++;
      }
      if( position > width || c == IStrioHardConstants.CHAR_EOL ) {
        // Найден переход строки. Осуществляем переход и отступ
        sb.append( IStrioHardConstants.CHAR_EOL );
        if( index < n - 1 ) {
          appendSpaces( sb, aIndent );
        }
        passSpaceAfterEol = true;
        position = aIndent;
        if( c == IStrioHardConstants.CHAR_EOL ) {
          continue;
        }
      }
      if( !passSpaceAfterEol || !spacing ) {
        sb.append( c );
      }
    }
    String printText = sb.toString();
    System.out.print( printText );
    logger.debug( stripAnsi( printText ) );
  }

  @Override
  public void updatePrompt() {
    // Обновление приглашения
    StringBuilder sbPrompt = new StringBuilder();
    sbPrompt.append( MSG_PROMPT_START );
    if( skConnection != null && skConnection.state() == ESkConnState.ACTIVE ) {
      // Информация о соединении с сервером
      sbPrompt.append( COLOR_SINGLE_VALUE );
      try {
        IS5SessionInfo sessionInfo =
            IS5ServerHardConstants.OP_BACKEND_SESSION_INFO.getValue( skConnection.backendInfo().params() ).asValobj();
        sbPrompt.append( sessionInfo.login() );
      }
      catch( @SuppressWarnings( "unused" ) RuntimeException e ) {
        sbPrompt.append( "???" ); //$NON-NLS-1$
      }
      sbPrompt.append( COLOR_RESET );
      sbPrompt.append( CHAR_AT );
      sbPrompt.append( COLOR_SINGLE_VALUE );
      sbPrompt.append( skHost != null ? skHost.address() : "???" ); //$NON-NLS-1$
      sbPrompt.append( COLOR_RESET );
      sbPrompt.append( CHAR_COLON );
      sbPrompt.append( COLOR_SINGLE_VALUE );
      sbPrompt.append( skHost != null ? Integer.toString( skHost.port() ) : "???" ); //$NON-NLS-1$
      sbPrompt.append( COLOR_RESET );
    }
    // Информация о текущем разделе
    sbPrompt.append( CHAR_SLASH );
    // Признак корневого разделаА
    boolean isRoot = (sectionId.equals( TsLibUtils.EMPTY_STRING ));
    IStringList idNames = (isRoot ? IStringList.EMPTY : StridUtils.getComponents( sectionId ));
    for( int index = 0, n = idNames.size(); index < n; index++ ) {
      sbPrompt.append( COLOR_ID );
      sbPrompt.append( idNames.get( index ) );
      sbPrompt.append( COLOR_RESET );
      if( index < n - 1 ) {
        sbPrompt.append( CHAR_SLASH );
      }
    }
    sbPrompt.append( MSG_PROMPT_FINISH );
    terminal.setPrompt( sbPrompt.toString() );
    try {
      terminal.flush();
    }
    catch( IOException ex ) {
      logger.error( ex );
    }
  }

  @Override
  public boolean isValidSectionId( String aSectionId ) {
    TsNullArgumentRtException.checkNull( aSectionId );
    // Признак корневого раздела
    boolean isRoot = (aSectionId.equals( TsLibUtils.EMPTY_STRING ));
    if( isRoot ) {
      // Корневой раздел всегда существует
      return true;
    }
    StridUtils.checkValidIdPath( aSectionId );
    // Список команд библиотеки
    IList<IAdminCmdDef> cmdDefs = library.availableCmds();
    // Проходим по всем командам и определяем если хоть одна команда имеющая ИД-путь-префикс aSectionId
    for( IAdminCmdDef cmdDef : cmdDefs ) {
      if( StridUtils.startsWithIdPath( cmdDef.id(), aSectionId ) ) {
        // Путь команды полностью совпал с разделом. Вывод: раздел существует
        return true;
      }
    }
    return false;
  }

  @Override
  public String getSectionId() {
    return sectionId;
  }

  @Override
  public boolean setSectionId( String aSectionId ) {
    TsItemNotFoundRtException.checkFalse( isValidSectionId( aSectionId ) );
    if( aSectionId.equals( sectionId ) ) {
      // Раздел уже текущий
      return false;
    }
    sectionId = aSectionId;
    syntaxParser.setSectionId( aSectionId );
    executeParser.setSectionId( aSectionId );
    completer.setSectionId( aSectionId );
    updatePrompt();
    return true;
  }

  @Override
  public boolean queryConfirm( String aMessage, boolean aDefault ) {
    TsNullArgumentRtException.checkNulls( aMessage );
    // Создание типа значения
    IOptionSetEdit constraints = new OptionSet();
    constraints.setStr( IAvMetaConstants.DDEF_DEFAULT_VALUE, (aDefault ? CHAR_YES : CHAR_NO) );
    // Создание возможных значений
    IPlexyValue cmdValueYes = pvSingleValue( avStr( CHAR_YES ) );
    IPlexyValue cmdValueNo = pvSingleValue( avStr( CHAR_NO ) );
    IList<IPlexyValue> possibles = new ElemArrayList<>( cmdValueYes, cmdValueNo );
    // Запрос значения у клиента
    IDataType dataType = new DataType( EAtomicType.STRING, constraints );
    IPlexyType cmdValueType = ptSingleValue( dataType );
    IPlexyValue confirm = readValue( cmdValueType, aMessage, possibles, aDefault ? CHAR_YES : CHAR_NO, false );
    if( confirm == IPlexyValue.NULL ) {
      // Клиент отказался предоставить значение
      return aDefault;
    }
    return CHAR_YES.equals( confirm.singleValue().asString() );
  }

  @Override
  public IPlexyValue readValue( IPlexyType aType, String aName, IList<IPlexyValue> aPossibleValues,
      String aDefaultValue, boolean aRetryForErrors ) {
    // Сохраняем парсер терминала и приглашение
    IAdminCmdSyntaxParser oldParser = terminal.getSyntaxParserOrNull();
    String oldPrompt = terminal.getPrompt();
    try {
      while( true ) {
        // Разделяем введеную строку на две части: с переходами строки и без перехода. Первая просто выводится
        // как есть, вторая часть определяется как приглашение к вводу. При этом, требуется повторить
        // ansi-последовательности во второй части которые были найденные в первой
        int lastEolIndex = aName.lastIndexOf( IStrioHardConstants.CHAR_EOL );
        String message = TsLibUtils.EMPTY_STRING;
        String prompt = aName;
        if( lastEolIndex >= 0 ) {
          message = aName.substring( 0, lastEolIndex );
          prompt = terminal.findAnsi( message ) + aName.substring( lastEolIndex + 1 );
        }
        if( !message.equals( TsLibUtils.EMPTY_STRING ) ) {
          System.out.println( message );
        }
        IAdminCmdContext context = library.context();
        // Парсер значения
        AdminCmdValueSyntaxParser parser = new AdminCmdValueSyntaxParser( context, aType, prompt, aPossibleValues );
        // TODO: по простому: если prompt = password, то скрываем значение. Если потребуется, изменить логику
        Character mask = (prompt.contains( PASSWORD_ARG_ID ) ? CHAR_MASK : null);
        // Замена парсера терминала. Для маскируемых значений подсветка синтаксиса запрещена
        terminal.setSyntaxParser( parser, mask == null );
        // Формирование приглашения
        StringBuilder sbPrompt = new StringBuilder( prompt );
        if( aPossibleValues.size() > 0 ) {
          sbPrompt.append( IStrioHardConstants.CHAR_ARRAY_BEGIN );
        }
        for( int index = 0, n = aPossibleValues.size(); index < n; index++ ) {
          IPlexyValue possible = aPossibleValues.get( index );
          switch( possible.type().kind() ) {
            case SINGLE_VALUE:
              sbPrompt.append( COLOR_SINGLE_VALUE );
              sbPrompt.append( possible.singleValue() );
              break;
            case VALUE_LIST:
              sbPrompt.append( COLOR_VALUE_LIST );
              sbPrompt.append( possible.valueList() );
              break;
            case OPSET:
              sbPrompt.append( COLOR_VALUE_LIST );
              sbPrompt.append( possible.getOpset() );
              break;
            case SINGLE_REF:
              sbPrompt.append( COLOR_SINGLE_REF );
              sbPrompt.append( possible.singleRef() );
              break;
            case REF_LIST:
              sbPrompt.append( COLOR_REF_LIST );
              sbPrompt.append( possible.refList() );
              break;
            default:
              throw new TsNotAllEnumsUsedRtException();
          }
          sbPrompt.append( COLOR_RESET );
          if( index < n - 1 ) {
            sbPrompt.append( CHAR_SLASH );
          }
        }
        if( aPossibleValues.size() > 0 ) {
          sbPrompt.append( IStrioHardConstants.CHAR_ARRAY_END );
          sbPrompt.append( CHAR_QUESTION );
        }
        sbPrompt.append( CHAR_COLON );
        sbPrompt.append( IStrioHardConstants.CHAR_SPACE );
        terminal.setPrompt( sbPrompt.toString() );
        // Признак того, что есть значение по умолчанию
        boolean hasDefaultValue = !aDefaultValue.equals( TsLibUtils.EMPTY_STRING );
        // Чтение строки значения из терминала
        String line = terminal.readLine( null, mask, (hasDefaultValue ? aDefaultValue : null) );
        if( line == null ) {
          // Пользователь отказался повторить ввод значения (ввод пустого значения/строки)
          return IPlexyValue.NULL;
        }
        // Анализ ошибок определения значения
        boolean hasValue = parser.hasValue();
        IList<ValidationResult> errors = parser.getErrors();
        if( hasValue && errors.size() == 0 ) {
          return parser.getValue();
        }
        // Значение было определено с ошибками
        if( !aRetryForErrors ) {
          // Запрещено повторять ввод
          return IPlexyValue.NULL;
        }
        for( ValidationResult error : errors ) {
          System.out.println( COLOR_ERROR + stripAnsi( error.message() ) + COLOR_RESET );
        }
        // Ошибка чтения значения
        boolean retry = queryConfirm( MSG_RETRY_ENTRY, true );
        if( !retry ) {
          // // Пользователь отказался повторить ввод значения
          return IPlexyValue.NULL;
        }
      }
    }
    catch( IOException e ) {
      // Неожиданная ошибка терминала
      throw new TsInternalErrorRtException( e, e.getLocalizedMessage() );
    }
    finally {
      // Восстанавливаем парсер терминала и приглашение терминала
      terminal.setSyntaxParser( oldParser, true );
      terminal.setPrompt( oldPrompt );
    }
  }

  @Override
  public IAdminCmdDef findCmdDef( String aCmdId ) {
    return library.findCommand( aCmdId );
  }

  @Override
  public IList<IAdminCmdDef> listCmdDefs() {
    return library.availableCmds();
  }

  @Override
  public void changeContext( IAdminCmdContext aContext ) {
    TsNullArgumentRtException.checkNull( aContext );
    library.setContext( aContext );
  }

  @Override
  public void close() {
    shutdown = true;
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  //
  /**
   * Создает терминал консоли
   *
   * @param aContext {@link IAdminCmdContext} контекст
   * @param aSyntaxParser {@link IAdminCmdSyntaxParser} - синтаксический анализатор командной строки
   * @param aSyntaxHighlighting boolean <b>true</b> c подсветкой синтаксиса; <b>false</b> без подсветки синтаксиса.
   * @return {@link AdminConsoleTeminal} - терминал консоли
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static AdminConsoleTeminal createTerminal( IAdminCmdContext aContext, IAdminCmdSyntaxParser aSyntaxParser,
      boolean aSyntaxHighlighting ) {
    TsNullArgumentRtException.checkNulls( aContext, aSyntaxParser );
    try {
      AdminConsoleTeminal retValue = new AdminConsoleTeminal( aContext, aSyntaxParser, aSyntaxHighlighting );
      // История команд
      FileHistory history = new FileHistory( new File( CONSOLE_HISTORY_FILE ) );
      history.setMaxSize( CONSOLE_HISTORY_SIZE );
      retValue.setHistory( history );
      return retValue;
    }
    catch( IOException e ) {
      throw new TsInternalErrorRtException( e, e.getLocalizedMessage() );
    }
  }

  /**
   * Проверяет наличие и если необходимо запрос у пользователя значений всех аргументов не имеющих значений по умолчанию
   *
   * @param aCmdDef {@link IAdminCmdDef} - описание команды
   * @param aArgValues {@link IStringMapEdit}&lt;{@link IPlexyValue}&gt; - редактируемая карта значений аргументов.
   *          Ключ: идентификатор аргумента.
   * @param aUser boolean <b>true</b> недостающие значения аргументов могут быть запрошены у пользователя; <b>false</b>
   *          если невозможно определить значение аргумента, то выдавать ошибку
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException пользователь отказался от ввода значений недостающих аргументов
   * @throws TsIllegalArgumentRtException недостаточно аргументов для выполнения команды
   */
  private void appendArgValues( IAdminCmdDef aCmdDef, IStringMapEdit<IPlexyValue> aArgValues, boolean aUser ) {
    TsNullArgumentRtException.checkNulls( aCmdDef, aArgValues );
    String cmdId = aCmdDef.id();
    for( IAdminCmdArgDef argDef : aCmdDef.argumentDefs() ) {
      String argId = argDef.id();
      IPlexyType argType = argDef.type();
      if( aArgValues.findByKey( argId ) != null ) {
        // Аргумент уже имеет значение
        continue;
      }
      IPlexyValue argValue = null;
      // Признак того, что аргумент имеет значение по умолчанию, при этом если значение является списком или набором,
      // то считается, что оно всегда имеет значение по умолчанию: пустой список или набор
      boolean hasDefaultValue = false;
      hasDefaultValue = switch( argType.kind() ) {
        case SINGLE_VALUE -> argDef.type().dataType().params().hasValue( IAvMetaConstants.DDEF_DEFAULT_VALUE );
        case VALUE_LIST, REF_LIST, OPSET -> true;
        case SINGLE_REF -> true;
        default -> throw new TsNotAllEnumsUsedRtException();
      };
      if( hasDefaultValue ) {
        continue;
      }
      // Список возможных значений
      if( aUser ) {
        // Попытка запроса значения недостающего аргумента у пользователя
        IList<IPlexyValue> possibleValues = library.getPossibleValues( cmdId, argId, aArgValues );
        String argName = COLOR_ID + argId + COLOR_RESET;
        argValue = readValue( argDef.type(), argName, possibleValues, TsLibUtils.EMPTY_STRING, true );
        if( argValue == IPlexyValue.NULL ) {
          // Пользователь отказался от ввода значения
          throw new TsIllegalArgumentRtException( MSG_ERR_CMD_REJECT, aCmdDef.id() );
        }
      }
      if( argValue == null ) {
        throw new TsIllegalArgumentRtException( MSG_ERR_ARG_NOT_FOUND, aCmdDef.id(), argId );
      }
      // Добавляем значение аргумента в карту аргументов
      aArgValues.put( argId, argValue );
    }
  }

  /**
   * Печатает результаты выполнения команды
   *
   * @param aCmdDef {@link IAdminCmdDef} - описание команды
   * @param aResults {@link IAdminCmdResult} - результат выполнения команды
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static void printCmdResults( IAdminCmdDef aCmdDef, IAdminCmdResult aResults ) {
    TsNullArgumentRtException.checkNulls( aCmdDef, aResults );
    IList<ValidationResult> validations = aResults.validations();
    if( validations.size() == 0 || //
        aCmdDef.id().equals( "echo" ) || //$NON-NLS-1$
        aCmdDef.id().equals( "batch" ) || //$NON-NLS-1$
        aCmdDef.id().equals( "signal" ) //$NON-NLS-1$
    ) {
      // Команда не имеет сообщений формирования результата или это специальные команды для которых не отображается
      // результат
      return;
    }
    boolean ok = aResults.isOk();
    Object result = (ok ? aResults.result() : null);
    // Формирование строк сообщения
    StringBuilder sbMessages = new StringBuilder();
    for( int index = 0, n = validations.size(); index < n; index++ ) {
      ValidationResult validation = validations.get( index );
      sbMessages.append( validation.isOk() ? COLOR_INFO : (validation.isWarning() ? COLOR_WARN : COLOR_ERROR) );
      sbMessages.append( validation.message() );
      sbMessages.append( COLOR_RESET );
    }
    StringBuilder sbResult = new StringBuilder();
    if( aCmdDef.resultType() != IPlexyType.NONE && result != null ) {
      // Идентификатор команды
      sbResult.append( format( MSG_CMD_FINISH_ID, aCmdDef.id() ) );
      // Результат выполнения
      sbResult.append( format( MSG_CMD_FINISH_RESULT, (ok ? COLOR_INFO : COLOR_ERROR), result ) );
    }
    // Строки сообщений
    if( validations.size() > 0 ) {
      sbResult.append( sbMessages );
    }
    sbResult.append( COLOR_RESET );
    System.out.println( sbResult );
    logger.debug( stripAnsi( sbResult.toString() ) );
  }

  /**
   * Добавление пробелов в строку
   *
   * @param aStringBuilder String - построитель строки
   * @param aQtty int - количество пробелов
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static void appendSpaces( StringBuilder aStringBuilder, int aQtty ) {
    TsNullArgumentRtException.checkNull( aStringBuilder );
    // Добавление пробелов
    for( int space = 0; space < aQtty; space++ ) {
      aStringBuilder.append( IStrioHardConstants.CHAR_SPACE );
    }
  }

  /**
   * Читает значение переменной окружения
   *
   * @param aId String идентификатор переменной окружения (ИД-путь)
   * @return {@link IAtomicValue} значение переменной окружения. {@link IAtomicValue#NULL} переменная не найдена
   * @throws TsNullArgumentRtException аргумент = null;
   */
  private static IAtomicValue readEnv( String aId ) {
    return readEnv( aId, IAtomicValue.NULL );
  }

  /**
   * Читает значение переменной окружения
   *
   * @param aId String идентификатор переменной окружения (ИД-путь)
   * @param aDefaultValue {@link IAtomicValue} значение по умолчанию
   * @return {@link IAtomicValue} значение переменной окружения. {@link IAtomicValue#NULL} переменная не найдена
   * @throws TsNullArgumentRtException любой аргумент = null;
   */
  private static IAtomicValue readEnv( String aId, IAtomicValue aDefaultValue ) {
    TsNullArgumentRtException.checkNulls( aId, aDefaultValue );
    StridUtils.checkValidIdPath( aId );
    // Переменная может быть указана через аргументы jvm (-Dxxx)
    String value = System.getProperty( aId );
    if( value != null ) {
      return AtomicValueKeeper.KEEPER.str2ent( value );
    }
    // Каталог запуска
    String workDir = System.getProperty( WORK_DIR );
    // Параметры загруженные из файла конфигурации. null: файл не существует
    File file = new File( workDir + File.separator + S5ADMIN_CFG_FILENAME );
    IOptionSet options = (file.exists() ? OptionSetKeeper.KEEPER.read( file ) : new OptionSet());
    if( options.hasValue( aId ) ) {
      // Переменная указана через файл конфигурации
      return options.getValue( aId );
    }
    // Переменная не найдена
    return aDefaultValue;
  }

  /**
   * Читает значение переменной окружения возвращаея результат как список строк
   * <p>
   * Метод ожидает, что исходная строка содержит символы ':' или ';'.
   *
   * @param aId String идентификатор переменной окружения (ИД-путь)
   * @param aDefaultValue {@link IAtomicValue} значение по умолчанию
   * @return {@link IAtomicValue} значение переменной окружения. {@link IAtomicValue#NULL} переменная не найдена
   * @throws TsNullArgumentRtException любой аргумент = null;
   */
  private static IStringList readEnv( String aId, IStringList aDefaultValue ) {
    TsNullArgumentRtException.checkNulls( aId, aDefaultValue );
    StridUtils.checkValidIdPath( aId );
    // Переменная может быть указана через аргументы jvm (-Dxxx)
    String value = System.getProperty( aId );
    if( value != null ) {
      return readStringList( value );
    }
    // Каталог запуска
    String workDir = System.getProperty( WORK_DIR );
    // Параметры загруженные из файла конфигурации. null: файл не существует
    File file = new File( workDir + File.separator + S5ADMIN_CFG_FILENAME );
    IOptionSet options = (file.exists() ? OptionSetKeeper.KEEPER.read( file ) : new OptionSet());
    if( options.hasValue( aId ) ) {
      // Переменная указана через файл конфигурации
      return readStringList( options.getValue( aId ).asString() );
    }
    // Переменная не найдена
    return aDefaultValue;
  }

  /**
   * Читает имя каталога из переменной окружения s5admin
   *
   * @param aId {@link IStridable} идентификатор переменной окружения (ИД-путь)
   * @return String значение переменной
   * @throws TsNullArgumentRtException аргумент = null;
   */
  private static String readEnvDirName( String aId ) {
    StridUtils.checkValidIdPath( aId );
    IAtomicValue dir = readEnv( aId );
    if( dir == IAtomicValue.NULL ) {
      dir = avStr( System.getProperty( WORK_DIR ) );
    }
    return new File( dir.asString() ).getAbsolutePath();
  }

  /**
   * Возвращает список строк из исходной строки
   *
   * @param aSource String исходная строка
   * @return {@link IStringList} список строк
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static IStringList readStringList( String aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    String[] value = aSource.split( CHAR_COLON );
    if( value.length <= 1 ) {
      value = aSource.split( CHAR_SEMICOLON );
    }
    return new StringArrayList( value );
  }

  /**
   * Слушатель контекста выполнения команд
   *
   * @author mvk
   */
  class ContextListener
      implements IAdminCmdContextListener {

    @Override
    public void onAddParam( IAdminCmdContext aContext, String aParamName ) {
      IPlexyValue paramValue = aContext.paramValue( aParamName );
      if( paramValue.type().kind() == EPlexyKind.SINGLE_REF && paramValue.singleRef() instanceof ISkConnection ) {
        skConnection = (ISkConnection)paramValue.singleRef();
        skConnection.addConnectionListener( consoleSkConnectionListener );
        // consoleSkConnectionListener.updatePrompt( skConnection );
      }
      if( paramValue.type().kind() == EPlexyKind.SINGLE_REF && paramValue.singleRef() instanceof S5Host ) {
        skHost = (S5Host)paramValue.singleRef();
        consoleSkConnectionListener.updatePrompt( skConnection );
      }
    }

    @Override
    public void onRemovingParam( IAdminCmdContext aContext, String aParamName ) {
      IPlexyValue paramValue = aContext.paramValue( aParamName );
      if( paramValue.type().kind() == EPlexyKind.SINGLE_REF && paramValue.singleRef() instanceof ISkConnection ) {
        skConnection = (ISkConnection)paramValue.singleRef();
        skConnection.removeConnectionListener( consoleSkConnectionListener );
      }
    }

    @Override
    public void onRemovedParam( IAdminCmdContext aContext, String aParamName ) {
      // nop
    }

    @Override
    public void onSetParamValue( IAdminCmdContext aContext, String aParamName ) {
      // nop
    }

    // ------------------------------------------------------------------------------------
    // Реализация IAdminCmdContextListener
    //
  }

  /**
   * Адаптер консоли sk-соединения
   *
   * @author mvk
   */
  class ConsoleSkConnectionListener
      implements ISkConnectionListener {

    private int connectCount = 0;

    // ------------------------------------------------------------------------------------
    // Реализация ISkConnectionListener
    //
    @Override
    public void onSkConnectionStateChanged( ISkConnection aSource, ESkConnState aOldState ) {
      updatePrompt( aSource );
    }

    public void updatePrompt( ISkConnection aSource ) {
      TsNullArgumentRtException.checkNull( aSource );
      if( aSource.state() == ESkConnState.ACTIVE ) {
        AdminConsole.this.skConnection = aSource;
        AdminConsole.this.updatePrompt();
        if( connectCount > 0 ) {
          terminal.showCmdSyntax();
        }
        connectCount++;
        return;
      }
      AdminConsole.this.updatePrompt();
      terminal.showCmdSyntax();
    }
  }

  /**
   * Считывает из манифеста время сборки сервера
   *
   * @return long время (мсек с начала эпохи) сборки сервера. {@link Long#MIN_VALUE} ошибка получения времени
   */
  @SuppressWarnings( "nls" )
  private static long getBuildTime() {
    Enumeration<URL> resources;
    SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
    try {
      ClassLoader classLoader = AdminConsole.class.getClassLoader();
      resources = classLoader.getResources( "META-INF/MANIFEST.MF" );
      while( resources.hasMoreElements() ) {
        try( InputStream os = resources.nextElement().openStream() ) {
          Manifest manifest = new Manifest( os );
          String builtDate = manifest.getMainAttributes().getValue( "Built-Date" );
          if( builtDate == null ) {
            // В манифесте нет времени сборки. Возможно это "чужой" манифест
            continue;
          }
          Date date = format.parse( builtDate );
          return date.getTime();
        }
      }
      return Long.MIN_VALUE;
    }
    catch( @SuppressWarnings( "unused" ) IOException | ParseException e ) {
      return Long.MIN_VALUE;
    }
  }

}
