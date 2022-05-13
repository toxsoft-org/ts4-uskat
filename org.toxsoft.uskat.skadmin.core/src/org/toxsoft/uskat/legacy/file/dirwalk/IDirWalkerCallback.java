package org.toxsoft.uskat.legacy.file.dirwalk;

import java.io.File;

/**
 * Интерфейс для реализации требуемой функциональности при обходе директории/файлов.
 * <p>
 * Логика вызова методов интерфейса (при вызове {@link DirWalker#walk(String)}) следующая:
 * <ul>
 * <li>Перед началом работы вызывается {@link #beforeStart(File)}, после чего обходчик рекурсивно обходит (если задана
 * такая опция) каталоги.</li>
 * <li>При входе в каталог, обходчик формирует список дочерных файлов (в соответствии с установками фильтра) и вызывает
 * {@link #dirEnter(File, File[], File[]) dirEnter()}.</li>
 * <li>реализация метода может провести некоторые действия, которые могут изменить содержимое директория (например,
 * удалить файлы), в таком случае реализация dirEnter() обязана вернуть true. Возврат false означает, что содержимое
 * директория не изменена.</li>
 * <li>Обходчик вызывает {@link #processStart(File, int) processStart()}. Если до этого содержимое директория была
 * изменена (dirEnter() вернул true), то список файлов для обработки будет обновлен.</li>
 * <li>Для всех файлов из списка обрабатываемых будет вызыван {@link #processFile(File, File) processFile()}. Перед
 * началом обработки файлов список сортируется по именам файлов, т.е. processFile() вызывается по порядку имен
 * файлов</li>
 * <li>После прохода по всем дочерным файлам/каталогам, до перехода в следующую директорию вызывается
 * {@link #processFinish(File) processFinish()}.</li>
 * <li>После processFinish() обходчик начинает рекурсивный обход подкаталогов и повторяется вышеописанная
 * процедура.</li>
 * <li>После "глубокого" прохода по директорию (уже и с поддиректориями) вызывается {@link #dirExit(File)
 * dirExit()}.</li>
 * <li>После окончания всей работы, перед возвратом из {@link DirWalker#walk(String) walk()} вызывается
 * {@link #afterFinish(boolean)}</li>
 * </ul>
 *
 * @author goga
 */
public interface IDirWalkerCallback {

  /**
   * Вызывается перед там, как вообще что-нибудь будет предпринято, но после проверки существования стартовой
   * директории.
   *
   * @param aStartDir File стартовая директория
   * @throws DirWalkerCanceledException если реализация выбросила данное исключение, то обход дерева будет прекращен.
   */
  void beforeStart( File aStartDir )
      throws DirWalkerCanceledException;

  /**
   * Вызывается при входе в директорию.
   * <p>
   * Метод может изменить содержимое директория (стереть, переименовать и т.п. как файлы так и поддиректории), но в
   * таком случае он должен вернуть true. Возврат false означает, что содержимое директория не изменена.
   *
   * @param aDir File директория, обработка которого будет начата
   * @param aFilesToBeProcessed File[] список файлов, которые будут обработаны до выхода из директория
   * @param aSubDirs File[] подкаталоги, которые будут обработаны (этот массив имеет нулевую длину, если рекрсия не была
   *          задана)
   * @return boolean
   *         <li>true - перед вызовом processStart() список файлов для обработки будет обновлен
   *         <li>false - во избежание лишней работы обходчик использует список, сформированный к моменту вызова
   *         dirEnter().
   * @throws DirWalkerCanceledException если реализация выбросила данное исключение, то обход дерева будет прекращен.
   */
  boolean dirEnter( File aDir, File aFilesToBeProcessed[], File aSubDirs[] )
      throws DirWalkerCanceledException;

  /**
   * Вызывается перед началом прохода по списку дочерных файлов.
   *
   * @param aDir File текущая директория, файлы в котором будут обработаны
   * @param aCount int количество файлов, которые будут обработаны
   * @throws DirWalkerCanceledException если реализация выбросила данное исключение, то обход дерева будет прекращен.
   */
  void processStart( File aDir, int aCount )
      throws DirWalkerCanceledException;

  /**
   * Обработать дочерный файл в каталоге.
   * <p>
   * Внимание! обработка файлов происходит по именам в восходящем порядке. <br>
   * Внимание! processFile() вызывается только для файлов, не для подкаталогов.
   *
   * @param aDir File родительский каталог
   * @param aFile File обрабатываемый файл
   * @throws DirWalkerCanceledException если реализация выбросила данное исключение, то обход дерева будет прекращен.
   */
  void processFile( File aDir, File aFile )
      throws DirWalkerCanceledException;

  /**
   * Вызывается после окончания обработки файлов в директории.
   *
   * @param aDir File директория, обработка файлов в котором завершилась
   * @throws DirWalkerCanceledException если реализация выбросила данное исключение, то обход дерева будет прекращен.
   */
  void processFinish( File aDir )
      throws DirWalkerCanceledException;

  /**
   * Вызывается перед выходом из обработанного директория.
   *
   * @param aDir File директория файлы и поддиректории которого были обработаны
   * @throws DirWalkerCanceledException если реализация выбросила данное исключение, то обход дерева будет прекращен.
   */
  void dirExit( File aDir )
      throws DirWalkerCanceledException;

  /**
   * Вызывается после всего обхода.
   *
   * @param aWasCancelled boolean true - обход был прерван выбрасываением DirWalkerCanceledException, false - обход был
   *          завершен полностью
   */
  void afterFinish( boolean aWasCancelled );
}
