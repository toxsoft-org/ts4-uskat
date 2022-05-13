package org.toxsoft.uskat.legacy.file;

/**
 * Фильтр выбора файлов в диалоге {@link javax.swing.JFileChooser}.
 * <p>
 * Поддерживает фильтрацию:
 * <ul>
 * <li>по типу объекта: файл и/или директория;
 * <li>скрытых файлов/каталогов (понимая "скрытность" в зависимости типа файловой системы);
 * <li>по набору расширений файлов (только для файлов, но не каталогов);
 * <li>имеет режим игнорирования регистра символов расширений.
 * </ul>
 * <p>
 * Под расширением понимается просто строка (не обязательно с точкой), на которую заканчивается имя файла. Например, для
 * имени файла "FileName.ext" расширениями будут являтся все строки: "ext", ".ext", "xt", "ame.ext" и т.п.
 * <p>
 * Внимание: все типы объектов файловой системы кроме собственно файлов и каталогов <b>никогда</b> не включаются в
 * показываемый список!
 *
 * @author goga
 */
public class FileChooserFileFilter
    extends TsFileFilter {

  /**
   * Создать фильтр с заданными всеми параметрами. Если набор расширений файлов не задан (т.е. aExtensions это пустой
   * массив или aExtensions = null), то фильтрация по расширениям не происходит, т.е. принимаются все файлы,
   * естественно, с учетом остальных критериев. Кроме отфильтрованных файлов, всегда показываются каталоги.
   *
   * @param aHiddenAccepted boolean принимать ли фильтру скрытые файлы/каталоги (true - фильтр принимает, false -
   *          отвергает скрытые файлы/каталоги)
   * @param aExtensionCaseIgnored boolean учитывать ли регистр симоволов расширения (true - считать расхирения "ExT",
   *          "ext" и т.п. одинаковыми, false - разными)
   * @param aExtensions String[] набор расширений файлов (строки без точки) или null если не надо обрабатывать
   *          расширения
   * @param aExculdeExtensions boolean как использовать набор расширений файлов:
   *          <li>true - принимать фильтром только файлы с заданными расширениями
   *          <li>false - принимать все файлы, а отвергать только с заданными расширениями
   * @param aDescription String отображаемое описание фильтра (для идентификации пользователем, не влияет на процесс
   *          фильтрации)
   */
  public FileChooserFileFilter( boolean aHiddenAccepted, boolean aExtensionCaseIgnored, String[] aExtensions,
      boolean aExculdeExtensions, String aDescription ) {
    super( FILES_AND_DIRS, aHiddenAccepted, aExtensionCaseIgnored, aExtensions, aExculdeExtensions, aDescription );
  }

  /**
   * Фильтр файлов (нескрытых) с заданными расширениями. Кроме отфильтрованных файлов, всегда показываются все каталоги.
   *
   * @param aExtensions String[] набор расширений файлов (строки без точки) или null если не надо обрабатывать
   *          расширения
   * @param aDescription String отображаемое описание фильтра (для идентификации пользователем, не влияет на процесс
   *          фильтрации)
   */
  public FileChooserFileFilter( String[] aExtensions, String aDescription ) {
    super( FILES_AND_DIRS, false, true, aExtensions, false, aDescription );
  }
}
