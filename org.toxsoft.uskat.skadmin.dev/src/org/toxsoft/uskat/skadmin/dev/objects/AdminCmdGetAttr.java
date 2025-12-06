package org.toxsoft.uskat.skadmin.dev.objects;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.dev.objects.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.dev.objects.IAdminHardResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.legacy.plexy.*;
import org.toxsoft.uskat.skadmin.core.*;
import org.toxsoft.uskat.skadmin.core.impl.*;

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
    // Исполнитель uskat-потоков
    ITsThreadExecutor threadExecutor = SkThreadExecutorService.getExecutor( coreApi );
    // Чтение атрибутов
    threadExecutor.syncExec( () -> {
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
    } );
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    IPlexyValue pxCoreApi = contextParamValueOrNull( CTX_SK_CORE_API );
    if( pxCoreApi == null ) {
      return IList.EMPTY;
    }
    IListEdit<IPlexyValue> retValues = new ElemArrayList<>();
    // Исполнитель uskat-потоков
    ITsThreadExecutor threadExecutor = SkThreadExecutorService.getExecutor( pxCoreApi.singleRef() );
    // Определение допускаемых значений
    threadExecutor.syncExec( () -> {
      ISkCoreApi coreApi = (ISkCoreApi)pxCoreApi.singleRef();
      ISkSysdescr sysdescr = coreApi.sysdescr();
      ISkObjectService objService = coreApi.objService();
      if( aArgId.equals( ARG_CLASSID.id() ) ) {
        // Список всех классов
        IStridablesList<ISkClassInfo> classInfos = sysdescr.listClasses();
        // Подготовка списка возможных значений
        for( int index = 0, n = classInfos.size(); index < n; index++ ) {
          IAtomicValue dataValue = AvUtils.avStr( classInfos.get( index ).id() );
          IPlexyValue plexyValue = pvSingleValue( dataValue );
          retValues.add( plexyValue );
        }
      }
      if( (aArgId.equals( ARG_STRID.id() ) && aArgValues.keys().hasElem( ARG_CLASSID.id() )) ) {
        // Идентификатор класса
        String classId = aArgValues.getByKey( ARG_CLASSID.id() ).singleValue().asString();
        // Список всех объектов с учетом наследников
        ISkidList objList = objService.listSkids( classId, true );
        // Значение '*'
        IAtomicValue dataValue = AvUtils.avStr( MULTI );
        IPlexyValue plexyValue = pvSingleValue( dataValue );
        retValues.add( plexyValue );
        for( int index = 0, n = objList.size(); index < n; index++ ) {
          dataValue = AvUtils.avStr( objList.get( index ).strid() );
          plexyValue = pvSingleValue( dataValue );
          retValues.add( plexyValue );
        }
      }
      if( aArgId.equals( ARG_ATTRID.id() ) && aArgValues.keys().hasElem( ARG_CLASSID.id() ) ) {
        String classId = aArgValues.getByKey( ARG_CLASSID.id() ).singleValue().asString();
        ISkClassInfo classInfo = sysdescr.findClassInfo( classId );
        if( classInfo == null ) {
          return;
        }
        IStridablesList<IDtoAttrInfo> attrInfos = classInfo.attrs().list();
        // Значение '*'
        IAtomicValue attrValue = AvUtils.avStr( MULTI );
        IPlexyValue plexyValue = pvSingleValue( attrValue );
        retValues.add( plexyValue );
        for( IDtoAttrInfo attrInfo : attrInfos ) {
          attrValue = AvUtils.avStr( attrInfo.id() );
          plexyValue = pvSingleValue( attrValue );
          retValues.add( plexyValue );
        }
      }
    } );
    return retValues;
  }
}
