package org.toxsoft.uskat.skadmin.dev.objects;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.dev.objects.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.dev.objects.IAdminHardResources.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;
import org.toxsoft.uskat.skadmin.core.impl.AbstractAdminCmd;

import ru.uskat.common.dpu.impl.DpuObject;
import ru.uskat.core.ISkCoreApi;
import ru.uskat.core.api.objserv.ISkObjectService;
import ru.uskat.core.api.sysdescr.*;
import ru.uskat.core.common.skobject.ISkObject;
import ru.uskat.core.connection.ISkConnection;

/**
 * Команда s5admin: Чтение значения атрибута объекта
 *
 * @author mvk
 */
public class AdminCmdSetAttr
    extends AbstractAdminCmd {

  /**
   * Конструктор
   */
  public AdminCmdSetAttr() {
    // Контекст: API ISkConnection
    addArg( CTX_SK_CONNECTION );
    // Идентификатор класса объекта
    addArg( ARG_CLASSID );
    // Строковый идентификатор объекта
    addArg( ARG_STRID );
    // Идентификатор атрибута
    addArg( ARG_ATTRID );
    // Новое значение для атрибута
    addArg( ARG_WRITE_VALUE );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_SET_ATTR_ID;
  }

  @Override
  public String alias() {
    return CMD_SET_ATTR_ALIAS;
  }

  @Override
  public String nmName() {
    return CMD_SET_ATTR_NAME;
  }

  @Override
  public String description() {
    return CMD_SET_ATTR_DESCR;
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
    String attrId = argSingleValue( ARG_ATTRID ).asString();
    IAtomicValue value = argSingleValue( ARG_WRITE_VALUE );
    // IAtomicValue value = AtomicValueKeeper.KEEPER.fromStr( argSingleValue( ARG_WRITE_VALUE ).asString() );
    if( strid.equals( EMPTY_STRING ) ) {
      strid = MULTI;
    }
    if( attrId.equals( EMPTY_STRING ) ) {
      attrId = MULTI;
    }
    // Время начала чтения значений
    long startTime = System.currentTimeMillis();
    // Получение идентификаторов атрибутов
    IList<Gwid> gwids = AdminObjectsUtils.getAttrGwids( coreApi, classId, strid, attrId );
    // Вывод значений атрибутов
    for( Gwid gwid : gwids ) {
      ISkObject obj = objService.get( gwid.skid() );
      IOptionSetEdit attrs = new OptionSet( obj.attrs() );
      attrs.setValue( gwid.propId(), value );
      objService.defineObject( new DpuObject( obj.skid(), attrs ) );
      // Время в текстовом виде
      String time = TimeUtils.timestampToString( System.currentTimeMillis() );
      addResultInfo( "\n" + MSG_CMD_GET_ATTR_VALUE, time, gwid, value ); //$NON-NLS-1$
    }
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
    ISkClassInfoManager classManager = sysdescr.classInfoManager();
    ISkObjectService objService = coreApi.objService();
    if( aArgId.equals( ARG_CLASSID.id() ) ) {
      // Список всех классов
      IStridablesList<ISkClassInfo> classInfos = classManager.listClasses();
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
    if( aArgId.equals( ARG_ATTRID.id() ) && aArgValues.keys().hasElem( ARG_CLASSID.id() ) ) {
      String classId = aArgValues.getByKey( ARG_CLASSID.id() ).singleValue().asString();
      ISkClassInfo classInfo = classManager.findClassInfo( classId );
      if( classInfo == null ) {
        return IList.EMPTY;
      }
      IStridablesList<ISkAttrInfo> attrInfos = classInfo.attrInfos();
      IListEdit<IPlexyValue> values = new ElemLinkedList<>();
      // Значение '*'
      IAtomicValue attrValue = AvUtils.avStr( MULTI );
      IPlexyValue plexyValue = pvSingleValue( attrValue );
      values.add( plexyValue );
      for( ISkAttrInfo attrInfo : attrInfos ) {
        attrValue = AvUtils.avStr( attrInfo.id() );
        plexyValue = pvSingleValue( attrValue );
        values.add( plexyValue );
      }
      return values;
    }
    return IList.EMPTY;
  }
}
