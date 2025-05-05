package org.toxsoft.uskat.ws.conn.mws.e4.handlers;

import static org.toxsoft.core.tsgui.dialogs.datarec.ITsDialogConstants.*;
import static org.toxsoft.uskat.core.gui.conn.m5.IConnectionConfigM5Constants.*;
import static org.toxsoft.uskat.ws.conn.mws.l10n.ISkWsConnSharedResources.*;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.dialogs.datarec.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.uskat.core.gui.conn.cfg.*;

/**
 * Вспомогательные (общие) методы выполнения команд.
 *
 * @author hazard157
 * @author mvk
 */
public class HandlerUtils {

  static void editCfgs( IEclipseContext aEclipseContext ) {
    ITsGuiContext ctx = new TsGuiContext( aEclipseContext );
    IM5Domain m5 = ctx.get( IM5Domain.class );
    IM5Model<IConnectionConfig> model = m5.getModel( MID_SK_CONN_CFG, IConnectionConfig.class );
    IConnectionConfigService ccService = ctx.get( IConnectionConfigService.class );
    IM5LifecycleManager<IConnectionConfig> lm = model.getLifecycleManager( ccService );
    TsDialogInfo cdi = new TsDialogInfo( ctx, DLG_EDIT_CONFIGS, DLG_EDIT_CONFIGS_D, DF_NO_APPROVE );
    cdi.setMinSizeShellRelative( 10, 50 );
    cdi.setMaxSizeShellRelative( 50, 80 );
    IM5CollectionPanel<IConnectionConfig> panel =
        model.panelCreator().createCollEditPanel( ctx, lm.itemsProvider(), lm );
    M5GuiUtils.showCollPanel( cdi, panel );
  }

  // static IConnectionConfig selectCfg( IEclipseContext aEclipseContext ) {
  // Shell shell = aEclipseContext.get( Shell.class );
  // IM5Domain m5 = aEclipseContext.get( IM5Domain.class );
  // IM5Model<IConnectionConfig> model = m5.getModel( ConnectionConfigM5Model.MODEL_ID, IConnectionConfig.class );
  // IM5LifecycleManager<IConnectionConfig> lm = model.getDefaultLifecycleManager();
  // CommonDialogInfo cdi = new CommonDialogInfo( shell, DLG_C_SELECT_CFG, DLG_T_SELECT_CFG );
  // cdi.setMinSizeShellRelative( 10, 50 );
  // cdi.setMaxSizeShellRelative( 50, 80 );
  // return M5GuiUtils.askSelectItem( aEclipseContext, model, null, cdi, lm.itemsProvider(), null );
  // }
  //
  // static IConnectionConfig selectEditCfg( IEclipseContext aEclipseContext ) {
  // Shell shell = aEclipseContext.get( Shell.class );
  // IM5Domain m5 = aEclipseContext.get( IM5Domain.class );
  // IM5Model<IConnectionConfig> model = m5.getModel( ConnectionConfigM5Model.MODEL_ID, IConnectionConfig.class );
  // IM5LifecycleManager<IConnectionConfig> lm = model.getDefaultLifecycleManager();
  // CommonDialogInfo cdi = new CommonDialogInfo( shell, DLG_C_SELECT_CFG, DLG_T_SELECT_CFG );
  // cdi.setMinSizeShellRelative( 10, 50 );
  // cdi.setMaxSizeShellRelative( 50, 80 );
  // return M5GuiUtils.askSelectItem( aEclipseContext, model, null, cdi, lm.itemsProvider(), lm );
  // }
  //
  // static void openConnection( IEclipseContext aEclipseContext, IConnectionConfig aCfg, ILoginInfo
  // aForcedLoginInfoOrNull
  // ) {
  // Shell shell = aEclipseContext.get( Shell.class );
  // ISkConnection skConn = aEclipseContext.get( ISkConnection.class );
  // ITsModularWorkstationService workstationService = aEclipseContext.get( ITsModularWorkstationService.class );
  // IMwsConnectionConfigService connCfgService = aEclipseContext.get( IMwsConnectionConfigService.class );
  //
  // // Контекст модульного приложения
  // ITsContext mwsContext = workstationService.mwsContext();
  // // Параметры модуля
  // IOptionSet mwsParam = mwsContext.params();
  //
  // // проверка конфигурации
  // ValidationResult vr = aCfg.validate();
  // if( TsDialogUtils.askContinueOnValidation( shell, vr, MSG_ASK_CONNECT_ON_WARN ) != ETsDialogCode.YES ) {
  // return;
  // }
  // // запросим логин/пароль, если нужно
  // ILoginInfo loginInfo;
  // if( aForcedLoginInfoOrNull == null ) {
  // // dima 14.10.22 по приказу Синько, по заказу графистов ММ
  // // ILoginInfo initialLoginInfo = DEFAULT_LOGIN_INFO.getValue( mwsParam );
  // ILoginInfo initialLoginInfo = LoginInfo.NONE;
  // loginInfo = PanelSystemLogin.askLoginInfo( shell, initialLoginInfo );
  // if( loginInfo == null ) {
  // return;
  // }
  // }
  // else {
  // loginInfo = aForcedLoginInfoOrNull;
  // }
  // String apiInitializator = EXT_SERV_PROVIDER_CLASS.getValue( aCfg.params() ).asString();
  // S5HostOptionList hosts = new S5HostOptionList();
  // String[] hostnames = HOST_NAME.getValue( aCfg.params() ).asString().split( "," ); //$NON-NLS-1$
  // String[] ports = PORT_NO.getValue( aCfg.params() ).asString().split( "," ); //$NON-NLS-1$
  // for( int index = 0, n = hostnames.length; index < n; index++ ) {
  // hosts.add( new S5HostOptionValue( hostnames[index], Integer.parseInt( ports[index] ) ) );
  // }
  // // Параметры подключения
  // IAtomicValue createTimeout = CREATE_TIMEOUT.getValue( aCfg.params() );
  // IAtomicValue failureTimeout = FAILURE_TIMEOUT.getValue( aCfg.params() );
  // final Display display = Display.getCurrent();
  // // Таймаут задачи разделения потоков
  // int timeout = SK_BACKEND_SEPARATOR_DOJOB_TIMEOUT.getValue( mwsParam ).asInt();
  //
  // // подключение...
  // display.asyncExec( () -> S5ClientUtils.createProgressMonitorDialog( shell, MSG_CONNECTING_TO_SERVER,
  // new IS5RunnableWithProgress() {
  //
  // @Override
  // public void run( IS5ProgressMonitor aMonitor ) {
  // try {
  // // Создание задачи разделения потоков
  // SkSeparatorTask separatorTask = new SkSeparatorTask( display, timeout );
  // // Открытие соединения
  // ITsContext ctx = new TsContext();
  // // сначала откроем соединение
  // SkUtils.REF_BACKEND_PROVIDER.setValue( ctx, new S5RemoteBackendProvider() );
  // SkUtils.OP_LOGIN.setValue( ctx.params(), avStr( loginInfo.loginName() ) );
  // SkUtils.OP_PASSWORD.setValue( ctx.params(), avStr( loginInfo.password() ) );
  // IS5RemoteBackendHardConstants.REF_S5BACKEND_CLASSLOADER.setValue( ctx,
  // AddonMwsModuleS5Conn.class.getClassLoader() );
  // IS5RemoteBackendHardConstants.REF_S5BACKEND_PROGRESS_MONITOR.setValue( ctx, aMonitor );
  // IS5RemoteBackendHardConstants.REF_S5BACKEND_HOSTS.setValue( ctx, hosts );
  // IS5RemoteBackendHardConstants.OP_S5BACKEND_CONNECT_TIMEOUT.setValue( ctx.params(), createTimeout );
  // IS5RemoteBackendHardConstants.OP_S5BACKEND_FAILURE_TIMEOUT.setValue( ctx.params(), failureTimeout );
  // SkUtils.OP_EXT_SERV_PROVIDER_CLASS.setValue( ctx.params(), avStr( apiInitializator ) );
  // SkBackendThreadSeparator.REF_BACKEND_THREAD_SEPARATOR.setValue( ctx, separatorTask.backendSeparator() );
  //
  // IS5RemoteBackendHardConstants.OP_S5BACKEND_CLIENT_PROGRAM.setValue( ctx.params(),
  // PROGRAM_NAME.getValue( mwsParam ) );
  // IS5RemoteBackendHardConstants.OP_S5BACKEND_CLIENT_VERSION.setValue( ctx.params(),
  // dvValobj( PROGRAM_VERSION.getValue( mwsParam ) ) );
  //
  // skConn.open( ctx );
  // // Запуск задачи разделения потоков
  // separatorTask.start( skConn );
  // // Установка имени соединения
  // connCfgService.setLastConfigName( aCfg.name() );
  // }
  // catch( RuntimeException ex ) {
  // LoggerUtils.errorLogger().error( ex );
  // // Соединение не было создано, но было активировано. Снимаем активацию
  // skConn.close();
  // S5ClientUtils.error( display, shell, cause( ex ) );
  // }
  // }
  // } ) );
  // }
  //
  // /**
  // * Инициализирует соединение с севрером после старта программы.
  // *
  // * @param aWinContext {@link IEclipseContext} - контекст уровня окна приложения
  // */
  // public static void processAutoConnection( IEclipseContext aWinContext ) {
  // TsNullArgumentRtException.checkNull( aWinContext );
  // ITsModularWorkstationService mws = aWinContext.get( ITsModularWorkstationService.class );
  // IMwsConnectionConfigService ccService = aWinContext.get( IMwsConnectionConfigService.class );
  // // определим стратегию авто-соединения
  // IConnectionConfig cfg = null;
  // EOpenCmdStartegy openStartegy = OPEN_CMD_STRATEGY.getValue( mws.mwsContext().params() );
  // switch( openStartegy ) {
  // case ALWAYS_ASK: {
  // // оставим cfg = null, чтобы запросить вручную
  // break;
  // }
  // case OPEN_DEFAULT: {
  // String cname = ccService.getDefaultConfigName();
  // cfg = ccService.configs().findByKey( cname );
  // break;
  // }
  // case OPEN_LAST: {
  // String cname = ccService.getLastConfigName();
  // cfg = ccService.configs().findByKey( cname );
  // break;
  // }
  // default:
  // throw new TsNotAllEnumsUsedRtException();
  // }
  // // не найдена последняя или умолчательная конфигурация, или запрошен ручной выбор? - запросим ручной ввод
  // if( cfg == null ) {
  // cfg = selectEditCfg( aWinContext );
  // // если пользователь отказался от подключения, просто выходим, без открытия соединения
  // if( cfg == null ) {
  // return;
  // }
  // }
  // // запомним принятое решение
  // switch( openStartegy ) {
  // case ALWAYS_ASK:
  // case OPEN_LAST: {
  // ccService.setLastConfigName( cfg.name() );
  // break;
  // }
  // case OPEN_DEFAULT: {
  // ccService.setDefaultConfigName( cfg.name() );
  // break;
  // }
  // default:
  // throw new TsNotAllEnumsUsedRtException();
  // }
  // // собственно, откроем соединение после старта программы
  // Display display = aWinContext.get( Display.class );
  // IConnectionConfig connConfig = cfg;
  // ILoginInfo loginInfo = DEFAULT_LOGIN_INFO.getValue( mws.mwsContext().params(), DEFAULT_LOGIN_INFO.defaultValue() );
  // display.asyncExec( () -> openConnection( aWinContext, connConfig, loginInfo ) );
  // }

  /**
   * No subclasses.
   */
  private HandlerUtils() {
    // nop
  }

}
