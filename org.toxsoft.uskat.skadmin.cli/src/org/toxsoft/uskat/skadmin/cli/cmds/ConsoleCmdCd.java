package org.toxsoft.uskat.skadmin.cli.cmds;

import static org.toxsoft.core.tslib.bricks.strid.impl.StridUtils.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.cli.cmds.IAdminResources.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.cli.IAdminConsole;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;
import org.toxsoft.uskat.skadmin.core.IAdminCmdDef;

/**
 * Команда консоли: 'Смена текущего раздела команд'
 *
 * @author mvk
 */
public class ConsoleCmdCd
    extends AbstractConsoleCmd {

  /**
   * Конструктор
   *
   * @param aConsole {@link IAdminConsole} консоль
   */
  public ConsoleCmdCd( IAdminConsole aConsole ) {
    super( aConsole );
    // Раздел на который требуется осуществить переход
    addArg( CD_ARG_SECTION_ID, CD_ARG_SECTION_ALIAS, CD_ARG_SECTION_NAME, PT_SINGLE_STRING, CD_ARG_SECTION_DESCR );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CD_CMD_ID;
  }

  @Override
  public String alias() {
    return CD_CMD_ALIAS;
  }

  @Override
  public String nmName() {
    return CD_CMD_NAME;
  }

  @Override
  public String description() {
    return CD_CMD_DESCR;
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
    // Путь текущего раздела
    String currSectionId = console.getSectionId();
    // Признак корневого раздела
    boolean isRoot = (currSectionId.equals( EMPTY_STRING ));
    // Путь родительского раздела
    String parentSectionId = (isRoot ? EMPTY_STRING : StridUtils.removeTailingIdNames( currSectionId, 1 ));
    // Новый раздел представленный аргументом
    String argSectionId = argSingleValue( CD_ARG_SECTION_ID ).asString();
    // Переход на корневой раздел
    boolean toRootSection = argSectionId.equals( ROOT_SECTION );
    // Переход на родительский раздел
    boolean toParentSection = argSectionId.equals( PARENT_SECTION );
    // Новый раздел
    String newSectionId = argSectionId;

    if( toRootSection ) {
      // Переход на корневой раздел
      console.setSectionId( EMPTY_STRING );
      resultOk();
      return;
    }
    if( toParentSection ) {
      // Переход на родительский раздел
      console.setSectionId( parentSectionId );
      resultOk();
      return;
    }
    if( !isRoot && !isIdAPath( newSectionId ) ) {
      // Дополняем раздел путем до корневого раздела
      newSectionId = StridUtils.makeIdPath( currSectionId, newSectionId );
    }
    // Переход на указанный раздел
    if( !console.isValidSectionId( newSectionId ) ) {
      addResultError( ERR_MSG_SECTION_NOT_FOUND, argSectionId );
      resultFail();
      return;
    }
    console.setSectionId( newSectionId );
    resultOk();
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    if( aArgId.equals( CD_ARG_SECTION_ID ) ) {
      IAdminConsole console = getConsole();
      IList<IAdminCmdDef> cmdDefs = console.listCmdDefs();
      IStringListEdit sectionIds = new StringArrayList( cmdDefs.size() );
      // Формирование списка возможных разделов
      for( int index = 0, n = cmdDefs.size(); index < n; index++ ) {
        IAdminCmdDef cmdDef = cmdDefs.get( index );
        String sectionId = StridUtils.removeTailingIdNames( cmdDef.id(), 1 );
        if( !sectionIds.hasElem( sectionId ) ) {
          sectionIds.add( sectionId );
        }
      }
      // Обязательные разделы
      sectionIds.add( ROOT_SECTION );
      sectionIds.add( PARENT_SECTION );
      // Подготовка списка возможных значений
      IListEdit<IPlexyValue> values = new ElemArrayList<>( sectionIds.size() );
      for( int index = 0, n = sectionIds.size(); index < n; index++ ) {
        IAtomicValue dataValue = AvUtils.avStr( sectionIds.get( index ) );
        IPlexyValue plexyValue = pvSingleValue( dataValue );
        values.add( plexyValue );
      }
      return values;
    }
    return IList.EMPTY;
  }
}
