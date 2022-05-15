package org.toxsoft.uskat.legacy.file.dirscanner;

import java.io.File;

/**
 * Интерфейс метода, извлекающего из файлов интересующую мета-информацию.
 * <p>
 * Под пользовательской мета-информацией понимается любая допонительная инофрмация о файле (кроме пути/имени, дины и
 * времени модификации). Например, при обработке JAR-файлов пользователь может извлечь мета-информацию из Mainfest-а,
 * извлеченного из JAR-архива. Пользовательская мета-информация в виде Object сохраняется в
 * {@link ScannedFileInfo#getMetaInfo() ScannedFileInfo.metaInfo}.
 * <p>
 * <b>Внимание!</b> Для корректной работы алгоритма определения изменений в фаловой системе (сканирования директория),
 * Object с мета-информацией <b>объязан</b> корректно переопределять методы {@link java.lang.Object#hashCode()} и
 * {@link java.lang.Object#equals(java.lang.Object)}.
 */
public interface IDirScanFileMetaInfoParser {

  /**
   * Обработать файл и вернуть его мета информацию.
   *
   * @param aFile File обрабатываемый файл
   * @return мета-информация о файле или null, если мета информация не нужна
   */
  Object getFileMetaInfo( File aFile );
}
