package org.toxsoft.uskat.s5.server.startup;

import static org.toxsoft.uskat.s5.utils.S5ManifestUtils.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.uskat.s5.common.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;
import org.toxsoft.uskat.s5.server.backend.addons.classes.*;
import org.toxsoft.uskat.s5.server.backend.addons.clobs.*;
import org.toxsoft.uskat.s5.server.backend.addons.commands.*;
import org.toxsoft.uskat.s5.server.backend.addons.events.*;
import org.toxsoft.uskat.s5.server.backend.addons.gwiddb.*;
import org.toxsoft.uskat.s5.server.backend.addons.links.*;
import org.toxsoft.uskat.s5.server.backend.addons.objects.*;
import org.toxsoft.uskat.s5.server.backend.addons.queries.*;
import org.toxsoft.uskat.s5.server.backend.addons.rtdata.*;
import org.toxsoft.uskat.s5.server.sequences.*;

/**
 * Начальная, неизменяемая, проектно-зависимая конфигурация реализации бекенда сервера uskat
 *
 * @author mvk
 */
public class S5UskatServer
    extends S5InitialImplementation {

  /**
   * Идентификатор сервера
   */
  public static final String SERVER_ID = "org.toxsoft.uskat.s5.server"; //$NON-NLS-1$

  /**
   * Идентификатор узла сервера
   */
  public static final String SERVER_NODE_ID = "org.toxsoft.uskat.server"; //$NON-NLS-1$

  /**
   * Имя модуля реализующего skat-s5 сервер
   */
  public static final String SERVER_NAME = "uskat server"; //$NON-NLS-1$

  /**
   * Описание модуля реализующего skat-s5 сервер
   */
  public static final String SERVER_DESCR = "uskat server"; //$NON-NLS-1$

  /**
   * Версия сервера
   */
  public static final TsVersion SERVER_VERSION = new TsVersion( 4, 1, getBuildTime() );

  /**
   * Конструктор
   */
  public S5UskatServer() {
    super( SERVER_ID, SERVER_NODE_ID, new S5Module( SERVER_ID, SERVER_NAME, SERVER_DESCR, SERVER_VERSION ) );
  }

  // ------------------------------------------------------------------------------------
  // S5InitialImplementation
  //
  @Override
  protected IOptionSet doProjectSpecificParams() {
    return super.doProjectSpecificParams();
  }

  @Override
  protected IStridablesList<IS5BackendAddonCreator> doProjectSpecificBaCreators() {
    return new StridablesList<>( //
        new S5BaClobsCreator(), //
        new S5BaGwidDbCreator(), //
        new S5BaClassesCreator(), //
        new S5BaObjectsCreator(), //
        new S5BaLinksCreator(), //
        new S5BaEventsCreator(), //
        new S5BaCommandsCreator(), //
        new S5BaRtdataCreator(), //
        new S5BaQueriesCreator() //
    );
  }

  @Override
  protected IOptionSet doProjectSpecificCreateClassParams( String aClassId ) {
    return super.doProjectSpecificCreateClassParams( aClassId );
  }

  @Override
  protected IS5SequenceImplementation doFindHistDataImplementation( Gwid aGwid, EAtomicType aType, boolean aSync ) {
    return super.doFindHistDataImplementation( aGwid, aType, aSync );
  }

  @Override
  protected IList<IS5SequenceImplementation> doGetHistDataImplementations() {
    return super.doGetHistDataImplementations();
  }

}
