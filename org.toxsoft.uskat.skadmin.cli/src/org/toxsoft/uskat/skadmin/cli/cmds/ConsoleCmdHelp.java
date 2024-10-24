package org.toxsoft.uskat.skadmin.cli.cmds;

import static java.lang.String.*;
import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.cli.AdminColors.*;
import static org.toxsoft.uskat.skadmin.cli.AdminConsoleUtils.*;
import static org.toxsoft.uskat.skadmin.cli.IAdminAnsiConstants.*;
import static org.toxsoft.uskat.skadmin.cli.cmds.IAdminResources.*;
import static org.toxsoft.uskat.skadmin.core.impl.AdminCmdLibraryUtils.*;
import static org.toxsoft.uskat.skadmin.core.plugins.AdminPluginUtils.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.legacy.plexy.*;
import org.toxsoft.uskat.skadmin.cli.*;
import org.toxsoft.uskat.skadmin.core.*;

/**
 * Команда консоли: 'Справка по командам и аргументам'
 *
 * @author mvk
 */
public class ConsoleCmdHelp
    extends AbstractConsoleCmd {

  /**
   * Конструктор
   *
   * @param aConsole {@link IAdminConsole} консоль
   */
  public ConsoleCmdHelp( IAdminConsole aConsole ) {
    super( aConsole );
    // Идентификатор команды по которой требуется справка. Пустая строка: по всем командам.
    addArg( HELP_ARG_CMD_ID, HELP_ARG_CMD_ALIAS, HELP_ARG_CMD_NAME, PT_SINGLE_STRING, HELP_ARG_CMD_DESCR );
    // Вывод справки по всем командам
    addArg( HELP_ARG_ALL_ID, HELP_ARG_ALL_ALIAS, HELP_ARG_ALL_NAME,
        createType( BOOLEAN, avBool( Boolean.parseBoolean( HELP_ARG_ALL_DEFAULT ) ) ), HELP_ARG_ALL_DESCR );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return HELP_CMD_ID;
  }

  @Override
  public String alias() {
    return HELP_CMD_ALIAS;
  }

  @Override
  public String nmName() {
    return HELP_CMD_NAME;
  }

  @Override
  public String description() {
    return HELP_CMD_DESCR;
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
    String cmdId = argSingleValue( HELP_ARG_CMD_ID ).asString();
    boolean all = argSingleValue( HELP_ARG_ALL_ID ).asBool();
    if( !cmdId.equals( EMPTY_STRING ) ) {
      IAdminCmdDef cmdDef = console.findCmdDef( cmdId );
      if( cmdDef == null ) {
        // Пробуем найти команду относительно текущего раздела
        String absCmdId = StridUtils.makeIdPath( console.getSectionId(), cmdId );
        cmdDef = console.findCmdDef( absCmdId );
        if( cmdDef != null ) {
          cmdId = absCmdId;
        }
      }
      if( cmdDef == null ) {
        // Команда не существует
        addResultError( ERR_MSG_CMD_NOT_FOUND, cmdId );
        resultFail();
        return;
      }
      // Вывод справки по команде
      printCmdHelp( console, cmdDef );
      resultOk();
      return;
    }
    if( all ) {
      // Вывод справки по всем командам
      IList<IAdminCmdDef> cmdDefs = console.listCmdDefs();
      for( int index = 0, n = cmdDefs.size(); index < n; index++ ) {
        printCmdHelp( console, cmdDefs.get( index ) );
        if( index < n - 1 ) {
          System.out.println();
        }
      }
      resultOk();
      return;
    }
    console.print( 0, true, MSG_HELP );
    resultOk();
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    if( aArgId.equals( HELP_ARG_ALL_ID ) ) {
      // Проверка возможности использования флага: 'все команды'
      if( aArgValues.hasKey( HELP_ARG_CMD_ID ) ) {
        // Определено значение аргумента 'команда'. Флаг не может быть использован
        IAtomicValue dataValue = AvUtils.avBool( false );
        return new ElemArrayList<>( pvSingleValue( dataValue ) );
      }
    }
    if( aArgId.equals( HELP_ARG_CMD_ID ) ) {
      // Проверка возможности использования аргумента: 'команда'
      if( aArgValues.hasKey( HELP_ARG_ALL_ID ) ) {
        // Определен флаг 'все команды'. Аргумент 'команда' не может быть использован
        IAtomicValue dataValue = AvUtils.AV_STR_EMPTY;
        return new ElemArrayList<>( pvSingleValue( dataValue ) );
      }
      // Формируем список возможных команд
      IAdminConsole console = getConsole();
      // Текущий раздел
      String sectionId = console.getSectionId();
      // Список описаний всех команд
      IList<IAdminCmdDef> cmdDefs = console.listCmdDefs();
      // Подготовка списка возможных значений
      IListEdit<IPlexyValue> values = new ElemArrayList<>( cmdDefs.size() );
      for( int index = 0, n = cmdDefs.size(); index < n; index++ ) {
        String cmdId = cmdDefs.get( index ).id();
        if( StridUtils.removeTailingIdNames( cmdId, 1 ).equals( sectionId ) ) {
          // Команда текущего раздела добавляется без префикса раздела
          cmdId = StridUtils.getLast( cmdId );
        }
        IAtomicValue dataValue = AvUtils.avStr( cmdId );
        IPlexyValue plexyValue = pvSingleValue( dataValue );
        values.add( plexyValue );
      }
      return values;
    }
    return IList.EMPTY;
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  //
  /**
   * Выводит на экран справку по команде
   *
   * @param aConsole {@link IAdminConsole} - консоль
   * @param aCmdDef {@link IAdminCmdDef} - описание команды
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void printCmdHelp( IAdminConsole aConsole, IAdminCmdDef aCmdDef ) {
    TsNullArgumentRtException.checkNulls( aConsole, aCmdDef );
    String sectionId = StridUtils.removeTailingIdNames( aCmdDef.id(), 1 );
    StringBuilder cmdId = new StringBuilder( StridUtils.getLast( (aCmdDef.id()) ) );
    String alias = aCmdDef.alias();
    String argInfoPattern = MSG_CMD_INFO;
    IStringList roles = aCmdDef.roles();
    if( !alias.equals( EMPTY_STRING ) ) {
      argInfoPattern = MSG_CMD_ALIAS_INFO;
      alias = StridUtils.getLast( alias );
      cmdId.append( IStrioHardConstants.CHAR_ITEM_SEPARATOR ).append( alias );
    }
    if( sectionId.equals( EMPTY_STRING ) ) {
      sectionId = ROOT_SECTION;
    }
    StringBuilder sbRoles = new StringBuilder();
    if( roles.size() == 0 ) {
      sbRoles.append( MSG_ROLES_ALL );
    }
    for( int index = 0, n = roles.size(); index < n; index++ ) {
      sbRoles.append( roles.get( index ) );
      if( index < n - 1 ) {
        sbRoles.append( IStrioHardConstants.CHAR_ITEM_SEPARATOR );
      }
    }
    System.out.println(
        format( argInfoPattern, sectionId, cmdId.toString(), sbRoles, textLighting( aCmdDef, aCmdDef.nmName() ) ) );
    System.out.print( MSG_CMD_DESCR );
    aConsole.print( 11, true, textLighting( aCmdDef, aCmdDef.description() ) + IStrioHardConstants.CHAR_EOL );
    // Аргументы с атомарными значениями
    IList<IAdminCmdArgDef> valueParams = getArgDefs( aCmdDef.argumentDefs(), false );
    // Аргументы со значениями объектных ссылок
    IList<IAdminCmdArgDef> refParams = getArgDefs( aCmdDef.argumentDefs(), true );
    System.out.println( refParams.size() > 0 ? MSG_CMD_CONTEXT : MSG_CMD_CONTEXT_NOT_USED );
    for( int index = 0, n = refParams.size(); index < n; index++ ) {
      printCmdArgHelp( aConsole, aCmdDef, refParams.get( index ) );
    }

    System.out.println( MSG_CMD_ARGS );
    for( int index = 0, n = valueParams.size(); index < n; index++ ) {
      printCmdArgHelp( aConsole, aCmdDef, valueParams.get( index ) );
    }
    printCmdResultHelp( aConsole, aCmdDef.resultType(), textLighting( aCmdDef, aCmdDef.resultDescription() ) );
    for( IAdminCmdContextParam contextParam : aCmdDef.resultContextParams() ) {
      printContextParams( aConsole, aCmdDef, contextParam );
    }
    System.out.println( IStrioHardConstants.CHAR_EOL );
  }

  /**
   * Выводит на экран справку по параметрам контекста команды
   *
   * @param aConsole {@link IAdminConsole} - консоль
   * @param aCmdDef {@link IAdminCmdDef} - описание команды
   * @param aContextParam {@link IAdminCmdContextParam} параметр контекста
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void printContextParams( IAdminConsole aConsole, IAdminCmdDef aCmdDef,
      IAdminCmdContextParam aContextParam ) {
    TsNullArgumentRtException.checkNulls( aConsole, aCmdDef, aContextParam );
    // Скорректированная (с учетом ansi) ширина символов
    String typeColor = getTypeNameColor( aContextParam.type() );
    String typeName = getTypeName( aContextParam.type() );
    System.out.print( format( MSG_CMD_CONTEXT_INFO, typeColor, aContextParam.id(), typeName ) );
    StringBuilder sb = new StringBuilder();
    if( !aContextParam.description().equals( EMPTY_STRING ) ) {
      // Полное описание аргумента
      sb.append( format( MSG_CMD_CONTEXT_DESCR, textLighting( aCmdDef, aContextParam.description() ) ) );
    }
    sb.append( IStrioHardConstants.CHAR_EOL );
    // Вывод на консоль
    aConsole.print( 39, true, sb.toString() );
  }

  /**
   * Выводит на экран справку по описанию аргумента команды
   *
   * @param aConsole {@link IAdminConsole} - консоль
   * @param aCmdDef {@link IAdminCmdDef} - описание команды
   * @param aArgDef {@link IAdminCmdArgDef} - описание аргумента
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void printCmdArgHelp( IAdminConsole aConsole, IAdminCmdDef aCmdDef, IAdminCmdArgDef aArgDef ) {
    TsNullArgumentRtException.checkNulls( aConsole, aCmdDef, aArgDef );
    IPlexyType argType = aArgDef.type();
    EPlexyKind argKind = argType.kind();
    // Скорректированная (с учетом ansi) ширина символов
    StringBuilder sb = new StringBuilder();
    sb.append( argKind.isReference() ? CHAR_DOLLAR : CHAR_ARG_PREFIX );
    sb.append( aArgDef.id() );
    if( !aArgDef.alias().equals( EMPTY_STRING ) ) {
      sb.append( IStrioHardConstants.CHAR_ITEM_SEPARATOR );
      sb.append( argKind.isReference() ? CHAR_DOLLAR : CHAR_ARG_PREFIX );
      sb.append( aArgDef.alias() );
    }
    String nameColor = (argKind.isReference() ? COLOR_SINGLE_REF : COLOR_ID);
    String typeColor = getTypeNameColor( argType );
    String typeName = getTypeName( argType );
    System.out.print( format( MSG_CMD_ARG_INFO, nameColor, sb.toString(), typeColor, typeName ) );
    sb = new StringBuilder();
    if( !aArgDef.nmName().equals( EMPTY_STRING ) ) {
      // Краткое описание аргумента
      sb.append( format( MSG_CMD_ARG_DESCR, textLighting( aCmdDef, aArgDef.nmName() ) ) );
    }
    if( !aArgDef.description().equals( EMPTY_STRING ) ) {
      // Полное описание аргумента
      sb.append( format( MSG_CMD_ARG_DESCR, textLighting( aCmdDef, aArgDef.description() ) ) );
    }
    if( argKind.isAtomic() && argType.dataType().params().hasValue( IAvMetaConstants.TSID_DEFAULT_VALUE ) ) {
      // Значение по умолчанию
      IAtomicValue defaultValue = aArgDef.type().dataType().params().getValue( IAvMetaConstants.TSID_DEFAULT_VALUE );
      sb.append( format( MSG_CMD_ARG_DEFAULT, defaultValue ) );
    }
    sb.append( IStrioHardConstants.CHAR_EOL );
    // Вывод на консоль
    aConsole.print( 39, true, sb.toString() );
  }

  /**
   * Выводит на экран справку по результатам команды
   *
   * @param aConsole {@link IAdminConsole} - консоль
   * @param aResultType {@link IPlexyType}&lt;?&gt; - тип результата
   * @param aResultDescription String - описание результата
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void printCmdResultHelp( IAdminConsole aConsole, IPlexyType aResultType, String aResultDescription ) {
    TsNullArgumentRtException.checkNulls( aConsole, aResultType, aResultDescription );
    if( aResultType == IPlexyType.NONE ) {
      // Нет результата
      System.out.print( MSG_NO_RESULT );
      return;
    }
    // Вывод на консоль
    String color = getTypeNameColor( aResultType );
    String typeName = getTypeName( aResultType );
    System.out.print( format( MSG_CMD_RESULT, color, typeName ) );
    aConsole.print( 39, true, aResultDescription + IStrioHardConstants.CHAR_EOL );
  }

  /**
   * Распознает в указанном тексте элементы формата командной строки и выделяет их с помощью ansi-последовательностей
   *
   * @param aCmdDef {@link IAdminCmdDef} - описание команды
   * @param aText String - текст для распознавания
   * @return текст с ansi-последовательностями
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static String textLighting( IAdminCmdDef aCmdDef, String aText ) {
    TsNullArgumentRtException.checkNulls( aCmdDef, aText );
    IList<IAdminCmdArgDef> argDefs = aCmdDef.argumentDefs();
    int argSize = argDefs.size();
    IStringListEdit argIds = new StringArrayList( argSize );
    for( int index = 0, n = argSize; index < n; index++ ) {
      argIds.add( argDefs.get( index ).id() );
    }
    char charContextPrefix = CHAR_DOLLAR.charAt( 0 );
    char charArgPrefix = CHAR_ARG_PREFIX.charAt( 0 );
    StringBuilder sb = new StringBuilder();
    for( int index = 0, n = aText.length(); index < n; index++ ) {
      char c = aText.charAt( index );
      if( c == charContextPrefix && index < n - 1 ) {
        // Найден префикс переменной контекста
        String id = readIdName( aText, index + 1 );
        // Прочитано имя параметра. Подсветка
        sb.append( COLOR_SINGLE_REF );
        sb.append( c );
        sb.append( id );
        sb.append( COLOR_RESET );
        index += id.length();
        continue;
      }
      if( c == charArgPrefix && index < n - 1 ) {
        // Найден префикс аргумента. Пробуем прочитать его ИД-имя
        String id = readIdName( aText, index + 1 );
        if( id.equals( EMPTY_STRING ) ) {
          // Невалидный идентификатор
          sb.append( c );
          continue;
        }
        if( id.equals( aCmdDef.id() ) ) {
          // Прочитан идентификатор команды. Подсветка
          sb.append( COLOR_ID );
          sb.append( id );
          sb.append( COLOR_RESET );
          index += id.length();
          continue;
        }
        if( argIds.hasElem( id ) ) {
          // Прочитан идентификатор аргумента. Подсветка
          sb.append( COLOR_ID );
          sb.append( c );
          sb.append( id );
          sb.append( COLOR_RESET );
          index += id.length();
          continue;
        }
      }
      sb.append( c );
    }
    return sb.toString();
  }

  /**
   * Считывает ИД-имя
   *
   * @param aSource String - строка из которой считывается ИД-имя
   * @param aFromPosition int - позиция в строке с которой происходит чтение
   * @return String считанное ИД-имя. Пустая строка: невозможно прочесть ИД-имя
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static String readIdName( String aSource, int aFromPosition ) {
    TsNullArgumentRtException.checkNull( aSource );
    StringBuilder sb = new StringBuilder();
    char ch = aSource.charAt( aFromPosition );
    if( !StridUtils.isIdStart( ch ) ) {
      return EMPTY_STRING;
    }
    sb.append( ch );
    for( int index = aFromPosition + 1, n = aSource.length(); index < n; index++ ) {
      ch = aSource.charAt( index );
      if( !StridUtils.isIdNamePart( ch ) ) {
        break;
      }
      sb.append( ch );
    }
    String idName = sb.toString();
    if( !StridUtils.isValidIdName( idName ) ) {
      return EMPTY_STRING;
    }
    return idName;
  }
}
