package org.toxsoft.uskat.core.impl;

import static java.lang.String.*;
import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.plugins.*;
import org.toxsoft.core.tslib.utils.plugins.impl.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.devapi.*;

/**
 * Контейнер скатлетов
 *
 * @author mvk
 */
public class SkatletBox
    extends PluginBox<SkatletUnit> {

  /**
   * API поддержки контейнра скателтов
   */
  private ISkatletSupport support;

  /**
   * Контекст контейнера
   */
  private ITsContext context;

  /**
   * Общее, разделяемое между модулями, соединение
   */
  private SharedConnection sharedConnection;

  /**
   * Конструктор
   *
   * @param aId String идентификатор контейнера
   * @param aParams {@link IOptionSet} параметры контейнера
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public SkatletBox( String aId, IOptionSet aParams ) {
    super( aId, aParams );
  }

  // ------------------------------------------------------------------------------------
  // PluginBox
  //
  @Override
  protected synchronized ValidationResult doInit( ITsContextRo aEnviron ) {
    // Клиент обязан разместить в контексте следующие параметры
    support = ISkatlet.REF_SKATLET_SUPPORT.getRef( aEnviron );
    // Создание контекста контейнера
    context = createContext( aEnviron );
    return super.doInit( aEnviron );
  }

  @Override
  protected synchronized boolean doQueryStop() {
    sharedConnection.shutdown();
    return super.doQueryStop();
  }

  @Override
  protected SkatletUnit doCreateUnit( String aPluginId, IPlugin aPlugin ) {
    return new SkatletUnit( aPluginId, params(), aPlugin );
  }

  @Override
  protected IList<IPluginInfo> doBeforeLoadUnits( IList<IPluginInfo> aPluginInfos ) {
    IStringList pluginIdsByOrder = ISkatlet.OPDEF_SKATLETS_LOAD_ORDER.getValue( params() ).asValobj();
    IStringMapEdit<IPluginInfo> source = new StringMap<>();
    for( IPluginInfo pluginInfo : aPluginInfos ) {
      source.put( pluginInfo.pluginId(), pluginInfo );
    }
    ElemArrayList<IPluginInfo> retValue = new ElemArrayList<>();
    // Добавление в результат описания плагинов в указанном порядке
    for( String pluginId : pluginIdsByOrder ) {
      IPluginInfo pluginInfo = source.findByKey( pluginId );
      if( pluginInfo != null ) {
        retValue.add( pluginInfo );
        source.removeByKey( pluginId );
      }
    }
    // Добавления остальных плагинов в конец списка
    retValue.addAll( source.values() );
    return retValue;
  }

  @Override
  protected IList<SkatletUnit> doBeforeRunUnits( IList<SkatletUnit> aUnits ) {
    IListEdit<SkatletUnit> retValue = new ElemLinkedList<>();
    // Инициализация всех скатлетов (регистрация типов и служб будущего соединения)
    for( SkatletUnit unit : aUnits ) {
      ValidationResult result = unit.initialize();
      if( result.isError() ) {
        support.logger().error( FMT_ERR_SKATLET_INITIALIZE, unit.id(), result );
        continue;
      }
      retValue.add( unit );
    }

    // Создание разделяемого соединения и его размещение в контексте
    sharedConnection = new SharedConnection( support.createConnection( id(), new TsContext() ), support.logger() );
    ISkatlet.REF_SHARED_CONNECTION.setRef( context, sharedConnection );

    // Установка контекста скатлетов
    for( SkatletUnit unit : retValue ) {
      unit.setContext( context );
    }
    return retValue;
  }

  @Override
  protected void doAfterRunUnits( IList<SkatletUnit> aPlugins ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // private methods
  //
  private static ITsContext createContext( ITsContextRo aContext ) {
    TsNullArgumentRtException.checkNull( aContext );
    ITsContext ctx = new TsContext( new IAskParent() {

      @Override
      public IAtomicValue findOp( String aId ) {
        return aContext.params().findValue( aId );
      }

      @Override
      public Object findRef( String aKey ) {
        return aContext.find( aKey );
      }

    } );
    ctx.params().setAll( aContext.params() );
    return ctx;
  }

  /**
   * Общееразделяемое между модулями) соединение
   *
   * @author mvk
   */
  public static final class SharedConnection
      implements ISkConnection {

    private final ISkConnection connection;
    private final ILogger       logger;

    /**
     * Конструктор
     *
     * @param aConnection {@link ISkConnection} исходное соединение
     * @param aLogger {@link ILogger} журнал
     * @throws TsNullArgumentRtException аргумент = null
     */
    public SharedConnection( ISkConnection aConnection, ILogger aLogger ) {
      TsNullArgumentRtException.checkNulls( aConnection, aLogger );
      connection = aConnection;
      logger = aLogger;
    }

    /**
     * Завершить работу
     */
    public void shutdown() {
      connection.close();
    }

    // ------------------------------------------------------------------------------------
    // ISkConnection
    //
    @Override
    public ESkConnState state() {
      return connection.state();
    }

    @Override
    public void open( ITsContextRo aArgs ) {
      logger.warning( FMT_WARN_ATTEMPT_OPEN_SHARE, currentThreadStackToString() );
    }

    @Override
    public void close() {
      logger.warning( FMT_WARN_ATTEMPT_CLOSE_SHARE, currentThreadStackToString() );
    }

    @Override
    public ISkCoreApi coreApi() {
      return connection.coreApi();
    }

    @Override
    public ISkBackendInfo backendInfo() {
      return connection.backendInfo();
    }

    @Override
    public void addConnectionListener( ISkConnectionListener aListener ) {
      connection.addConnectionListener( aListener );
    }

    @Override
    public void removeConnectionListener( ISkConnectionListener aListener ) {
      connection.removeConnectionListener( aListener );
    }

    @Override
    public ITsContext scope() {
      return connection.scope();
    }

    private static String currentThreadStackToString() {
      StringBuilder sb = new StringBuilder();
      StackTraceElement[] stack = Thread.currentThread().getStackTrace();
      for( int index = 2, n = stack.length; index < n; index++ ) {
        sb.append( format( "   %s\n", stack[index] ) ); //$NON-NLS-1$
      }
      return sb.toString();
    }
  }

}
