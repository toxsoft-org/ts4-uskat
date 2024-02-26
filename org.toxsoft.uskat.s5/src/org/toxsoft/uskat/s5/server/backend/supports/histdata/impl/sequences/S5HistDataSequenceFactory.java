package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.impl.dto.IDtoHardConstants.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5HistDataHardConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.IS5Resources.*;
import static org.toxsoft.uskat.s5.utils.IS5HardConstants.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants;
import org.toxsoft.core.tslib.av.metainfo.IDataType;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.av.utils.IParameterizedEdit;
import org.toxsoft.core.tslib.bricks.strid.impl.StridableParameterized;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoRtdataInfo;
import org.toxsoft.uskat.s5.common.sysdescr.ISkSysdescrReader;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5HistDataHardConstants;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.async.*;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.sync.*;
import org.toxsoft.uskat.s5.server.sequences.*;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceFactory;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceImplementation;
import org.toxsoft.uskat.s5.server.startup.IS5InitialImplementation;

/**
 * Фабрика формирования последовательности блоков исторических данных
 *
 * @author mvk
 */
public final class S5HistDataSequenceFactory
    extends S5SequenceFactory<ITemporalAtomicValue> {

  private static final long serialVersionUID = 157157L;

  /**
   * Идентификатор фабрики
   */
  private static final String ID = "histdata"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // null-значения примитивных типов
  //
  /**
   * Значение примитивного типа boolean: true
   */
  public static final byte BOOLEAN_TRUE = 1;

  /**
   * Значение примитивного типа boolean: false
   */
  public static final byte BOOLEAN_FALSE = 0;

  /**
   * Значение примитивного типа boolean: null
   */
  public static final byte BOOLEAN_NULL = -1;

  /**
   * Значение примитивного типа long: null
   */
  public static final long LONG_NULL = Long.MIN_VALUE + 1;

  /**
   * Значение примитивного типа double: null
   */
  public static final double DOUBLE_NULL = -Double.MAX_VALUE + 1;

  // ------------------------------------------------------------------------------------
  // Оболочки для null-значения примитивных типов
  //
  /**
   * Значение-оболочка примитивного типа boolean: true
   */
  @SuppressWarnings( "unused" )
  private static final Byte Byte_BOOLEAN_TRUE = Byte.valueOf( BOOLEAN_TRUE );

  /**
   * Значение-оболочка примитивного типа boolean: false
   */
  @SuppressWarnings( "unused" )
  private static final Byte Byte_BOOLEAN_FALSE = Byte.valueOf( BOOLEAN_FALSE );

  /**
   * Значение-оболочка примитивного типа boolean: null
   */
  private static final Byte Byte_BOOLEAN_NULL = Byte.valueOf( BOOLEAN_NULL );

  /**
   * Значение примитивного типа long: null
   */
  private static final Long Long_LONG_NULL = Long.valueOf( LONG_NULL );

  /**
   * Значение примитивного типа double: null
   */
  private static final Double Double_DOUBLE_NULL = Double.valueOf( DOUBLE_NULL );

  /**
   * Значение объектного типа: null
   */
  private static final Object VALOBJ_NULL = new Object();

  /**
   * Конструктор
   *
   * @param aInitialConfig {@link IS5InitialImplementation} начальная, неизменяемая, проектно-зависимая конфигурация
   * @param aSysdescrReader {@link ISkSysdescrReader} читатель системного описания
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5HistDataSequenceFactory( IS5InitialImplementation aInitialConfig, ISkSysdescrReader aSysdescrReader ) {
    super( ID, STR_D_HISTDATA_FACTORY, aInitialConfig, aSysdescrReader );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5SequenceFactory
  //
  @Override
  protected IList<IS5SequenceTableNames> doTableNames() {
    IListEdit<IS5SequenceImplementation> impls = new ElemArrayList<>( false );
    // Проектные описания реализаций
    impls.addAll( initialConfig().getHistDataImplementations() );
    // Встроенные описания реализаций
    impls.add(
        new S5SequenceImplementation( S5HistDataAsyncBooleanEnity.class, S5HistDataAsyncBooleanBlobEntity.class ) );
    impls.add(
        new S5SequenceImplementation( S5HistDataAsyncIntegerEntity.class, S5HistDataAsyncIntegerBlobEntity.class ) );
    impls.add(
        new S5SequenceImplementation( S5HistDataAsyncFloatingEntity.class, S5HistDataAsyncFloatingBlobEntity.class ) );
    impls.add( new S5SequenceImplementation( S5HistDataAsyncTimestampEntity.class,
        S5HistDataAsyncTimestampBlobEntity.class ) );
    impls.add(
        new S5SequenceImplementation( S5HistDataAsyncStringEntity.class, S5HistDataAsyncStringBlobEntity.class ) );
    impls.add(
        new S5SequenceImplementation( S5HistDataAsyncValobjEntity.class, S5HistDataAsyncValobjBlobEntity.class ) );

    impls.add(
        new S5SequenceImplementation( S5HistDataSyncBooleanEntity.class, S5HistDataSyncBooleanBlobEntity.class ) );
    impls.add(
        new S5SequenceImplementation( S5HistDataSyncIntegerEntity.class, S5HistDataSyncIntegerBlobEntity.class ) );
    impls.add(
        new S5SequenceImplementation( S5HistDataSyncFloatingEntity.class, S5HistDataSyncFloatingBlobEntity.class ) );
    impls.add(
        new S5SequenceImplementation( S5HistDataSyncTimestampEntity.class, S5HistDataSyncTimestampBlobEntity.class ) );
    impls.add( new S5SequenceImplementation( S5HistDataSyncStringEntity.class, S5HistDataSyncStringBlobEntity.class ) );
    impls.add( new S5SequenceImplementation( S5HistDataSyncValobjEntity.class, S5HistDataSyncValobjBlobEntity.class ) );
    // TODO: остальные встроенные типы

    IListEdit<IS5SequenceTableNames> retValue = new ElemArrayList<>( impls.size() );
    for( IS5SequenceImplementation impl : impls ) {
      int tableCount = impl.tableCount();
      if( tableCount == 1 ) {
        // Однотабличное хранение
        retValue.add( tableNames( impl.blockClassName(), impl.blobClassName() ) );
        continue;
      }
      // Многотабличное хранение
      for( int index = 0; index < tableCount; index++ ) {
        retValue.add( tableNames( impl.blockClassName() + index, impl.blobClassName() + index ) );
      }
    }
    return retValue;
  }

  @Override
  public IParameterized doTypeInfo( Gwid aGwid ) {
    String classId = aGwid.classId();
    String dataId = aGwid.propId();
    ISkClassInfo classInfo = sysdescrReader().getClassInfo( classId );
    IDtoRtdataInfo rtdataInfo = classInfo.rtdata().list().getByKey( dataId );
    if( !rtdataInfo.isHist() ) {
      // Данное не является хранимым
      throw new TsIllegalArgumentRtException( ERR_NO_HISTDATA, aGwid );
    }
    // Атомарный тип данного
    EAtomicType atomicType = rtdataInfo.dataType().atomicType();

    // Для описания типа данного используются параметры описания события
    IParameterizedEdit typeInfo = new StridableParameterized( classId, rtdataInfo.params() );
    IOptionSetEdit params = typeInfo.params();
    // Атомарный тип значений
    IS5HistDataHardConstants.OP_ATOMIC_TYPE.setValue( params, avValobj( atomicType ) );
    // Установка значений неизменяемых опций("защита от дурака")
    IAvMetaConstants.DDEF_DEFAULT_VALUE.setValue( params, rtdataInfo.dataType().defaultValue() );
    IS5SequenceHardConstants.OP_IS_SYNC.setValue( params, avBool( rtdataInfo.isSync() ) );
    IS5SequenceHardConstants.OP_SYNC_DT.setValue( params, avInt( rtdataInfo.syncDataDeltaT() ) );

    // Определение классов хранения значения по типу, признаку синхронности
    IS5SequenceImplementation impl = getImplementation( initialConfig(), aGwid, atomicType, rtdataInfo.isSync() );
    IS5SequenceHardConstants.OP_BLOCK_IMPL_CLASS.setValue( params, avStr( impl.blockClassName() ) );
    IS5SequenceHardConstants.OP_BLOB_IMPL_CLASS.setValue( params, avStr( impl.blobClassName() ) );
    // Установка значений опций значениями подсистемы если они уже не представлены в описании данного
    copyValueIfAbsent( params, OP_BLOCK_SIZE_MIN, IS5SequenceHardConstants.OP_BLOCK_SIZE_MIN );
    copyValueIfAbsent( params, OP_BLOCK_SIZE_MAX, IS5SequenceHardConstants.OP_BLOCK_SIZE_MAX );
    copyValueIfAbsent( params, OP_VALUE_SIZE_MAX, IS5SequenceHardConstants.OP_VALUE_SIZE_MAX );

    return typeInfo;
  }

  @Override
  public IS5SequenceEdit<ITemporalAtomicValue> doCreateSequence( Gwid aGwid, IQueryInterval aInterval,
      Iterable<IS5SequenceBlockEdit<ITemporalAtomicValue>> aBlocks ) {
    return new S5HistDataSequence( this, aGwid, aInterval, aBlocks );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public Object doCreateValueArray( IParameterized aTypeInfo, int aSize ) {
    IDataType type = OPDEF_DATA_TYPE.getValue( aTypeInfo.params() ).asValobj();
    switch( type.atomicType() ) {
      case BOOLEAN:
        return new byte[aSize];
      case INTEGER:
        return new long[aSize];
      case TIMESTAMP:
        return new long[aSize];
      case FLOATING:
        return new double[aSize];
      case STRING:
        return new String[aSize];
      case VALOBJ:
        return new Object[aSize];
      case NONE:
        throw new TsIllegalArgumentRtException();
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  }

  @Override
  public Object doGetSyncDefaultValue( IParameterized aTypeInfo ) {
    IDataType type = OPDEF_DATA_TYPE.getValue( aTypeInfo.params() ).asValobj();
    EAtomicType atomicType = type.atomicType();
    IAtomicValue defaultValue = IAvMetaConstants.DDEF_DEFAULT_VALUE.getValue( type.params() );
    Object retValue = null;
    retValue = switch( atomicType ) {
      case BOOLEAN -> (defaultValue.isAssigned() ? Byte.valueOf( defaultValue.asString() ) : Boolean.valueOf( false ));
      case INTEGER, TIMESTAMP -> (defaultValue.isAssigned() ? Long.valueOf( defaultValue.asString() )
          : Long.valueOf( 0 ));
      case FLOATING -> (defaultValue.isAssigned() ? Double.valueOf( defaultValue.asString() ) : Double.valueOf( 0 ));
      case STRING -> (defaultValue.isAssigned() ? defaultValue.asString() : TsLibUtils.EMPTY_STRING);
      case VALOBJ -> (defaultValue.isAssigned() ? defaultValue.asValobj() : VALOBJ_NULL);
      case NONE -> throw new TsUnsupportedFeatureRtException();
      default -> throw new TsNotAllEnumsUsedRtException();
    };
    if( retValue == null ) {
      throw new TsNullArgumentRtException();
    }
    return retValue;
  }

  @Override
  public Object doGetSyncNullValue( IParameterized aTypeInfo ) {
    IDataType type = OPDEF_DATA_TYPE.getValue( aTypeInfo.params() ).asValobj();
    switch( type.atomicType() ) {
      case BOOLEAN:
        return Byte_BOOLEAN_NULL;
      case INTEGER:
      case TIMESTAMP:
        return Long_LONG_NULL;
      case FLOATING:
        return Double_DOUBLE_NULL;
      case STRING:
      case VALOBJ:
        return VALOBJ_NULL;
      case NONE:
        throw new TsUnsupportedFeatureRtException();
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Возвращает информацию о реализации хранения последовательности значений данного
   *
   * @param aInitialConfig {@link IS5InitialImplementation} проектная, незавимая конфигурация
   * @param aGwid {@link Gwid} идентфикатор данного
   * @param aType {@link EAtomicType} атомарный тип данного
   * @param aSync boolean <b>true</b> синхронное данное;<b>false</b> асинхронное данное.
   * @return String имя таблицы
   */
  private static IS5SequenceImplementation getImplementation( IS5InitialImplementation aInitialConfig, Gwid aGwid,
      EAtomicType aType, boolean aSync ) {
    TsNullArgumentRtException.checkNulls( aInitialConfig, aGwid, aType );
    // Попытка определить реализацию хранения через неизменяемую конфигурацию
    IS5SequenceImplementation retValue = aInitialConfig.findHistDataImplementation( aGwid, aType, aSync );
    if( retValue != null && retValue.tableCount() == 1 ) {
      // Конфигурация определена. Хранение в одной таблице
      return retValue;
    }
    if( retValue == null ) {
      // Реализация не найдена. автоматический подбор таблицы хранения
      if( !aSync ) {
        // Таблицы с асинхронными значениями
        switch( aType ) {
          case BOOLEAN:
            return new S5SequenceImplementation( S5HistDataAsyncBooleanEnity.class,
                S5HistDataAsyncBooleanBlobEntity.class );
          case INTEGER:
            return new S5SequenceImplementation( S5HistDataAsyncIntegerEntity.class,
                S5HistDataAsyncIntegerBlobEntity.class );
          case FLOATING:
            return new S5SequenceImplementation( S5HistDataAsyncFloatingEntity.class,
                S5HistDataAsyncFloatingBlobEntity.class );
          case TIMESTAMP:
            return new S5SequenceImplementation( S5HistDataAsyncTimestampEntity.class,
                S5HistDataAsyncTimestampBlobEntity.class );
          case STRING:
            return new S5SequenceImplementation( S5HistDataAsyncStringEntity.class,
                S5HistDataAsyncStringBlobEntity.class );
          case VALOBJ:
            return new S5SequenceImplementation( S5HistDataAsyncValobjEntity.class,
                S5HistDataAsyncValobjBlobEntity.class );
          case NONE:
          default:
            throw new TsNotAllEnumsUsedRtException();
        }
      }
      // Таблицы с синхронными значениями
      switch( aType ) {
        case BOOLEAN:
          return new S5SequenceImplementation( S5HistDataSyncBooleanEntity.class,
              S5HistDataSyncBooleanBlobEntity.class );
        case INTEGER:
          return new S5SequenceImplementation( S5HistDataSyncIntegerEntity.class,
              S5HistDataSyncIntegerBlobEntity.class );
        case FLOATING:
          return new S5SequenceImplementation( S5HistDataSyncFloatingEntity.class,
              S5HistDataSyncFloatingBlobEntity.class );
        case TIMESTAMP:
          return new S5SequenceImplementation( S5HistDataSyncTimestampEntity.class,
              S5HistDataSyncTimestampBlobEntity.class );
        case STRING:
          return new S5SequenceImplementation( S5HistDataSyncStringEntity.class, S5HistDataSyncStringBlobEntity.class );
        case VALOBJ:
          return new S5SequenceImplementation( S5HistDataSyncValobjEntity.class, S5HistDataSyncValobjBlobEntity.class );
        case NONE:
        default:
          throw new TsNotAllEnumsUsedRtException();
      }
    }
    // Многотабличная реализация найдена в проектной конфигурации. Автоматическое определение таблицы для хранения
    int tableCount = retValue.tableCount();
    // Индекс таблицы хранения
    int tableIndex = calcTableIndex( aGwid, tableCount );
    try {
      // Формирование описания реализации хранения в одной таблице
      return new S5SequenceImplementation( //
          Class.forName( retValue.blockClassName() + tableIndex ), //
          Class.forName( retValue.blobClassName() + tableIndex ) );
    }
    catch( ClassNotFoundException e ) {
      // Не найден класс реализации хранения histdata
      String blockClass = retValue.blockClassName() + tableIndex;
      String blobClass = retValue.blobClassName() + tableIndex;
      Integer count = Integer.valueOf( tableCount );
      throw new TsInternalErrorRtException( e, ERR_HISTDATA_IMPL_NOT_FOUND, blockClass, blobClass, count, cause( e ) );
    }
  }

  /**
   * Определение индекса таблицы для хранения значения данного
   *
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aTableCount int количество таблиц в которых возможно хранение значений этого данного
   * @return int индекс таблицы хранения
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static int calcTableIndex( Gwid aGwid, int aTableCount ) {
    TsNullArgumentRtException.checkNull( aGwid );
    int hash = Math.abs( aGwid.hashCode() );
    int retValue = hash % aTableCount;
    return retValue;
  }
}
