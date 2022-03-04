package org.toxsoft.uskat.classes.impl;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.classes.IS5ClassNode.*;
import static org.toxsoft.uskat.classes.IS5ClassServer.*;
import static org.toxsoft.uskat.classes.impl.IS5Resources.*;
import static ru.uskat.common.dpu.impl.IDpuHardConstants.*;

import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetUtils;
import org.toxsoft.core.tslib.coll.helpers.CollConstraint;
import org.toxsoft.core.tslib.coll.primtypes.impl.SingleStringList;
import org.toxsoft.core.tslib.gw.IGwHardConstants;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.classes.*;

import ru.uskat.common.dpu.impl.DpuSdClassInfo;
import ru.uskat.common.dpu.impl.DpuSdLinkInfo;
import ru.uskat.core.ISkCoreApi;
import ru.uskat.core.api.objserv.ISkObjectService;
import ru.uskat.core.api.sysdescr.ISkClassInfoManager;

/**
 * Константы пакета
 *
 * @author mvk
 */
public class S5ClassUtils {

  // ------------------------------------------------------------------------------------
  // {@link IS5ClassNode}
  //
  /**
   * Связь {@link IS5ClassNode#LNKID_SERVER}: Сервер/кластер, в рамках которого работает узел {@link IS5ClassServer}
   * <p>
   * Классы объектов связи: {@link IS5ClassServer#CLASS_ID}.
   */
  private static final DpuSdLinkInfo LNKINF_NODE_SERVER = DpuSdLinkInfo.create1( //
      IS5ClassNode.LNKID_SERVER, //
      DDEF_NAME, STR_N_LINK_NODE_SERVER, //
      DDEF_DESCRIPTION, STR_D_LINK_NODE_SERVER, //
      OP_RIGHT_CLASS_IDS, AvUtils.avValobj( new SingleStringList( CLASS_SERVER ) ), //
      OP_LINK_CONSTRAINT, AvUtils.avValobj( new CollConstraint( 1, true, true, true ) )//
  );

  // ------------------------------------------------------------------------------------
  // {@link IS5ClassBackend}
  //
  /**
   * Связь {@link IS5ClassBackend#LNKID_NODE}: Узел кластера, в рамках которого работает бекенд {@link IS5ClassNode}
   * <p>
   * Классы объектов связи: {@link IS5ClassServer#CLASS_ID}.
   */
  private static final DpuSdLinkInfo LNKINF_BACKEND_NODE = DpuSdLinkInfo.create1( //
      IS5ClassBackend.LNKID_NODE, //
      DDEF_NAME, STR_N_LINK_BACKEND_NODE, //
      DDEF_DESCRIPTION, STR_D_LINK_BACKEND_NODE, //
      OP_RIGHT_CLASS_IDS, AvUtils.avValobj( new SingleStringList( CLASS_NODE ) ), //
      OP_LINK_CONSTRAINT, AvUtils.avValobj( new CollConstraint( 1, true, true, true ) )//
  );

  // ------------------------------------------------------------------------------------
  // {@link IS5ClassHistorableBackend}
  //
  // ------------------------------------------------------------------------------------
  //
  //
  /**
   * Создание классов s5
   *
   * @param aCoreApi {@link ISkCoreApi} ядро сервера
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static void createS5Classes( ISkCoreApi aCoreApi ) {
    TsNullArgumentRtException.checkNull( aCoreApi );

    ISkClassInfoManager cm = aCoreApi.sysdescr().classInfoManager();

    // IS5ClassServer
    if( cm.findClassInfo( IS5ClassServer.CLASS_ID ) == null ) {
      DpuSdClassInfo sci = new DpuSdClassInfo( IS5ClassServer.CLASS_ID, IGwHardConstants.GW_ROOT_CLASS_ID );
      sci.params().addAll( OptionSetUtils.createOpSet( //
          DDEF_NAME, STR_N_CLASS_SERVER, //
          DDEF_DESCRIPTION, STR_D_CLASS_SERVER //
      ) );
      cm.defineClass( sci );
    }

    // IS5ClassNode
    if( cm.findClassInfo( IS5ClassNode.CLASS_ID ) == null ) {
      DpuSdClassInfo sci = new DpuSdClassInfo( IS5ClassNode.CLASS_ID, IGwHardConstants.GW_ROOT_CLASS_ID );
      sci.params().addAll( OptionSetUtils.createOpSet( //
          DDEF_NAME, STR_N_CLASS_NODE, //
          DDEF_DESCRIPTION, STR_D_CLASS_NODE //
      ) );
      sci.linkInfos().addAll( //
          LNKINF_NODE_SERVER //
      );
      cm.defineClass( sci );
    }

    // IS5ClassBackend
    if( cm.findClassInfo( IS5ClassBackend.CLASS_ID ) == null ) {
      DpuSdClassInfo sci = new DpuSdClassInfo( IS5ClassBackend.CLASS_ID, IGwHardConstants.GW_ROOT_CLASS_ID );
      sci.params().addAll( OptionSetUtils.createOpSet( //
          DDEF_NAME, STR_N_CLASS_BACKEND, //
          DDEF_DESCRIPTION, STR_D_CLASS_BACKEND //
      ) );
      sci.linkInfos().addAll( //
          LNKINF_BACKEND_NODE //
      );
      cm.defineClass( sci );
    }

    // IS5ClassHistorableBackend
    if( cm.findClassInfo( IS5ClassHistorableBackend.CLASS_ID ) == null ) {
      DpuSdClassInfo sci = new DpuSdClassInfo( IS5ClassHistorableBackend.CLASS_ID, IS5ClassBackend.CLASS_ID );
      sci.params().addAll( OptionSetUtils.createOpSet( //
          DDEF_NAME, STR_N_CLASS_HISTORABLE_BACKEND, //
          DDEF_DESCRIPTION, STR_D_CLASS_HISTORABLE_BACKEND //
      ) );
      cm.defineClass( sci );
    }
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
    os.registerObjectCreator( IS5ClassServer.CLASS_ID, S5ClassServer.CREATOR );
    os.registerObjectCreator( IS5ClassNode.CLASS_ID, S5ClassBackendNode.CREATOR );
    os.registerObjectCreator( IS5ClassBackend.CLASS_ID, S5ClassBackend.CREATOR );
    os.registerObjectCreator( IS5ClassHistorableBackend.CLASS_ID, S5ClassHistorableBackend.CREATOR );
  }
}
