package org.toxsoft.uskat.legacy.plugins.impl;

import static org.toxsoft.uskat.legacy.plugins.IPluginManagerOps.*;

import java.io.File;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.legacy.plugins.*;

/**
 * Компонента управления плагинами, реализация {@link IPluginManagerComponent}.
 *
 * @author goga
 */
public class PluginManagerComponent
    implements IPluginManagerComponent {

  private IOptionSet             ops            = IOptionSet.NULL;
  private IPluginsStorage        storage        = null;
  private IPluginsChangeListener changeListener = IPluginsChangeListener.NULL;
  private long                   lastCheckTime  = 0L;

  /**
   * пустой конструктор, вся инициализация - в {@link #init(IOptionSet, Object...)}.
   */
  public PluginManagerComponent() {
    // ничего не делает
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IInitializable
  //

  @Override
  public boolean isInit() {
    return storage != null;
  }

  @Override
  public void init( IOptionSet aOps, Object... aDirs ) {
    TsIllegalStateRtException.checkNoNull( storage );
    ops = TsNullArgumentRtException.checkNull( aOps );
    ErrorUtils.checkArrayArg( aDirs );
    TsIllegalArgumentRtException.checkFalse( ops.hasValue( PLUGIN_TYPE_ID ) );
    TsIllegalArgumentRtException.checkFalse( ops.hasValue( DIR_CHECK_INTERVAL ) );
    IPluginsStorage ps = new PluginStorage( ops.getStr( PLUGIN_TYPE_ID ) );
    for( Object o : aDirs ) {
      TsIllegalArgumentRtException.checkFalse( o instanceof File );
      ps.addPluginJarPath( (File)o, false );
    }
    storage = ps;
  }

  @Override
  public void close() {
    ops = IOptionSet.NULL;
    storage = null;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса ICooperativeMultiTaskable
  //

  @Override
  public void doJob() {
    // каждые DIR_CHECK_INTERVAL проверим директорию плагинов
    if( storage != null ) {
      if( System.currentTimeMillis() - lastCheckTime > ops.getLong( DIR_CHECK_INTERVAL ) ) {
        storage.checkChanges();
        IChangedPluginsInfo cpi = storage.getChanges();
        if( cpi.isChanges() ) {
          changeListener.onPluginsChanged( cpi );
        }
        lastCheckTime = System.currentTimeMillis();
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IPluginManagerComponent
  //

  @Override
  public Object createPluginInstance( String aPluginId ) {
    TsIllegalStateRtException.checkNull( storage );
    try {
      return storage.createPluginInstance( aPluginId );
    }
    catch( ClassNotFoundException e ) {
      throw new TsItemNotFoundRtException( e );
    }
  }

  @Override
  public IList<IPluginInfo> listPlugins() {
    TsIllegalStateRtException.checkNull( storage );
    return storage.listPlugins();
  }

  @Override
  public String pluginTypeId() {
    TsIllegalStateRtException.checkNull( storage );
    return storage.pluginTypeId();
  }

  @Override
  public void setPluginsChangeListener( IPluginsChangeListener aChangeListener ) {
    TsNullArgumentRtException.checkNull( aChangeListener );
    changeListener = aChangeListener;
  }

}
