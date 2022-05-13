package org.toxsoft.uskat.legacy.plugins.impl;

import static org.toxsoft.uskat.legacy.plugins.IPluginsHardConstants.*;
import static org.toxsoft.uskat.legacy.plugins.impl.ISkResources.*;

import java.io.File;
import java.io.IOException;
import java.util.jar.*;

import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.TsVersion;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.legacy.plugins.*;
import org.toxsoft.uskat.legacy.plugins.IPluginInfo.IDependencyInfo;

/**
 * Вспомогательные методы для создания менеджера подключаемых модулей.
 * <p>
 * Описание работы с плагинами см. в {@link IPluginsHardConstants}.
 *
 * @author goga
 * @author Dima
 */
public class PluginUtils {

  /**
   * Считывает описание плагина из JAR-файла.
   * <p>
   * Если файл не является плагином (или описание в манифесте неверное), то выбрасывает исключение.
   *
   * @param aJarFile {@link File} - файл, который считается плагином
   * @return {@link IPluginInfo} - описание плагина
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException файл не является плагином (или описание в манифесте неверное)
   */
  static IList<IPluginInfo> readPluginInfoesFromJarFile( File aJarFile ) {
    TsNullArgumentRtException.checkNull( aJarFile );
    // загрузим манифест
    Manifest manifest;
    try( JarFile jarfile = new JarFile( aJarFile ) ) {
      manifest = jarfile.getManifest();
    }
    catch( IOException e ) {
      throw new TsIoRtException( e );
    }
    if( manifest == null ) {
      throw new TsIllegalArgumentRtException( MSG_ERR_NO_MANIFEST, aJarFile.getAbsolutePath() );
    }
    // проверим наличие,ворректность формата и поддерживаемую версию формата контейнера плагинов
    Attributes attrs = manifest.getMainAttributes();
    String containerVersionStr = attrs.getValue( MF_MAIN_ATTR_PLUGIN_CONTAINER_VERSION );
    if( containerVersionStr == null ) {
      // 2020-01-05 mvk в каталоге могут находится обычные jar
      // throw new TsIllegalArgumentRtException( MSG_ERR_NOT_PLUGIN_CONTAINER, aJarFile.getAbsolutePath() );
      return IList.EMPTY;
    }
    int containetrVersion;
    try {
      containetrVersion = Integer.parseInt( containerVersionStr.trim() );
    }
    catch( NumberFormatException ex ) {
      throw new TsIllegalArgumentRtException( ex, MSG_ERR_INV_CONTAINER_VERSION_FORMAT, aJarFile.getAbsolutePath() );
    }
    if( containetrVersion != IPluginsHardConstants.TS_PLUGIN_CONTAINER_MAINFEST_VERSION ) {
      throw new TsIllegalArgumentRtException( MSG_ERR_INV_CONTAINER_VERSION, aJarFile.getAbsolutePath(),
          Integer.valueOf( containetrVersion ), Integer.valueOf( TS_PLUGIN_CONTAINER_MAINFEST_VERSION ) );
    }
    return readPluginInfoes( manifest, aJarFile );
  }

  /**
   * Создает менеджер подключаемых модулей заданного типа.
   *
   * @param aPluginType String - строка-идентификатор типа модулей,<br>
   *          пустая строка (т.е. строка нулевой длины) - означает все типы модулей
   * @return менеджер подключаемых модулей
   */
  public static IPluginsStorage createPluginStorage( String aPluginType ) {
    IPluginsStorage pluginManager = new PluginStorage( aPluginType );
    return pluginManager;
  }

  /**
   * Создает менеджер подключаемых модулей заданного типа из заданного директория.
   *
   * @param aPluginType String - строка-идентификаторт типа модулей,<br>
   *          пустая строка (т.е. строка нулевой длины) - означает все типы модулей
   * @param aPath File - директория поиска подключаемых модулей (с поддиректориями)
   * @return менеджер подключаемых модулей
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static IPluginsStorage createPluginStorage( String aPluginType, File aPath ) {
    TsNullArgumentRtException.checkNulls( aPluginType, aPath );
    IPluginsStorage pluginManager = new PluginStorage( aPluginType );
    pluginManager.addPluginJarPath( aPath, true );
    return pluginManager;
  }

  // ------------------------------------------------------------------------------------
  // Внутренные методы
  //

  private static IList<IPluginInfo> readPluginInfoes( Manifest aManifest, File aPath ) {
    IListEdit<IPluginInfo> result = new ElemArrayList<>();
    String filename = aPath.getAbsolutePath();
    String pluginId = aManifest.getMainAttributes().getValue( "Automatic-Module-Name" ); //$NON-NLS-1$
    if( pluginId != null ) {
      IPluginInfo pluginInfo = readAttributes( filename, pluginId.trim(), aManifest.getMainAttributes() );
      if( pluginInfo != null ) {
        result.add( readAttributes( filename, pluginId.trim(), aManifest.getMainAttributes() ) );
      }
      return result;
    }
    // проходим по всем разделам, предполагаея, что имя раздела - идентификатор плагина
    for( String key : aManifest.getEntries().keySet() ) {
      Attributes attrs = aManifest.getAttributes( key );
      IPluginInfo pluginInfo = readAttributes( filename, key.trim(), attrs );
      if( pluginInfo != null ) {
        result.add( pluginInfo );
        break;
      }
    }
    return result;
  }

  private static IPluginInfo readAttributes( String aFileName, String aPluginId, Attributes aAttrs ) {
    // можно иметь в манифесте другие разделы кроме описания плагинов, поэтому, убедимся, что это раздел плагина
    // заодно, запомним свойства полей плагина
    String strClassName = null, strType = null, strVersion = null;
    IStringMapEdit<String> extProps = new StringMap<>(); // другие, неизвестные свойства раздела
    IListEdit<IDependencyInfo> deps = new ElemArrayList<>();
    for( Object o : aAttrs.keySet() ) {
      String propName = ((Attributes.Name)o).toString().trim();
      if( propName.equals( MF_ATTR_PLUGIN_CLASS_NAME ) ) {
        strClassName = aAttrs.getValue( propName ).trim();
        continue;
      }
      if( propName.equals( MF_ATTR_PLUGIN_TYPE ) ) {
        strType = aAttrs.getValue( propName ).trim();
        continue;
      }
      if( propName.equals( MF_ATTR_PLUGIN_VERSION ) ) {
        strVersion = aAttrs.getValue( propName ).trim();
        continue;
      }
      if( propName.startsWith( MF_ATTR_PREFIX_DEPENDENCY ) ) {
        deps.add( DepedencyInfo.createFromString( extProps.getByKey( propName ).trim() ) );
      }
      // extProps содержит только неизвестные подсистеме плагинов свойства из этого раздела
      extProps.put( propName.toString().trim(), aAttrs.getValue( propName ).trim() );
    }
    if( strClassName == null && strType == null && strVersion == null ) {
      return null;
    }
    if( strClassName == null || strType == null || strVersion == null ) {
      throw new TsIllegalArgumentRtException( MSG_ERR_INCOPLETE_PLUGIN_INFO_SECTION, aPluginId, aFileName );
    }
    // проверим значения и инициализируем описание плагина
    if( !StridUtils.isValidIdPath( aPluginId ) ) {
      throw new TsIllegalArgumentRtException( MSG_ERR_PLUGIN_ID_NOT_ID_PATH, aPluginId, aFileName );
    }
    if( !StridUtils.isValidIdPath( strType ) ) {
      throw new TsIllegalArgumentRtException( MSG_ERR_PLUGIN_TYPE_NOT_ID_PATH, strType, aPluginId, aFileName );
    }
    TsVersion ver;
    try {
      ver = TsVersion.KEEPER.str2ent( strVersion );
    }
    catch( Exception e ) {
      throw new TsIllegalArgumentRtException( e, MSG_ERR_INV_PLUGIN_VERSION, aPluginId, aFileName );
    }
    return new PluginInfo( aFileName, strType, aPluginId, strClassName, ver, deps, extProps );
  }

  /**
   * Запрет на создание экземпляров.
   */
  private PluginUtils() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Методы для тестирования
  //

  // @SuppressWarnings("nls")
  // public static void printManifest( String aHeader, Manifest aManifest ) {
  // pl( aHeader );
  //
  // Attributes attrs = aManifest.getMainAttributes();
  // pl( " Main attributes" );
  // for( Object attrKey : attrs.keySet() ) {
  // Attributes.Name attrName = (Attributes.Name)attrKey;
  // pl( " %s: %s", attrName.toString(), attrs.getValue( attrName ) );
  // }
  // Map<String, Attributes> entries = aManifest.getEntries();
  // for( String entryName : entries.keySet() ) {
  // pl( " Entry: %s", entryName );
  // attrs = entries.get( entryName );
  // for( Object attrKey : attrs.keySet() ) {
  // Attributes.Name attrName = (Attributes.Name)attrKey;
  // pl( " %s: %s", attrName.toString(), attrs.getValue( attrName ) );
  // }
  // }
  // }

  // @SuppressWarnings("nls")
  // public static final void printPluginInfo( IPluginInfo aInfo ) {
  // pl( "Plugin: %s", aInfo.pluginId() );
  // pl( " Type= %s", aInfo.pluginType() );
  // pl( " JAR-file= %s", aInfo.pluginJarFileName() );
  // pl( " Class= %s", aInfo.pluginClassName() );
  // pl( " Version= %s", VersionUtils.getVersionString( aInfo.pluginVersion() ) );
  // }

  /**
   * Тест считывания описаний плагинов из JAR-файла.
   *
   * @param aArgs String[] - аргументы командной строки
   */
  // @SuppressWarnings("nls")
  // public static void main( String[] aArgs ) {
  // File f = new File( "/home/goga/test.zip" );
  // IList<IPluginInfo> infoes = readPluginInfoesFromJarFile( f );
  // pl( "JAR-file %s content:", f.getAbsolutePath() );
  // for( IPluginInfo i : infoes ) {
  // printPluginInfo( i );
  // }
  // }

}
