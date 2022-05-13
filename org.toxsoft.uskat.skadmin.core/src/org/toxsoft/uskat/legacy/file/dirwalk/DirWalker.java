package org.toxsoft.uskat.legacy.file.dirwalk;

import static org.toxsoft.uskat.legacy.file.dirwalk.ISkResources.*;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.legacy.file.FileUtils;
import org.toxsoft.uskat.legacy.file.TsFileFilter;

/**
 * Рекурсивный обходчик директории и файлов. Работает совместно с обработчиком файлов (реализация интерфейса
 * {@link IDirWalkerCallback}.
 * <p>
 * Обладает следующими возможностьями:
 * <ul>
 * <li>нерекурсивность - может не заходить в поддиректории;</li>
 * <li>фильтры файлов - для составления списка обрабатываемых файлов можно использовать фильтры файлов (реализацию
 * {@link java.io.FileFilter});</li>
 * <li>подробно информирует обработчик о проиходящем.</li>
 * </ul>
 * <p>
 * Логика работы описана в комментарии к {@link IDirWalkerCallback}.
 *
 * @author goga
 */
public class DirWalker {

  // FIXME либо вообще убрать, либо переделать под использование Files.walk() или Files.walkFileTree()

  /**
   * Сравниватель файлов по имени.
   */
  private static final Comparator<File> FILE_NAME_COMPARATOR = Comparator.comparing( File::getName );

  /**
   * Пустой массив файлов - чтобы не создавать каждый раз.
   */
  private static final File  EMPTY_FILES_ARR[] = {};
  private IDirWalkerCallback callback;
  private final boolean      includeSubDirs;
  private final boolean      processFiles;
  private final FileFilter   fileFilter;

  /**
   * Создать обходчик по всем вложенным директориям и файлам.
   *
   * @param aCallback IDirWalkerCallback обработчик файлов
   */
  public DirWalker( IDirWalkerCallback aCallback ) {
    this( aCallback, true, true, null );
  }

  /**
   * Создать обходчик с заданием всех возможных инвариантов.
   *
   * @param aCallback IDirWalkerCallback обработчик файлов
   * @param aIncludeSubDirs boolean true - оходить поддиректории, false - ограничится только заданной директорией
   * @param aProcessFiles boolean true - обрабатывать файлы, false - ходить только по директориям, без обработки файлов
   *          (т.е. без вызова processFile()).
   * @param aFileFilter FileFilter фильтр обрабатываемых файлов/директории или null - рассматриваются все
   *          файлы/директории
   * @throws TsIllegalArgumentRtException если не задан обработчик (aCallback = null)
   */
  public DirWalker( IDirWalkerCallback aCallback, boolean aIncludeSubDirs, boolean aProcessFiles,
      FileFilter aFileFilter ) {
    TsNullArgumentRtException.checkNull( aCallback, ERR_DW_NO_CALLBACK );
    callback = aCallback;
    includeSubDirs = aIncludeSubDirs;
    processFiles = aProcessFiles;
    if( aFileFilter == null ) {
      fileFilter = TsFileFilter.FILTER_ALLFILES;
    }
    else {
      fileFilter = aFileFilter;
    }
  }

  /**
   * Получить ссылку на обработчик.
   *
   * @return IDirWalkerCallback текущий обработчик
   */
  public IDirWalkerCallback getCallback() {
    return callback;
  }

  /**
   * Установить обработчик обхода директория.
   *
   * @param aCallback IDirWalkerCallback новый обработчик
   * @throws TsIllegalArgumentRtException если не задан обработчик (aCallback = null)
   */
  public void setCallback( IDirWalkerCallback aCallback ) {
    TsNullArgumentRtException.checkNull( aCallback, ERR_DW_NO_CALLBACK );
    callback = aCallback;
  }

  /**
   * Реальная обработка директория.
   *
   * @param aDir File обрабатываемя директория
   * @throws DirWalkerCanceledException когда IDirWalkerCallback выбросил его
   */
  private void processDir( File aDir )
      throws DirWalkerCanceledException {
    // определим перечень файлов и перечень подкаталогов для обработки
    File pFiles[] = EMPTY_FILES_ARR;
    File subDirs[] = EMPTY_FILES_ARR;
    if( includeSubDirs ) {
      subDirs = aDir.listFiles( TsFileFilter.FILTER_ALLDIRS );
    }
    if( processFiles ) {
      pFiles = aDir.listFiles( fileFilter );
    }
    if( callback.dirEnter( aDir, pFiles, subDirs ) ) {
      if( processFiles ) {
        pFiles = aDir.listFiles( fileFilter );
      }
      if( includeSubDirs ) {
        subDirs = aDir.listFiles( TsFileFilter.FILTER_ALLDIRS );
      }
    }

    // создадим отсортированный список файлов для обработки
    Arrays.sort( pFiles, FILE_NAME_COMPARATOR );
    IListEdit<File> filesList = new ElemArrayList<>( 64 );
    for( int i = 0; i < pFiles.length; i++ ) {
      File file = pFiles[i];
      if( file.isFile() ) {
        filesList.add( file );
      }
    }
    pFiles = null; // отдаем сборщику мусора как можно раньше
    callback.processStart( aDir, filesList.size() );
    for( int i = 0, n = filesList.size(); i < n; i++ ) {
      callback.processFile( aDir, filesList.get( i ) );
    }
    callback.processFinish( aDir );

    // теперь обработаем подкаталоги
    if( includeSubDirs ) {
      for( int i = 0; i < subDirs.length; i++ ) {
        processDir( subDirs[i] );
      }
    }
    callback.dirExit( aDir );
  }

  /**
   * Совершить обход директория.
   *
   * @param aStartDir String директория, с которой начинается обход
   * @return boolean true - обход был досрочно завершен обработчиком файлов, false - обход был произведен полностью.
   * @throws TsNullArgumentRtException если aStartDir = null
   * @throws IllegalArgumentException если aStartDir не существет или не является директорией
   */
  public boolean walk( String aStartDir ) {
    TsNullArgumentRtException.checkNulls( aStartDir );
    return walk( new File( aStartDir ) );
  }

  /**
   * Совершить обход директория.
   *
   * @param aStartDir File директория, с которой начинается обход
   * @return <b>true</b> - обход был досрочно завершен в обработчиком файлов;<br>
   *         <b>false</b> - обход был произведен полностью. .
   * @throws TsNullArgumentRtException aStartDir = null
   * @throws TsIoRtException aStartDir не существет, не директориея или нельзя читать
   */
  public boolean walk( File aStartDir ) {
    FileUtils.checkDirReadable( aStartDir );
    try {
      callback.beforeStart( aStartDir );
      processDir( aStartDir );
      callback.afterFinish( false );
      return false;
    }
    catch( @SuppressWarnings( "unused" ) DirWalkerCanceledException ex ) {
      // Это исключение не ошибка - оно означает желание прервать обход,
      // хотя может быть вызван внутренней ошибкой, обрамленной в
      // DirWalkerCanceledException
      callback.afterFinish( true );
      return true;
    }
  }
}
