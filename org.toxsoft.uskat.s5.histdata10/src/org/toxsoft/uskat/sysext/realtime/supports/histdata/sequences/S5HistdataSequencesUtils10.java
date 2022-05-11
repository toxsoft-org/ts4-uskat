package org.toxsoft.uskat.sysext.realtime.supports.histdata.sequences;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsNotAllEnumsUsedRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceImplementation;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceImplementation;
import org.toxsoft.uskat.sysext.realtime.supports.histdata.sequences.async.*;
import org.toxsoft.uskat.sysext.realtime.supports.histdata.sequences.sync.*;

/**
 * Вспомогательные методы пакета
 *
 * @author mvk
 */
public class S5HistdataSequencesUtils10 {

  /**
   * Количество таблиц на один тип
   */
  private static final int TABLE_COUNT_PER_TYPE = 10;

  /**
   * Возвращает описание реализации хранения указанного хранимого данного
   *
   * @param aGwid {@link Gwid} идентификатор хранимого данного
   * @param aType {@link EAtomicType} тип хранигого данного
   * @param aSync <b>true</b> синхронное данное; <b>false</b> асинхронное значение
   * @return {@link IS5SequenceImplementation} описание хранения. null: неопределяется в проектной конфигурации и
   *         выбирается по умолчанию
   */
  public static IS5SequenceImplementation findHistDataImplementation( Gwid aGwid, EAtomicType aType, boolean aSync ) {
    TsNullArgumentRtException.checkNulls( aGwid, aType );
    Class<?> block, blob;
    blob = switch( aType ) {
      case BOOLEAN -> {
        block = (aSync ? S5HistDataSyncBooleanEntity0.class : S5HistDataAsyncBooleanEntity0.class);
        yield (aSync ? S5HistDataSyncBooleanBlobEntity0.class : S5HistDataAsyncBooleanBlobEntity0.class);
      }
      case FLOATING -> {
        block = (aSync ? S5HistDataSyncFloatingEntity0.class : S5HistDataAsyncFloatingEntity0.class);
        yield (aSync ? S5HistDataSyncFloatingBlobEntity0.class : S5HistDataAsyncFloatingBlobEntity0.class);
      }
      case INTEGER -> {
        block = (aSync ? S5HistDataSyncIntegerEntity0.class : S5HistDataAsyncIntegerEntity0.class);
        yield (aSync ? S5HistDataSyncIntegerBlobEntity0.class : S5HistDataAsyncIntegerBlobEntity0.class);
      }
      case STRING -> {
        block = (aSync ? S5HistDataSyncStringEntity0.class : S5HistDataAsyncStringEntity0.class);
        yield (aSync ? S5HistDataSyncStringBlobEntity0.class : S5HistDataAsyncStringBlobEntity0.class);
      }
      case TIMESTAMP -> {
        block = (aSync ? S5HistDataSyncTimestampEntity0.class : S5HistDataAsyncTimestampEntity0.class);
        yield (aSync ? S5HistDataSyncTimestampBlobEntity0.class : S5HistDataAsyncTimestampBlobEntity0.class);
      }
      case VALOBJ -> {
        block = (aSync ? S5HistDataSyncValobjEntity0.class : S5HistDataAsyncValobjEntity0.class);
        yield (aSync ? S5HistDataSyncValobjBlobEntity0.class : S5HistDataAsyncValobjBlobEntity0.class);
      }
      case NONE -> throw new TsNotAllEnumsUsedRtException();
    };
    return new S5SequenceImplementation( block, blob, TABLE_COUNT_PER_TYPE );
  }

  /**
   * Возвращает список всех реализаций хранения данных
   *
   * @return {@link IList}&lt;{@link IS5SequenceImplementation}&gt; список описаний (одно табличные и/или
   *         многотабличные). Пустой список: нет проектно-зависимых реализаций данных
   */
  public static IList<IS5SequenceImplementation> getHistDataImplementations() {
    IListEdit<IS5SequenceImplementation> retValue = new ElemArrayList<>();
    //@formatter:off
    retValue.add( new S5SequenceImplementation( S5HistDataAsyncBooleanEntity0.class, S5HistDataAsyncBooleanBlobEntity0.class, TABLE_COUNT_PER_TYPE ) );
    retValue.add( new S5SequenceImplementation( S5HistDataSyncBooleanEntity0.class, S5HistDataSyncBooleanBlobEntity0.class, TABLE_COUNT_PER_TYPE ) );

    retValue.add( new S5SequenceImplementation( S5HistDataAsyncFloatingEntity0.class, S5HistDataAsyncFloatingBlobEntity0.class, TABLE_COUNT_PER_TYPE ) );
    retValue.add( new S5SequenceImplementation( S5HistDataSyncFloatingEntity0.class, S5HistDataSyncFloatingBlobEntity0.class, TABLE_COUNT_PER_TYPE ) );

    retValue.add( new S5SequenceImplementation( S5HistDataAsyncIntegerEntity0.class, S5HistDataAsyncIntegerBlobEntity0.class, TABLE_COUNT_PER_TYPE ) );
    retValue.add( new S5SequenceImplementation( S5HistDataSyncIntegerEntity0.class, S5HistDataSyncIntegerBlobEntity0.class, TABLE_COUNT_PER_TYPE ) );

    retValue.add( new S5SequenceImplementation( S5HistDataAsyncStringEntity0.class, S5HistDataAsyncStringBlobEntity0.class, TABLE_COUNT_PER_TYPE ) );
    retValue.add( new S5SequenceImplementation( S5HistDataSyncStringEntity0.class, S5HistDataSyncStringBlobEntity0.class, TABLE_COUNT_PER_TYPE ) );

    retValue.add( new S5SequenceImplementation( S5HistDataAsyncTimestampEntity0.class, S5HistDataAsyncTimestampBlobEntity0.class, TABLE_COUNT_PER_TYPE ) );
    retValue.add( new S5SequenceImplementation( S5HistDataSyncTimestampEntity0.class, S5HistDataSyncTimestampBlobEntity0.class, TABLE_COUNT_PER_TYPE ) );

    retValue.add( new S5SequenceImplementation( S5HistDataAsyncValobjEntity0.class, S5HistDataAsyncValobjBlobEntity0.class, TABLE_COUNT_PER_TYPE ) );
    retValue.add( new S5SequenceImplementation( S5HistDataSyncValobjEntity0.class, S5HistDataSyncValobjBlobEntity0.class, TABLE_COUNT_PER_TYPE ) );
    //@formatter:on
    return retValue;
  }

}
