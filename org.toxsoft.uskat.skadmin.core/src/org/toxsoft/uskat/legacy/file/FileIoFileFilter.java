package org.toxsoft.uskat.legacy.file;

import org.toxsoft.core.tslib.utils.TsLibUtils;

/**
 * Фильтр составления списка файлов в методе {@link java.io.File#listFiles(java.io.FileFilter)}.
 * <p>
 * Поддерживает фильтрацию:
 * <li>по типу объекта: файл и/или директория;
 * <li>скрытых файлов/каталогов (понимая "скрытность" в зависимости типа файловой системы);
 * <li>по набору расширений файлов (только для файлов, но не каталогов);
 * <li>имеет режим игнорирования регистра символов расширений.
 * <p>
 * Под расширением понимается просто строка (не обязательно с точкой), на которую заканчивается имя файла. Например, для
 * имени файла "FileName.ext" расширениями будут являтся все строки: "ext", ".ext", "xt", "ame.ext" и т.п.
 * <p>
 * Внимание: все типы объектов файловой системы кроме собственно файлов и каталогов <b>никогда</b> не включаются в
 * показываемый список!
 *
 * @author goga
 */
public class FileIoFileFilter
    extends TsFileFilter {

  /**
   * Создает фильтр с заданными всеми параметрами. Если набор расширений файлов не задан (т.е. aExtensions это пустой
   * массив или aExtensions = null), то фильтрация по расширениям не происходит, т.е. принимаются все файлы,
   * естественно, с учетом остальных критериев.
   *
   * @param aWhatAccepted int что принимается фильтром: файлы, директории или и те, и другие. Одна из констант
   *          {@link #FILES_AND_DIRS}, {@link #ONLY_DIRS} или {@link #ONLY_FILES}.
   * @param aHiddenAccepted boolean принимать ли фильтру скрытые файлы/каталоги (true - фильтр принимает, false -
   *          отвергает скрытые файлы/каталоги)
   * @param aExtensionCaseIgnored boolean учитывать ли регистр симоволов расширения (true - считать расхирения "ExT",
   *          "ext" и т.п. одинаковыми, false - разными)
   * @param aExtensions String[] набор расширений файлов (строки без точки) или null если не надо обрабатывать
   *          расширения
   * @param aExculdeExtensions boolean как использовать набор расширений файлов:
   *          <li>true - принимать фильтром только файлы с заданными расширениями
   *          <li>false - принимать все файлы, а отвергать только с заданными расширениями
   */
  public FileIoFileFilter( int aWhatAccepted, boolean aHiddenAccepted, boolean aExtensionCaseIgnored,
      String[] aExtensions, boolean aExculdeExtensions ) {
    super( aWhatAccepted, aHiddenAccepted, aExtensionCaseIgnored, aExtensions, aExculdeExtensions,
        TsLibUtils.EMPTY_STRING );
  }

  /**
   * Создает фильтр только файлов с заданными расширениями. При этом, принимаются только нескрытые файлы и регистр
   * расширения не учитывается.
   *
   * @param aExtensions String[] набор расширений файлов (строки без точки) или null если не надо обрабатывать
   *          расширения
   */
  public FileIoFileFilter( String[] aExtensions ) {
    super( ONLY_FILES, false, true, aExtensions, false, TsLibUtils.EMPTY_STRING );
  }

}
