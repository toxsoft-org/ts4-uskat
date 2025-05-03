package org.toxsoft.uskat.ws.conn.mws.e4.processors;

import static org.toxsoft.core.tsgui.graphics.icons.ITsStdIconIds.*;
import static org.toxsoft.core.tsgui.mws.IMwsCoreConstants.*;
import static org.toxsoft.uskat.ws.conn.mws.ISkWsConnConstants.*;

import java.util.*;

import org.eclipse.e4.core.contexts.*;
import org.eclipse.e4.ui.model.application.*;
import org.eclipse.e4.ui.model.application.commands.*;
import org.eclipse.e4.ui.model.application.ui.*;
import org.eclipse.e4.ui.model.application.ui.basic.*;
import org.eclipse.e4.ui.model.application.ui.menu.*;
import org.eclipse.e4.ui.workbench.modeling.*;
import org.toxsoft.core.tsgui.mws.osgi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.ws.conn.mws.*;

import jakarta.annotation.*;

/**
 * Процессор модуля.
 *
 * @author goga
 */
public class ProcessorWsConnMws {

  // DOTO вынести insertFirstXXX() (и дрегие???) методы в базовый класс

  MTrimmedWindow window;
  EModelService  modelService;
  MApplication   application;

  @PostConstruct
  void init( IEclipseContext aAppContext ) {
    application = aAppContext.get( MApplication.class );
    modelService = aAppContext.get( EModelService.class );
    window = findElement( modelService, application, MWSID_WINDOW_MAIN, MTrimmedWindow.class, EModelService.ANYWHERE );
    TsInternalErrorRtException.checkNull( window );
    initMainMenu();
    initProjectToolbar();
  }

  /**
   * Tuines application main menu.
   */
  private void initMainMenu() {
    MMenu menuServer =
        findElement( modelService, window, MWSID_MENU_MAIN_SERVER, MMenu.class, EModelService.IN_MAIN_MENU );
    MMenu menuFile = findElement( modelService, window, MWSID_MENU_MAIN_FILE, MMenu.class, EModelService.IN_MAIN_MENU );
    if( isFileMenuAlwaysUsed() && menuFile != null ) {
      initAsFileMenu( menuFile );
      return;
    }
    if( menuServer != null ) {
      initAsServerMenu( menuServer );
      return;
    }
    if( menuFile != null ) {
      initAsFileMenu( menuFile );
      return;
    }
  }

  private void initAsFileMenu( MMenu aMenu ) {
    if( isMultiConfig() ) {
      insertFirstMenuSeparator( aMenu );
      MMenu subMenu = modelService.createModelElement( MMenu.class );
      aMenu.getChildren().add( 0, subMenu );
      subMenu.setLabel( Messages.STR_L_SUBMENU_CONFIGS );
      subMenu.setTooltip( Messages.STR_P_SUBMENU_CONFIGS );
      subMenu.setIconURI( tsguiIconUri( ICONID_TRANSPARENT ) );
      insertFirstMenuItem( subMenu, CMDID_S5CONN_INFO, tsguiIconUri( ICONID_DIALOG_INFORMATION ) );
      insertFirstMenuSeparator( subMenu );
      insertFirstMenuItem( subMenu, CMDID_S5CONN_EXPORT, tsguiIconUri( ICONID_TRANSPARENT ) );
      insertFirstMenuItem( subMenu, CMDID_S5CONN_IMPORT, tsguiIconUri( ICONID_TRANSPARENT ) );
      insertFirstMenuSeparator( subMenu );
      insertFirstMenuItem( subMenu, CMDID_S5CONN_EDIT, tsguiIconUri( ICONID_TRANSPARENT ) );
      insertFirstMenuSeparator( subMenu );
      insertFirstMenuItem( subMenu, CMDID_S5CONN_SELECT, tsguiIconUri( ICONID_TRANSPARENT ) );
      insertFirstMenuSeparator( aMenu );
    }
    insertFirstMenuItem( aMenu, CMDID_S5CONN_CLOSE, pluginIconUri( ICONID_USKAT_DISCONNECT ) );
    insertFirstMenuItem( aMenu, CMDID_S5CONN_OPEN, pluginIconUri( ICONID_USKAT_CONNECT ) );
  }

  private void initAsServerMenu( MMenu aMenu ) {
    // Инициализация меню
    insertFirstMenuItem( aMenu, CMDID_S5CONN_INFO, tsguiIconUri( ICONID_DIALOG_INFORMATION ) );
    insertFirstMenuSeparator( aMenu );
    if( isMultiConfig() ) {
      insertFirstMenuItem( aMenu, CMDID_S5CONN_EXPORT, tsguiIconUri( ICONID_TRANSPARENT ) );
      insertFirstMenuItem( aMenu, CMDID_S5CONN_IMPORT, tsguiIconUri( ICONID_TRANSPARENT ) );
      insertFirstMenuItem( aMenu, CMDID_S5CONN_EDIT, tsguiIconUri( ICONID_TRANSPARENT ) );
      insertFirstMenuSeparator( aMenu );
    }
    insertFirstMenuItem( aMenu, CMDID_S5CONN_CLOSE, pluginIconUri( ICONID_USKAT_DISCONNECT ) );
    if( isMultiConfig() ) {
      insertFirstMenuItem( aMenu, CMDID_S5CONN_SELECT, tsguiIconUri( ICONID_TRANSPARENT ) );
    }
    insertFirstMenuItem( aMenu, CMDID_S5CONN_OPEN, pluginIconUri( ICONID_USKAT_CONNECT ) );
  }

  void insertFirstMenuSeparator( MMenu aMenu ) {
    aMenu.getChildren().add( 0, modelService.createModelElement( MMenuSeparator.class ) );
  }

  void insertFirstMenuItem( MMenu aMenu, String aCmdId, String aIconUri ) {
    MHandledMenuItem mItem = modelService.createModelElement( MHandledMenuItem.class );
    MCommand cmd = findElement( modelService, application, aCmdId, MCommand.class, EModelService.ANYWHERE );
    mItem.setCommand( cmd );
    mItem.setIconURI( aIconUri );
    aMenu.getChildren().add( 0, mItem );
  }

  /**
   * Инициализация панели инструментоы.
   * <p>
   * Добавляет кнопки работы с проектом в панель инструментов. Если в есть панель
   * {@link IMwsGuiConstants#MWSID_TOOLBAR_S5SERVER}, то кнопки добавляются туда. Иначе, кнопки добавляются в
   * обязательное подменю {@link IMwsGuiConstants#MWSID_TOOLBAR_MAIN}.
   */
  private void initProjectToolbar() {
    // найдем (если есть) панель инструментов, куда надо добавить команды работы с проектом
    MToolBar tbProject =
        findElement( modelService, window, MWSID_TOOLBAR_S5SERVER, MToolBar.class, EModelService.IN_TRIM );
    if( tbProject == null ) {
      tbProject = findElement( modelService, window, MWSID_TOOLBAR_MAIN, MToolBar.class, EModelService.IN_TRIM );
      if( tbProject == null ) {
        return;
      }
    }
    // добавим команды
    insertFirstToolbarItem( tbProject, CMDID_S5CONN_CLOSE, pluginIconUri( ICONID_USKAT_DISCONNECT ) );
    insertFirstToolbarItem( tbProject, CMDID_S5CONN_OPEN, pluginIconUri( ICONID_USKAT_CONNECT ) );
  }

  void insertFirstToolbarItem( MToolBar aToolbar, String aCmdId, String aIconUri ) {
    MHandledToolItem tItem = modelService.createModelElement( MHandledToolItem.class );
    MCommand cmd = findElement( modelService, application, aCmdId, MCommand.class, EModelService.ANYWHERE );
    tItem.setCommand( cmd );
    tItem.setIconURI( aIconUri );
    aToolbar.getChildren().add( 0, tItem );
  }

  private static boolean isMultiConfig() {
    IMwsOsgiService mws = Activator.getInstance().getOsgiService( IMwsOsgiService.class );
    return IMwsModuleS5ConnConstants.USE_MUTLI_CONFIGS.getValue( mws.context().params() ).asBool();
  }

  boolean isFileMenuAlwaysUsed() {
    IMwsOsgiService mws = Activator.getInstance().getOsgiService( IMwsOsgiService.class );
    return IMwsModuleS5ConnConstants.ALWAYS_USE_FILE_MENU.getValue( mws.context().params() ).asBool();
  }

  @SuppressWarnings( "nls" )
  String pluginIconUri( String aIconId ) {
    // пример: "platform:/plugin/ru.toxsoft.mws.app.main/icons/is48x48/ts-logo.png";
    return "platform:/plugin/" + Activator.PLUGIN_ID + "/icons/is16x16/" + aIconId + ".png";
  }

  @SuppressWarnings( "nls" )
  String tsguiIconUri( String aIconId ) {
    return "platform:/plugin/" + org.toxsoft.core.tsgui.Activator.PLUGIN_ID + "/icons/is16x16/" + aIconId + ".png";
  }

  @SuppressWarnings( "rawtypes" )
  <T> T findElement( EModelService aModelService, MElementContainer aRoot, String aId, Class<T> aClass, int aFlags ) {
    ElementMatcher matcher = new ElementMatcher( aId, aClass, (String)null );
    List<T> elems = aModelService.findElements( aRoot, aClass, aFlags, matcher );
    if( elems.isEmpty() ) {
      return null;
    }
    return elems.get( 0 );
  }

}
