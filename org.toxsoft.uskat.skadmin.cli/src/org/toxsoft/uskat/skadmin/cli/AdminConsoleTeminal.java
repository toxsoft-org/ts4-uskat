package org.toxsoft.uskat.skadmin.cli;

import static org.toxsoft.uskat.skadmin.cli.AdminColors.*;
import static org.toxsoft.uskat.skadmin.cli.IAdminAnsiConstants.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;

import java.io.*;
import java.util.*;

import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.skadmin.cli.parsers.*;
import org.toxsoft.uskat.skadmin.core.*;

import scala.tools.jline.console.*;

/**
 * Терминал консоли
 *
 * @author mvk
 */
public class AdminConsoleTeminal
    extends ConsoleReader {

  /**
   * Таймаут (мсек) раскраски синтаксиса команды
   */
  private static final long SYNTAX_COLORING_TIMEOUT = 300;

  /**
   * Контекст
   */
  private final IAdminCmdContext context;

  /**
   * Парсер синтаксического анализа. null: парсер отключен
   */
  private IAdminCmdSyntaxParser syntaxParser;

  /**
   * Признак необходимости подсветки синтаксиса
   */
  private final boolean syntaxHighlighting;

  /**
   * Признак необходимости подсветки синтаксиса парсера
   */
  private boolean syntaxParserHighlighting = true;

  /**
   * Таймер обновления строки
   */
  private final Timer updateTimer;

  /**
   * Задача раскраски синтаксиса
   */
  private SyntaxColoringTimerTask syntaxColoringTimerTask;

  /**
   * Признак того, что читатель находится в ожидании отображения синтаксиса команды
   */
  private boolean waitShowSyntax;

  /**
   * Конструктор
   *
   * @param aContext {@link IAdminCmdContext} контекст
   * @param aSyntaxParser {@link IAdminCmdSyntaxParser} - синтаксический анализатор командной строки
   * @param aSyntaxHighlighting boolean <b>true</b> c подсветкой синтаксиса; <b>false</b> без подсветки синтаксиса.
   * @throws TsNullArgumentRtException любой аргумент null
   * @throws IOException ошибка создания терминала
   */
  public AdminConsoleTeminal( IAdminCmdContext aContext, IAdminCmdSyntaxParser aSyntaxParser,
      boolean aSyntaxHighlighting )
      throws IOException {
    TsNullArgumentRtException.checkNulls( aContext, aSyntaxParser );
    // Таймер обновления в режиме "daemon"
    updateTimer = new Timer( true );
    context = aContext;
    syntaxParser = aSyntaxParser;
    syntaxHighlighting = aSyntaxHighlighting;
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Завершить работу терминала
   */
  public void close() {
    // Останавливаем таймер обновления
    updateTimer.cancel();
    // Восстанавливаем состояние терминала в состояние до запуска
    try {
      getTerminal().restore();
    }
    catch( Exception e ) {
      throw new TsInternalErrorRtException( e, e.getLocalizedMessage() );
    }
  }

  /**
   * Возвращает текущий парсер командной строки или null если он не установлен
   *
   * @return {@link IAdminCmdSyntaxParser} - парсер командной строки. null: парсер не установлен
   */
  public IAdminCmdSyntaxParser getSyntaxParserOrNull() {
    return syntaxParser;
  }

  /**
   * Установить синтаксический анализатор командной строки
   *
   * @param aSyntaxParser {@link IAdminCmdSyntaxParser} - синтаксический анализатор командной строки. null: парсер не
   *          установлен
   * @param aSyntaxHighlighting boolean <b>true</b> c подсветкой синтаксиса; <b>false</b> без подсветки синтаксиса.
   */
  public void setSyntaxParser( IAdminCmdSyntaxParser aSyntaxParser, boolean aSyntaxHighlighting ) {
    syntaxParser = aSyntaxParser;
    syntaxParserHighlighting = aSyntaxHighlighting;
  }

  /**
   * Ищет в указанном тексте escape(ansi) последовательности
   *
   * @param aAnsiText String текст с escape-последовательностями
   * @return String список escape-последовательностей
   * @throws TsNullArgumentRtException аргумент = null
   */
  public String findAnsi( String aAnsiText ) {
    TsNullArgumentRtException.checkNull( aAnsiText );
    String noAnsiText = stripAnsi( aAnsiText );
    int noAnsiIndex = 0;
    StringBuilder sb = new StringBuilder();
    for( int index = 0, n = aAnsiText.length(); index < n; index++ ) {
      char nextChar = aAnsiText.charAt( index );
      if( nextChar == noAnsiText.charAt( noAnsiIndex ) ) {
        noAnsiIndex++;
        continue;
      }
      sb.append( nextChar );
    }
    return sb.toString();
  }

  // ------------------------------------------------------------------------------------
  // Переопределение ConsoleReader
  //
  @Override
  protected void onCharEvent( char aChar, Operation aOperation ) {
    switch( aOperation ) {
      case UNKNOWN:
        if( IStrioHardConstants.DEFAULT_DELIMITER_CHARS.indexOf( aChar ) >= 0 ) {
          // Введены символы разделителя. Немедленная проверка синтаксиса
          cancelShowSyntaxTask();
          showCmdSyntax();
          return;
        }
        // Отложенная по таймеру проверка синтаксиса
        startShowSyntaxTask();
        break;
      case COMPLETE:
      case NEXT_HISTORY:
      case PASTE:
      case PASTE_NEXT:
      case PASTE_PREV:
      case PREV_HISTORY:
      case DELETE_META:
      case DELETE_NEXT_CHAR:
      case DELETE_NEXT_WORD:
      case DELETE_PREV_CHAR:
      case DELETE_PREV_WORD:
        // Немедленная проверка синтаксиса
        cancelShowSyntaxTask();
        showCmdSyntax();
        break;
      case ABORT:
      case ADD:
      case CHANGE_CASE:
      case CHANGE_META:
      case CLEAR_LINE:
      case CLEAR_SCREEN:
      case END_OF_HISTORY:
      case END_WORD:
      case EXIT:
      case INSERT:
      case KILL_LINE:
      case KILL_LINE_PREV:
      case MOVE_TO_BEG:
      case MOVE_TO_END:
      case NEWLINE:
      case NEXT_CHAR:
      case NEXT_SPACE_WORD:
      case NEXT_WORD:
      case PREV_CHAR:
      case PREV_SPACE_WORD:
      case PREV_WORD:
      case REDISPLAY:
      case REPEAT_NEXT_CHAR:
      case REPEAT_PREV_CHAR:
      case REPEAT_SEARCH_NEXT:
      case REPEAT_SEARCH_PREV:
      case REPLACE_CHAR:
      case REPLACE_MODE:
      case SEARCH_NEXT:
      case SEARCH_PREV:
      case START_OF_HISTORY:
      case SUBSTITUTE_CHAR:
      case SUBSTITUTE_LINE:
      case TO_END_WORD:
      case TO_NEXT_CHAR:
      case TO_PREV_CHAR:
      case UNDO:
      default:
        // Проверка синтаксиса если до этого был запущен таймер проверки синтаксиса
        if( waitShowSyntax ) {
          cancelShowSyntaxTask();
          showCmdSyntax();
        }
        break;
    }
  }

  @Override
  public String readLine( String aPrompt, final Character aMask, String aBufferString )
      throws IOException {
    return super.readLine( aPrompt, aMask, aBufferString );
  }

  @Override
  public boolean clearScreen()
      throws IOException {
    // Команда очистки экрана
    printAnsiSequence( ESCAPE_CLEAR_SCREAN );
    // Инициализация позиции курсора
    printAnsiSequence( 0 + ESCAPE_CURSOR_TO_AT );
    return true;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Отменяет отложенную задачу подсветки синтаксиса
   */
  private void cancelShowSyntaxTask() {
    if( syntaxColoringTimerTask != null ) {
      syntaxColoringTimerTask.cancel();
    }
  }

  /**
   * Запускает отложенную задачу подсветки синтаксиса
   *
   * @throws TsNullArgumentRtException аргумент = null
   */
  private void startShowSyntaxTask() {
    if( syntaxParser == null ) {
      // Отображение синтаксиса запрещено
      return;
    }
    // Отменяем предыдущую задачу раскраски синтаксиса
    cancelShowSyntaxTask();
    // Создаем задачу таймера раскраски синтаксиса с новым временем
    syntaxColoringTimerTask = new SyntaxColoringTimerTask( this );
    updateTimer.schedule( syntaxColoringTimerTask, SYNTAX_COLORING_TIMEOUT );
    waitShowSyntax = true;
  }

  void showCmdSyntax() {
    ITsThreadExecutor threadExecutor = (ITsThreadExecutor)context.paramValue( CTX_THREAD_EXECUTOR ).singleRef();
    threadExecutor.syncExec( this::showCmdSyntaxImpl );
  }

  /**
   * Отображение синтаксиса введенной команды
   */
  void showCmdSyntaxImpl() {
    if( syntaxParser == null ) {
      // Парсер не установлен
      return;
    }
    CursorBuffer cursorBuffer = getCursorBuffer();
    int cursorPosition = getCursorPosition();
    try {
      StringBuilder sb = new StringBuilder();
      String line = cursorBuffer.toString();
      IList<IAdminCmdToken> tokens = syntaxParser.parse( line );
      if( !syntaxHighlighting || !syntaxParserHighlighting ) {
        // Подсветка синтаксиса запрещена. Только разбор командной строки
        return;
      }
      int lastIndex = 0;
      for( IAdminCmdToken token : tokens ) {
        ETokenType type = token.type();
        int startIndex = token.startIndex();
        int finishIndex = token.finishIndex();
        if( startIndex < 0 || finishIndex < 0 ) {
          // Пустая лексема
          continue;
        }
        if( lastIndex < token.startIndex() ) {
          // Копирование данных строки до лексемы
          sb.append( line.substring( lastIndex, startIndex ) );
        }
        // Тип лексемы
        switch( type ) {
          case UNDEF:
            sb.append( COLOR_ERROR );
            break;
          case CONTEXT:
            sb.append( COLOR_SINGLE_REF );
            break;
          case LIST_CONTEXT:
            sb.append( COLOR_REF_LIST );
            break;
          case STATEMENT:
            sb.append( COLOR_ID );
            break;
          case ID:
            sb.append( COLOR_ID );
            break;
          case VALUE:
            sb.append( COLOR_SINGLE_VALUE );
            // TODO: 2022-09-23 mvk
            if( token.quoted() ) {
              // startIndex++;
              // finishIndex += 1;
            }
            break;
          case LIST_VALUE:
            sb.append( COLOR_VALUE_LIST );
            break;
          case NAMED_VALUE:
            sb.append( COLOR_VALUE_LIST );
            break;
          default:
            throw new TsNotAllEnumsUsedRtException();
        }
        // Данные лексемы
        // sb.append( token.data() );
        int endIndex = Math.min( finishIndex + 1, line.length() );
        String colorText = line.substring( Math.min( startIndex, endIndex ), endIndex );
        sb.append( colorText );
        sb.append( COLOR_RESET );
        lastIndex = finishIndex + 1;
      }
      if( lastIndex < line.length() ) {
        // Копирование данных строки до завершения строки
        sb.append( line.substring( lastIndex ) );
      }
      String prompt = getPrompt();
      int terminalWidth = getTerminal().getWidth();
      int purePromptLength = stripAnsi( prompt ).length();
      int pureTextLength = stripAnsi( sb.toString() ).length();
      for( int index = 0, n = terminalWidth - purePromptLength - pureTextLength - 1; index < n; index++ ) {
        sb.append( ' ' );
      }
      line = ESCAPE_CURSOR_TO_START + prompt + sb.toString();
      out.write( line );
      // Восстановление позиции курсора
      printAnsiSequence( cursorPosition + 1 + ESCAPE_CURSOR_TO_AT );
    }
    catch( IOException e ) {
      throw new TsInternalErrorRtException( e, e.getLocalizedMessage() );
    }
    waitShowSyntax = false;
  }

  /**
   * Задача таймера: раскраска синтаксиса команды
   */
  private final class SyntaxColoringTimerTask
      extends TimerTask {

    /**
     * Конструктор
     *
     * @param aReader AdminConsoleReader - читатель консоли
     * @throws TsNullArgumentRtException аргумент = null
     */
    SyntaxColoringTimerTask( AdminConsoleTeminal aReader ) {
      TsNullArgumentRtException.checkNulls( aReader );
    }

    // ------------------------------------------------------------------------------------
    // Реализация Runnable
    //
    @Override
    public void run() {
      showCmdSyntax();
    }
  }
}
