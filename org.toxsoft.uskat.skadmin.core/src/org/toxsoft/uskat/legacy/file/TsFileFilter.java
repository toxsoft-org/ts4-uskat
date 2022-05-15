package org.toxsoft.uskat.legacy.file;

import static org.toxsoft.uskat.legacy.file.ISkResources.*;

import java.io.File;

import org.toxsoft.core.tslib.coll.primtypes.IStringListEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Основа для фильтров файлов для JFileChooser и File.listFiles().
 * <p>
 * Поддерживает фильтрацию:
 * <li>по типу объекта: файл и/или директория;</li>
 * <li>скрытых файлов/каталогов (понимая "скрытность" в зависимости типа файловой системы);</li>
 * <li>по набору расширений файлов (только для файлов, но не каталогов);</li>
 * <li>имеет режим игнорирования регистра символов расширений.</li>
 * <p>
 * Под расширением понимается просто строка (не обязательно с точкой), на которую заканчивается имя файла. Например, для
 * имени файла "FileName.ext" расширениями будут являтся все строки: "ext", ".ext", "xt", "ame.ext" и т.п.
 * <p>
 * Внимание: все типы объектов файловой системы кроме собственно файлов и каталогов <b>никогда</b> не включаются в
 * показываемый список!
 *
 * @author goga
 */
public class TsFileFilter
    extends javax.swing.filechooser.FileFilter
    implements java.io.FileFilter {

  /**
   * Включать в список только файлы (но не директории). <br>
   * Используется в качестве агрумента aWhatAccepted конструктора
   * {@link #TsFileFilter(int, boolean, boolean, String[], boolean, String) TsFileFilter()}
   */
  public static final int ONLY_FILES = 0x01;

  /**
   * Включать в список только директории (но не файлы). <br>
   * Используется в качестве агрумента aWhatAccepted конструктора
   * {@link #TsFileFilter(int, boolean, boolean, String[], boolean, String) TsFileFilter()}
   */
  public static final int ONLY_DIRS = 0x02;

  /**
   * Включать в список файлы и директории (другие типы объектов не включаются). <br>
   * Используется в качестве агрумента aWhatAccepted конструктора
   * {@link #TsFileFilter(int, boolean, boolean, String[], boolean, String) TsFileFilter()}
   */
  public static final int FILES_AND_DIRS = ONLY_FILES | ONLY_DIRS;

  /**
   * Фильтр для выборки нескрытых директории.
   */
  public static final TsFileFilter FILTER_DIRS =
      new TsFileFilter( TsFileFilter.ONLY_DIRS, false, true, null, false, MSG_FILTER_DIRS_DESCR );

  /**
   * Фильтр для выборки выборки всех директориев, включая скрытые.
   */
  public static final TsFileFilter FILTER_ALLDIRS =
      new TsFileFilter( TsFileFilter.ONLY_DIRS, true, true, null, false, MSG_FILTER_ALLDIRS_DESCR );

  /**
   * Фильтр для выборки нескрытых файлов.
   */
  public static final TsFileFilter FILTER_FILES =
      new TsFileFilter( TsFileFilter.ONLY_FILES, false, true, null, false, MSG_FILTER_FILES_DESCR );

  /**
   * Фильтр для выборки выборки всех файлов, включая скрытые.
   */
  public static final TsFileFilter FILTER_ALLFILES =
      new TsFileFilter( TsFileFilter.ONLY_FILES, true, true, null, false, MSG_FILTER_ALLFILES_DESCR );

  /**
   * Фильтр для выборки выборки всех файлов и директории, кроме скрытых.
   */
  public static final TsFileFilter FILTER_UNHIDDEN =
      new TsFileFilter( TsFileFilter.FILES_AND_DIRS, false, true, null, false, MSG_FILTER_UNHIDDEN_DESCR );

  /**
   * Фильтр для выборки выборки всех файлов и директории, включая скрытые.
   */
  public static final TsFileFilter FILTER_EVERYTHING =
      new TsFileFilter( TsFileFilter.FILES_AND_DIRS, true, true, null, false, MSG_FILTER_EVERYTHING_DESCR );

  /**
   * Что принимается фильтром: файлы, директории или и те, и другие.
   */
  private int whatAccepted = FILES_AND_DIRS;

  /**
   * Флаг: принимать ли фильтром скрытые файлы/директории.
   */
  private boolean hiddenAccepted = false;

  /**
   * Флаг: игнорировать ли регистр символов расширения.
   */
  private boolean extensionCaseIgnored = true;

  /**
   * Флаг: как использовать набор расширений файлов:
   * <li>true - принимать фильтром только файлы с заданными расширениями
   * <li>false - принимать все файлы, а отвергать только с заданными расширениями
   */
  private boolean exculdeExtensions = false;

  /**
   * Список расширений, рассматриваемых фильтром. Если список пустой, то фильтрация по расширениям не происходит.
   */
  private final IStringListEdit extensions          = new StringArrayList();
  /**
   * То же, что и {@link #extensions}, но в нижнем регистре.
   */
  private final IStringListEdit extensionsLowerCase = new StringArrayList();

  /**
   * Описание фильтра.
   */
  private String description = TsLibUtils.EMPTY_STRING;

  /**
   * Создать фильтр с заданными всеми параметрами. Если набор расширений файлов не задан (т.е. aExtensions это пустой
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
   * @param aDescription String отображаемое описание фильтра (для идентификации пользователем, не влияет на процесс
   *          фильтрации)
   */
  public TsFileFilter( int aWhatAccepted, boolean aHiddenAccepted, boolean aExtensionCaseIgnored, String[] aExtensions,
      boolean aExculdeExtensions, String aDescription ) {
    setWhatAccepted( aWhatAccepted );
    hiddenAccepted = aHiddenAccepted;
    extensionCaseIgnored = aExtensionCaseIgnored;
    exculdeExtensions = aExculdeExtensions;
    if( aExtensions != null ) {
      for( String s : aExtensions ) {
        addExtension( s );
      }
    }
    description = aDescription;
  }

  /**
   * Конструктор копирования.
   *
   * @param aSource {@link TsFileFilter} - источник
   * @throws TsNullArgumentRtException аргумент = null
   */
  public TsFileFilter( TsFileFilter aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    setWhatAccepted( aSource.whatAccepted );
    hiddenAccepted = aSource.isHiddenAccepted();
    extensionCaseIgnored = aSource.isExtensionCaseIgnored();
    exculdeExtensions = aSource.isExtensionsExcluded();
    extensions.setAll( aSource.getExtensions() );
    description = aSource.getDescription();
    extensionsLowerCase.setAll( aSource.extensionsLowerCase );
  }

  /**
   * Создает филльтр для отбора обычных файло по расширению с учетеом регистра символов.
   *
   * @param aExtensionWithoutDot String - расширение (без точки)
   * @return {@link TsFileFilter} - созданный фильтр
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static TsFileFilter createByExt( String aExtensionWithoutDot ) {
    TsNullArgumentRtException.checkNull( aExtensionWithoutDot );
    String[] exts = { aExtensionWithoutDot };
    return new TsFileFilter( ONLY_FILES, false, false, exts, false, TsLibUtils.EMPTY_STRING );
  }

  /**
   * Определить, что принимается фильтром: файлы, директории или и те, и другие.
   *
   * @return int одна из констант {@link #FILES_AND_DIRS}, {@link #ONLY_DIRS} или {@link #ONLY_FILES}
   */
  public int getWhatAccepted() {
    return whatAccepted;
  }

  /**
   * Задать, что принимается фильтром: файлы, директории или и те, и другие.
   *
   * @param aWhatAccepted int одна из констант {@link #FILES_AND_DIRS}, {@link #ONLY_DIRS} или {@link #ONLY_FILES}
   */
  public final void setWhatAccepted( int aWhatAccepted ) {
    whatAccepted = aWhatAccepted & FILES_AND_DIRS;
  }

  /**
   * Принимает ли фильтр скрытые файлы/директории.
   *
   * @return boolean true - принимает, false - отвергает
   */
  public boolean isHiddenAccepted() {
    return hiddenAccepted;
  }

  /**
   * Задать, принимает ли фильтр скрытые файлы/директории.
   *
   * @param aHiddenAccepted boolean true - принимает, false - отвергает
   */
  public void setHiddenAccepted( boolean aHiddenAccepted ) {
    hiddenAccepted = aHiddenAccepted;
  }

  /**
   * Игнорируется ли регистр символов расширения файла.
   *
   * @return boolean true - игнорируется, false - учитывается регистр символов
   */
  public boolean isExtensionCaseIgnored() {
    return extensionCaseIgnored;
  }

  /**
   * Задать, игнорируется ли регистр символов расширения файла.
   *
   * @param aExtensionCaseIgnored boolean true - игнорируется, false - учитывается регистр символов
   */
  public void setExtensionCaseIgnored( boolean aExtensionCaseIgnored ) {
    extensionCaseIgnored = aExtensionCaseIgnored;
  }

  /**
   * Возвращает признак исключения заданных расширений.
   *
   * @return boolean - как используется набор расширений файлов:
   *         <li>true - принимать фильтром только файлы с заданными расширениями
   *         <li>false - принимать все файлы, а отвергать только с заданными расширениями
   */
  public boolean isExtensionsExcluded() {
    return exculdeExtensions;
  }

  /**
   * Задает признак исключения заданных расширений {@link #isExtensionsExcluded()}.
   *
   * @param aExclude boolean - признак исключения заданных расширений
   */
  public void setExtensionsExculded( boolean aExclude ) {
    exculdeExtensions = aExclude;
  }

  /**
   * Добавить расширение файла в список расширений, которые рассматриваются фильтром.
   *
   * @param aExtension String расширение файла без точки, напр. "Jpg"
   */
  public void addExtension( String aExtension ) {
    if( !extensions.hasElem( aExtension ) ) {
      extensions.add( aExtension );
      String lowerCaseExt = aExtension.toLowerCase();
      if( !extensionsLowerCase.hasElem( lowerCaseExt ) ) {
        extensionsLowerCase.add( lowerCaseExt );
      }
    }
  }

  /**
   * Добавить расширения файлов в список расширений, которые рассматриваются фильтром.
   *
   * @param aExtensions String[] массив расширений файлов без точки
   */
  public void addExtensions( String[] aExtensions ) {
    for( String s : aExtensions ) {
      addExtension( s );
    }
  }

  /**
   * Убрать расширение файла из списка расширений, которые рассматриваются фильтром. Следует указать расширение
   * aExtension именно в том регистре, как оно было добавлено в список, даже если включена опция extensionCaseIgnored.
   * Если будут убраны все расширения, то фильтрация по расширениям будет отключена.
   *
   * @param aExtension String расширение файла без точки, напр. "Jpg"
   */
  public void removeExtension( String aExtension ) {
    extensions.remove( aExtension );
    extensionsLowerCase.remove( aExtension.toLowerCase() );
  }

  /**
   * Получить массив всех расширений, которые рассматриваются фильтром.
   *
   * @return String[] массив всех расширений
   */
  public String[] getExtensions() {
    return extensions.toArray();
  }

  /**
   * Убрать все расширения файла из списка расширений, которые рассматриваются фильтром. Естественно, фильтрация по
   * расширениям будет отключена.
   */
  public void removeAllExtensions() {
    extensions.clear();
    extensionsLowerCase.clear();
  }

  /**
   * Задать отображаемое описание фильтра.
   *
   * @param aDescr String отображаемое описание фильтра
   */
  public void getDescription( String aDescr ) {
    description = aDescr;
  }

  /**
   * Получить отображаемое описание фильтра.
   *
   * @return String отображаемое описание фильтра
   */
  @Override
  public String getDescription() {
    return description;
  }

  /**
   * Определить, принять или отвергнуть заданное в aPathName объект файловой системы при формировании списка файлов.
   *
   * @param aPathName File рассматриваемый файл или каталог
   * @return boolean
   *         <li>true - включить aPathName (принять) в формируемый список файлов
   *         <li>false - не включать aPathName (отвергнуть) в формируемый список файлов
   */
  @Override
  public boolean accept( File aPathName ) {
    boolean isFile = false;

    // Оценка по критерию - файл или каталог
    if( aPathName.isDirectory() ) {
      if( (whatAccepted & ONLY_DIRS) == 0 ) {
        return false;
      }
    }
    else {
      if( aPathName.isFile() ) {
        isFile = true;
        if( (whatAccepted & ONLY_FILES) == 0 ) {
          return false;
        }
      }
      else {
        // Принимаем только файлы или директории - другие типы объектов файловой
        // системы игнорируем
        return false;
      }
    }

    // оценка по критерию - скрытый ли файл/каталог
    if( aPathName.isHidden() && !hiddenAccepted ) {
      return false;
    }

    // проверка на расширения производится только для файлов, но не каталогов.
    if( isFile ) {
      // оценка по критерию - расширение файла
      if( extensions.isEmpty() ) {
        return !exculdeExtensions;
      }
      String ext = FileUtils.extractExtension( aPathName.getName() );
      boolean wasMatch;
      if( extensionCaseIgnored ) {
        wasMatch = extensionsLowerCase.hasElem( ext.toLowerCase() );
      }
      else {
        wasMatch = extensions.hasElem( ext );
      }
      return (wasMatch != exculdeExtensions);
    }
    return true;
  }
}
