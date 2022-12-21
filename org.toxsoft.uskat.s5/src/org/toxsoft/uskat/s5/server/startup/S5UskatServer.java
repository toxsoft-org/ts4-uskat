package org.toxsoft.uskat.s5.server.startup;

import static org.toxsoft.uskat.s5.utils.S5ManifestUtils.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.TsVersion;
import org.toxsoft.uskat.s5.common.S5Module;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonCreator;
import org.toxsoft.uskat.s5.server.backend.addons.classes.S5BaClassesCreator;
import org.toxsoft.uskat.s5.server.backend.addons.clobs.S5BaClobsCreator;
import org.toxsoft.uskat.s5.server.backend.addons.commands.S5BaCommandsCreator;
import org.toxsoft.uskat.s5.server.backend.addons.events.S5BaEventsCreator;
import org.toxsoft.uskat.s5.server.backend.addons.gwiddb.S5BaGwidDbCreator;
import org.toxsoft.uskat.s5.server.backend.addons.links.S5BaLinksCreator;
import org.toxsoft.uskat.s5.server.backend.addons.objects.S5BaObjectsCreator;
import org.toxsoft.uskat.s5.server.backend.addons.queries.S5BaQueriesCreator;
import org.toxsoft.uskat.s5.server.backend.addons.rtdata.S5BaRtdataCreator;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceImplementation;

/**
 * Начальная, неизменяемая, проектно-зависимая конфигурация реализации бекенда сервера uskat
 *
 * @author mvk
 */
public class S5UskatServer
    extends S5InitialImplementation {

  /**
   * Идентификатор модуля реализующего сервер
   */
  public static final String SERVER_ID = "org.toxsoft.uskat.s5.server"; //$NON-NLS-1$

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
    super( new S5Module( SERVER_ID, SERVER_NAME, SERVER_DESCR, SERVER_VERSION ) );
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
