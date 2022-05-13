package org.toxsoft.uskat.legacy.file.dirscanner;

import static org.toxsoft.uskat.legacy.file.dirscanner.ISkResources.*;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.legacy.file.FileUtils;
import org.toxsoft.uskat.legacy.file.TsFileFilter;
import org.toxsoft.uskat.legacy.file.dirwalk.*;

/**
 * Сканер содержимого директория.
 * <p>
 * Назначение сканера - по требованию клиента проверять содержимое директория и возвражать информацию об измененных
 * файлах (новый, удаленные, с измененным содержанием). Одно из использований сканера - наблюдение за измененями в
 * директории загружаемых модулей программы. По мере появления новых плагинов, программа может загружать их "на лету".
 * <p>
 * Алгоритм использования сканера прост:
 * <ul>
 * <li><b>Шаг 1. </b>Создать экземпляр класса {@link #DirScanner(File, FileFilter, boolean, IDirScanFileMetaInfoParser)
 * DirScanner()}</li>
 * <li><b>Шаг 2. </b>Просканировать директорию ({@link #scan(IDirScanCallback)}) и получить список интересующих файлов (
 * {@link #getScannedFiles()})</li>
 * <li><b>Шаг 3. </b>Периодически пересканировать директорию ( {@link #scan(IDirScanCallback)}) и по результатам
 * найденных изменений ( {@link IDirScanResult}) сделать нужную работу.</li>
 * </ul>
 *
 * @author goga
 */
public class DirScanner {

  /**
   * Расширения библиотек Java-архивов (JAR-файлов).
   */
  private static final String[] JAR_EXTS = { "jar" }; //$NON-NLS-1$

  /**
   * Фильтр отбора только нескрытых JAR-файлов.
   */
  public static final FileFilter JAR_FILTER =
      new TsFileFilter( TsFileFilter.ONLY_FILES, false, true, JAR_EXTS, false, STR_JAR_FILE_FILTER_DESCR );

  /**
   * Директория, содержимое которого будет сканироваться.
   */
  final File scanPath;

  /**
   * Фильтр отбора сканируемых файлов.
   */
  private final FileFilter scanFilter;

  /**
   * true - поддиректорий включены в сканирование, false - без поддиректориев.
   */
  private final boolean includeSubDirs;

  /**
   * Способ извлечения пользовательской мета-информации о файлах.
   */
  final IDirScanFileMetaInfoParser metaInfoParser;

  /**
   * Список известных к началу очередного сканирования файлов.
   */
  List<ScannedFileInfo> scannedFiles = new ArrayList<>();

  /**
   * Когда было последнее сканирование (метка времени согласно System.currentTimeMillis()).
   */
  private long lastScanMillisecs = 0L;

  /**
   * Флаг, показывающий, что сейчас происходит сканирование.
   */
  private boolean scanningOn = false;

  // --------------------------------------------------------------------------
  // Конструктор(ы)
  //

  /**
   * Создать сканнер директория.<br>
   * Сканеру указывается метод обработки файлов для извлечения формат-зависимой мета-информации (агрумент
   * aMetaInfoParser). Метод этого агрумента вызывается при обработке каждого файла. Извлеченная мета-информаяция
   * заносится в {@link ScannedFileInfo#getMetaInfo()}.<br>
   * Практически во всех случаях рекомендуется создавать сканер с быстырм (aHardScan = false) алгоритмом обнаружения
   * изменений.
   *
   * @param aScanPath File путь, который следует сканировать
   * @param aFilter FileFilter фильтр для отбора файлов, если null, то используется фильтр, включающий все файлы список
   *          сканируемых
   * @param aIncludeSubDirs boolean включать ли поддиретории при сканировании
   * @param aMetaInfoParser IDirScanFileMetaInfoParser способ определения мета информации об известных пользователю
   *          типах файлов, или null если мета-информация в {@link ScannedFileInfo#getMetaInfo()} не требуется
   * @throws TsNullArgumentRtException если aScanPath == null
   * @throws TsIllegalArgumentRtException если aScanPath не директория
   */
  public DirScanner( File aScanPath, FileFilter aFilter, boolean aIncludeSubDirs,
      IDirScanFileMetaInfoParser aMetaInfoParser ) {
    FileUtils.checkDirReadable( aScanPath );
    if( aFilter != null ) {
      scanFilter = aFilter;
    }
    else {
      scanFilter = TsFileFilter.FILTER_ALLFILES;
    }
    scanPath = aScanPath;
    includeSubDirs = aIncludeSubDirs;
    metaInfoParser = aMetaInfoParser;
  }

  /**
   * Создаеть сканер поиска JAR-файлов (по расширению, в поддиректориях), без обработки мета информации.
   *
   * @param aScanPath File путь, который следует сканировать
   * @return созданный сканер
   */
  public static DirScanner createJarScanner( File aScanPath ) {
    return new DirScanner( aScanPath, JAR_FILTER, true, null );
  }

  // --------------------------------------------------------------------------
  // Реализация алгоритма сканирования
  //

  private static class CounterCb
      extends DirWalkerCallbackAdapter {

    private int count;

    CounterCb() {
      count = 0;
    }

    @Override
    public void processStart( File aDir, int aCount )
        throws DirWalkerCanceledException {
      count += aCount;
    }

    public int getCount() {
      return count;
    }
  }

  /**
   * Класс обработки обратного вызова при сканировании директрия.
   *
   * @author goga
   * @version $id$
   */
  private class ScannerCb
      extends DirWalkerCallbackAdapter {

    private final String                scanPathStr;
    private final int                   maxCount;
    private final DirScanResult         result;
    private final List<ScannedFileInfo> knownFiles;
    private final IDirScanCallback      dsCallback;

    private int doneCount = 0;

    ScannerCb( DirScanResult aResult, int aMaxCount, List<ScannedFileInfo> aKnownFiles, IDirScanCallback aCallback ) {
      TsNullArgumentRtException.checkNull( aKnownFiles );
      TsIllegalArgumentRtException.checkTrue( aMaxCount < 0 );
      if( aMaxCount == 0 ) {
        maxCount = 1;
      }
      else {
        maxCount = aMaxCount;
      }
      result = aResult;
      knownFiles = aKnownFiles;
      dsCallback = aCallback;
      scanPathStr = scanPath.getAbsolutePath() + File.separator;
    }

    private String dir2RelPath( File aDir ) {
      String dirPath = aDir.getAbsolutePath() + File.separator;
      return dirPath.substring( scanPathStr.length() );
    }

    private ScannedFileInfo findKnwonFile( String aRelPath, String aFileName ) {
      for( ScannedFileInfo sfi : knownFiles ) {
        if( sfi.getFileName().equals( aFileName ) && sfi.getRelativePath().equals( aRelPath ) ) {
          return sfi;
        }
      }
      return null;
    }

    @Override
    public void processFile( File aDir, File aFile )
        throws DirWalkerCanceledException {
      Object metaInfo = null;
      if( metaInfoParser != null ) {
        metaInfo = metaInfoParser.getFileMetaInfo( aFile );
      }
      ScannedFileInfo argSfi =
          new ScannedFileInfo( dir2RelPath( aDir ), aFile.getName(), aFile.length(), aFile.lastModified(), metaInfo );
      ScannedFileInfo fSfi = findKnwonFile( argSfi.getRelativePath(), argSfi.getFileName() );
      if( fSfi != null ) { // Это - не новый файл
        knownFiles.remove( fSfi );
        if( fSfi.equals( argSfi ) ) { // Это - неизмененный старый файл
          scannedFiles.add( argSfi );
        }
        else { // Это - измененный файл
          result.changedFiles.add( argSfi );
        }
      }
      else { // Обрабатываем новый файл
        result.getNewFiles().add( argSfi );
      }
      if( dsCallback != null ) {
        double done = ((double)doneCount) / ((double)maxCount);
        String fileObjName = argSfi.getRelativePath() + argSfi.getFileName();
        dsCallback.step( done, ISkResources.MSG_STEP_PROCESSING_FILE, fileObjName );
      }
      ++doneCount;
    }

    /**
     * Вызывается после последнего вызова processFile(), после окончания скнаирования.
     */
    public void scanDone() {
      // Все еще необработанные
      result.removedFiles = knownFiles;
      for( ScannedFileInfo sfi : result.getNewFiles() ) {
        scannedFiles.add( sfi );
      }
      if( result.newFiles.size() != 0 || result.changedFiles.size() != 0 || result.removedFiles.size() != 0 ) {
        result.setChangedState();
      }
    }
  }

  // --------------------------------------------------------------------------
  // Открытое API
  //

  /**
   * Получить список известных после окончания очередного сканирования файлов.<br>
   * До первого сканирования список пуст. После очередного сканирования включает все файлы, отобранные заданным
   * фильтром. Полученный список является неизменяемым - т.е. попытки его изменения приведут к исключению
   * UnsupportedOperationException.
   *
   * @return неизменяемый список сканированных файлов
   */
  public List<ScannedFileInfo> getScannedFiles() {
    return Collections.unmodifiableList( scannedFiles );
  }

  /**
   * Получить время, когда закончилось последнее сканирование.
   *
   * @return long метка времени согласно System.currentTimeMillis()
   */
  public long getLastScanMillisecs() {
    return lastScanMillisecs;
  }

  /**
   * Произвести сканирование директория.<br>
   * После сканирования обновляются список известных файлов {@link #getScannedFiles()}.
   *
   * @param aCallback IDirScanCallback обработчик очередногошага сканирования, или null
   * @return IDirScanResult результат сканирования (информация об изменениях в файлах)
   * @throws TsIoRtException - путь сканирования более не является валидным каталогом
   */
  public IDirScanResult scan( IDirScanCallback aCallback ) {
    DirScanResult result = new DirScanResult();
    if( !scanningOn ) {
      scanningOn = true;
      if( aCallback != null ) {
        aCallback.beforeStart();
      }
      try {
        FileUtils.checkDirReadable( scanPath );
        // Просканируем и посчитаем количество объектов, подлежащих обработке
        CounterCb ccb = new CounterCb();
        DirWalker dw = new DirWalker( ccb, includeSubDirs, true, scanFilter );
        dw.walk( scanPath );

        // Просканируем и определим список новых и измененных файлов
        List<ScannedFileInfo> fList = scannedFiles;
        scannedFiles = new ArrayList<>();
        ScannerCb scb = new ScannerCb( result, ccb.getCount(), fList, aCallback );
        dw.setCallback( scb );
        dw.walk( scanPath );
        scb.scanDone();
      }
      finally {
        if( aCallback != null ) {
          aCallback.afterFinish();
        }
        lastScanMillisecs = System.currentTimeMillis();
        scanningOn = false;
      }
    }
    return result;
  }

}
