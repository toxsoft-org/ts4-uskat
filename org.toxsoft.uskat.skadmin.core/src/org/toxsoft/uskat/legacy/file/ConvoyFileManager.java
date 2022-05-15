package org.toxsoft.uskat.legacy.file;

import static org.toxsoft.uskat.legacy.file.ISkResources.*;

import java.io.File;

import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.bricks.strio.StrioRtException;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.LoggerUtils;

/**
 * Работа с конвой-файлами.
 * <p>
 * TODO описать использование ConvoyFileManager
 *
 * @author goga
 */
public class ConvoyFileManager {

  /**
   * Имя конвей-файла директория (находится в директории).
   */
  private static final String DEFAULT_DIR_CONVOY_FILE_PREFIX = "dir"; //$NON-NLS-1$

  private static final char   CHAR_DOT = '.';
  private static final String STR_DOT  = "."; //$NON-NLS-1$

  private final String convoyFileExt;
  private final int    lengthOfConvoyFileExtWithDot;
  private final String prefixOfDirConvoyFileName;
  private final String dirConvoyFileName;

  /**
   * Конструктор со всеми инвариантами.
   *
   * @param aConvoyFileExt String - расширение (без точки) конвой-файлов для файлов (должно быть ИД-именем)
   * @param aDirConvoyFilePrefix String - имя конвой-файлоа директория (должно быть ИД-именем)
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException любое имя не ИД-путь
   */
  public ConvoyFileManager( String aConvoyFileExt, String aDirConvoyFilePrefix ) {
    StridUtils.checkValidIdName( aConvoyFileExt );
    StridUtils.checkValidIdName( aDirConvoyFilePrefix );
    convoyFileExt = aConvoyFileExt;
    lengthOfConvoyFileExtWithDot = convoyFileExt.length() + 1;
    prefixOfDirConvoyFileName = aDirConvoyFilePrefix;
    dirConvoyFileName = STR_DOT + prefixOfDirConvoyFileName + STR_DOT + convoyFileExt;
  }

  /**
   * Конструктор с префиксом конвой-файла директория по умолчанию.
   *
   * @param aConvoyFileExt String - расширение конвой-файлов для файлов (должно быть ИД-именем)
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException любое имя не ИД-путь
   */
  public ConvoyFileManager( String aConvoyFileExt ) {
    StridUtils.checkValidIdName( aConvoyFileExt );
    convoyFileExt = aConvoyFileExt;
    lengthOfConvoyFileExtWithDot = convoyFileExt.length() + 1;
    prefixOfDirConvoyFileName = DEFAULT_DIR_CONVOY_FILE_PREFIX;
    dirConvoyFileName = STR_DOT + prefixOfDirConvoyFileName + STR_DOT + convoyFileExt;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Проверят, что файл является конвоем.
   * <p>
   * Проверяется, что имя файла соответствует требьованиям форматирования имени путем
   * {@link #formatConvoyFileName(String)}.
   *
   * @param aFileName String - имя файла (возможно, с путем)
   * @return boolean - имя является именем коновой-файла
   */
  public boolean isConvoyFile( String aFileName ) {
    TsNullArgumentRtException.checkNull( aFileName );
    String ext = FileUtils.extractExtension( aFileName );
    String bare = FileUtils.extractBareFileName( aFileName );
    if( ext.toLowerCase().equals( convoyFileExt ) ) {
      return !bare.isEmpty() && bare.charAt( 0 ) == CHAR_DOT;
    }
    return false;
  }

  /**
   * Создает и возвращает имя сопроводительного файла к указанному файлу.
   * <p>
   * Существование файла или сопроводительного файла не проверяется.
   * <p>
   * Внимание: метод не понимает директории, любой {@link File} интерпретирует как файл.
   *
   * @param aFileName String - имя файла (возможно с путем, котрый игнорируется)
   * @return {@link File} - сопроводительный файл к файлу
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException файл уже является конвой-файлом по правилам {@link #isConvoyFile(String)}
   */
  public final String formatConvoyFileName( String aFileName ) {
    StridUtils.checkValidIdName( aFileName );
    TsIllegalArgumentRtException.checkTrue( isConvoyFile( aFileName ) );
    return CHAR_DOT + FileUtils.extractFileName( aFileName ) + CHAR_DOT + convoyFileExt;
  }

  /**
   * Возвращает имя конвой-файла из файла или директории.
   * <p>
   * Существование и тип файлов не проверяется.
   *
   * @param aFile {@link File} - файл или директория
   * @return {@link File} - сопроводительный файл к файлу или директории
   * @throws TsNullArgumentRtException аргумент = null
   */
  public File toConvoyFile( File aFile ) {
    TsNullArgumentRtException.checkNull( aFile );
    if( aFile.isDirectory() ) {
      return new File( aFile, dirConvoyFileName );
    }
    return new File( aFile.getParentFile(), formatConvoyFileName( aFile.getName() ) );
  }

  /**
   * Возвращает полный путь к файлу (или директории) из конвой-файла.
   *
   * @param aConvoyFile {@link File} - конвой-файл
   * @return {@link File} - файл или директория
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException аргумент не соответствует формату имени конвой-файла
   */
  public final File toMediaFile( File aConvoyFile ) {
    TsNullArgumentRtException.checkNull( aConvoyFile );
    String convoyName = aConvoyFile.getName();
    if( convoyName.equals( dirConvoyFileName ) ) {
      return aConvoyFile.getParentFile();
    }
    TsIllegalArgumentRtException.checkFalse( isConvoyFile( convoyName ) );
    String startingDotRemoved = convoyName.substring( 1 );
    String mediaName = startingDotRemoved.substring( 0, startingDotRemoved.length() - lengthOfConvoyFileExtWithDot );
    return new File( aConvoyFile.getParentFile(), mediaName );
  }

  /**
   * Записывает (переписывает) конвой-файл к файлу.
   *
   * @param <T> - тип конвоя
   * @param aFile {@link File} - файл для конвориования
   * @param aConvoy &lt;T&gt; - конвой
   * @param aKeeper {@link IEntityKeeper}&lt;T&gt; - хранитель конвоя
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aFile сам является конвой-файлом
   * @throws TsIoRtException aFile не является существующим файлом
   */
  public <T> void writeFileConvoy( File aFile, T aConvoy, IEntityKeeper<T> aKeeper ) {
    TsNullArgumentRtException.checkNulls( aConvoy, aKeeper );
    FileUtils.checkFileReadable( aFile );
    TsIllegalArgumentRtException.checkTrue( isConvoyFile( aFile.getName() ) );
    File convFile = toConvoyFile( aFile );
    aKeeper.write( convFile, aConvoy );
  }

  /**
   * Записывает (переписывает) конвой-файл к директории.
   *
   * @param <T> - тип конвоя
   * @param aDir {@link File} - директория для ковоирования
   * @param aConvoy &lt;T&gt; - конвой
   * @param aKeeper {@link IEntityKeeper}&lt;T&gt; - хранитель конвоя
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aDir является конвой-файлом
   * @throws TsIoRtException aDir не является существующей директорией
   */
  public <T> void writeDirConvoy( File aDir, T aConvoy, IEntityKeeper<T> aKeeper ) {
    TsNullArgumentRtException.checkNulls( aConvoy, aKeeper );
    TsIllegalArgumentRtException.checkTrue( isConvoyFile( aDir.getName() ) );
    FileUtils.checkDirReadable( aDir );
    File convFile = toConvoyFile( aDir );
    aKeeper.write( convFile, aConvoy );
  }

  /**
   * Считывает конвой-файл файлу.
   * <p>
   * Если нет или поврежден файл конвоя, то записывает конвой по умолчанию и возвращает считанный конвой.
   *
   * @param <T> - тип конвоя
   * @param aFile {@link File} - файл для конвориования
   * @param aKeeper {@link IEntityKeeper}&lt;T&gt; - хранитель конвоя
   * @param aDefaultConvoy &lt;T&gt; - конвой по умолчанию
   * @return &lt;T&gt; - считанный конвой
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aFile сам является конвой-файлом
   * @throws TsIoRtException aFile не является существующим файлом
   * @throws TsIoRtException конвой-файл невозможно считать
   * @throws StrioRtException ошибка формата конвой-файла
   */
  public <T> T readFileConvoy( File aFile, IEntityKeeper<T> aKeeper, T aDefaultConvoy ) {
    TsNullArgumentRtException.checkNulls( aKeeper, aDefaultConvoy );
    FileUtils.checkFileReadable( aFile );
    TsIllegalArgumentRtException.checkTrue( isConvoyFile( aFile.getName() ) );
    File convFile = toConvoyFile( aFile );
    if( !convFile.exists() ) {
      try {
        aKeeper.write( convFile, aDefaultConvoy );
        LoggerUtils.errorLogger().warning( FMT_INFO_CREATED_DEFAULT_FILE_CONVOY, aFile );
      }
      catch( Exception ex ) {
        LoggerUtils.errorLogger().error( ex );
        aKeeper.write( convFile, aDefaultConvoy );
      }
      return aDefaultConvoy;
    }
    try {
      return aKeeper.read( convFile );
    }
    catch( Exception ex ) {
      LoggerUtils.errorLogger().error( ex );
      aKeeper.write( convFile, aDefaultConvoy );
    }
    return aKeeper.read( convFile );
  }

  /**
   * Считывает конвой-файл директории.
   * <p>
   * Если нет или поврежден файл конвоя, то записывает конвой по умолчанию и возвращает считанный конвой.
   *
   * @param <T> - тип конвоя
   * @param aDir {@link File} - директория для конвориования
   * @param aKeeper {@link IEntityKeeper}&lt;T&gt; - хранитель конвоя
   * @param aDefaultConvoy &lt;T&gt; - конвой по умолчанию
   * @return &lt;T&gt; - считанный конвой
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aDir сам является конвой-файлом
   * @throws TsIoRtException aDir не является существующей директорией
   * @throws TsIoRtException конвой-файл невозможно считать
   * @throws StrioRtException ошибка формата конвой-файла
   */
  public <T> T readDirConvoy( File aDir, IEntityKeeper<T> aKeeper, T aDefaultConvoy ) {
    TsNullArgumentRtException.checkNulls( aKeeper, aDefaultConvoy );
    TsIllegalArgumentRtException.checkTrue( isConvoyFile( aDir.getName() ) );
    FileUtils.checkDirReadable( aDir );
    File convFile = toConvoyFile( aDir );
    if( !convFile.exists() ) {
      try {
        aKeeper.write( convFile, aDefaultConvoy );
        LoggerUtils.errorLogger().warning( FMT_INFO_CREATED_DEFAULT_DIR_CONVOY, aDir );
      }
      catch( Exception ex ) {
        LoggerUtils.errorLogger().error( ex );
        aKeeper.write( convFile, aDefaultConvoy );
      }
      return aDefaultConvoy;
    }
    try {
      return aKeeper.read( convFile );
    }
    catch( Exception ex ) {
      LoggerUtils.errorLogger().error( ex );
      aKeeper.write( convFile, aDefaultConvoy );
    }
    return aKeeper.read( convFile );
  }

  /**
   * Проверяет наличие конвой-файла к файлу или директории.
   *
   * @param aFileOrDir {@link File} - файл или директория для конвоирования
   * @return boolean - признак наличия читабельного файла конвоя
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException файловый объект аргумент не существует
   * @throws TsIllegalArgumentRtException аргумент сам имеет имя конвой-файла
   */
  public boolean hasConvoyFile( File aFileOrDir ) {
    TsNullArgumentRtException.checkNull( aFileOrDir );
    TsItemNotFoundRtException.checkFalse( aFileOrDir.exists() );
    TsIllegalArgumentRtException.checkTrue( isConvoyFile( aFileOrDir.getName() ) );
    return toConvoyFile( aFileOrDir ).exists();
  }

  /**
   * Возвращает расширение конвой-файлов (без начальной точки).
   *
   * @return String - расширение конвой-файлов (всегда ИД-имя)
   */
  public String convoyFileExt() {
    return convoyFileExt;
  }

  /**
   * Возвращает префикс для формирования имени конвой-файла директория.
   *
   * @return String - префикс имени конвой-файла директория
   */
  public String prefixOfDirConvoyFileName() {
    return prefixOfDirConvoyFileName;
  }

  /**
   * Возвращает имя конвой-файла директория.
   * <p>
   * Имя начинается с точки и дальше идет префикс {@link #prefixOfDirConvoyFileName()} и расширение
   * {@link #convoyFileExt()}.
   *
   * @return String - имя конвой-файла директория
   */
  public String dirConvoyFileName() {
    return dirConvoyFileName;
  }

}
