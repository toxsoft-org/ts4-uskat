package org.toxsoft.uskat.legacy.file;

import java.io.File;

import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Неизменяемый класс - информация о пути в файловой системе, представленный в виде текста.
 * <p>
 * Класс предназначен для инкапсулирования информации о файловом объекте на основании анализа пути, заданной в текстовом
 * виде. Кроме того, метод {@link #normalizedPath} возвращает абсолютный путь к файловому объекту, и если объект
 * (предположительно или действительно) директория, в конце будет {@link File#separatorChar}.
 * <p>
 * Много кода в этом файл взято из исходника <code>org.apache.commons.io.FilenameUtils.java</code>
 *
 * @author goga
 */
public class PathStringInfo {

  private final String  originalPath;
  private final String  normalizedPath;
  private final File    file;
  private final boolean isProbableFile;
  private final boolean isProbableDir;
  /**
   * Предполагаем относительный путь, в getXxxPrefixLength() определим, если путь абсолютный.
   */
  private boolean       isAbsolute = false;

  /**
   * Создает информацию о заданном пути.
   *
   * @param aPath String - путь к файловому объекту
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException недопустимое имя файлового объекта
   */
  public PathStringInfo( String aPath ) {
    originalPath = TsNullArgumentRtException.checkNull( aPath );
    file = new File( aPath );
    if( file.exists() ) {
      isProbableFile = file.isFile();
      isProbableDir = file.isDirectory();
    }
    else {
      if( aPath.isEmpty() ) {
        isProbableDir = true;
        isProbableFile = false;
      }
      else {
        isProbableDir = aPath.endsWith( File.separator );
        isProbableFile = !isProbableDir;
      }
    }
    normalizedPath = doNormalize( aPath, isProbableDir );
  }

  // ------------------------------------------------------------------------------------
  // Внутренные методы
  //

  /**
   * The Unix separator character.
   */
  private static final char UNIX_SEPARATOR = '/';

  /**
   * The Windows separator character.
   */
  private static final char WINDOWS_SEPARATOR = '\\';

  private static boolean isUnix() {
    return File.separatorChar == UNIX_SEPARATOR;
  }

  private static boolean isDoubleDotSlash( char[] aArray, int aIndex ) {
    return aArray[aIndex] == File.separatorChar && aArray[aIndex - 1] == '.' && aArray[aIndex - 2] == '.';
  }

  private static boolean isDotSlash( char[] aArray, int aIndex ) {
    return aArray[aIndex] == File.separatorChar && aArray[aIndex - 1] == '.';
  }

  /**
   * Возвращает длину префикса в UNIX системе
   * <p>
   * В этом методе определяется значение признака {@link #isAbsolute} для UNIX системы.
   *
   * @param aFileName String - имя файлового объекта (не null, и не пустая строка)
   * @return int - длина префикса
   */
  private int getUnixPrefixLength( String aFileName ) {
    switch( aFileName.charAt( 0 ) ) {
      case '~':
        int len = aFileName.length();
        if( len == 1 ) {
          return 2; // строка "~"
        }
        int sepPos = aFileName.indexOf( UNIX_SEPARATOR );
        if( sepPos == -1 ) { // строка вида "~user"
          return len + 1;
        }
        return sepPos + 1; // строка вида "~/a/b/c.txt" или "~user/a/b/c.txt"
      case UNIX_SEPARATOR: // абсолютный путь
        isAbsolute = true;
        return 1;
      default: // относительный путь
        return 0;
    }
  }

  /**
   * Возвращает длину префикса в Windows системе
   * <p>
   * В этом методе определяется значение признака {@link #isAbsolute} для Windows системы.
   *
   * @param aFileName String - имя файлового объекта (не null, и не пустая строка)
   * @return int - длина префикса
   */
  private int getWindowsPrefixLength( String aFileName ) {
    int len = aFileName.length();
    char ch0 = aFileName.charAt( 0 );
    if( ch0 == ':' ) {
      return -1;
    }
    if( len == 1 ) {
      if( ch0 == WINDOWS_SEPARATOR ) {
        isAbsolute = true;
        return 1; // строка "\" корень текущего диска, абсолютный путь
      }
      return 0; // строка "c" - относительный путь к файлу с однобуквенным именем
    }
    char ch1 = aFileName.charAt( 1 );
    if( ch1 == ':' ) { // есть перфикс "c:"
      ch0 = Character.toUpperCase( ch0 );
      if( ch0 >= 'A' && ch0 <= 'Z' ) {
        if( len == 2 ) {
          return 2; // строка "D:"
        }
        if( aFileName.charAt( 2 ) != WINDOWS_SEPARATOR ) {
          return 2; // относительный путь на заданном диске "D:a\b\c.txt"
        }
        isAbsolute = true;
        return 3; // абсолютный путь на заданном диске "C:\a\b\c.txt"
      }
      return -1; // первый символ перед ':' должна была быть ьуквой
    }
    if( ch0 == WINDOWS_SEPARATOR || ch1 == WINDOWS_SEPARATOR ) { // проверим на UNC имя "\\Server\путь"
      int posWin = aFileName.indexOf( WINDOWS_SEPARATOR, 2 );
      if( posWin == -1 || posWin == 2 ) {
        return -1; // это не UNC имя, хоть и начинается с двух обратных слэшов
      }
      isAbsolute = true;
      return posWin + 1; // UNC имя "\\Server\путь"
    }
    if( ch0 == WINDOWS_SEPARATOR ) {
      isAbsolute = true;
      return 1; // абсолютный путь на текущем диске
    }
    return 0; // относительный путь
  }

  /**
   * Возвращает длину парефикса в имени файла, типа <code>C:/</code> или <code>~/</code>.
   * <p>
   * Этот метод обрабатывает имя файла в формате текущей операционной системы.
   * <p>
   * Примеры префиксов имен файлов:
   *
   * <pre>
   * Windows:
   * a\b\c.txt           --> ""          --> относительный путь
   * \a\b\c.txt          --> "\"         --> абсолютный путь на текущем диске
   * C:a\b\c.txt         --> "C:"        --> относительный путь на указанном диске
   * C:\a\b\c.txt        --> "C:\"       --> абсолютный путь
   * \\server\a\b\c.txt  --> "\\server\" --> UNC формат имени файла
   *
   * Unix:
   * a/b/c.txt           --> ""          --> относительный путь
   * /a/b/c.txt          --> "/"         --> абсолютный путь
   * ~/a/b/c.txt         --> "~/"        --> абсолютный путь в домашней директории текущего пользователя
   * ~                   --> "~/"        --> домашняя директория текущего пользователя (добавлен разделитель в конце)
   * ~user/a/b/c.txt     --> "~user/"    --> абсолютный путь в домашней директории указаного пользователя
   * ~user               --> "~user/"    --> домашняя директория указанного пользователя (добавлен разделитель в конце)
   * </pre>
   * <p>
   * Полученное значение можно использовать для извлечения префикса (с начала строки длиной в полученное значение).<br>
   * <b>Обратите внимание</b>,что в есть случай, когда возвращается значение <b>больше</b> длины переданного имени
   * файла. В этих случаях (которые только в UNIX), предполагается добавление разделителя в конец полученного префикса.
   * <p>
   * В этом методе определяется значение признака {@link #isAbsolute}.
   *
   * @param aFileName String - анализируемое имя файла
   * @return int - длина префикса или -1 если неверен формат имени файла
   * @throws TsNullArgumentRtException аргумент = null
   */
  public int getPrefixLength( String aFileName ) {
    TsNullArgumentRtException.checkNull( aFileName );
    int len = aFileName.length();
    if( len == 0 ) { // пустая строка ""
      return 0;
    }
    if( isUnix() ) {
      return getUnixPrefixLength( aFileName );
    }
    return getWindowsPrefixLength( aFileName );
  }

  /**
   * Internal method to perform the normalization.
   *
   * @param aFileName the filename
   * @param aEnsureLastSeparator boolean - завершать символом {@link File#separatorChar}
   * @return the normalized filename
   * @throws TsIoRtException недопустимое имя файлового объекта
   */
  private String doNormalize( String aFileName, boolean aEnsureLastSeparator ) {
    int size = aFileName.length();
    if( size == 0 ) {
      return aFileName;
    }
    int prefixLen = getPrefixLength( aFileName );
    if( prefixLen < 0 ) {
      throw new TsIllegalArgumentRtException( aFileName );
    }

    char[] array = new char[size + 2]; // +1 for possible extra slash, +2 for arraycopy
    aFileName.getChars( 0, aFileName.length(), array, 0 );

    // add extra separator on the end to simplify code below
    if( array[size - 1] != File.separatorChar ) {
      array[size++] = File.separatorChar;
    }

    // adjoining slashes
    for( int i = prefixLen + 1; i < size; i++ ) {
      if( array[i] == File.separatorChar && array[i - 1] == File.separatorChar ) {
        System.arraycopy( array, i, array, i - 1, size - i );
        size--;
        i--;
      }
    }

    // dot slash
    for( int i = prefixLen + 1; i < size; i++ ) {
      if( isDotSlash( array, i ) && (i == prefixLen + 1 || array[i - 2] == File.separatorChar) ) {
        System.arraycopy( array, i + 1, array, i - 1, size - i );
        size -= 2;
        i--;
      }
    }

    // обработка содержащихся в пути подъема на один уровень вверх "../"
    int start = prefixLen;
    // для относительного пути пропустим начальные подъемы вверх
    while( start < size && isDoubleDotSlash( array, start + 2 ) ) {
      if( isAbsolute ) { // абсолютный путь не может начинаться с продъема вверх, ведь выше точно некуда!
        throw new TsIllegalArgumentRtException( aFileName );
      }
      start += 3;
    }
    // удалим содержащейся внутри пути подъемы вверх
    outer:
    for( int i = start + 2; i < size; i++ ) {
      if( isDoubleDotSlash( array, i ) && (i == start + 2 || array[i - 3] == File.separatorChar) ) {
        int j;
        for( j = i - 4; j >= start; j-- ) {
          if( array[j] == File.separatorChar ) {
            // remove b/../ from a/b/../c
            System.arraycopy( array, i + 1, array, j + 1, size - i );
            size -= (i - j);
            i = j + 1;
            continue outer;
          }
        }
        // remove a/../ from a/../c
        System.arraycopy( array, i + 1, array, start, size - i );
        size -= (i + 1 - start);
        i = start + 1;
      }
    }

    if( size <= 0 ) { // should never be less than 0
      return TsLibUtils.EMPTY_STRING;
    }
    if( size <= prefixLen ) { // should never be less than prefix
      return new String( array, 0, size );
    }
    array[size] = File.separatorChar;
    if( aEnsureLastSeparator ) {
      return new String( array, 0, size ); // keep trailing separator
    }
    return new String( array, 0, size - 1 ); // lose trailing separator
  }

  /**
   * Возвращает оригинальный путь - аргумент конструктора {@link #PathStringInfo(String)}.
   *
   * @return String - оригинальный путь
   */
  public String originalPath() {
    return originalPath;
  }

  /**
   * Возвращает путь в нормализованном виде.
   * <p>
   * Нормализация пути - чисто синтаксическая обработка строки и включает в себя следующее:
   * <ul>
   * <li>удаление повторяющихся разделителей {@link File#separatorChar} (кроме начала UNC-пути в Windows вида
   * \\ServerName\path);</li>
   * <li>удаление ненужных ссылок на текущую директорию "./" в пути;</li>
   * <li>удаление спуска/подъема на уровни вида "b/c/../../" в пути вида "a/b/c/../../d/file.ext";</li>
   * <li>наличие для директории (и отсутствие для файлов) разделителя {@link File#separatorChar} в конце
   * нормализованного пути.</li>
   * </ul>
   *
   * @return String - путь в нормализованном виде
   */
  public String normalizedPath() {
    return normalizedPath;
  }

  /**
   * Возвращает признак, что что путь указывает на файл.
   * <p>
   * Если такой объект в системе реально сущствует - возвращается информация о существующем объекте.
   * <p>
   * Если такой объект не существует, делаются следующие предположения о том, файл это, или директория:
   * <ul>
   * <li>пустая строка всегда считается директорией;</li>
   * <li>строка, заканчивающейся на {@link File#separatorChar} счтается директорией, все другие - считаются
   * файлами.</li>
   * </ul>
   *
   * @return boolean - признак, что объект заданный путем, (предположительно) является файлом
   */
  public boolean isFile() {
    return isProbableFile;
  }

  /**
   * Возвращает признак, что что путь указывает на директорию.
   * <p>
   * Если такой объект в системе реально сущствует - возвращается информация о существующем объекте.
   * <p>
   * Если такой объект не существует, делаются следующие предположения о том, файл это, или директория:
   * <ul>
   * <li>пустая строка всегда считается директорией;</li>
   * <li>строка, заканчивающейся на {@link File#separatorChar} счтается директорией, все другие - считаются
   * файлами.</li>
   * </ul>
   *
   * @return boolean - признак, что объект заданный путем, (предположительно) является директорией
   */
  public boolean isDir() {
    return isProbableDir;
  }

  /**
   * Возвращает ссылку на файл, созданный аргментом конструктора.
   *
   * @return {@link File} - файл, созданный аргментом конструктора
   */
  public File file() {
    return file;
  }

  /**
   * Возвращает признак того, путь является абсолютным.
   * <p>
   * Абсолютныйм считается путь, не зависящий от текущей директории:
   * <ul>
   * <li>В UNIX это просто - если путь начинается в разделителя '/' - он относительный, все остальные пути (включая те,
   * которые начинаются с тильды '~' (символ домашнего каталога пользователя) считаются не-абсолютными (относительными);
   * </li>
   * <li>В Windows ситуация сложнее - UNC-пути (вида "\\Server\path") и пути начинающейся с имени диска и разделителя
   * "X:\" считаются абсолютными, пути без имени диска - относительными. А вот путь вида "X:a\b\c", содержащий имя
   * диска, но не разделитель, тоже считаются относительными, хотя диск и жестко задан, но ведь диретория на диске
   * отсчитывается относительно.</li>
   * </ul>
   *
   * @return boolean - признак того, путь является абсолютным
   */
  public boolean isAbsolute() {
    return isAbsolute;
  }

  // @SuppressWarnings("nls")
  // void print() {
  // try {
  // TsTestUtils.pl( getClass().getSimpleName() );
  // TsTestUtils.pl( " originalPath: %s", originalPath );
  // TsTestUtils.pl( " normalizedPath: %s", normalizedPath );
  // TsTestUtils.pl( " canonicalPath: %s", file.getCanonicalPath() );
  // TsTestUtils.pl( " absolutePath: %s", file.getAbsolutePath() );
  // TsTestUtils.pl( " isProbableFile: %s", Boolean.toString( isProbableFile ) );
  // TsTestUtils.pl( " isProbableDir: %s", Boolean.toString( isProbableDir ) );
  // TsTestUtils.pl( " isAbsolute: %s", Boolean.toString( isAbsolute ) );
  // }
  // catch( IOException e ) {
  // e.printStackTrace();
  // }
  // }
  //
  // @SuppressWarnings({ "nls", "javadoc" })
  // public static void main( String[] aArgs ) {
  // PathStringInfo p;
  // p = new PathStringInfo( "" );
  // p.print();
  // p = new PathStringInfo( "/a/bbb/c/d.txt" );
  // p.print();
  // p = new PathStringInfo( "/a/bbb/../c/d.txt" );
  // p.print();
  // p = new PathStringInfo( "/a/bbb/ee/ffffff/../../../c1/../d.txt" );
  // p.print();
  // p = new PathStringInfo( "./../../home/goga" );
  // p.print();
  // p = new PathStringInfo( "../../home/goga" );
  // p.print();
  // p = new PathStringInfo( "" );
  // p.print();
  //
  // String s = "0x123ABC 123 456f";
  // ICharInputStream chIn = new CharInputStreamString( s );
  // IStridReader sr = new StridReader( chIn );
  // for( int i = 0; i < 3; i++ ) {
  // long n = sr.readLong();
  // TsTestUtils.pl( "long n = 0x%X", n );
  // }
  //
  // }

}
