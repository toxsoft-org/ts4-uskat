package org.toxsoft.uskat.legacy.file;

import static org.toxsoft.uskat.legacy.file.ISkResources.*;

import java.io.File;

import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;

/**
 * Типы фаловых систем. Известны следующие типы: UNIX (Linux), Windows и все остальные.
 *
 * @author goga
 */
public enum EFileSystemType {

  /**
   * Не-Windows и не-UNIX подобная файловая система.
   */
  OTHER( "OtherOS", E_EFS_TYPE_OTHER ), //$NON-NLS-1$

  /**
   * UNIX подобная файловая система.
   */
  UNIX( "Unix", E_EFS_TYPE_UNIX ), //$NON-NLS-1$

  /**
   * Windows подобная файловая система.
   */
  WINDOWS( "Windows", E_EFS_TYPE_WINDOWS ); //$NON-NLS-1$

  /**
   * Определяет тип файловой системы.
   *
   * @return EFileStsyemType тип файловой системы
   */
  public static EFileSystemType currentFsType() {
    if( File.separatorChar == '/' ) {
      return UNIX;
    }
    if( File.separatorChar == '\\' ) {
      return WINDOWS;
    }
    return OTHER;
  }

  /**
   * Строка-идентификатор константы.
   */
  private final String id;

  /**
   * Отображаемоей описание константы.
   */
  private final String description;

  /**
   * Создать константу со всеми инвариантами.
   *
   * @param aId String - идентификатор константы
   * @param aDesctiption String - описание константы
   */
  EFileSystemType( String aId, String aDesctiption ) {
    id = aId;
    description = aDesctiption;
  }

  /**
   * Возвращает строку-идентификатор константы.
   *
   * @return String - уникальный идентификатор константы
   */
  public String getId() {
    return id;
  }

  /**
   * Вовзращает краткое описание константы.
   *
   * @return String - отображаемое описание
   */
  public String getDescription() {
    return description;
  }

  /**
   * Возвращает константу по ее идентификатору.
   *
   * @param aId String - идентификатор константы
   * @return EFileSystemType - консткнту, соответствющую идентификатору
   * @throws TsItemNotFoundRtException - если нет константы по заданному идентификатору
   */
  public static EFileSystemType findById( String aId ) {
    for( EFileSystemType e : EFileSystemType.values() ) {
      if( e.id.equals( aId ) ) {
        return e;
      }
    }
    throw new TsItemNotFoundRtException();
  }

  /**
   * Получить константу по его идентификатору.
   *
   * @param aDescription String - описание константы
   * @return EFileSystemType - консткнту, соответствющую описанию
   * @throws TsItemNotFoundRtException - если нет константы по заданному описанию
   */
  public static EFileSystemType findByDescription( String aDescription ) {
    for( EFileSystemType e : EFileSystemType.values() ) {
      if( e.description.equals( aDescription ) ) {
        return e;
      }
    }
    throw new TsItemNotFoundRtException();
  }

}
