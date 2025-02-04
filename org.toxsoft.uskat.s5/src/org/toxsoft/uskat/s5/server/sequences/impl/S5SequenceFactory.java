package org.toxsoft.uskat.s5.server.sequences.impl;

import static java.lang.String.*;
import static org.toxsoft.core.tslib.bricks.strid.impl.StridUtils.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.sequences.IS5SequenceHardConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.sequences.maintenance.S5DatabaseConfig.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import java.io.*;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.utils.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.common.sysdescr.*;
import org.toxsoft.uskat.s5.server.sequences.*;
import org.toxsoft.uskat.s5.server.sequences.maintenance.*;
import org.toxsoft.uskat.s5.server.startup.*;
import org.toxsoft.uskat.s5.utils.collections.*;
import org.toxsoft.uskat.s5.utils.threads.impl.*;

/**
 * Абстрактная реализация фабрики последовательности
 *
 * @param <V> тип значения последовательности
 * @author mvk
 */
public abstract class S5SequenceFactory<V extends ITemporal<?>>
    extends Stridable
    implements IS5SequenceFactory<V>, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Начальная, неизменяемая, проектно-зависимая конфигурация
   */
  private final IS5InitialImplementation initialConfig;

  /**
   * Конфигурация подсистемы {@link S5SequenceConfig#SYBSYSTEM_ID_PREFIX}.
   */
  private final IOptionSet configuration;

  /**
   * Читатель системного описания
   */
  private final ISkSysdescrReader sysdescrReader;

  /**
   * Блокировка доступа к {@link #creators}
   */
  private final S5Lockable creatorsLock = new S5Lockable();

  /**
   * Карта конструкторов блоков значений
   * <p>
   * Ключ: полное имя класса реализации блока значений;<br>
   * Значение: статический метод создания блока; create( IParameterized aTypeInfo, Gwid aGwid, IList aValues )
   */
  private final IMapEdit<String, Method> creators = new WrapperMap<>( new HashMap<String, Method>() );

  /**
   * Блокировка доступа к {@link #creators}
   */
  private final S5Lockable constructorsLock = new S5Lockable();

  /**
   * Карта конструкторов блоков значений из курсора JDBC ({@link ResultSet})
   * <p>
   * Ключ: полное имя класса реализации блока значений ;<br>
   * Значение: конструктор блока
   */
  private final IMapEdit<String, Constructor<IS5SequenceBlockEdit<V>>> constructors =
      new WrapperMap<>( new HashMap<String, Constructor<IS5SequenceBlockEdit<V>>>() );

  /**
   * Карта идентификаторов данных.
   * <p>
   * Ключ: идентификатор данного;<br>
   * Значение: параметризованное описание типа данного
   */
  private final IMapEdit<Gwid, IParameterized> allGwids = new ElemMap<>();

  /**
   * Конструктор
   *
   * @param aId String идентификатор фабрики
   * @param aName String имя фабрики
   * @param aInitialConfig {@link IS5InitialImplementation} начальная, неизменяемая, проектно-зависимая конфигурация
   * @param aConfiguration {@link IOptionSet} конфигурация подсистемы {@link S5SequenceConfig#SYBSYSTEM_ID_PREFIX}.
   * @param aSysdescrReader {@link ISkSysdescrReader} читатель системного описания
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected S5SequenceFactory( String aId, String aName, IS5InitialImplementation aInitialConfig,
      IOptionSet aConfiguration, ISkSysdescrReader aSysdescrReader ) {
    super( aId, aName, TsLibUtils.EMPTY_STRING );
    initialConfig = TsNullArgumentRtException.checkNull( aInitialConfig );
    configuration = TsNullArgumentRtException.checkNull( aConfiguration );
    sysdescrReader = TsNullArgumentRtException.checkNull( aSysdescrReader );
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает карту известных (на данный момент идентификаторов с возможностью редактирования.
   *
   * @return {@link IMapEdit}&lt; {@link Gwid}, {@link IParameterized}&lt; редактируемая карта идентификаторов.<br>
   *         Ключ: идентификатор данного;<br>
   *         Значение: параметризованное описание типа данного
   */

  // public final IMapEdit<Gwid, IParameterized> gwidsEditor() {
  // return allGwids;
  // }

  /**
   * Удалить описание данного по указанному {@link Gwid}-идентификатору.
   * <p>
   * Если описание не существует, то ничего не делает.
   *
   * @param aGwid {@link Gwid} идентифификатор данного
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void removeTypeInfo( Gwid aGwid ) {
    TsNullArgumentRtException.checkNull( aGwid );
    synchronized (allGwids) {
      allGwids.removeByKey( aGwid );
    }
  }

  /**
   * Удалить описание данного по указанному идентификатору класса.
   * <p>
   * Если описание не существует, то ничего не делает.
   *
   * @param aClassId String идентифификатор класса
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void removeTypeInfo( String aClassId ) {
    TsNullArgumentRtException.checkNull( aClassId );
    synchronized (allGwids) {
      for( Gwid gwid : allGwids.keys() ) {
        if( gwid.classId().equals( aClassId ) ) {
          allGwids.removeByKey( gwid );
        }
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5SequenceFactory
  //
  @Override
  public final IList<IS5SequenceTableNames> tableNames() {
    return doTableNames();
  }

  @Override
  public final int getTableDepth( String aTableName ) {
    TsNullArgumentRtException.checkNull( aTableName );
    for( IS5SequenceTableNames tables : tableNames() ) {
      if( tables.blockTableName().equals( aTableName ) || tables.blobTableName().equals( aTableName ) ) {
        IAtomicValue retValue = configuration.findByKey( aTableName );
        if( retValue == null ) {
          // Глубина конкретной таблицы не найдена. Используется общая глубина хранения данных в базе данных.
          retValue = DATABASE_DEPTH.getValue( configuration );
        }
        return retValue.asInt();
      }
    }
    // Таблица не существует
    throw new TsItemNotFoundRtException( ERR_TABLE_NOT_EXSIST, aTableName );
  }

  @Override
  public final IParameterized typeInfo( Gwid aGwid ) {
    TsNullArgumentRtException.checkNull( aGwid );
    synchronized (allGwids) {
      IParameterized retValue = allGwids.findByKey( aGwid );
      if( retValue != null ) {
        // Идентификатор найден в кэше
        return retValue;
      }
      // Идентификатор не найден, запрос у наследника
      retValue = doTypeInfo( aGwid );
      allGwids.put( aGwid, retValue );
      return retValue;
    }
  }

  @Override
  public final IS5SequenceEdit<V> createSequence( Gwid aGwid, IQueryInterval aInterval,
      Iterable<IS5SequenceBlockEdit<V>> aBlocks ) {
    TsNullArgumentRtException.checkNulls( aGwid, aInterval, aBlocks );
    return doCreateSequence( aGwid, aInterval, aBlocks );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public final IS5SequenceBlockEdit<V> createBlock( Gwid aGwid, ITimedList<V> aValues ) {
    TsNullArgumentRtException.checkNulls( aGwid, aValues );
    try {
      // Параметризованное описание типа данного
      IParameterized typeInfo = typeInfo( aGwid );
      // Имя класса реализации блока
      String blockImplClass = OP_BLOCK_IMPL_CLASS.getValue( typeInfo.params() ).asString();
      // Статический метод создания блока
      Method createMethod = getCreateMethod( blockImplClass );
      // Создание блока
      return (IS5SequenceBlockEdit<V>)createMethod.invoke( null, typeInfo, aGwid, aValues );
    }
    catch( Throwable e ) {
      // Неожиданная ошибка создания блока значений
      throw new TsInternalErrorRtException( e, ERR_CREATE_BLOCK_UNEXPECTED, aGwid, cause( e ) );
    }
  }

  @Override
  public final IS5SequenceBlockEdit<V> createBlock( String aBlockImplClassName, ResultSet aResultSet ) {
    TsNullArgumentRtException.checkNulls( aBlockImplClassName, aResultSet );
    try {
      // Конструктор блока через курсор JDBC
      Constructor<IS5SequenceBlockEdit<V>> constructorMethod = getConstructorMethod( aBlockImplClassName );
      // Создание блока через курсор JDBC
      return constructorMethod.newInstance( aResultSet );
    }
    catch( Throwable e ) {
      // Неожиданная ошибка создания блока значений из курсора dbms(ResultSet)
      throw new TsInternalErrorRtException( e, ERR_CREATE_BLOCK_UNEXPECTED, aBlockImplClassName, cause( e ) );
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5SequenceValueFactory
  //
  @Override
  public final <BLOB_ARRAY> BLOB_ARRAY createValueArray( IParameterized aTypeInfo, int aSize ) {
    TsNullArgumentRtException.checkNull( aTypeInfo );
    return doCreateValueArray( aTypeInfo, aSize );
  }

  @Override
  public final Object getSyncDefaultValue( IParameterized aTypeInfo ) {
    TsNullArgumentRtException.checkNull( aTypeInfo );
    return doGetSyncDefaultValue( aTypeInfo );
  }

  @Override
  public final Object getSyncNullValue( IParameterized aTypeInfo ) {
    TsNullArgumentRtException.checkNull( aTypeInfo );
    return doGetSyncNullValue( aTypeInfo );
  }

  // ------------------------------------------------------------------------------------
  // API для наследников
  //
  /**
   * Возвращает начальную, неизменяемую, проектно-зависимую конфигурацию
   *
   * @return {@link IS5InitialImplementation} конфигурация
   */
  protected final IS5InitialImplementation initialConfig() {
    return initialConfig;
  }

  /**
   * Возвращает конфигурацию подсистемы {@link S5SequenceConfig}.
   *
   * @return {@link IS5InitialImplementation} конфигурация подсистемы
   */
  protected final IOptionSet configuration() {
    return configuration;
  }

  /**
   * Возвращает читатель системного описания
   *
   * @return {@link ISkSysdescrReader} читатель
   */
  protected final ISkSysdescrReader sysdescrReader() {
    return sysdescrReader;
  }

  /**
   * Формирует пару имен таблиц хранения значений данного из представленных классов
   *
   * @param aBlockClass {@link Class} класс реализации блока
   * @param aBlobClass {@link Class} класс реализации blob
   * @return {@link IS5SequenceTableNames} пара имен таблиц хранения значений данных
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected static final IS5SequenceTableNames tableNames( Class<? extends S5SequenceBlock<?, ?, ?>> aBlockClass,
      Class<? extends S5SequenceBlob<?, ?, ?>> aBlobClass ) {
    TsNullArgumentRtException.checkNulls( aBlockClass, aBlobClass );
    return tableNames( aBlockClass.getName(), aBlobClass.getName() );
  }

  /**
   * Формирует пару имен таблиц хранения значений данного из представленных классов
   *
   * @param aBlockClassName String полное имя класса реализации блока
   * @param aBlobClassName String полное имя класса реализации blob
   * @return {@link IS5SequenceTableNames} пара имен таблиц хранения значений данных
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected static final IS5SequenceTableNames tableNames( String aBlockClassName, String aBlobClassName ) {
    TsNullArgumentRtException.checkNulls( aBlockClassName, aBlobClassName );
    return new S5SequenceTableNames( getLast( aBlockClassName ), getLast( aBlobClassName ) );
  }

  // ------------------------------------------------------------------------------------
  // Методы для реализации наследниками
  //
  /**
   * Список имен таблиц базы данных в которых возможно хранение значений данных
   *
   * @return {@link IList}&lt;{@link IS5SequenceTableNames}&gt; список пар определяющих хранение блока и его blob
   */
  protected abstract IList<IS5SequenceTableNames> doTableNames();

  /**
   * Возвращает описание типа для указанного данного
   *
   * @param aGwid {@link Gwid} идентификатор типа
   * @return {@link IParameterized} параметризованное описание типа данного
   * @throws TsIllegalArgumentRtException несуществующее данное
   */
  protected abstract IParameterized doTypeInfo( Gwid aGwid );

  /**
   * Создание последовательности значений данного
   *
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aInterval {@link IQueryInterval} интервал времени последовательности, подробности смотри в
   *          {@link IS5Sequence#interval()}
   * @param aBlocks {@link Iterable}&lt;{@link IS5SequenceBlock}&gt; список блоков представляющих последовательность
   * @return {@link IS5SequenceEdit} последовательность с возможностью редактирования
   */
  protected abstract IS5SequenceEdit<V> doCreateSequence( Gwid aGwid, IQueryInterval aInterval,
      Iterable<IS5SequenceBlockEdit<V>> aBlocks );

  /**
   * Сформировать массив значений для блока
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aSize int количество значений в массиве
   * @return Object массив значений
   * @param <BLOB_ARRAY> тип массива blob-a в котором хранятся значения блока
   */
  protected abstract <BLOB_ARRAY> BLOB_ARRAY doCreateValueArray( IParameterized aTypeInfo, int aSize );

  /**
   * Возвращает значение по умолчанию используемое для инициализации синхронных значений в блоках
   * <p>
   * Если тип значений является примитивным типом, то для него возвращается соответствующая оболочка для примитивного
   * типа
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @return Object значение по умолчанию.
   */
  protected abstract Object doGetSyncDefaultValue( IParameterized aTypeInfo );

  /**
   * Возвращает null-значение используемое для синхронных значений в блоках
   * <p>
   * Если тип значений является примитивным типом, то для него возвращается соответствующая оболочка для примитивного
   * типа
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @return Object значение по умолчанию.
   */
  protected abstract Object doGetSyncNullValue( IParameterized aTypeInfo );

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Возвращает метод создания блока для указанного данного
   *
   * @param aBlockClass String полное имя класса реализации блока значений, наследник S5SequenceB
   * @return {@link Method} метод сигнатуры: IS5SequenceBlockEdit&lt;I, E, V&gt; create( I aInfo, IList&ltV&gt; aValues
   *         )
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException не найден метод
   */
  private Method getCreateMethod( String aBlockClass ) {
    TsNullArgumentRtException.checkNull( aBlockClass );
    Method method = null;
    // Попытка найти метод создания блоков значений в кэше
    lockRead( creatorsLock );
    try {
      method = creators.findByKey( aBlockClass );
    }
    finally {
      unlockRead( creatorsLock );
    }
    if( method == null ) {
      // Попытка найти метод создания блоков значений
      method = lookupCreateMethod( aBlockClass );
      // Сохранение метода в кэше
      lockWrite( creatorsLock );
      try {
        creators.put( aBlockClass, method );
      }
      finally {
        unlockWrite( creatorsLock );
      }
    }
    return method;
  }

  /**
   * Возвращает метод создания блока для указанного данного
   *
   * @param aBlockImplClassName String полное имя класса реализации блока значений, наследник S5SequenceB
   * @return {@link Constructor} конструктор: ({@link ResultSet} )
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException не найден метод
   */
  @SuppressWarnings( "unchecked" )
  private Constructor<IS5SequenceBlockEdit<V>> getConstructorMethod( String aBlockImplClassName ) {
    TsNullArgumentRtException.checkNull( aBlockImplClassName );
    Constructor<IS5SequenceBlockEdit<V>> constructor = null;
    // Попытка найти метод создания блоков значений в кэше
    lockRead( constructorsLock );
    try {
      constructor = constructors.findByKey( aBlockImplClassName );
    }
    finally {
      unlockRead( constructorsLock );
    }
    if( constructor == null ) {
      // Попытка найти метод создания блоков значений из курсора
      constructor = (Constructor<IS5SequenceBlockEdit<V>>)lookupConstructorMethod( aBlockImplClassName );
      // Сохранение метода в кэше
      lockWrite( constructorsLock );
      try {
        constructors.put( aBlockImplClassName, constructor );
      }
      finally {
        unlockWrite( constructorsLock );
      }
    }
    return constructor;
  }

  /**
   * Проводит поиск и возвращает метод создания блока значений
   *
   * @param aBlockImplClassName String полное имя класса реализации блока значений
   * @return {@link Method} метод создания блока значений данного
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException не найден класс реализации
   * @throws TsIllegalStateRtException не найден метод создания блоков
   */
  private static Method lookupCreateMethod( String aBlockImplClassName ) {
    TsNullArgumentRtException.checkNull( aBlockImplClassName );
    String methodName = S5SequenceBlock.BLOCK_CREATE_METHOD;
    // Определение класса реализации блока значений данного
    Class<?> blockClass = getBlockImplClass( aBlockImplClassName );
    try {
      Method[] methods = blockClass.getDeclaredMethods();
      for( Method method : methods ) {
        Class<?> paramTypes[] = method.getParameterTypes();
        if( method.getName().equals( methodName ) && //
            paramTypes.length == 3 && //
            IParameterized.class.isAssignableFrom( paramTypes[0] ) && //
            Gwid.class.equals( paramTypes[1] ) && //
            ITimedList.class.isAssignableFrom( paramTypes[2] ) ) {
          method.setAccessible( true );
          return method;
        }
      }
      // Метод не найден
      throw new NoSuchMethodException( format( ERR_METHOD_NOT_FOUND, methodName, IParameterized.class.getName(),
          Gwid.class.getName(), ITimedList.class.getName() ) );
    }
    catch( NoSuchMethodException | SecurityException e ) {
      // Не найден класс реализации
      throw new TsIllegalArgumentRtException( ERR_CREATE_METHOD_NOT_FOUND, aBlockImplClassName, methodName,
          cause( e ) );
    }
  }

  /**
   * Проводит поиск и возвращает метод создания блока значений из курсора dbms
   *
   * @param aBlockClass String полное имя класса реализации блока значений
   * @return {@link Method} метод создания блока значений данного
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException не найден класс реализации
   * @throws TsIllegalStateRtException не найден метод создания блоков
   */
  @SuppressWarnings( "nls" )
  private static Constructor<?> lookupConstructorMethod( String aBlockClass ) {
    TsNullArgumentRtException.checkNull( aBlockClass );
    String methodName = getLast( aBlockClass ) + "(" + ResultSet.class.getName() + ")";
    // Определение класса реализации блока значений данного
    Class<?> blockClass = getBlockImplClass( aBlockClass );
    try {
      Constructor<?> retValue = blockClass.getDeclaredConstructor( ResultSet.class );
      retValue.setAccessible( true );
      return retValue;
    }
    catch( NoSuchMethodException | SecurityException e ) {
      // Не найден класс реализации
      throw new TsIllegalArgumentRtException( ERR_CREATE_METHOD_NOT_FOUND, aBlockClass, methodName, cause( e ) );
    }
  }

  /**
   * Проводит поиск и возвращает класс реализации блока значений
   *
   * @param aBlockImplClassName String полное имя класса реализации блока значений
   * @return Class класс реализации блока значений данного
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException не найден класс реализации
   */
  private static Class<?> getBlockImplClass( String aBlockImplClassName ) {
    TsNullArgumentRtException.checkNull( aBlockImplClassName );
    try {
      return Class.forName( aBlockImplClassName );
    }
    catch( ClassNotFoundException e ) {
      // Не найден класс реализации
      throw new TsIllegalArgumentRtException( ERR_BLOCK_IMPL_NOT_FOUND, aBlockImplClassName, cause( e ) );
    }
  }
}
