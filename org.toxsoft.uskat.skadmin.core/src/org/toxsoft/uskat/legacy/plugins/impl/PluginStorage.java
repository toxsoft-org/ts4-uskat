package org.toxsoft.uskat.legacy.plugins.impl;

import static org.toxsoft.uskat.legacy.plugins.impl.ISkResources.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.utils.TsVersion;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.legacy.file.FileUtils;
import org.toxsoft.uskat.legacy.file.dirscanner.*;
import org.toxsoft.uskat.legacy.plugins.*;
import org.toxsoft.uskat.legacy.plugins.IChangedPluginsInfo.IChangedPluginInfo;
import org.toxsoft.uskat.legacy.plugins.IPluginInfo.IDependencyInfo;

/**
 * Реализация интрефейса IPluginManager. <br>
 * Реализация менеджера плагинов.
 *
 * @author Дима
 */
class PluginStorage
    implements IPluginsStorage {

  /**
   * Хранит и накапливает изменения плагинов между вызовами getChanges()
   */
  private ChangedPluginsInfo changedModulesInfo = new ChangedPluginsInfo();

  /**
   * Карта для хранения связок "Строка идентификатора плагина -> Описание плагина"
   */
  private Map<String, IPluginInfo> id2PluginInfoMap = new TreeMap<>();

  /**
   * Контейнер для хранения списка описаний плагинов. Dima. Сознательно ввожу некоторую избыточность (можно обойтись
   * id2PluginInfoMap), для того, чтобы иметь возможность отследить порядок регистрации плагинов
   */
  private IListEdit<IPluginInfo> registeredPluginInfoList = new ElemArrayList<>();

  /**
   * Храним тип плагинов.
   */
  private String plugInType;

  /**
   * Хранилище зарегистрированных директорий поиска плагинов.
   */
  private List<PluginDir> pluginDirs = new ArrayList<>();

  /**
   * Контейнер для хранения информации по директории для поиска плагинов.
   */
  private static class PluginDir {

    /**
     * Путь поиска плагинов
     */
    private File       path;
    /**
     * Сканер директории
     */
    private DirScanner jarScanner;

    public PluginDir( File aPath, boolean aIsIncludeSubDirs ) {
      super();
      this.path = aPath;
      jarScanner = new DirScanner( aPath, DirScanner.JAR_FILTER, aIsIncludeSubDirs, null );
    }

    public File getPath() {
      return path;
    }

    public DirScanner getJarScanner() {
      return jarScanner;
    }
  }

  public PluginStorage( String aPlugInType ) {
    TsNullArgumentRtException.checkNull( aPlugInType );
    plugInType = aPlugInType;
  }

  /**
   * Создает строковое предстваление полного пути к jar-файлу
   *
   * @param aJarDir путь к директории jar'a
   * @param jarFileInfo описание файла сделанное сканером
   * @return объект типа String
   */
  private static String createPathStringToJar( File aJarDir, ScannedFileInfo jarFileInfo ) {
    return aJarDir.getAbsolutePath() + File.separatorChar + jarFileInfo.getRelativePath() + jarFileInfo.getFileName();
  }

  /**
   * Региструем плагины. Механизм регистрации следующий: <br>
   * - если плагин новый (плагина с таким идентификатором нет в системе), то регистрируется безусловно <br>
   * - если плагин уже зарегистрирован, то проверятся версия нового плагина, если она более свежая, то плагин
   * регистрируется (старое описание удаляется), иначе он игнорируется.
   *
   * @param aPluginsInfoList IList&lt;IPluginInfo&gt; - ???
   */
  private void registerPlugins( IList<IPluginInfo> aPluginsInfoList ) {
    // Пробегаемся по всему списку
    for( IPluginInfo pluginInfo : aPluginsInfoList ) {
      // 2021-02-06 mvk
      if( !plugInType.equals( pluginInfo.pluginType() ) ) {
        // Необрабатываемый тип плагина
        continue;
      }
      // Проверяем плагин на новизну
      if( !isPluginRegistered( pluginInfo ) ) {
        // Новый плагин просто регистрируем
        registerPluginInfo( pluginInfo );
      }
      else {
        // Плагин уже зарегистрирован, проверяем версии
        IPluginInfo registeredPlugin = id2PluginInfoMap.get( pluginInfo.pluginId() );
        if( isNeedToUpdate( registeredPlugin, pluginInfo ) ) {
          updatePluginInfo( registeredPlugin, pluginInfo );
        }
      }
    }
  }

  /**
   * Отменяет регистрацию плагина.
   *
   * @param aRegisteredPlugin IPluginInfo - ???
   */
  private void deregisterPluginInfo( IPluginInfo aRegisteredPlugin ) {
    registeredPluginInfoList.remove( aRegisteredPlugin );
    id2PluginInfoMap.remove( aRegisteredPlugin.pluginId() );
  }

  /**
   * Регистрация описания плагина.
   *
   * @param pluginInfo объект типа IPluginInfo
   */
  private void registerPluginInfo( IPluginInfo pluginInfo ) {
    registeredPluginInfoList.add( pluginInfo );
    id2PluginInfoMap.put( pluginInfo.pluginId(), pluginInfo );
  }

  /**
   * Проверяет плагин на регистрацию в системе
   *
   * @param aPluginInfo описание плагина
   * @return true, если плагин с таким идентификатором уже зарегистрирован, иначе false
   */
  private boolean isPluginRegistered( IPluginInfo aPluginInfo ) {
    return (id2PluginInfoMap.get( aPluginInfo.pluginId() ) != null);
  }

  /**
   * Проходит по всему списку зависимостей плагина и пытается загрузить
   *
   * @param aPluginInfo описан плагина
   * @throws ClassNotFoundException - не найден плагин, от которого зависит aPluginInfo
   */
  private void checkDependencies( IPluginInfo aPluginInfo )
      throws ClassNotFoundException {
    Iterable<IDependencyInfo> dependencyInfoIterable = aPluginInfo.listDependencies();
    Iterator<IDependencyInfo> iterator = dependencyInfoIterable.iterator();
    while( iterator.hasNext() ) {
      IDependencyInfo dependencyInfo = iterator.next();
      // Проверяем номер версии
      checkDependenceVersion( dependencyInfo );
      createPluginInstance( dependencyInfo.pluginId() );
    }
  }

  /**
   * Проверяет пригодность версии зависимого модуля
   *
   * @param aPluginInfo описание модуля зависимости
   * @throws ClassNotFoundException - плагин, от которого зависит aPluginInfo, либо не найден, либо версия не та
   */
  @SuppressWarnings( "nls" )
  private void checkDependenceVersion( IDependencyInfo aPluginInfo )
      throws ClassNotFoundException {
    // Получаем описание зависимого плагина
    IPluginInfo registeredPluginInfo = id2PluginInfoMap.get( aPluginInfo.pluginId() );
    if( registeredPluginInfo == null ) {
      throw new ClassNotFoundException( MSG_ERR_CANT_RESOLVE_DEPENDENCE_TYPE + aPluginInfo.pluginType() + ",\n "
          + MSG_ERR_CANT_RESOLVE_DEPENDENCE_ID + aPluginInfo.pluginId() );
    }
    TsVersion version = aPluginInfo.pluginVersion();
    if( aPluginInfo.isExactVersionNeeded() ) {
      // Требуется точная версия
      if( version.compareTo( registeredPluginInfo.pluginVersion() ) == 0 ) {
        return;
      }
      throw new ClassNotFoundException( MSG_ERR_FOR_DEPENDENCE + aPluginInfo.pluginType() + ",\n "
          + MSG_ERR_CANT_RESOLVE_DEPENDENCE_ID + aPluginInfo.pluginId() + ",\n " + MSG_ERR_EXACT_VERSION_NUMBER
          + getVersionNumber( aPluginInfo.pluginVersion() ) + ",\n " + MSG_ERR_AVAILABLE_VERSION_NUMBER
          + getVersionNumber( registeredPluginInfo.pluginVersion() ) );
    }
    // Достаточно просто более новой версии
    if( registeredPluginInfo.pluginVersion().compareTo( version ) >= 0 ) {
      return;
    }
    throw new ClassNotFoundException( MSG_ERR_FOR_DEPENDENCE + aPluginInfo.pluginType() + ",\n "
        + MSG_ERR_CANT_RESOLVE_DEPENDENCE_ID + aPluginInfo.pluginId() + ",\n " + MSG_ERR_NEED_NEWER_VERSION_NUMBER
        + getVersionNumber( aPluginInfo.pluginVersion() ) + ",\n " + MSG_ERR_AVAILABLE_VERSION_NUMBER
        + getVersionNumber( registeredPluginInfo.pluginVersion() ) );
  }

  /**
   * Проверяет удаленные файлы в зарегистрованной директории сканирования на предмет содержания в них подгружаемых
   * модулей. Все плагины из удаленных файлов разрегистрируются
   *
   * @param aJarDir директория сканирования
   * @param aRemovedFiles объект типа List<ScannedFileInfo>
   * @return true, если в списке удаленных файлов есть jar-файлы с зарегистрированными плагинами, иначе false
   */
  private boolean hasRemovedPlugins( File aJarDir, List<ScannedFileInfo> aRemovedFiles ) {
    boolean retVal = false;
    // Проходим по списку всех файлов
    for( ScannedFileInfo scannedFileInfo : aRemovedFiles ) {
      // Получаем список описаний плагинов
      File removedJarFile = new File( createPathStringToJar( aJarDir, scannedFileInfo ) );
      // MVK: bug: нельзя запрашивать описание у файла которого может уже не быть :)
      // List<IPluginInfo> readedPluginsInfoList = readPluginsInfoFromJarManifest(
      // removedJarFile );
      List<IPluginInfo> readedPluginsInfoList = getJarPlugins( removedJarFile.getAbsolutePath() );
      // Идем по списку плагинов и проверяем их регистрацию
      for( IPluginInfo removedPluginInfo : readedPluginsInfoList ) {
        if( isPluginRegistered( removedPluginInfo ) ) {
          // Зарегистрированный плагин, отменяем регистрацию и заносим в список удаленных
          // плагинов объекта
          // хранящего измения плагинов
          deregisterPluginInfo( removedPluginInfo );
          // Заносим в изменения
          getChangedModulesInfo().addRemovedPlugin( removedPluginInfo );
          retVal = true;
        }
      }
    }
    return retVal;
  }

  // MVK
  /**
   * Возвращает список плагинов которые определяется в указанном jar-файле
   *
   * @param aJarFileName String - имя jar-файла
   * @return List&lt;{@link IPluginInfo}&gt; - список описаний плагинов
   * @throws TsNullArgumentRtException - aJarFileName = null
   */
  private List<IPluginInfo> getJarPlugins( String aJarFileName ) {
    TsNullArgumentRtException.checkNull( aJarFileName );
    List<IPluginInfo> retValue = new LinkedList<>();
    for( IPluginInfo pluginInfo : registeredPluginInfoList ) {
      if( aJarFileName.equals( pluginInfo.pluginJarFileName() ) ) {
        retValue.add( pluginInfo );
      }
    }
    return retValue;
  }

  /**
   * Проверяет изменившиеся файлы в зарегистрованной директории сканирования на предмет содержания в них подгружаемых
   * модулей. Все плагины в изменившихся jar-файлах анализируются на предмет новизны в системе или на предмет появления
   * более новой версии зарегистрированных в системе. В первом случае плагин просто регистрируется, во втором старый
   * плагин заменяется на плагин с более новой версией. В случае изменения типа и/или идентификатора подключаемого
   * модуля - считается, что предыдущий модуль был удален, и появился новый. Т.е. указанный модуль окажется в списках
   * удаленных (со старым типом/идентификатором) и в списке новых, с текущими свойствами.
   *
   * @param aJarDir директория сканирования
   * @param aChangedFiles объект типа List<ScannedFileInfo>
   * @return true, если в списке файлов есть jar-файлы с новыми плагинами и/или с существующими плагинами другой версии,
   *         иначе false
   * @throws TsIoRtException невозможно открыть aJarDir как JAR-файл
   * @throws TsRuntimeException различные ошибки ???
   */
  private boolean hasChangedPlugins( File aJarDir, List<ScannedFileInfo> aChangedFiles ) {

    boolean retVal = false;
    // Проходим по списку всех файлов
    for( ScannedFileInfo scannedFileInfo : aChangedFiles ) {
      // Получаем список описаний плагинов
      File changedJarFile = new File( createPathStringToJar( aJarDir, scannedFileInfo ) );
      // Получить список всех плагинов бывших в данном jar-файле (контрольный список)
      List<IPluginInfo> oldRegisteredPluginInfoList = getRegisteredPluginsFromThisJar( changedJarFile );
      IList<IPluginInfo> piList = PluginUtils.readPluginInfoesFromJarFile( changedJarFile );
      // Идем по списку плагинов и проверяем их новизну (незарегистрированные плагины
      // и новые версии зарегистрированных)
      for( IPluginInfo addedPluginInfo : piList ) {
        if( !isPluginRegistered( addedPluginInfo ) ) {
          // Незарегистрированный плагин, регистрируем и заносим в список добавленных
          // плагинов объекта хранящего измения плагинов
          registerPluginInfo( addedPluginInfo );
          // Заносим в изменения
          getChangedModulesInfo().addAddedPlugin( addedPluginInfo );
          retVal = true;
        }
        else {
          // Есть такой плагин, смотрим его версию и если она свежее, то обновляем
          IPluginInfo registeredPluginInfo = id2PluginInfoMap.get( addedPluginInfo.pluginId() );
          updatePluginInfo( registeredPluginInfo, addedPluginInfo );
          // Заносим изменения
          IChangedPluginInfo changedPluginInfo =
              getChangedModulesInfo().createChangedPluginInfo( addedPluginInfo, registeredPluginInfo.pluginVersion() );
          getChangedModulesInfo().addChangedPlugin( changedPluginInfo );
          // Удаляем из контрольного списка
          for( IPluginInfo info : oldRegisteredPluginInfoList ) {
            if( (info.pluginId().compareTo( registeredPluginInfo.pluginId() ) == 0) ) {
              oldRegisteredPluginInfoList.remove( info );
              break;
            }
          }
          retVal = true;
        }
      }
      // Оставшиеся в контрольном списке плагины разрегистрируем и добавляем в список
      // удаленных
      for( IPluginInfo info : oldRegisteredPluginInfoList ) {
        deregisterPluginInfo( info );
        getChangedModulesInfo().addRemovedPlugin( info );
        retVal = true;
      }
    }
    return retVal;
  }

  /**
   * Получить список всех плагинов бывших в данном jar-файле (контрольный список)
   *
   * @param aChangedJarFile - файл с плагинами
   * @return List&lt;IPluginInfo&gt; - список плагинов в файле
   */
  private List<IPluginInfo> getRegisteredPluginsFromThisJar( File aChangedJarFile ) {
    List<IPluginInfo> retVal = new ArrayList<>();
    String jarPathString;
    try {
      jarPathString = aChangedJarFile.getCanonicalPath();
    }
    catch( IOException e ) {
      throw new TsIoRtException( e );
    }
    for( IPluginInfo pluginInfo : registeredPluginInfoList ) {
      if( jarPathString.compareTo( pluginInfo.pluginJarFileName() ) == 0 ) {
        retVal.add( pluginInfo );
      }
    }
    return retVal;
  }

  /**
   * Проверяет новые файлы появивишиеся в зарегистрованной директории сканирования на предмет содержания в них
   * подгружаемых модулей. Все плагины в новых jar-файлах анализируются на предмет новизны в системе или на предмет
   * появления более новой версии зарегистрированных в системе. В первом случае плагин просто регистрируется, во втором
   * старый плагин заменяется на плагин с более новой версией.
   *
   * @param aJarDir объект типа File - директория сканирования
   * @param aNewFiles объект типа List<ScannedFileInfo>
   * @return true, если в списке файлов есть jar-файлы с новыми плагинами и/или с существующими плагинами другой версии,
   *         иначе false
   */
  private boolean hasAddedPlugins( File aJarDir, List<ScannedFileInfo> aNewFiles ) {
    boolean retVal = false;
    // Проходим по списку всех файлов
    for( ScannedFileInfo scannedFileInfo : aNewFiles ) {
      // Получаем список описаний плагинов
      File addedJarFile = new File( createPathStringToJar( aJarDir, scannedFileInfo ) );
      IList<IPluginInfo> piList = PluginUtils.readPluginInfoesFromJarFile( addedJarFile );
      // Идем по списку плагинов и проверяем их новизну (незарегистрированные плагины
      // и новые версии зарегистрированных)
      for( IPluginInfo pluginInfo : piList ) {
        if( !isPluginRegistered( pluginInfo ) ) {
          // Незарегистрированный плагин, регистрируем и заносим в список добавленных
          // плагинов объекта
          // хранящего измения плагинов
          registerPluginInfo( pluginInfo );
          // Заносим в изменения
          getChangedModulesInfo().addAddedPlugin( pluginInfo );
          retVal = true;
        }
        else {
          // Есть такой плагин, смотрим его версию и если она свежее, то обновляем
          IPluginInfo registeredPluginInfo = id2PluginInfoMap.get( pluginInfo.pluginId() );
          if( isNeedToUpdate( registeredPluginInfo, pluginInfo ) ) {
            updatePluginInfo( registeredPluginInfo, pluginInfo );
            // Заносим изменения
            IChangedPluginInfo changedPluginInfo =
                getChangedModulesInfo().createChangedPluginInfo( pluginInfo, registeredPluginInfo.pluginVersion() );
            getChangedModulesInfo().addChangedPlugin( changedPluginInfo );
            retVal = true;
          }
        }
      }
    }
    return retVal;
  }

  /**
   * Заменяет описание плагина aRegisteredPluginInfo на описание более свежей версии aAddedPluginInfo
   *
   * @param aRegisteredPluginInfo объект типа IPluginInfo
   * @param aAddedPluginInfo объект типа IPluginInfo
   */
  private void updatePluginInfo( IPluginInfo aRegisteredPluginInfo, IPluginInfo aAddedPluginInfo ) {
    deregisterPluginInfo( aRegisteredPluginInfo );
    registerPluginInfo( aAddedPluginInfo );
  }

  /**
   * Оценить необходимость обновления плагина
   *
   * @param aRegisteredPluginInfo описание зарегистрированного плагина
   * @param aAddedPluginInfo описание его добавленной версии
   * @return true, обновление необходимо, иначе false
   */
  private static boolean isNeedToUpdate( IPluginInfo aRegisteredPluginInfo, IPluginInfo aAddedPluginInfo ) {
    TsVersion registeredVersion = aRegisteredPluginInfo.pluginVersion();
    TsVersion addedVersion = aAddedPluginInfo.pluginVersion();
    if( addedVersion.compareTo( registeredVersion ) > 0 ) {
      return true;
    }
    return false;
  }

  public ChangedPluginsInfo getChangedModulesInfo() {
    return changedModulesInfo;
  }

  // --------------------------------------------------------------------------
  // Реализация интерфейса IPluginManager
  //

  @Override
  public String pluginTypeId() {
    return plugInType;
  }

  @Override
  public void addPluginJarPath( File aPath, boolean aIncludeSubDirs ) {
    FileUtils.checkDirReadable( aPath );
    PluginDir addedPluginDir = new PluginDir( aPath, aIncludeSubDirs );
    pluginDirs.add( addedPluginDir );
    // Сканируем выбранный путь на предмет обнаружения jar-файлов с манифестами
    // Сделать обработку обратных вызовов сканера (если она нужна в данном контексте)
    IDirScanResult dirScanResult = addedPluginDir.getJarScanner().scan( null );
    if( !dirScanResult.isChanged() ) {
      // Ничего не изменилось, уходим ...
      return;
    }
    // Исследуем новые jar-файлы
    List<ScannedFileInfo> jarFileList = dirScanResult.getNewFiles();
    Iterator<ScannedFileInfo> iterator = jarFileList.iterator();
    while( iterator.hasNext() ) {
      ScannedFileInfo jarFileInfo = iterator.next();
      String jarPathString = createPathStringToJar( aPath, jarFileInfo );
      File jarFile = new File( jarPathString );
      IList<IPluginInfo> pList = PluginUtils.readPluginInfoesFromJarFile( jarFile );
      registerPlugins( pList );
    }
  }

  @Override
  public IList<IPluginInfo> listPlugins() {
    return registeredPluginInfoList;
  }

  @Override
  public Object createPluginInstance( String aPluginId )
      throws ClassNotFoundException {
    TsNullArgumentRtException.checkNull( aPluginId );
    IPluginInfo pInfo = id2PluginInfoMap.get( aPluginId );
    TsItemNotFoundRtException.checkNull( pInfo );
    checkDependencies( pInfo );
    File pluginJarFile = new File( pInfo.pluginJarFileName() );
    FileUtils.checkFileReadable( pluginJarFile );
    try {
      URL[] serverURLs;
      serverURLs = new URL[] { pluginJarFile.toURI().toURL() };
      /**
       * TODO Тут возникает предупреждение о resource leak - и правильно, вопрос надо решить.
       * <p>
       * FIXME mvk указал, что при закрытии ClassLoader-а перестает работать плагин - другие классы оттуда не могут быть
       * загружены. Однако, возникает проблема - при обновлении плагина, старый ClassLoader остается активным! Нужно
       * отслеживать, чтобы при обновлении или удалении плагина закрывался бы открытый ClassLoader.
       * <p>
       * TODO: 2021-01-05 mvk c переходом на jdk11 проблема осталась (проверено)
       */
      // try( URLClassLoader classLoader = new URLClassLoader( serverURLs ) ) {
      URLClassLoader classLoader = new URLClassLoader( serverURLs );
      Class<?> cls = classLoader.loadClass( pInfo.pluginClassName() );
      Constructor<?> defaultConstructor = cls.getConstructor();
      return defaultConstructor.newInstance();
      // }
    }
    catch( Exception e ) {
      String msg = String.format( MSG_ERR_CANT_CREATE_PLUGIN_OBJECT, pInfo.pluginId(), pInfo.pluginType() );
      throw new ClassNotFoundException( msg, e );
    }
  }

  @Override
  public boolean checkChanges() {
    boolean retVal = false;
    // Сканируем все директории поиска на предмет изменений
    Iterator<PluginDir> iterator = pluginDirs.iterator();
    while( iterator.hasNext() ) {
      PluginDir currPluginDir = iterator.next();
      //
      IDirScanResult dirScanResult = currPluginDir.getJarScanner().scan( null );
      if( dirScanResult.isChanged() ) {
        // Что-то изменилось, выясняем подробности
        if( hasAddedPlugins( currPluginDir.getPath(), dirScanResult.getNewFiles() ) ) {
          // Появились новые подгружаемые модули
          retVal = true;
        }
        if( hasChangedPlugins( currPluginDir.getPath(), dirScanResult.getChangedFiles() ) ) {
          // Изменились подгружаемые модули
          retVal = true;
        }
        if( hasRemovedPlugins( currPluginDir.getPath(), dirScanResult.getRemovedFiles() ) ) {
          // удалились подгружаемые модули
          retVal = true;
        }
      }
    }
    return retVal;
  }

  @Override
  public IChangedPluginsInfo getChanges() {
    IChangedPluginsInfo retVal = changedModulesInfo;
    // Сбрасываем накопленные изменения
    changedModulesInfo = new ChangedPluginsInfo();
    return retVal;
  }

  // --------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Формирует и возвращает строку числовой части версии.
   * <p>
   * Формат строки является частью API, и представлен в виде строки слудующего вида: "1.3", то есть, как вызов метода:
   * <br>
   * <b><tt>Formatter.format("%d.%d", verMajor, verMinor);</tt> </b>
   *
   * @param aVersion ITsVersion - версия, преобразуемая в строку
   * @return String - сторка включающий в себя номер версии
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static String getVersionNumber( TsVersion aVersion ) {
    TsNullArgumentRtException.checkNull( aVersion );
    Integer vMaj = Integer.valueOf( aVersion.verMajor() );
    Integer vMin = Integer.valueOf( aVersion.verMinor() );
    return String.format( "%d.%d", vMaj, vMin ); //$NON-NLS-1$
  }

}
