package org.toxsoft.uskat.classes.impl;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.classes.impl.IS5Resources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.s5.server.*;
import org.toxsoft.uskat.s5.server.statistics.*;

/**
 * Константы пакета
 *
 * @author mvk
 */
public class S5ClassUtils {

  // ------------------------------------------------------------------------------------
  // {@link ISkServerNode}
  //
  /**
   * Связь {@link ISkServerNode#LNKID_SERVER}: Сервер/кластер, в рамках которого работает узел {@link ISkServer}
   * <p>
   * Классы объектов связи: {@link ISkServer#CLASS_ID}.
   */
  private static final DtoLinkInfo LNKINF_NODE_SERVER = DtoLinkInfo.create1( //
      ISkServerNode.LNKID_SERVER, //
      new SingleStringList( ISkServer.CLASS_ID ), //
      new CollConstraint( 1, true, true, true ), //
      OptionSetUtils.createOpSet( //
          DDEF_NAME, STR_N_LINK_NODE_SERVER, //
          DDEF_DESCRIPTION, STR_D_LINK_NODE_SERVER ) );

  /**
   * Данное: с узлом сервера установлена связь.
   * <p>
   * Тип: {@link EAtomicType#BOOLEAN}
   */
  private static final DtoRtdataInfo RTDINF_NODE_ONLINE = DtoRtdataInfo.create2( //
      ISkServerNode.RTDID_ONLINE, //
      DataType.create( BOOLEAN, //
          TSID_FORMAT_STRING, FMT_BOOL_CHECK, //
          TSID_DEFAULT_VALUE, AV_FALSE //
      ), //
      true, true, false, 1000, //
      TSID_NAME, STR_RTD_ONLINE, //
      TSID_DESCRIPTION, STR_RTD_ONLINE_D //
  );

  /**
   * Данное: интегральная оценка состояния подключенных к узлу ресурсов. 0 - нет связи, 100 - все подключено и работает.
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  private static final DtoRtdataInfo RTDINF_NODE_HEALTH = DtoRtdataInfo.create2( //
      ISkServerNode.RTDID_HEALTH, //
      DataType.create( INTEGER, //
          TSID_DEFAULT_VALUE, AV_0 //
      ), //
      true, true, false, 1000, //
      TSID_NAME, STR_RTD_HEALTH, //
      TSID_DESCRIPTION, STR_RTD_HEALTH_D //
  );

  // ------------------------------------------------------------------------------------
  // {@link ISkServerBackend}
  //
  /**
   * Связь {@link ISkServerBackend#LNKID_NODE}: Узел кластера, в рамках которого работает бекенд {@link ISkServerNode}
   * <p>
   * Классы объектов связи: {@link ISkServer#CLASS_ID}.
   */
  private static final DtoLinkInfo LNKINF_BACKEND_NODE = DtoLinkInfo.create1( //
      ISkServerBackend.LNKID_NODE, //
      new SingleStringList( ISkServerNode.CLASS_ID ), //
      new CollConstraint( 1, true, true, true ), //
      OptionSetUtils.createOpSet( //
          DDEF_NAME, STR_N_LINK_BACKEND_NODE, //
          DDEF_DESCRIPTION, STR_D_LINK_BACKEND_NODE //
      ) );

  // ------------------------------------------------------------------------------------
  // {@link ISkServerHistorable}
  //
  // ------------------------------------------------------------------------------------
  //
  //
  /**
   * Обновление описания sk-классов
   *
   * @param aCoreApi {@link ISkCoreApi} ядро сервера
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static void updateSkClasses( ISkCoreApi aCoreApi ) {
    TsNullArgumentRtException.checkNull( aCoreApi );
    ISkSysdescr cm = aCoreApi.sysdescr();
    // ISkServer
    DtoClassInfo sci = new DtoClassInfo( //
        ISkServer.CLASS_ID, //
        IGwHardConstants.GW_ROOT_CLASS_ID, //
        OptionSetUtils.createOpSet( //
            DDEF_NAME, STR_N_CLASS_SERVER, //
            DDEF_DESCRIPTION, STR_D_CLASS_SERVER //
        ) );
    cm.defineClass( sci );

    // ISkServerNode
    sci = new DtoClassInfo( //
        ISkServerNode.CLASS_ID, //
        IGwHardConstants.GW_ROOT_CLASS_ID, //
        OptionSetUtils.createOpSet( //
            DDEF_NAME, STR_N_CLASS_NODE, //
            DDEF_DESCRIPTION, STR_D_CLASS_NODE //
        ) );
    sci.linkInfos().addAll( //
        LNKINF_NODE_SERVER //
    );
    sci.rtdataInfos().addAll( //
        RTDINF_NODE_ONLINE, //
        RTDINF_NODE_HEALTH //
    );
    sci.rtdataInfos().addAll( listStatRtdInfos( IS5ServerHardConstants.STAT_BACKEND_NODE_PARAMS ) );
    cm.defineClass( sci );

    // ISkServerBackend
    sci = new DtoClassInfo( //
        ISkServerBackend.CLASS_ID, //
        IGwHardConstants.GW_ROOT_CLASS_ID, //
        OptionSetUtils.createOpSet( //
            DDEF_NAME, STR_N_CLASS_BACKEND, //
            DDEF_DESCRIPTION, STR_D_CLASS_BACKEND //
        ) );
    sci.linkInfos().addAll( //
        LNKINF_BACKEND_NODE //
    );
    cm.defineClass( sci );

    // ISkServerHistorable
    sci = new DtoClassInfo( //
        ISkServerHistorable.CLASS_ID, //
        ISkServerBackend.CLASS_ID, OptionSetUtils.createOpSet( //
            DDEF_NAME, STR_N_CLASS_HISTORABLE_BACKEND, //
            DDEF_DESCRIPTION, STR_D_CLASS_HISTORABLE_BACKEND //
        ) );
    sci.rtdataInfos().addAll( listStatRtdInfos( IS5ServerHardConstants.STAT_HISTORABLE_BACKEND_PARAMS ) );
    cm.defineClass( sci );
  }

  /**
   * Регистрация конструкторов объектов uskat/metro
   *
   * @param aCoreApi {@link ISkCoreApi} API сервера
   * @throws TsNullArgumentRtException аргумент = null;
   */
  public static void registerObjectCreators( ISkCoreApi aCoreApi ) {
    TsNullArgumentRtException.checkNull( aCoreApi );
    // Служба объектов
    ISkObjectService os = aCoreApi.objService();
    // Регистрация создателей объектов
    os.registerObjectCreator( ISkServer.CLASS_ID, SkServer.CREATOR );
    os.registerObjectCreator( ISkServerNode.CLASS_ID, SkServerNode.CREATOR );
    os.registerObjectCreator( ISkServerBackend.CLASS_ID, SkServerBackend.CREATOR );
    os.registerObjectCreator( ISkServerHistorable.CLASS_ID, SkServerHistorable.CREATOR );
  }

  /**
   * Проверяет, если необходимо добавляет в класс указанного объекта, указанные параметры статистики
   *
   * @param aStatInfos {@link IStridablesList} список описаний параметров статистики
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static IList<IDtoRtdataInfo> listStatRtdInfos( IStridablesList<S5StatisticParamInfo> aStatInfos ) {
    TsNullArgumentRtException.checkNulls( aStatInfos );
    IListEdit<IDtoRtdataInfo> retValue = new ElemLinkedList<>();
    for( S5StatisticParamInfo statInfo : aStatInfos ) {
      for( IS5StatisticInterval interval : statInfo.intervals() ) {
        String dataId = S5StatisticWriter.getDataId( interval, statInfo.id() );
        // Описание базового данного
        IDtoRtdataInfo info = DtoRtdataInfo.create1( //
            dataId, new DataType( statInfo.atomicType() ), true, true, true, interval.milli(), //
            OptionSetUtils.createOpSet( //
                DDEF_NAME, statInfo.nmName() + '(' + interval.nmName() + ')', //
                DDEF_DESCRIPTION, statInfo.description() + '(' + statInfo.description() + ')' ) //
        );
        retValue.add( info );
      }
    }
    return retValue;
  }
}
