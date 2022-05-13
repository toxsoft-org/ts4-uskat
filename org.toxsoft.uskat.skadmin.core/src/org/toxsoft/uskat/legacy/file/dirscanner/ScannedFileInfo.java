package org.toxsoft.uskat.legacy.file.dirscanner;

import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Информация о файле, сохраняемое при слежении за содержимым директория.<br>
 * Данный класс является неизменяемым.
 *
 * @author goga
 */
public final class ScannedFileInfo {

  /**
   * Имя подсистемы для создания исключении ТоксСофт.
   */
  private static final String SUBSYSTEM = ScannedFileInfo.class.getName();

  /**
   * Путь к файлу относительно корневого директория слежения.
   */
  private final String relativePath;

  /**
   * Имя файла (без пути).
   */
  private final String fileName;

  /**
   * Длина файла в байтах.
   */
  private final long fileLength;

  /**
   * Время последнего изменения (не доступа!) файла.
   */
  private final long modificationTime;

  /**
   * Мета информация, задается и используется внешним обработчиком файлов.
   */
  private final Object metaInfo;

  /**
   * Хэш-код, вычисляется в конструкторе.
   */
  private final int hashCodeValue;

  /**
   * Создание объекта со всеми инвариантами.
   *
   * @param aRelativePath String путь к файлу относительно корневого директория слежения
   * @param aFileName String имя файла (без пути).
   * @param aFileLength long длина файла в байтах.
   * @param aModficationTime long время последнего изменения (не доступа!) файла.
   * @param aMetaInfo Object мета информация, задается и используется внешним обработчиком файлов.
   * @throws TsNullArgumentRtException если имя файла или путь равны null
   */
  ScannedFileInfo( String aRelativePath, String aFileName, long aFileLength, long aModficationTime, Object aMetaInfo ) {
    TsNullArgumentRtException.checkNulls( SUBSYSTEM, aRelativePath, aFileName );
    relativePath = aRelativePath;
    fileName = aFileName;
    fileLength = aFileLength;
    modificationTime = aModficationTime;
    metaInfo = aMetaInfo;
    hashCodeValue = calculateHashCode();
  }

  /**
   * Вычисляет хэш-код
   *
   * @return значение хэш-кода
   */
  private int calculateHashCode() {
    int i = 17;
    i = 37 * i + relativePath.hashCode();
    i = 37 * i + fileName.hashCode();
    i = 37 * i + (int)(fileLength ^ (fileLength >>> 32));
    i = 37 * i + (int)(modificationTime ^ (modificationTime >>> 32));
    i = 37 * i + ((metaInfo == null) ? 0 : metaInfo.hashCode());
    return i;
  }

  /**
   * Получить путь к файлу относительно корневого директория слежения.
   *
   * @return String путь к файлу (с разделителем пути на конце)
   */
  public String getRelativePath() {
    return relativePath;
  }

  /**
   * Получить имя файла (без пути).
   *
   * @return String имя файла
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Получить длину файла в байтах.
   *
   * @return long длина файла в байтах
   */
  public long getFileLength() {
    return fileLength;
  }

  /**
   * Получить время последнего изменения (не доступа!) файла.
   *
   * @return long время в миллисекундах с начала эпохи
   */
  public long getModifictionTime() {
    return modificationTime;
  }

  /**
   * Получить дополнительную (мета) информацию о файле. Значение задается и интерпретируется необязательным внешным
   * обработчиком содеримого файла.
   *
   * @return Object информация о файле (если информации нет, то null)
   */
  public Object getMetaInfo() {
    return metaInfo;
  }

  // --------------------------------------------------------------------------
  // Переопределенные методы Object
  //

  /**
   * Проверка на равенство. Два объекта равны, если вся информация о файле у них одинакова.
   */
  @Override
  public boolean equals( Object aObj ) {
    if( aObj == this ) {
      return true;
    }
    if( aObj instanceof ScannedFileInfo fi ) {
      if( relativePath.equals( fi.relativePath ) && fileName.equals( fi.fileName ) && (fileLength == fi.fileLength)
          && (modificationTime == fi.modificationTime) ) {
        if( metaInfo == fi.metaInfo ) {
          return true;
        }
        if( (metaInfo != null) && (fi.metaInfo != null) ) {
          return metaInfo.equals( fi.metaInfo );
        }
      }
    }
    return false;
  }

  /**
   * Получить хэш-код.
   */
  @Override
  public int hashCode() {
    return hashCodeValue;
  }

}
