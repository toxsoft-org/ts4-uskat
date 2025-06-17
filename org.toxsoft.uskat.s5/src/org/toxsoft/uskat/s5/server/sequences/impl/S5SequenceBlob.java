package org.toxsoft.uskat.s5.server.sequences.impl;

import static java.lang.String.*;
import static org.toxsoft.core.tslib.bricks.strid.impl.StridUtils.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.S5DataID.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceBlock.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceSQL.*;

import java.io.*;
import java.lang.reflect.*;
import java.lang.reflect.Array;
import java.sql.*;

import javax.persistence.*;

import org.toxsoft.core.tslib.bricks.validator.vrl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Абстрактная реализация хранения данных блоков в blob.
 *
 * @author mvk
 * @param <BLOCK> блок данных которому принадлежат значения blob
 * @param <BLOB_ARRAY> тип массива используемый для хранения значений, например double[]
 * @param <BLOB_ARRAY_HOLDER> тип объекта хранящий массив значений
 */
@MappedSuperclass
@Inheritance( strategy = InheritanceType.TABLE_PER_CLASS )
public class S5SequenceBlob<BLOCK extends S5SequenceBlock<?, ?, ?>, BLOB_ARRAY, BLOB_ARRAY_HOLDER>
    implements Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Значения блока
   */
  public static final String FIELD_VALUES = "_values"; //$NON-NLS-1$

  /**
   * Первичный составной (gwid,startTime) ключ
   */
  @EmbeddedId
  private S5DataID id;

  /**
   * Блок
   */
  @MapsId
  @OneToOne( mappedBy = FIELD_BLOB )
  @JoinColumns( value = { //
      @JoinColumn( name = FIELD_GWID ), //
      @JoinColumn( name = FIELD_START_TIME ) } )
  private BLOCK block;

  /**
   * Значения хранимые в блоке в сериализованном виде
   */
  @Column( insertable = true, updatable = true, nullable = false, unique = false, length = Integer.MAX_VALUE )
  private BLOB_ARRAY_HOLDER _values;

  /**
   * Значения хранимые в блоке
   */
  private transient BLOB_ARRAY values;

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5SequenceBlob() {
  }

  /**
   * Конструктор blob для нового блока
   *
   * @param aValues BLOB_ARRAY массив значений
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected S5SequenceBlob( BLOB_ARRAY aValues ) {
    TsNullArgumentRtException.checkNull( aValues );
    setValues( aValues );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  @SuppressWarnings( "unchecked" )
  protected S5SequenceBlob( ResultSet aResultSet ) {
    try {
      id = new S5DataID( aResultSet );
      if( getGenericClass( 1 ) != byte[].class ) {
        try( InputStream is = aResultSet.getBinaryStream( FIELD_VALUES ) ) {
          _values = ((BLOB_ARRAY_HOLDER)new ObjectInputStream( is ).readObject());
        }
      }
      else {
        _values = (BLOB_ARRAY_HOLDER)aResultSet.getBytes( FIELD_VALUES );
      }
    }
    catch( Throwable e ) {
      // Неожиданная ошибка создания blob из курсора dbms
      throw new TsInternalErrorRtException( e, ERR_CREATE_BLOB_FROM_CURSOR, cause( e ) );
    }
  }

  // ------------------------------------------------------------------------------------
  // Методы для переопределения наследниками
  //
  /**
   * Провести сериализацию массива значений
   *
   * @param aValues BLOB_ARRAY массив значений
   * @return BLOB_ARRAY_HOLDER массив значений в сериализованном виде
   */
  @SuppressWarnings( "unchecked" )
  protected BLOB_ARRAY_HOLDER doSerialize( BLOB_ARRAY aValues ) {
    return (BLOB_ARRAY_HOLDER)aValues;
  }

  /**
   * Провести десериализацию массива значений
   *
   * @param aValues BLOB_ARRAY_HOLDER массив значений в сериализованном виде
   * @return BLOB_ARRAY массив значений
   */
  @SuppressWarnings( "unchecked" )
  protected BLOB_ARRAY doDeserialize( BLOB_ARRAY_HOLDER aValues ) {
    return (BLOB_ARRAY)aValues;
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Возвращает идентификатор blob
   *
   * @return {@link S5DataID} идентификатор данного
   */
  final S5DataID id() {
    return id;
  }

  /**
   * Возвращает блок значений которому принадлежат значения blob
   *
   * @return BLOCK блок значений. null: неопределен
   */
  final BLOCK block() {
    return block;
  }

  /**
   * Устанавливает блок значений которому принадлежат значения blob
   *
   * @param aBlock BLOCK блок значений
   * @throws TsNullArgumentRtException аргумент = null
   */
  @SuppressWarnings( "unchecked" )
  final void setBlockEntity( Object aBlock ) {
    TsNullArgumentRtException.checkNull( aBlock );
    block = (BLOCK)aBlock;
    id = block.id();
  }

  /**
   * Возвращает значения блока
   *
   * @return BLOB_ARRAY массив значений
   */
  final BLOB_ARRAY values() {
    if( values == null ) {
      values = doDeserialize( _values );
    }
    return values;
  }

  /**
   * Установка значений blob
   *
   * @param aValues BLOB_ARRAY массив значений blob
   * @throws TsNullArgumentRtException аргумент = null
   */
  final void setValues( BLOB_ARRAY aValues ) {
    TsNullArgumentRtException.checkNull( aValues );
    values = aValues;
    _values = doSerialize( aValues );
  }

  /**
   * Проводит валидацию (исправление содержимого blob) если это необходимо
   *
   * @return {@link IVrList} результаты валидации
   */
  @SuppressWarnings( "static-method" )
  final IVrList validation() {
    return new VrList();
  }

  /**
   * Выполняет операцию записи blob в базу данных
   *
   * @param aEntityManager {@link AbstractSkObjectManager} менеджер постоянства
   * @throws TsNullArgumentRtException аргумент = null
   */
  final void executeInsert( EntityManager aEntityManager ) {
    TsNullArgumentRtException.checkNull( aEntityManager );
    // Имя таблицы реализации блока
    String tableName = getLast( getClass().getName() );
    // Параметры запроса. Ключ: имя поля. Значение: значение поля
    IStringMap<Object> params = doInsertQueryParams();
    // Создание запроса
    Query query = createInsertQuery( aEntityManager, tableName, params );
    // Выполнение запроса
    query.executeUpdate();
  }

  // ------------------------------------------------------------------------------------
  // Реализация Object
  //
  @Override
  public String toString() {
    BLOB_ARRAY v = values();
    String vs = (v != null ? String.valueOf( Array.getLength( v ) ) : null);
    return format( BLOB_TO_STRING_FORMAT, getClass().getSimpleName(), id(), vs );
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  //
  /**
   * Возвращает параметры необходимые для выполнения запроса записи blob в БД
   * <p>
   * Наследники могут переопределять метод для добавления собственных параметров к уже определенным в базовом классе
   *
   * @return {@link IStringMap} карта параметров с возможностью добавления. <br>
   *         Ключ: имя поля в таблице;<br>
   *         Значение: значение поля.
   */
  protected IStringMapEdit<Object> doInsertQueryParams() {
    IStringMapEdit<Object> retValue = new StringMap<>();
    retValue.put( FIELD_GWID, id.gwid().toString() );
    retValue.put( FIELD_START_TIME, id.startTime() );
    // retValue.put( FIELD_VALUES, _values );
    retValue.put( FIELD_VALUES, new byte[0] );
    return retValue;
  }

  /**
   * Возвращает параметры необходимые для выполнения запроса обновления blob в БД
   * <p>
   * Наследники могут переопределять метод для добавления собственных параметров к уже определенным в базовом классе
   *
   * @return {@link IStringMap} карта параметров с возможностью добавления. <br>
   *         Ключ: имя поля в таблице;<br>
   *         Значение: значение поля.
   */
  protected IStringMapEdit<Object> doUpdateQueryParams() {
    IStringMapEdit<Object> retValue = new StringMap<>();
    // retValue.put( FIELD_VALUES, _values );
    retValue.put( FIELD_VALUES, new byte[0] );
    return retValue;
  }

  /**
   * Возвращает класс generic-класса использумого наследником
   *
   * @param aParamIndex int индекс generic-класса в списке параметров
   * @return Class<?> generic-класс
   */
  private Class<?> getGenericClass( int aParamIndex ) {
    Class<?> genericClass =
        (Class<?>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[aParamIndex];
    return genericClass;
  }
}
