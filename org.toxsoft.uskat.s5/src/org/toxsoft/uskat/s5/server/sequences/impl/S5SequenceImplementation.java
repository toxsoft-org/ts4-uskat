package org.toxsoft.uskat.s5.server.sequences.impl;

import static java.lang.String.*;
import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.IS5Resources.*;

import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceImplementation;

/**
 * Реализация {@link IS5SequenceImplementation}
 *
 * @author mvk
 */
public final class S5SequenceImplementation
    implements IS5SequenceImplementation {

  private final String blockClassName;
  private final String blobClassName;
  private final int    tableCount;

  private static final ILogger logger = getLogger( S5SequenceImplementation.class );

  /**
   * Конструктор для описания хранения в одной таблице
   *
   * @param aBlockClass Class класс реализации хранения блока последовательности значений
   * @param aBlobClass Class класс реализации хранения blob последовательности значений
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SequenceImplementation( Class<?> aBlockClass, Class<?> aBlobClass ) {
    TsNullArgumentRtException.checkNulls( aBlobClass, aBlobClass );
    blockClassName = aBlockClass.getName();
    blobClassName = aBlobClass.getName();
    tableCount = 1;
  }

  /**
   * Конструктор для описания хранения в нескольких таблицах
   * <p>
   * Для определения хранения значений данного в нескольких таблицах, указывается класс первой таблицы с индексом 0 и их
   * количество например:<br>
   * <code>
   * IS5SequenceImplementation info = new S5SequenceImplementation( S5HistDataAsyncBooleanEntity.class, S5HistDataAsyncBooleanBlobEntity.class,  10 );
   * </code>
   *
   * @param aBlockClass Class класс реализации хранения блока последовательности значений
   * @param aBlobClass Class класс реализации хранения blob последовательности значений
   * @param aTableCount int количество таблиц хранящих последовательности значений
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException имя класса реализации должен завершаться индексом 0
   */
  public S5SequenceImplementation( Class<?> aBlockClass, Class<?> aBlobClass, int aTableCount ) {
    TsNullArgumentRtException.checkNulls( aBlobClass, aBlobClass );
    checkExistClasses( aBlockClass, aTableCount );
    checkExistClasses( aBlobClass, aTableCount );
    blockClassName = tablePrefix( aBlockClass );
    blobClassName = tablePrefix( aBlobClass );
    tableCount = aTableCount;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5SequenceImplementation
  //
  @Override
  public String blockClassName() {
    return blockClassName;
  }

  @Override
  public String blobClassName() {
    return blobClassName;
  }

  @Override
  public int tableCount() {
    return tableCount;
  }

  // ------------------------------------------------------------------------------------
  // Object
  //
  @Override
  public String toString() {
    return format( "%s.%s[%d]", blockClassName, blobClassName, Integer.valueOf( tableCount ) ); //$NON-NLS-1$
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + blockClassName.hashCode();
    result = TsLibUtils.PRIME * result + blobClassName.hashCode();
    result = TsLibUtils.PRIME * result + (tableCount ^ (tableCount >>> 32));
    return result;
  }

  @Override
  public boolean equals( Object aObject ) {
    if( this == aObject ) {
      return true;
    }
    if( aObject == null ) {
      return false;
    }
    if( getClass() != aObject.getClass() ) {
      return false;
    }
    S5SequenceImplementation other = (S5SequenceImplementation)aObject;
    if( !blockClassName.equals( other.blockClassName ) ) {
      return false;
    }
    if( !blobClassName.equals( other.blobClassName ) ) {
      return false;
    }
    if( tableCount != other.tableCount ) {
      return false;
    }
    return true;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Возвращает префикс имени таблицы хранения значений
   *
   * @param aTableClass Class класс таблицы
   * @return String префикс таблицы хранения
   * @throws TsNullArgumentRtException аругмент = null
   * @throws TsIllegalArgumentRtException имя класса реализации должен завершаться индексом 0
   */
  private static String tablePrefix( Class<?> aTableClass ) {
    TsNullArgumentRtException.checkNull( aTableClass );
    String tableName = aTableClass.getName();
    TsIllegalArgumentRtException.checkFalse( tableName.charAt( tableName.length() - 1 ) == '0' );
    return tableName.substring( 0, tableName.length() - 1 );
  }

  /**
   * Проверяет существование класса
   *
   * @param aClass Class первый класс, должен иметь индекс 0
   * @param aTableCount int количество проверяемых классов
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException имя класса реализации должен завершаться индексом 0
   * @throws TsIllegalArgumentRtException класс не найден
   */
  private static void checkExistClasses( Class<?> aClass, int aTableCount ) {
    TsNullArgumentRtException.checkNull( aClass );
    String tablePrefix = tablePrefix( aClass );
    for( int index = 0; index < aTableCount; index++ ) {
      String className = tablePrefix + index;
      try {
        Class.forName( className );
      }
      catch( ClassNotFoundException e ) {
        // Класс не найден
        logger.error( e, ERR_SEQUENCE_IMPL_NOT_FOUND, className );
      }
    }
  }
}
