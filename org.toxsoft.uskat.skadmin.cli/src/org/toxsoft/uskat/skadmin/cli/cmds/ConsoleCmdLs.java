package org.toxsoft.uskat.skadmin.cli.cmds;

import static java.lang.String.*;
import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.bricks.strid.impl.StridUtils.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.skadmin.cli.AdminColors.*;
import static org.toxsoft.uskat.skadmin.cli.IAdminAnsiConstants.*;
import static org.toxsoft.uskat.skadmin.cli.cmds.IAdminResources.*;
import static org.toxsoft.uskat.skadmin.core.plugins.AdminPluginUtils.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListBasicEdit;
import org.toxsoft.core.tslib.coll.impl.SortedElemLinkedBundleList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.cli.IAdminConsole;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;
import org.toxsoft.uskat.skadmin.core.IAdminCmdDef;

/**
 * Команда консоли: 'Вывод на экран идентификаторов разделов и команд доступных в текущем или указанном разделе'
 *
 * @author mvk
 */
public class ConsoleCmdLs
    extends AbstractConsoleCmd {

  /**
   * Конструктор
   *
   * @param aConsole {@link IAdminConsole} консоль
   */
  public ConsoleCmdLs( IAdminConsole aConsole ) {
    super( aConsole );
    // Раздел по которому требуется вывести информацию. Пустая строка: вывод по текущему разделу
    addArg( LS_ARG_SECTION_ID, LS_ARG_SECTION_ALIAS, LS_ARG_SECTION_NAME,
        createType( EAtomicType.STRING, EMPTY_STRING ), LS_ARG_SECTION_DESCR );
    // Вывод описаний команд
    addArg( LS_ARG_DESCRIPTION_ID, LS_ARG_DESCRIPTION_ALIAS, LS_ARG_DESCRIPTION_NAME,
        createType( BOOLEAN, LS_ARG_DESCRIPTION_DEFAULT ), LS_ARG_DESCRIPTION_DESCR );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return LS_CMD_ID;
  }

  @Override
  public String alias() {
    return LS_CMD_ALIAS;
  }

  @Override
  public String nmName() {
    return LS_CMD_NAME;
  }

  @Override
  public String description() {
    return LS_CMD_DESCR;
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
    // Раздел отображаемых команд
    String sectionId = argSectionParse( console.getSectionId(), argSingleValue( LS_ARG_SECTION_ID ).asString() );
    // Раздел по которому требуется вывести команды
    boolean showDescription = argSingleValue( LS_ARG_DESCRIPTION_ID ).asBool();
    if( !console.isValidSectionId( sectionId ) ) {
      // Раздел не существует
      addResultError( ERR_MSG_SECTION_NOT_FOUND, sectionId );
      resultFail();
      return;
    }
    // Признак того, что раздел является корневым
    boolean isRoot = (sectionId.equals( EMPTY_STRING ));
    // Список доступных команд
    IList<IAdminCmdDef> listCmdDefs = console.listCmdDefs();
    // Список отображаемых элементов
    IListBasicEdit<Item> items = new SortedElemLinkedBundleList<>();
    for( IAdminCmdDef cmdDef : listCmdDefs ) {
      String cmdId = cmdDef.id();
      if( !isRoot && !startsWithIdPath( cmdId, sectionId ) ) {
        // Команда из другого раздела
        continue;
      }
      String cmdIdPrefix = StridUtils.removeTailingIdNames( cmdId, 1 );
      if( cmdIdPrefix.equals( sectionId ) ) {
        // Команда текущего раздела
        items.add( new Item( StridUtils.getLast( cmdDef.id() ), cmdDef.nmName() ) );
        continue;
      }
      // Команда в дочернем разделе
      String childSectionId = cmdId;
      if( !isRoot ) {
        // Удаляем путь текущего раздела
        int idpathSize = StridUtils.getComponents( sectionId ).size();
        childSectionId = StridUtils.removeStartingIdNames( childSectionId, idpathSize );
      }
      childSectionId = StridUtils.getComponent( childSectionId, 0 ) + CHAR_SLASH;
      if( !Item.hasItem( items, childSectionId ) ) {
        items.add( new Item( childSectionId, MSG_CHILD_SECTION ) );
      }
    }
    if( showDescription ) {
      // Вывод с описанием команд
      printCmdNames( console, items );
    }
    else {
      // Простой вывод
      printPlain( console, items );
    }
    resultOk();
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    return IList.EMPTY;
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  //
  /**
   * Сделать вывод разделов и команд с описанием команд
   *
   * @param aConsole {@link IAdminConsole} консоль
   * @param aItems {@link IList}&lt;{@link Item}&gt; список элементов отображения
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void printCmdNames( IAdminConsole aConsole, IList<Item> aItems ) {
    TsNullArgumentRtException.checkNulls( aConsole, aItems );
    int descrIndent = 19;
    // Шаблон вывода
    String pattern = PATTERN_START + descrIndent + PATTERN_FINISH;
    // Вывод разделов
    for( int index = 0, n = aItems.size(); index < n; index++ ) {
      Item item = aItems.get( index );
      System.out.print( COLOR_ID );
      System.out.print( format( pattern, item.getName() ) );
      System.out.print( COLOR_RESET );
      System.out.println( item.getDesciption() );
    }
  }

  /**
   * Сделать вывод разделов и команд в обычном варианте
   *
   * @param aConsole {@link IAdminConsole} консоль
   * @param aItems {@link IList}&lt;{@link Item}&gt; список элементов отображения
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void printPlain( IAdminConsole aConsole, IList<Item> aItems ) {
    TsNullArgumentRtException.checkNulls( aConsole, aItems );
    int consoleWidth = aConsole.getWidth();
    // Находим самый длинный идентификатор (1 - чтобы избежать деления на 0)
    int widthMax = 1;
    for( int index = 0, n = aItems.size(); index < n; index++ ) {
      String name = aItems.get( index ).getName();
      if( widthMax < name.length() ) {
        widthMax = name.length();
      }
    }
    // Количество столбцов
    int columnQtty = consoleWidth / widthMax / 2;
    // Шаблон вывода
    String pattern = PATTERN_START + 2 * widthMax + PATTERN_FINISH;

    // Вывод разделов
    int currColumn = 0;
    for( int index = 0, n = aItems.size(); index < n; index++ ) {
      Item item = aItems.get( index );
      System.out.print( COLOR_ID );
      System.out.print( format( pattern, item.getName() ) );
      System.out.print( COLOR_RESET );
      if( ++currColumn >= columnQtty ) {
        System.out.println();
        currColumn = 0;
      }
    }
    System.out.println();
  }

  /**
   * Анализирует значение аргумента "раздел команд" и возвращает раздел по которому требуется выполнить команду
   *
   * @param aCurrSectionId String - текущий раздел консоли
   * @param aArgSectionId String - раздел консоли введенный пользователем
   * @return String раздел по которому требуется выполнить команду
   */
  private static String argSectionParse( String aCurrSectionId, String aArgSectionId ) {
    // Признак отображения корневого раздела
    boolean showRoot = aArgSectionId.equals( ROOT_SECTION );
    // Признак отображения корневого раздела
    boolean showParent = aArgSectionId.equals( PARENT_SECTION );
    if( showRoot ) {
      // Отображение корневого раздела
      return EMPTY_STRING;
    }
    if( showParent ) {
      // Отображение родительского раздела
      return (aCurrSectionId.equals( EMPTY_STRING ) ? EMPTY_STRING
          : StridUtils.removeTailingIdNames( aCurrSectionId, 1 ));
    }
    if( aArgSectionId.equals( EMPTY_STRING ) ) {
      // Отображение текущего раздела
      return aCurrSectionId;
    }
    // Отображение раздела введенного пользователем
    return aArgSectionId;
  }

  /**
   * Элемент отображения командой ls
   */
  static final class Item
      implements Comparable<Item> {

    private final String name;
    private final String description;

    /**
     * Конструктор
     *
     * @param aName - имя элемента
     * @param aDescription - описание элемент
     * @throws TsNullArgumentRtException аргумент = null
     */
    Item( String aName, String aDescription ) {
      TsNullArgumentRtException.checkNulls( aName, aDescription );
      name = aName;
      description = aDescription;
    }

    // ------------------------------------------------------------------------------------
    // Открытое API
    //
    /**
     * Возвращает имя элемента
     *
     * @return String имя элемента
     */
    public String getName() {
      return name;
    }

    /**
     * Возвращает описание элемента
     *
     * @return String описание элемента
     */
    public String getDesciption() {
      return description;
    }

    // ------------------------------------------------------------------------------------
    // Внутреннее API
    //
    @Override
    public int compareTo( Item aOther ) {
      return name.compareTo( aOther.name );
    }

    /**
     * Возвращает признак того, что элемент с указанным именем есть в в указанном списке
     *
     * @param aItems {@link IList}&lt{@link Item}&gt; - список элементов отображения
     * @param aName String имя элемента
     * @return boolean <b>true</b> есть элемент <b>false</b> нет элемента
     * @throws TsNullArgumentRtException любой аргумент null
     */
    static boolean hasItem( IList<Item> aItems, String aName ) {
      TsNullArgumentRtException.checkNulls( aItems, aName );
      for( int index = 0, n = aItems.size(); index < n; index++ ) {
        if( aItems.get( index ).getName().equals( aName ) ) {
          return true;
        }
      }
      return false;
    }
  }

}
