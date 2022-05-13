package org.toxsoft.uskat.legacy.file;

import static org.toxsoft.uskat.legacy.file.ISkResources.*;

import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Виды ошибок файлового ввода вывода.
 * <p>
 *
 * @author goga
 */
public enum EFileIoErrorKind {

  /**
   * Не найден файл (каталог).
   */
  NOT_FOUND( "NotFound", ERR_MSG_FILEIO_NOT_FOUND ), //$NON-NLS-1$

  /**
   * Нет прав чтения из файла.
   */
  NO_READ_RIGHTS( "NoReadRights", ERR_MSG_FILEIO_NO_READ_RIGHTS ), //$NON-NLS-1$

  /**
   * Нет прав записи в файл.
   */
  NO_WRITE_RIGHTS( "NoWriteRights", ERR_MSG_FILEIO_NO_WRITE_RIGHTS ), //$NON-NLS-1$

  /**
   * Не является файлом.
   */
  NOT_A_FILE( "NotAFile", ERR_MSG_FILEIO_NOT_A_FILE ), //$NON-NLS-1$

  /**
   * Не является директорией.
   */
  NOT_A_DIR( "NotADir", ERR_MSG_FILEIO_NOT_A_DIR ), //$NON-NLS-1$

  /**
   * Указанный файл (директория) уже существует.
   */
  ALREADY_EXISTS( "AlreadyExists", ERR_MSG_ALREADY_EXISTS ), //$NON-NLS-1$

  /**
   * Ошибка при закрытии входного/выходного потока (файла).
   */
  CLOSE_FAIL( "CloseFile", ERR_MSG_CLOSE_FAIL ), //$NON-NLS-1$

  /**
   * Ошибка при создании файла (каталога).
   */
  CANT_CREATE( "CantCreate", ERR_MSG_CANT_CREATE ), //$NON-NLS-1$

  /**
   * Исключение IOException с неизвестным объектом.
   */
  IO_EXCEPTION( "IOException", ERR_MSG_IO_EXCEPTION ), //$NON-NLS-1$

  /**
   * Другая или неопознанная ошибка при работе с файлом.
   */
  GENERAL( "General", ERR_MSG_FILEIO_GENERAL_ERROR ), //$NON-NLS-1$

  /**
   * Недопустимое имя файла.
   */
  INV_NAME( "InvName", ERR_MSG_FILEIO_INV_NAME ), //$NON-NLS-1$

  ;

  /**
   * Строка-идентификатор константы.
   */
  private final String id;

  /**
   * Отображаемое сообщение, соответствующее ошибке.
   */
  private final String message;

  /**
   * Создать константу со всеми инвариантами.
   *
   * @param aId String - идентификатор
   * @param aMessage String - Отображаемое сообщение, соответствующее ошибке
   */
  EFileIoErrorKind( String aId, String aMessage ) {
    id = aId;
    message = aMessage;
  }

  /**
   * Возвращает строку-идентификатор.
   *
   * @return String - уникальный идентификатор константы
   */
  public String id() {
    return id;
  }

  /**
   * Вовзращает Отображаемое сообщение, соответствующее ошибке.
   * <p>
   * Текст сообщения составлен так, что получится осмысленное пояснение к ошиюбке простым добавлением (без пробела)
   * имени файла (каталога) в конец сообщения.
   *
   * @return String - Отображаемое сообщение, соответствующее ошибке
   */
  public String message() {
    return message;
  }

  // ----------------------------------------------------------------------------------
  // Методы проверки
  //

  /**
   * Определяет, существует ли константа с заданным идентификатором.
   *
   * @param aId String - идентификатор {@link #id()} константы
   * @return boolean - <b>true</b> - да, есть константа с таким идентификатором;<br>
   *         <b>false</b> - нет такой константы.
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static boolean isItemById( String aId ) {
    return findByIdOrNull( aId ) != null;
  }

  /**
   * Определяет, существует ли константа с заданным сообщением.
   *
   * @param aMessage String - сообщение {@link #message()} константы
   * @return boolean - <b>true</b> - да, есть константа с таким описанием;<br>
   *         <b>false</b> - нет такой константы.
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static boolean isItemByMessage( String aMessage ) {
    return findByMessageOrNull( aMessage ) != null;
  }

  // ----------------------------------------------------------------------------------
  // Методы поиска
  //

  /**
   * Находит константу с заданным идентификатором, а если нет такой константы, возвращает null.
   *
   * @param aId String - идентификатор {@link #id()} константы
   * @return Foo - найденная константа или null
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static EFileIoErrorKind findByIdOrNull( String aId ) {
    TsNullArgumentRtException.checkNull( aId );
    for( EFileIoErrorKind item : values() ) {
      if( item.id.equals( aId ) ) {
        return item;
      }
    }
    return null;
  }

  /**
   * Находит константу с заданным идентификатором, а если нет такой константы, выбрасывает исключение.
   *
   * @param aId String - идентификатор {@link #id()} константы
   * @return Foo - найденная константа или null
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким идентификатором
   */
  public static EFileIoErrorKind findById( String aId ) {
    return TsItemNotFoundRtException.checkNull( findByIdOrNull( aId ) );
  }

  /**
   * Находит константу с заданным сообщением, а если нет такой константы, возвращает null.
   *
   * @param aMessage String - сообщение {@link #message()} константы
   * @return Foo - найденная константа или null
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static EFileIoErrorKind findByMessageOrNull( String aMessage ) {
    TsNullArgumentRtException.checkNull( aMessage );
    for( EFileIoErrorKind item : values() ) {
      if( item.message.equals( aMessage ) ) {
        return item;
      }
    }
    return null;
  }

  /**
   * Находит константу с заданным сообщением, а если нет такой константы, выбрасывает исключение.
   *
   * @param aMessage String - сообщение {@link #message()} константы
   * @return Foo - найденная константа или null
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким описанием
   */
  public static EFileIoErrorKind findByMessage( String aMessage ) {
    return TsItemNotFoundRtException.checkNull( findByMessageOrNull( aMessage ) );
  }

}
