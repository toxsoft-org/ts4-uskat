package org.toxsoft.uskat.skadmin.dev.objects;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.dev.objects.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.dev.objects.IAdminHardResources.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.uskat.core.ISkCoreApi;
import org.toxsoft.uskat.core.api.objserv.ISkObject;
import org.toxsoft.uskat.core.api.objserv.ISkObjectService;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.core.api.sysdescr.ISkSysdescr;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoAttrInfo;
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;
import org.toxsoft.uskat.skadmin.core.IAdminCmdContextParam;
import org.toxsoft.uskat.skadmin.core.impl.AbstractAdminCmd;

/**
 * Команда s5admin: Чтение значения атрибута объекта
 *
 * @author mvk
 */
public class AdminCmdGetAttr
    extends AbstractAdminCmd {

  /**
   * Конструктор
   */
  public AdminCmdGetAttr() {
    // Контекст: API ISkConnection
    addArg( CTX_SK_CONNECTION );
    // Идентификатор класса объекта
    addArg( ARG_CLASSID );
    // Строковый идентификатор объекта
    addArg( ARG_STRID );
    // Идентификатор атрибута
    addArg( ARG_ATTRID );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_GET_ATTR_ID;
  }

  @Override
  public String alias() {
    return CMD_GET_ATTR_ALIAS;
  }

  @Override
  public String nmName() {
    return CMD_GET_ATTR_NAME;
  }

  @Override
  public String description() {
    return CMD_GET_ATTR_DESCR;
  }

  @Override
  public IPlexyType resultType() {
    return CTX_SK_ATOMIC_VALUE.type();
  }

  @Override
  public String resultDescription() {
    return CTX_SK_ATOMIC_VALUE.description();
  }

  @Override
  public IStridablesList<IAdminCmdContextParam> resultContextParams() {
    IStridablesListEdit<IAdminCmdContextParam> params = new StridablesList<>();
    params.add( CTX_SK_ATOMIC_VALUE );
    return params;
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
    // Последнее прочитанное значение атрибута
    IAtomicValue value = IAtomicValue.NULL;
    // Вывод значений атрибутов
    for( Gwid gwid : gwids ) {
      ISkObject obj = objService.get( gwid.skid() );
      // Время в текстовом виде
      String time = TimeUtils.timestampToString( System.currentTimeMillis() );
      // Вывод текущего значения канала
      value = obj.attrs().getValue( gwid.propId() );
      addResultInfo( "\n" + MSG_CMD_GET_ATTR_VALUE, time, gwid, value ); //$NON-NLS-1$
    }
    addResultInfo( "\n\n" + MSG_CMD_TIME, Long.valueOf( System.currentTimeMillis() - startTime ) ); //$NON-NLS-1$

    IPlexyValue pxValue = pvSingleRef( value );
    setContextParamValue( CTX_SK_ATOMIC_VALUE, pxValue );
    resultOk( pxValue );
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
    if( aArgId.equals( ARG_ATTRID.id() ) && aArgValues.keys().hasElem( ARG_CLASSID.id() ) ) {
      String classId = aArgValues.getByKey( ARG_CLASSID.id() ).singleValue().asString();
      ISkClassInfo classInfo = sysdescr.findClassInfo( classId );
      if( classInfo == null ) {
        return IList.EMPTY;
      }
      IStridablesList<IDtoAttrInfo> attrInfos = classInfo.attrs().list();
      IListEdit<IPlexyValue> values = new ElemLinkedList<>();
      // Значение '*'
      IAtomicValue attrValue = AvUtils.avStr( MULTI );
      IPlexyValue plexyValue = pvSingleValue( attrValue );
      values.add( plexyValue );
      for( IDtoAttrInfo attrInfo : attrInfos ) {
        attrValue = AvUtils.avStr( attrInfo.id() );
        plexyValue = pvSingleValue( attrValue );
        values.add( plexyValue );
      }
      return values;
    }
    return IList.EMPTY;
  }
}
