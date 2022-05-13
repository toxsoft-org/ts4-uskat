package org.toxsoft.uskat.legacy.plugins.impl;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.TsVersion;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.legacy.plugins.IPluginInfo;

/**
 * неизменяемый класс - реализация интерфейса IPluginInfo.
 *
 * @author Дима
 * @author goga
 */
class PluginInfo
    implements IPluginInfo {

  private final String                 jarFileName;
  private final String                 className;
  private final IList<IDependencyInfo> dependencyInfoList;
  private final IStringMap<String>     userProps;
  private final TsVersion              version;
  private final String                 type;
  private final String                 id;

  /**
   * Создает описание плагина со всеми инвариантами.
   * <p>
   * Внимание: все ссылки запоминаются без создания защитной копии!
   *
   * @param aJarFilePath String - путь к JAR-файлу плагина
   * @param aType String - тип плагина
   * @param aId String - идентификатор плагина
   * @param aClassName String - полное имя Java-класса плагина
   * @param aVer TsVersion - версия плагина
   * @param aDepInfoes IList&lt;IDependencyInfo&gt; - требуемые зависимости
   * @param aUserProps IStringMap&lt;String&gt; - другие свойства из раздела описания плагина
   * @throws TsNullArgumentRtException любой аргумент = null плагина
   * @throws TsIllegalArgumentRtException aType или aId не валидные ИД-пути
   */
  public PluginInfo( String aJarFilePath, String aType, String aId, String aClassName, TsVersion aVer,
      IList<IDependencyInfo> aDepInfoes, IStringMap<String> aUserProps ) {
    id = aId;
    type = aType;
    jarFileName = aJarFilePath;
    className = aClassName;
    userProps = aUserProps;
    version = aVer;
    dependencyInfoList = aDepInfoes;
  }

  /**
   * Конструктор копирования.
   *
   * @param aPluginInfo PluginInfo - источник
   * @throws TsNullArgumentRtException аргумент = null
   */
  public PluginInfo( PluginInfo aPluginInfo ) {
    TsNullArgumentRtException.checkNull( aPluginInfo );
    id = aPluginInfo.id;
    type = aPluginInfo.type;
    className = aPluginInfo.className;
    jarFileName = aPluginInfo.jarFileName;
    userProps = aPluginInfo.userProps;
    version = aPluginInfo.version;
    dependencyInfoList = new ElemArrayList<>( aPluginInfo.dependencyInfoList );
  }

  /**
   * Конструктор копирования с замещением версии.
   *
   * @param aPluginInfo PluginInfo - источник
   * @param aChangedVersion TsVersion - версия, замещающая версию источника
   * @throws TsNullArgumentRtException аргумент = null
   */
  public PluginInfo( PluginInfo aPluginInfo, TsVersion aChangedVersion ) {
    TsNullArgumentRtException.checkNulls( aPluginInfo, aChangedVersion );
    id = aPluginInfo.id;
    type = aPluginInfo.type;
    className = aPluginInfo.className;
    jarFileName = aPluginInfo.jarFileName;
    version = aChangedVersion;
    dependencyInfoList = aPluginInfo.dependencyInfoList;
    userProps = aPluginInfo.userProps;
  }

  public IList<IDependencyInfo> getDependencyInfoList() {
    return dependencyInfoList;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IPluginInfo
  //

  @Override
  public String pluginJarFileName() {
    return jarFileName;
  }

  @Override
  public String pluginClassName() {
    return className;
  }

  @Override
  public TsVersion pluginVersion() {
    return version;
  }

  @Override
  public String pluginType() {
    return type;
  }

  @Override
  public String pluginId() {
    return id;
  }

  @Override
  public IList<IDependencyInfo> listDependencies() {
    return dependencyInfoList;
  }

  @Override
  public IStringMap<String> userProperties() {
    return userProps;
  }

}
