package org.toxsoft.uskat.skadmin.dev.objects;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.dev.objects.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.dev.objects.IAdminHardResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.legacy.plexy.*;
import org.toxsoft.uskat.skadmin.core.*;
import org.toxsoft.uskat.skadmin.core.impl.*;

/**
 * Команда s5admin: удаление объекта(ов)
 *
 * @author mvk
 */
public class AdminCmdRemoveObject
    extends AbstractAdminCmd {

  /**
   * Конструктор
   */
  public AdminCmdRemoveObject() {
    // Контекст: API ISkConnection
    addArg( CTX_SK_CONNECTION );
    // Идентификатор класса объекта
    addArg( ARG_CLASSID );
    // Строковый идентификатор объекта
    addArg( ARG_STRID );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_REMOVE_OBJ_ID;
  }

  @Override
  public String alias() {
    return CMD_REMOVE_OBJ_ALIAS;
  }

  @Override
  public String nmName() {
    return CMD_REMOVE_OBJ_NAME;
  }

  @Override
  public String description() {
    return CMD_REMOVE_OBJ_DESCR;
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
  public void doExec( IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback ) {
    ISkConnection connection = argSingleRef( CTX_SK_CONNECTION );
    ISkCoreApi coreApi = connection.coreApi();
    ISkObjectService objService = coreApi.objService();
    String classId = argSingleValue( ARG_CLASSID ).asString();
    String strid = argSingleValue( ARG_STRID ).asString();
    if( strid.equals( EMPTY_STRING ) ) {
      strid = MULTI;
    }
    // Время начала чтения значений
    long startTime = System.currentTimeMillis();
    // Получение идентификаторов атрибутов
    ISkidList objIds = AdminObjectsUtils.getObjSkids( coreApi, classId, strid );
    // Запись в журнал
    addResultInfo( "\n" + MSG_CMD_REMOVE_OBJ, Integer.valueOf( objIds.size() ) ); //$NON-NLS-1$
    // Уадление объектов
    objService.removeObjects( objIds );
    addResultInfo( "\n\n" + MSG_CMD_TIME, Long.valueOf( System.currentTimeMillis() - startTime ) ); //$NON-NLS-1$
    resultOk();
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    IPlexyValue pxCoreApi = contextParamValueOrNull( CTX_SK_CORE_API );
    if( pxCoreApi == null ) {
      return IList.EMPTY;
    }
    ISkCoreApi coreApi = (ISkCoreApi)pxCoreApi.singleRef();
    ISkSysdescr sysdescr = coreApi.sysdescr();
    ISkObjectService objService = coreApi.objService();
    if( aArgId.equals( ARG_CLASSID.id() ) ) {
      // Список всех классов
      IStridablesList<ISkClassInfo> classInfos = sysdescr.listClasses();
      // Подготовка списка возможных значений
      IListEdit<IPlexyValue> values = new ElemArrayList<>( classInfos.size() );
      for( int index = 0, n = classInfos.size(); index < n; index++ ) {
        IAtomicValue dataValue = AvUtils.avStr( classInfos.get( index ).id() );
        IPlexyValue plexyValue = pvSingleValue( dataValue );
        values.add( plexyValue );
      }
      return values;
    }
    if( (aArgId.equals( ARG_STRID.id() ) && aArgValues.keys().hasElem( ARG_CLASSID.id() )) ) {
      // Идентификатор класса
      String classId = aArgValues.getByKey( ARG_CLASSID.id() ).singleValue().asString();
      // Список всех объектов с учетом наследников
      ISkidList objList = objService.listSkids( classId, true );
      // Подготовка списка возможных значений
      IListEdit<IPlexyValue> values = new ElemArrayList<>( objList.size() );
      // Значение '*'
      IAtomicValue dataValue = AvUtils.avStr( MULTI );
      IPlexyValue plexyValue = pvSingleValue( dataValue );
      values.add( plexyValue );
      for( int index = 0, n = objList.size(); index < n; index++ ) {
        dataValue = AvUtils.avStr( objList.get( index ).strid() );
        plexyValue = pvSingleValue( dataValue );
        values.add( plexyValue );
      }
      return values;
    }
    return IList.EMPTY;
  }
}
