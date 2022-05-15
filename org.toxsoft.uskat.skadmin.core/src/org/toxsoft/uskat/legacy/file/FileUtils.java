package org.toxsoft.uskat.legacy.file;

import static org.toxsoft.uskat.legacy.file.EFileIoErrorKind.*;
import static org.toxsoft.uskat.legacy.file.ISkResources.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;

import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Набор методов для работы с файлами и каталогами.
 *
 * @author goga
 */
public class FileUtils {

  /**
   * Разделитель между именем и расширением файла - символ точка.
   * <p>
   * Расширением считается часть имени файла после <b>последней</b> точки.
   */
  public static final char CHAR_EXT_SEPARATOR = '.';

  /**
   * Символ {@link #CHAR_EXT_SEPARATOR} в виде строки.
   */
  public static final String STR_EXT_SEPARATOR = "" + CHAR_EXT_SEPARATOR; //$NON-NLS-1$

  /**
   * Просто пустой массиы файлов.
   */
  public static final File[] EMPTY_FILES_ARRAY = {};

  /**
   * Компаратор имён файлов по возрастанию.
   */
  public static final Comparator<File> FILE_CMP_ASC = Comparator.comparing( File::getName );

  /**
   * Компаратор имён файлов по убыванию.
   */
  public static final Comparator<File> FILE_CMP_DESC = Comparator.comparing( File::getName ).reversed();

  /**
   * Компаратор имён файлов по возрастанию, директорий всегда в начале.
   */
  public static final Comparator<File> FILEDIR_CMP_ASC = ( f1, f2 ) -> {
    if( f1.isDirectory() != f2.isDirectory() ) {
      return f1.isDirectory() ? -1 : 1;
    }
    return f1.getName().compareTo( f2.getName() );
  };

  /**
   * Компаратор имён файлов по убыванию, директорий всегда в начале.
   */
  public static final Comparator<File> FILEDIR_CMP_DESC = ( f1, f2 ) -> {
    if( f1.isDirectory() != f2.isDirectory() ) {
      return f1.isDirectory() ? -1 : 1;
    }
    return f2.getName().compareTo( f1.getName() );
  };

  /**
   * Запретим создание экземпляра класса.
   */
  private FileUtils() {
    // пустой конструктор
  }

  /**
   * Возвращает исходную строку, при необходимости добавляя символ разделителя директории {@link File#separatorChar} в
   * конец строки.
   * <p>
   * Смысл этого метода в том, чтобы после его использования можно было просто добавлять имя файла (или поддиректория) к
   * аргументу для формирования пути к файловому объекту. Поэтому, особый случай - когда аргумент пустая строка. В таком
   * случае, строка возвращается без измненении, чтобы не происходило преобразование относительного пути в абсолютное.
   *
   * @param aPath String - исходная строка, обычно, имя директория
   * @return String - исходная строка, или исходняя строка с добавлением разделителя в конце
   * @throws TsNullArgumentRtException aPath = null
   */
  public static String ensureEndingSeparator( String aPath ) {
    TsNullArgumentRtException.checkNull( aPath );
    int pathLen = aPath.length();
    if( pathLen == 0 ) {
      return aPath;
    }
    if( EFileSystemType.currentFsType() == EFileSystemType.WINDOWS ) {
      if( aPath.charAt( pathLen - 1 ) == File.pathSeparatorChar ) {
        return aPath;
      }
    }
    if( aPath.charAt( pathLen - 1 ) == File.separatorChar ) {
      return aPath;
    }
    return aPath + File.separatorChar;
  }

  /**
   * Возвращает исходную строку, при необходимости убрав все символы разделителя директории {@link File#separatorChar} с
   * окончания строки.
   *
   * @param aPath String - исходная строка, обычно, имя директория
   * @return String - исходная строка, или исходняя строка без разделитей в конце
   * @throws TsNullArgumentRtException aPath = null
   */
  public static String removeEndingSeparator( String aPath ) {
    TsNullArgumentRtException.checkNull( aPath );
    int pathLen = aPath.length();
    if( pathLen == 0 ) {
      return aPath;
    }
    int index = pathLen;
    while( aPath.charAt( index - 1 ) == File.separatorChar ) {
      --index;
    }
    return aPath.substring( 0, index );
  }

  /**
   * Возвращает исходную строку, при необходимости убрав все символы разделителя директории {@link File#separatorChar} с
   * начала строки.
   *
   * @param aPath String - исходная строка, обычно, имя директория
   * @return String - исходная строка, или исходняя строк абез разделителя в начале
   * @throws TsNullArgumentRtException aPath = null
   */
  public static String removeStartingSeparator( String aPath ) {
    TsNullArgumentRtException.checkNull( aPath );
    int pathLen = aPath.length();
    if( pathLen == 0 ) {
      return aPath;
    }
    int count = 0;
    while( aPath.charAt( count ) == File.separatorChar ) {
      ++count;
    }
    return aPath.substring( count );
  }

  /**
   * Удаляет расширение из имени файла.
   * <p>
   * Возвращает имя (и возможно путь) файла без расширения. Корректно обрабатываются пути с точками - т.е. точка
   * расшиерния ищется только в имени файла, а не в пути.
   * <p>
   * Данный метод работает только со строкой и не проводит никаких обращений к файловой системе!
   *
   * @param aFileName String имя файла, может содержать путь к файлу
   * @return String имя/путь файла без расширения
   */
  public static String removeExtension( String aFileName ) {
    String fn = aFileName;
    int pathIdx = fn.lastIndexOf( File.separatorChar );
    int extIdx = fn.lastIndexOf( CHAR_EXT_SEPARATOR );
    if( extIdx >= 0 && extIdx > pathIdx ) {
      fn = fn.substring( 0, extIdx );
    }
    return fn;
  }

  /**
   * Возвращает только имя (без пути и расширения).
   * <p>
   * Если агрумент трактуется как имя директория (т.е. заказнчивается на символ разделителя пути), то просто возвращает
   * имя директория, не удаляя расширение имени директория.
   * <p>
   * Данный метод работает только со строкой и не проводит никаких обращений к файловой системе!
   *
   * @param aFileName String - имя файла, может содержать полный путь
   * @return String - имя файла (без пути и расширения)
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static String extractBareFileName( String aFileName ) {
    TsNullArgumentRtException.checkNull( aFileName );
    int pathIdx = aFileName.lastIndexOf( File.separatorChar );
    if( pathIdx >= 0 && pathIdx == aFileName.length() - 1 ) { // это имя директрия
      if( aFileName.length() == 1 ) {
        return aFileName;
      }
      pathIdx = aFileName.lastIndexOf( File.separatorChar, pathIdx - 1 );
      return aFileName.substring( pathIdx + 1, aFileName.length() - 1 );
    }
    int extIdx = aFileName.lastIndexOf( CHAR_EXT_SEPARATOR );
    int startIdx = 0;
    int endIdx = aFileName.length();
    if( pathIdx >= 0 ) {
      startIdx = pathIdx + 1;
    }
    if( extIdx >= 0 && extIdx > pathIdx ) {
      endIdx = extIdx;
    }
    return aFileName.substring( startIdx, endIdx );
  }

  /**
   * Возвращает только имя файла с расширением, без пути к файлу.
   * <p>
   * Если агрумент трактуется как имя директория (т.е. заканчивается на символ разделителя пути), то возвращает пустую
   * строку.
   * <p>
   * Данный метод работает только со строкой и не проводит никаких обращений к файловой системе!
   *
   * @param aFileName String - имя файла, может содержать полный путь
   * @return String - имя файла с расширением, но без пути
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static String extractFileName( String aFileName ) {
    TsNullArgumentRtException.checkNull( aFileName );
    if( aFileName.isEmpty() ) {
      return TsLibUtils.EMPTY_STRING;
    }
    if( aFileName.charAt( aFileName.length() - 1 ) == File.separatorChar ) {
      return TsLibUtils.EMPTY_STRING;
    }
    File f = new File( aFileName );
    return f.getName();
  }

  /**
   * Извлекает имя директория из пути к файлу.
   * <p>
   * Особый случай, когда aFilePath указывает на директорию. Поскольку метод производсит только синтаксический анализ,
   * то он последнюю компоненту пути считает директорией, если она заканчивается на {@link File#separator}, и вовзаращет
   * aFilePath без изменения. В противном случае, последняя компоенента пути рассматривается как имя файла, и
   * отбрасывается.
   * <p>
   * Метод производит только синтаксический анализ - не разрешая относительные пути. Например, если aFilePath содержит
   * только имя файла, метод вернет пустую строку.
   * <p>
   * Если аргумент пустая строка, то метод тоже возвращает пустую строку.
   *
   * @param aFilePath String - имя файла, включая необязательный путь к нему
   * @return String - путь к файлу
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static String extractPath( String aFilePath ) {
    TsNullArgumentRtException.checkNull( aFilePath );
    int argLen = aFilePath.length();
    if( argLen == 0 || aFilePath.charAt( argLen - 1 ) == File.separatorChar ) {
      return aFilePath;
    }
    int lastSeparatorIndex = aFilePath.lastIndexOf( File.separatorChar );
    if( lastSeparatorIndex < 0 ) { // только имя файла
      return TsLibUtils.EMPTY_STRING;
    }
    return aFilePath.substring( 0, lastSeparatorIndex + 1 );
  }

  /**
   * Возвращает путь файла относительно к одному из директорий, в которой он находится.
   * <p>
   * Если aChildFile не находится в структру поддиректори каталога aParentDir, возвращает null. Если директрии
   * совпадают, возвращает пусту строку.
   *
   * @param aParentDir File - одна из директории в иерархии от корня до указанного файла
   * @param aChildFile File - файл (или директория), чей относительный путь ищеться
   * @return String - путь aChildFile относительно aParentDir или <code>null</code>
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aParentDir не директория
   */
  public static String extractRelativePath( File aParentDir, File aChildFile ) {
    TsNullArgumentRtException.checkNulls( aParentDir, aChildFile );
    TsIllegalArgumentRtException.checkFalse( aParentDir.isDirectory() );
    String p1 = removeEndingSeparator( aParentDir.getAbsolutePath() );
    String p2 = removeEndingSeparator( aChildFile.getAbsolutePath() );
    if( p1.length() > p2.length() ) {
      return null;
    }
    if( !p2.startsWith( p1 ) ) {
      return null;
    }
    return removeStartingSeparator( p2.substring( p1.length() ) );
  }

  /**
   * Возвращает путь файла относительно к указанной директорий.
   * <p>
   * В некоторых случаях (например, на объекты с разных дисков Windows) возвращает абсолютный путь.
   * <p>
   * Метод производит только синтаксический анализ, не обращаясь к файловой системе.
   *
   * @param aBasePath File - директория, путь относительно которой ищеться
   * @param aTargetPath File - файл (или директория), чей относительный путь ищеться
   * @return String - путь aChildFile относительно aParentDir
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aParentDir не директория
   * @throws TsIllegalArgumentRtException недопустимое имя файлового объекта
   */
  public static String getRelativePath( String aBasePath, String aTargetPath ) {
    PathStringInfo psiBase = new PathStringInfo( ensureEndingSeparator( aBasePath ) );
    PathStringInfo psiTarget = new PathStringInfo( aTargetPath );
    String[] base = psiBase.normalizedPath().split( Pattern.quote( File.separator ) );
    String[] target = psiTarget.normalizedPath().split( Pattern.quote( File.separator ) );
    // First get all the common elements. Store them as a string, and also count how many of them there are.
    StringBuilder common = new StringBuilder();
    int commonIndex = 0;
    while( commonIndex < target.length && commonIndex < base.length
        && target[commonIndex].equals( base[commonIndex] ) ) {
      common.append( target[commonIndex] );
      common.append( File.separator );
      commonIndex++;
    }
    if( commonIndex == 0 ) {
      // No single common path element. This most likely indicates differing drive letters, like C: and D:.
      // These paths cannot be relativized.
      return aTargetPath;
    }
    // The number of directories we have to backtrack depends on whether the base is a file or a dir
    // For example, the relative path from
    // /foo/bar/baz/gg/ff to /foo/bar/baz
    // ".." if ff is a file
    // "../.." if ff is a directory
    // The following is a heuristic to figure out if the base refers to a file or dir. It's not perfect, because
    // the resource referred to by this path may not actually exist, but it's the best I can do
    StringBuilder relative = new StringBuilder();
    if( base.length != commonIndex ) {
      int numDirsUp = psiBase.isFile() ? base.length - commonIndex - 1 : base.length - commonIndex;
      for( int i = 0; i < numDirsUp; i++ ) {
        relative.append( ".." ); //$NON-NLS-1$
        relative.append( File.separatorChar );
      }
    }
    relative.append( psiTarget.normalizedPath().substring( common.length() ) );
    return relative.toString();
  }

  /**
   * Возвращает расширение имени файла.
   * <p>
   * Расширением считается часть имени файла от последей точки до конца имени, не включая саму точку.
   * <p>
   * Если агрумент трактуется как имя директория (т.е. заказнчивается на символ разделителя пути), то возвращает пустуй
   * строку, независимо от наличия точек в имени директория.
   * <p>
   * Данный метод работает только со строкой и не проводит никаких обращений к файловой системе.
   *
   * @param aFileName String - имя файла, может содержать полный путь
   * @return String - расширение файла (без начальной точки)
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static String extractExtension( String aFileName ) {
    TsNullArgumentRtException.checkNull( aFileName );
    if( aFileName.length() == 0 ) {
      return TsLibUtils.EMPTY_STRING;
    }
    int pathIdx = aFileName.lastIndexOf( File.separatorChar );
    if( pathIdx == aFileName.length() - 1 ) { // это имя директрия
      return TsLibUtils.EMPTY_STRING;
    }
    int extIdx = aFileName.lastIndexOf( CHAR_EXT_SEPARATOR );
    if( extIdx < pathIdx ) { // файл без расширения, а точка была имери одного из родительских директории
      return TsLibUtils.EMPTY_STRING;
    }
    return aFileName.substring( extIdx + 1 );
  }

  /**
   * Определяет, находится ли каталог/файл aChild в поддереве директории aParentDir.
   * <p>
   * Если директории совпадают, метод возвращает <code>true</code>.
   *
   * @param aParentDir {@link File} - предполагаемый родительский каталог
   * @param aChild {@link File} - предполагаемый подкаталог/файл
   * @return boolean - признак находжения в поддереве директория<br>
   *         <b>true</b> - aChild находится в поддереве директория aParentDir или они одинаковые;<br>
   *         <b>false</b> - aChild не пересекаются с поддеревом директория aParentDir
   * @throws TsNullArgumentRtException если любой агрумент null
   * @throws TsIllegalArgumentRtException aParentDir не является директорией
   */
  public static boolean isChild( File aParentDir, File aChild ) {
    TsNullArgumentRtException.checkNulls( aParentDir, aChild );
    if( !aParentDir.isDirectory() ) {
      throw new TsIllegalArgumentRtException( FMT_ERR_PARENT_ARG_MUST_BE_DIRS, aParentDir.getAbsolutePath() );
    }
    String p1 = aParentDir.getAbsolutePath();
    String p2 = aChild.getAbsolutePath();
    int len1 = p1.length();
    int len2 = p2.length();
    if( len1 > len2 ) {
      return false;
    }
    if( len1 == len2 ) {
      return p1.equals( p2 );
    }
    String p2sub = p2.substring( 0, len1 );
    // начало путей совпадает
    if( p1.equals( p2sub ) ) {
      // проверим, что обрезали p2sub не посредине имени последей директории
      if( p2.charAt( len1 ) == File.separatorChar ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Определяет, находится ли каталог/файл aChild непосредственно в директории aParentDir.
   *
   * @param aParentDir {@link File} - предполагаемый родительский каталог
   * @param aChild {@link File} - предполагаемый подкаталог/файл
   * @return boolean - признак находжения в директориb<br>
   *         <b>true</b> - aChild находится в директории aParentDir или они одинаковые;<br>
   *         <b>false</b> - aChild не находится в aParentDir, но может находится глубже в пооддиректориях
   * @throws TsNullArgumentRtException если любой агрумент null
   * @throws TsIllegalArgumentRtException aParentDir не является директорией
   */
  public static boolean isDirectChild( File aParentDir, File aChild ) {
    TsNullArgumentRtException.checkNulls( aParentDir, aChild );
    if( !aParentDir.isDirectory() ) {
      throw new TsIllegalArgumentRtException( FMT_ERR_PARENT_ARG_MUST_BE_DIRS, aParentDir.getAbsolutePath() );
    }
    String p1 = aParentDir.getAbsolutePath();
    String p2 = aChild.getAbsolutePath();
    if( p1.length() > p2.length() ) {
      return false;
    }
    if( p1.length() == p2.length() ) {
      return p1.equals( p2 );
    }
    if( p2.lastIndexOf( File.separatorChar ) > p1.length() ) {
      return false;
    }
    File reducedChild = new File( p2.substring( 0, p1.length() ) );
    return aParentDir.compareTo( reducedChild ) == 0;
  }

  /**
   * Возвращает ту начальную часть пути, которая реально существует в файловой системе.
   * <p>
   * Например, aPath = "/home/user/temp/cache/info.txt", но не если не существует директория temp, то метод вернет
   * "/home/user", а если такой файл существует, то просто возвращает аргумент. Если аргумент - абсолютный путь, то в
   * UNIX системах в худшем случае метод вернет "/", а в Windows, при отсутствии заданного диска (например, "f:"),
   * вернет пустуй строку. Если аргумент относительный путь, и он не существует - метод вернет пустую строку.
   * <p>
   * Надо учесть, что пустая строка означает текущую директорию. Поэтому, в UNIX во всех случаях возвращаемое значение
   * осмысленно - либо корневой каталог "/", либо пустая строка "", которая и должна означать текущую директорию, когда
   * аргумент был относительным путем. В Windows же ситуация запутанная (привет Биллу Гейтсу) - возвращаемая пустая
   * строка может означать как текущую директорию (которая по определению существует, так и отсуствующий диск при
   * заданном в аргументе несуществующем абсолютном пути. Так что, наш совет - пользуйтесь UNIX и Linux :)
   *
   * @param aPath String - путь к каталогу или файлу
   * @return String - существующая часть пути или пустая строка, если аргумент абсолютный путь и указывает на
   *         несуществующий диск в Windows
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static String extractExistingPartOfPath( String aPath ) {
    TsNullArgumentRtException.checkNull( aPath );
    String s = aPath;
    int index = aPath.length();
    // убираем кормпоненты пути с конца, пока не доберемся до сущестующей пути
    while( (index = s.lastIndexOf( File.separator, index - 1 )) >= 0 ) {
      File f = new File( s );
      if( f.exists() ) {
        return s;
      }
      s = s.substring( 0, index + 1 );
    }
    // убрали все поддиректории - проверяем остаток: в Windows это диск, а в UNIX мы тут не окажемся :)
    File f = new File( s );
    if( f.exists() ) {
      return s;
    }
    return TsLibUtils.EMPTY_STRING;
  }

  // ------------------------------------------------------------------------------------
  // Работа со списками файлов
  //

  /**
   * Сортирует массив файлов по возрастанию или убыванию имен.
   *
   * @param aFiles File[] сортируемый массив
   * @param aAscending boolean true= сортировка по возрастанию (а-я), false= сортировка по убыванию (я-а)
   * @param aDirsFirst boolean признак, что директории всегда перед файлами
   */
  public static void sortFiles( File aFiles[], boolean aAscending, boolean aDirsFirst ) {
    if( aDirsFirst ) {
      if( aAscending ) {
        Arrays.sort( aFiles, FILEDIR_CMP_ASC );
      }
      else {
        Arrays.sort( aFiles, FILEDIR_CMP_DESC );
      }
    }
    else {
      if( aAscending ) {
        Arrays.sort( aFiles, FILE_CMP_ASC );
      }
      else {
        Arrays.sort( aFiles, FILE_CMP_DESC );
      }
    }
  }

  /**
   * Сортирует массив файлов по возрастанию или убыванию имен.
   * <p>
   * Директории будут распологаться <b>до</b> файлов. Равнозначно вызову метода
   * {@link #sortFiles(File[], boolean, boolean) sortFiles(aFiles,aAscending,<b>true</b>)}
   *
   * @param aFiles File[] сортируемый массив
   * @param aAscending boolean true= сортировка по возрастанию (а-я), false= сортировка по убыванию (я-а)
   */
  public static void sortFiles( File aFiles[], boolean aAscending ) {
    sortFiles( aFiles, aAscending, true );
  }

  /**
   * Оболочка к фильтру, которая исключает НЕ-файлы из рассмотрения фильтром.
   *
   * @author goga
   */
  static final class OnlyFileFilterWrapper
      implements FileFilter {

    private final FileFilter fileFilter;

    /**
     * Создать оболочку к фильтру.
     *
     * @param aFilter фильтр-оригинал
     * @throws TsNullArgumentRtException если aFilter = null
     */
    OnlyFileFilterWrapper( FileFilter aFilter ) {
      TsNullArgumentRtException.checkNulls( aFilter );
      fileFilter = aFilter;
    }

    /**
     * Сначала исключает объекты не-файлы, а потом применяет правило фильтра-оригинала,
     *
     * @param aFilePath File -проверяемый файл
     * @return boolean - признак включения файла в результат
     */
    @Override
    public boolean accept( File aFilePath ) {
      if( aFilePath.isFile() ) {
        return fileFilter.accept( aFilePath );
      }
      return false;
    }

  }

  /**
   * Создает оболочкук фильтру, отсекающие объекты не-файлы.<br>
   * реализация гарантирует, что к рассмотрению фильтра-оригинала <b>не будут</b> допущены объекты не-файлы (т.е. те,
   * для которых File.isFile() = false).<br>
   * Пример применения: фильтр-оригинал отсеивает объекты файловой системы только по расширениям, но чтобы туда не
   * попали директории, специальные файлы UNIX и т.п., можно использовать этй оболочку.
   *
   * @param aFilter фильтр-оригинал
   * @return FileFilter новый объект, реализующий интерыейс файлового фильтра
   * @throws TsNullArgumentRtException если aFilter = null
   */
  public static FileFilter wrapOnlyFileFilter( FileFilter aFilter ) {
    return new OnlyFileFilterWrapper( aFilter );
  }

  // ------------------------------------------------------------------------------------
  // Методы проверки
  //

  /**
   * Проверяет файл на существование и права на чтение из него с выбрасываением исключений.
   *
   * @param aFile File - проверяемый файл
   * @return {@link File} - врозвращает аргумент
   * @throws TsNullArgumentRtException aFile = null
   * @throws TsIoRtException (NOT_FOUND) файл не найден
   * @throws TsIoRtException (NOT_A_FILE) это не файл (директория или еще что-то)
   * @throws TsIoRtException (NO_READ_RIGHTS) нет прав на чтение
   */
  public static File checkFileReadable( File aFile ) {
    TsNullArgumentRtException.checkNull( aFile );
    TsIoRtException.checkFalse( aFile.exists(), NOT_FOUND.toString(), aFile );
    TsIoRtException.checkFalse( aFile.isFile(), NOT_A_FILE.toString(), aFile );
    TsIoRtException.checkFalse( aFile.canRead(), NO_READ_RIGHTS.toString(), aFile );
    return aFile;
  }

  /**
   * Проверяет файл на существование и права на чтение из него.
   *
   * @param aFile File - проверяемый файл
   * @return <b>true</b> - да, aFile это существующий файл с правами на чтение;<br>
   *         <b>false</b> - нет, aFile либе не сущестует. либо не файл или нельзя читать.
   * @throws TsNullArgumentRtException aFile = null
   */
  public static boolean isFileReadable( File aFile ) {
    TsNullArgumentRtException.checkNull( aFile );
    return aFile.exists() && aFile.isFile() && aFile.canRead();
  }

  /**
   * Проверяет файл на существование и права на запись в него с выбрасываением исключений.
   *
   * @param aFile File - проверяемый файл
   * @return {@link File} - врозвращает аргумент
   * @throws TsNullArgumentRtException aFile = null
   * @throws TsIoRtException (NOT_FOUND) файл не найден
   * @throws TsIoRtException (NOT_A_FILE) это не файл (директория или еще что-то)
   * @throws TsIoRtException (NO_WRITE_RIGHTS) нет прав на запись
   */
  public static File checkFileWritable( File aFile ) {
    TsNullArgumentRtException.checkNull( aFile );
    TsIoRtException.checkFalse( aFile.exists(), NOT_FOUND.toString(), aFile );
    TsIoRtException.checkFalse( aFile.isFile(), NOT_A_FILE.toString(), aFile );
    TsIoRtException.checkFalse( aFile.canWrite(), NO_WRITE_RIGHTS.toString(), aFile );
    return aFile;
  }

  /**
   * Проверяет, что в заданнный файл можно дописывать, а не существующий - создать.
   *
   * @param aFile File - проверяемый файл
   * @return {@link File} - врозвращает аргумент
   * @throws TsNullArgumentRtException aFile = null
   * @throws TsIoRtException (NOT_A_FILE) это не файл (директория или еще что-то)
   * @throws TsIoRtException (NO_WRITE_RIGHTS) нет прав на запись
   * @throws TsIoRtException (NOT_FOUND) родительская директория не существует
   */
  public static File checkFileAppendable( File aFile ) {
    TsNullArgumentRtException.checkNull( aFile );
    if( aFile.exists() ) {
      TsIoRtException.checkFalse( aFile.isFile(), NOT_A_FILE.toString(), aFile );
      TsIoRtException.checkFalse( aFile.canWrite(), NO_WRITE_RIGHTS.toString(), aFile );
    }
    else {
      File parentDir = aFile.getParentFile();
      if( parentDir != null ) {
        TsIoRtException.checkFalse( parentDir.exists(), NOT_FOUND.toString(), parentDir );
        TsIoRtException.checkFalse( parentDir.isDirectory(), NOT_A_DIR.toString(), parentDir );
        TsIoRtException.checkFalse( parentDir.canWrite(), NO_WRITE_RIGHTS.toString(), parentDir );
      }
    }
    return aFile;
  }

  /**
   * Проверяет файл на существование и права на запись в него.
   *
   * @param aFile File - проверяемый файл
   * @return <b>true</b> - да, aFile это существующий файл с правами на запись;<br>
   *         <b>false</b> - нет, aFile либе не сущестует. либо не файл или нельзя писать.
   * @throws TsNullArgumentRtException aFile = null
   */
  public static boolean isFileWritable( File aFile ) {
    TsNullArgumentRtException.checkNull( aFile );
    return aFile.exists() && aFile.isFile() && aFile.canWrite();
  }

  /**
   * Проверяет, что в заданнный файл можно дописывать, а не существующий - создать.
   *
   * @param aFile File - проверяемый файл
   * @return <b>true</b> - да, aFile это существующий файл с правами на запись;<br>
   *         <b>false</b> - нет, aFile либо сущестует как не-записываемый, либо не файл или нельзя писать/создать.
   * @throws TsNullArgumentRtException aFile = null
   */
  public static boolean isFileAppendable( File aFile ) {
    TsNullArgumentRtException.checkNull( aFile );
    if( aFile.exists() ) {
      return aFile.isFile() && aFile.canWrite();
    }
    File parentDir = aFile.getParentFile();
    if( parentDir != null ) {
      return parentDir.exists() && parentDir.isDirectory() && parentDir.canWrite();
    }
    return true;
  }

  /**
   * Проверяет файл на существование и права на чтение из него.
   * <p>
   * В результате проверки возвращается либо {@link ValidationResult#SUCCESS}, либо ошибка
   * {@link ValidationResult#isError()} с осмысленным сообщением, готовым для отображения пользователю.
   *
   * @param aFile {@link File} - проверяемый файл
   * @return {@link ValidationResult} - результат проверки
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static ValidationResult validateFileReadable( File aFile ) {
    TsNullArgumentRtException.checkNull( aFile );
    if( !aFile.exists() ) {
      return ValidationResult.error( FMT_ERR_FILE_NOT_EXISTS, aFile.getAbsolutePath() );
    }
    if( !aFile.isFile() ) {
      return ValidationResult.error( FMT_ERR_PATH_IS_NOT_FILE, aFile.getAbsolutePath() );
    }
    if( !aFile.canRead() ) {
      return ValidationResult.error( FMT_ERR_FILE_NOT_READABLE, aFile.getAbsolutePath() );
    }
    return ValidationResult.SUCCESS;
  }

  /**
   * Проверяет файл на существование и права на чтение из и запись в него.
   * <p>
   * В результате проверки возвращается либо {@link ValidationResult#SUCCESS}, либо ошибка
   * {@link ValidationResult#isError()} с осмысленным сообщением, готовым для отображения пользователю.
   *
   * @param aFile {@link File} - проверяемый файл
   * @return {@link ValidationResult} - результат проверки
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static ValidationResult validateFileWriteable( File aFile ) {
    ValidationResult r = validateFileReadable( aFile );
    if( !r.isOk() ) {
      return r;
    }
    if( !aFile.canWrite() ) {
      return ValidationResult.error( FMT_ERR_FILE_NOT_WRITEABLE, aFile.getAbsolutePath() );
    }
    return ValidationResult.SUCCESS;
  }

  /**
   * Проверяет, что в заданнный файл можно дописывать, а не существующий - создать.
   * <p>
   * В результате проверки возвращается либо {@link ValidationResult#SUCCESS}, либо ошибка
   * {@link ValidationResult#isError()} с осмысленным сообщением, готовым для отображения пользователю.
   *
   * @param aFile {@link File} - проверяемый файл
   * @return {@link ValidationResult} - результат проверки
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static ValidationResult validateFileAppendable( File aFile ) {
    TsNullArgumentRtException.checkNull( aFile );
    if( aFile.exists() ) {
      if( !aFile.isFile() ) {
        return ValidationResult.error( FMT_ERR_PATH_IS_NOT_FILE, aFile.getAbsolutePath() );
      }
      if( !aFile.canRead() ) {
        return ValidationResult.error( FMT_ERR_FILE_NOT_READABLE, aFile.getAbsolutePath() );
      }
      if( !aFile.canWrite() ) {
        return ValidationResult.error( FMT_ERR_FILE_NOT_WRITEABLE, aFile.getAbsolutePath() );
      }
      return ValidationResult.SUCCESS;
    }
    if( aFile.getName().isEmpty() ) {
      return ValidationResult.error( MSG_ERR_EMPTY_FILE_NAME, aFile.getAbsolutePath() );
    }
    File parentDir = aFile.getParentFile();
    if( parentDir != null ) {
      return validateDirWriteable( parentDir );
    }
    return ValidationResult.SUCCESS;
  }

  /**
   * Проверяет директорию на существование и права на чтение из него с выбрасываением исключений.
   *
   * @param aDir {@link File} - проверяемая директория
   * @return {@link File} - врозвращает аргумент
   * @throws TsNullArgumentRtException aDir = null
   * @throws TsIoRtException (NOT_FOUND) директория не найдена
   * @throws TsIoRtException (NOT_A_DIR) это не директория (файл или еще что-то)
   * @throws TsIoRtException (NO_READ_RIGHTS) нет прав на чтение
   */
  public static File checkDirReadable( File aDir ) {
    TsNullArgumentRtException.checkNull( aDir );
    TsIoRtException.checkFalse( aDir.exists(), NOT_FOUND.toString(), aDir );
    TsIoRtException.checkFalse( aDir.isDirectory(), NOT_A_DIR.toString(), aDir );
    TsIoRtException.checkFalse( aDir.canRead(), NO_READ_RIGHTS.toString(), aDir );
    return aDir;
  }

  /**
   * Проверяет директорию на существование и права на чтение из него с выбрасываением исключений.
   *
   * @param aDir {@link Path} - проверяемая директория
   * @return {@link Path} - врозвращает аргумент
   * @throws TsNullArgumentRtException aDir = null
   * @throws TsIoRtException (NOT_FOUND) директория не найдена
   * @throws TsIoRtException (NOT_A_DIR) это не директория (файл или еще что-то)
   * @throws TsIoRtException (NO_READ_RIGHTS) нет прав на чтение
   */
  public static Path checkDirReadable( Path aDir ) {
    TsNullArgumentRtException.checkNull( aDir );
    TsIoRtException.checkFalse( Files.exists( aDir ), NOT_FOUND.toString(), aDir.toString() );
    TsIoRtException.checkFalse( Files.isDirectory( aDir ), NOT_A_DIR.toString(), aDir.toString() );
    TsIoRtException.checkFalse( Files.isReadable( aDir ), NO_READ_RIGHTS.toString(), aDir.toString() );
    return aDir;
  }

  /**
   * Проверяет директорию на существование и права на чтение из него.
   *
   * @param aDir {@link File} - проверяемая директория
   * @return <b>true</b> - да, aDir это существующую директорию с правами на чтение;<br>
   *         <b>false</b> - нет, aDir либе не сущестует. либо не файл или нельзя читать.
   * @throws TsNullArgumentRtException aDir или aSubSystem = null
   */
  public static boolean isDirReadable( File aDir ) {
    TsNullArgumentRtException.checkNull( aDir );
    return aDir.exists() && aDir.isDirectory() && aDir.canRead();
  }

  /**
   * Проверяет директорию на существование и права на чтение из него.
   *
   * @param aDir {@link Path} - проверяемая директория
   * @return <b>true</b> - да, aDir это существующую директорию с правами на чтение;<br>
   *         <b>false</b> - нет, aDir либе не сущестует. либо не файл или нельзя читать.
   * @throws TsNullArgumentRtException aDir или aSubSystem = null
   */
  public static boolean isDirReadable( Path aDir ) {
    TsNullArgumentRtException.checkNull( aDir );
    return Files.exists( aDir ) && Files.isDirectory( aDir ) && Files.isReadable( aDir );
  }

  /**
   * Проверяет директорию на существование и права на чтение из него.
   * <p>
   * В результате проверки возвращается либо {@link ValidationResult#SUCCESS}, либо ошибка
   * {@link ValidationResult#isError()} с осмысленным сообщением, готовым для отображения пользователю.
   *
   * @param aDir {@link File} - проверяемая директория
   * @return {@link ValidationResult} - результат проверки
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static ValidationResult validateDirReadable( File aDir ) {
    TsNullArgumentRtException.checkNull( aDir );
    if( !aDir.exists() ) {
      return ValidationResult.error( FMT_ERR_DIR_NOT_EXISTS, aDir.getAbsolutePath() );
    }
    if( !aDir.isDirectory() ) {
      return ValidationResult.error( FMT_ERR_PATH_IS_NOT_DIRECTORY, aDir.getAbsolutePath() );
    }
    if( !aDir.canRead() ) {
      return ValidationResult.error( FMT_ERR_DIR_NOT_READABLE, aDir.getAbsolutePath() );
    }
    return ValidationResult.SUCCESS;
  }

  /**
   * Проверяет директорию на существование и права на чтение из и запись в него.
   * <p>
   * В результате проверки возвращается либо {@link ValidationResult#SUCCESS}, либо ошибка
   * {@link ValidationResult#isError()} с осмысленным сообщением, готовым для отображения пользователю.
   *
   * @param aDir {@link File} - проверяемая директория
   * @return {@link ValidationResult} - результат проверки
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static ValidationResult validateDirWriteable( File aDir ) {
    ValidationResult r = validateDirReadable( aDir );
    if( !r.isOk() ) {
      return r;
    }
    if( !aDir.canWrite() ) {
      return ValidationResult.error( FMT_ERR_DIR_NOT_WRITEABLE, aDir.getAbsolutePath() );
    }
    return ValidationResult.SUCCESS;
  }

  // ------------------------------------------------------------------------------------
  // Копирование файлов
  //

  /**
   * Размер временно создаваемого буфера при копировании файлов.
   */
  private static final int FILE_BUF_SIZE = 32 * 1024;

  /**
   * Максимальное количество перебираемых имен файлов для уникальностив методе {@link #uniqueDestFile(File)}.
   */
  static final int MAX_UNIQUE_FILE_NAME_PREFIXES = 100500;

  /**
   * Интерфейс извещателя о степени выполнения копирования файлов.
   *
   * @author goga
   */
  public interface IFileCopyProgressCounter {

    /**
     * "Нулевой" извещатель, ничего не делает.
     */
    IFileCopyProgressCounter NULL = ( aTotalSteps, aCurrentStep ) -> false;

    /**
     * Вызывается при очередном шаге копирования файла(ов).
     * <p>
     * Этот метод вызвается aTotalSteps<b>+1</b> раз. В первый раз, со значением aCurrentStep<b>=0</b> метод вызыватеся
     * до начала копирования. В дальнейшем, метод вызвается после копирования очередно части файла с увеличивающимся
     * значением.
     *
     * @param aTotalSteps long - общее количество шагов к выполнению (не меняется со временем)
     * @param aCurrentStep long - номер выполненного шага
     * @return boolean - признак прекращения процесса копирования<br>
     *         <b>true</b> - копирование немедленно прекращается, оставив копируемый файл в полузаписанном виде;<br>
     *         <b>false</b> - копирование продолжается.
     */
    boolean onFileCopyProgress( long aTotalSteps, long aCurrentStep );

  }

  /**
   * Возвращает имя несуществующего файла, основанное на aDestFile.
   * <p>
   * Предназначен для избежания переписывания существующего файла (например, при копирвании поверх существующего файла).
   * Возвращается несуществующий файл, который находится в той же директории и имеет такое же расширение, как аргумент.
   * Новое имя создается путем добавления уникального постфикса к имени файла.
   * <p>
   * Если aфайл-аргумент не существует, то возвращает аргумент.
   *
   * @param aDestFile {@link File} - проверяемый файл
   * @return {@link File} - схожий с аргументом по имени, но не существующи файл
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException все возможные уникальные имена уже заняты
   */
  public static File uniqueDestFile( File aDestFile ) {
    TsNullArgumentRtException.checkNull( aDestFile );
    if( !aDestFile.exists() ) {
      return aDestFile;
    }
    File dir = aDestFile.getParentFile();
    String bare = FileUtils.extractBareFileName( aDestFile.getName() );
    String ext = FileUtils.extractExtension( aDestFile.getName() );
    for( int i = 1; i <= 100500; i++ ) {
      String name = bare + ' ' + i;
      File f = new File( dir, name + CHAR_EXT_SEPARATOR + ext );
      if( !f.exists() ) {
        return f;
      }
    }
    throw new TsIllegalStateRtException( FMT_ERR_CANT_UNIQUE_FILE, aDestFile.getName() );
  }

  /**
   * Возвращает результат {@link File#listFiles()} в виде отсортированного списка.
   *
   * @param aDir {@link File} - директория
   * @return {@link IListEdit}&lt;{@link File}&gt; - сортированный список файлов, не бывает <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIoRtException не прошла проверка {@link FileUtils#checkDirReadable(File)}
   */
  public static IList<File> listFiles( File aDir ) {
    FileUtils.checkDirReadable( aDir );
    File[] filesArray = aDir.listFiles();
    if( filesArray == null || filesArray.length == 0 ) {
      return IList.EMPTY;
    }
    Arrays.sort( filesArray );
    return new ElemArrayList<>( filesArray );
  }

  /**
   * Возвращает результат {@link File#listFiles(FileFilter)} в виде отсортированного списка.
   *
   * @param aDir {@link File} - директория
   * @param aFilter {@link FileFilter} - фильтр выбора файлов в директории
   * @return {@link IListEdit}&lt;{@link File}&gt; - сортированный список файлов, не бывает <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIoRtException не прошла проверка {@link FileUtils#checkDirReadable(File)}
   */
  public static IList<File> listFiles( File aDir, FileFilter aFilter ) {
    FileUtils.checkDirReadable( aDir );
    TsNullArgumentRtException.checkNull( aFilter );
    File[] filesArray = aDir.listFiles( aFilter );
    if( filesArray == null || filesArray.length == 0 ) {
      return IList.EMPTY;
    }
    Arrays.sort( filesArray );
    return new ElemArrayList<>( filesArray );
  }

  /**
   * Копирует файл aSrc в файл aDest.
   * <p>
   * Если файл назначения существует, то он перезаписывается.
   *
   * @param aSrc {@link File} - исходный файл
   * @param aDest {@link File} - файл назначения
   * @param aProgressCounter {@link IFileCopyProgressCounter} - извещатель о степени выполнения
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIoRtException ошибка доступа к исходному файлу на чтение
   * @throws TsIoRtException ошибка доступа к файлу назначения на запись
   * @throws TsIoRtException ошибка чтения из исходного файла
   * @throws TsIoRtException ошибка записи в файл назначения
   */
  public static void copyFile( File aSrc, File aDest, IFileCopyProgressCounter aProgressCounter ) {
    TsNullArgumentRtException.checkNull( aProgressCounter );
    checkFileReadable( aSrc );
    checkFileAppendable( aDest );
    try {
      long totalSteps = aSrc.length() / FILE_BUF_SIZE + 1;
      long currentStep = 0;
      if( aProgressCounter.onFileCopyProgress( totalSteps, currentStep ) ) {
        return;
      }
      try( InputStream in = new FileInputStream( aSrc ); OutputStream out = new FileOutputStream( aDest ) ) {
        byte[] buf = new byte[FILE_BUF_SIZE];
        int len;
        while( (len = in.read( buf )) > 0 ) {
          out.write( buf, 0, len );
          if( aProgressCounter.onFileCopyProgress( totalSteps, currentStep ) ) {
            break;
          }
        }
      }
    }
    catch( IOException e ) {
      throw new TsIoRtException( e );
    }
  }

  /**
   * Копирует файл aSrc в файл aDest.
   * <p>
   * Равнозначен вызове {@link #copyFile(File, File, IFileCopyProgressCounter)} с аргументом
   * {@link IFileCopyProgressCounter#NULL}.
   *
   * @param aSrc {@link File} - исходный файл
   * @param aDest {@link File} - файл назначения
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIoRtException ошибка доступа к исходному файлу на чтение
   * @throws TsIoRtException ошибка доступа к файлу назначения на запись
   * @throws TsIoRtException ошибка чтения из исходного файла
   * @throws TsIoRtException ошибка записи в файл назначения
   */
  public static void copyFile( File aSrc, File aDest ) {
    copyFile( aSrc, aDest, IFileCopyProgressCounter.NULL );
  }

  /**
   * Удаляет директорию со всеми поддиректориями.
   *
   * @param aDirectory {@link File} - удаляемая директория
   * @param aProgressCounter {@link IFileCopyProgressCounter} - извещатель о степени выполнения
   * @return boolean - признак успешного удаления директория
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIoRtException такая директория не существует или нет прав на удаления
   */
  public static boolean deleteDirectory( File aDirectory, IFileCopyProgressCounter aProgressCounter ) {
    FileUtils.checkDirReadable( aDirectory );
    TsNullArgumentRtException.checkNull( aProgressCounter );
    File[] files = aDirectory.listFiles();
    if( null != files ) {
      for( int i = 0; i < files.length; i++ ) {
        aProgressCounter.onFileCopyProgress( files.length, 0 );
        if( files[i].isDirectory() ) {
          deleteDirectory( files[i], IFileCopyProgressCounter.NULL );
        }
        else {
          files[i].delete();
        }
      }
      aProgressCounter.onFileCopyProgress( files.length, files.length );
    }
    return aDirectory.delete();
  }

  /**
   * Удаляет содержимое, оставляя директорию пустой.
   *
   * @param aDirectory {@link File} - удаляемая директория
   * @param aProgressCounter {@link IFileCopyProgressCounter} - извещатель о степени выполнения
   * @return boolean - признак, что содержимое директория пустое
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIoRtException такая директория не существует или нет прав на удаления
   */
  public static boolean deleteDirectoryContent( File aDirectory, IFileCopyProgressCounter aProgressCounter ) {
    FileUtils.checkDirReadable( aDirectory );
    TsNullArgumentRtException.checkNull( aProgressCounter );
    IList<File> files = listFiles( aDirectory );
    for( File f : files ) {
      aProgressCounter.onFileCopyProgress( files.size(), 0 );
      if( f.isDirectory() ) {
        deleteDirectory( f, IFileCopyProgressCounter.NULL );
      }
      else {
        f.delete();
      }
    }
    aProgressCounter.onFileCopyProgress( files.size(), files.size() );
    return listFiles( aDirectory ).isEmpty();
  }

}
