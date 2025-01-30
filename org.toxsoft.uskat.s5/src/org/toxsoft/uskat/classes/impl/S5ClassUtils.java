package org.toxsoft.uskat.classes.impl;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.classes.impl.IS5Resources.*;

import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.impl.dto.*;

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
   * Создание классов s5
   *
   * @param aCoreApi {@link ISkCoreApi} ядро сервера
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static void createS5Classes( ISkCoreApi aCoreApi ) {
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
}
