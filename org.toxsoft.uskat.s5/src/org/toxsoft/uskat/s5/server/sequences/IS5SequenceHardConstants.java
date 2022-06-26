package org.toxsoft.uskat.s5.server.sequences;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.IS5Resources.*;

import javax.enterprise.concurrent.ManagedExecutorService;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.uskat.s5.utils.IS5HardConstants;

/**
 * Константы по умолчанию определяющие работу механизма {@link IS5Sequence}.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface IS5SequenceHardConstants
    extends IS5HardConstants {

  // ------------------------------------------------------------------------------------
  // Опции для параметризованного описания типов данных (aTypeInfo {@link IParameterized})
  //
  /**
   * String prefix of the all s5 sequence identifiers.
   */
  String SEQUENCE_PREFIX = "s5.sequenses";

  /**
   * Опция {@link IS5Sequence#typeInfo()}: значения данного являются синхронными.
   * <p>
   * Тип: {@link EAtomicType#BOOLEAN}
   */
  IDataDef OP_IS_SYNC = IDtoHardConstants.OP_IS_SYNC;

  /**
   * Опция {@link IS5Sequence#typeInfo()}: интервал (мсек) значения данного имеющего синхронные значения.
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  IDataDef OP_SYNC_DT = IDpuHardConstants.OP_SYNC_DT;

  /**
   * Опция {@link IS5Sequence#typeInfo()}: полное имя java-класса реализации блока.
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_BLOCK_IMPL_CLASS = create( SEQUENCE_PREFIX + ".BlockImplClass", EAtomicType.STRING, //
      TSID_NAME, STR_N_BLOCK_IMPL_CLASS, //
      TSID_DESCRIPTION, STR_D_BLOCK_IMPL_CLASS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AV_STR_EMPTY );

  /**
   * Опция {@link IS5Sequence#typeInfo()}: полное имя java-класса реализации blob значений.
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_BLOB_IMPL_CLASS = create( SEQUENCE_PREFIX + ".BlobImplClass", EAtomicType.STRING, //
      TSID_NAME, STR_N_BLOB_IMPL_CLASS, //
      TSID_DESCRIPTION, STR_D_BLOB_IMPL_CLASS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AV_STR_EMPTY );

  /**
   * Опция {@link IS5Sequence#typeInfo()}: количество значений в одном блоке.
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  IDataDef OP_BLOCK_SIZE_MAX = create( SEQUENCE_PREFIX + ".BlockSizeMax", EAtomicType.INTEGER, //
      TSID_NAME, STR_N_BLOCK_SIZE_MAX, //
      TSID_DESCRIPTION, STR_D_BLOCK_SIZE_MAX, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avInt( 5000 ) );

  /**
   * Опция {@link IS5Sequence#typeInfo()}: максимальный размер одного значения (байты).
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   * <p>
   * При использовании характеристики следует учитывать, что для асинхронных значений фактический размер значения будет
   * определяться как: {@link #OP_VALUE_SIZE_MAX} + 8(количество байт в метке времени)
   */
  IDataDef OP_VALUE_SIZE_MAX = create( SEQUENCE_PREFIX + ".ValueSizeMax", EAtomicType.INTEGER, //
      TSID_NAME, STR_N_VALUE_SIZE_MAX, //
      TSID_DESCRIPTION, STR_D_VALUE_SIZE_MAX, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avInt( 8 ) );

  // ------------------------------------------------------------------------------------
  // Константы
  //
  /**
   * Максимальное количество блоков которое может запрошено у dbms без риска превысить доступный java heap, по умолчанию
   * <p>
   * Характеристика не гарантирует, что не появлится ошибка OutOfMemory. В общем случае, необходимо расчитывать
   * количество блоков которое может быть прочитано (текущее состояние java-heap, блоки каких размер попадают в интервал
   * запроса)
   */
  int SEQUENCE_READ_COUNT_MAX = 2000;

  /**
   * Таймаут(мсек) ожидания завершения транзакции обработки события таймера. -1: Бесконечно
   */
  long SEQUENCE_TIMER_TRANSACTION_TIMEOUT_DEFAULT = 120 * 60 * 1000;

  /**
   * Таймаут(мсек) ожидания завершения транзакции завершения задачи объединения блоков значений последовательностей всех
   * данных. -1: Бесконечно
   */
  long SEQUENCE_UNION_TRANSACTION_TIMEOUT_DEFAULT = 120 * 60 * 1000;

  /**
   * Таймаут(мсек) ожидания завершения транзакции проверки блоков значений последовательностей одного данного. -1:
   * Бесконечно
   */
  long SEQUENCE_VALIDATION_TRANSACTION_TIMEOUT_DEFAULT = 120 * 60 * 1000;

  /**
   * JNDI-имя исполнителя асинхронных задач чтения блоков {@link ManagedExecutorService}
   */
  String READ_EXECUTOR_JNDI = "java:jboss/ee/concurrency/executor/s5/sequence/read"; //$NON-NLS-1$

  /**
   * JNDI-имя исполнителя асинхронных задач записи блоков {@link ManagedExecutorService}
   */
  String WRITE_EXECUTOR_JNDI = "java:jboss/ee/concurrency/executor/s5/sequence/write"; //$NON-NLS-1$

  /**
   * JNDI-имя исполнителя асинхронных задач объединения блоков {@link ManagedExecutorService}
   */
  String UNION_EXECUTOR_JNDI = "java:jboss/ee/concurrency/executor/s5/sequence/union"; //$NON-NLS-1$

  /**
   * JNDI-имя исполнителя асинхронных задач проверки блоков {@link ManagedExecutorService}
   */
  String VALIDATION_EXECUTOR_JNDI = "java:jboss/ee/concurrency/executor/s5/sequence/validation"; //$NON-NLS-1$

  /**
   * JNDI-имя исполнителя асинхронных задач передачи команд и их состояний {@link ManagedExecutorService}
   */
  String COMMAND_EXECUTOR_JNDI = "java:jboss/ee/concurrency/executor/s5/commands"; //$NON-NLS-1$

  /**
   * Идентификатор журнала писателя
   */
  String LOG_WRITER_ID = "S5SequenceWriter";

  /**
   * Идентификатор журнала писателя для дефрагментации
   */
  String LOG_UNITER_ID = "S5SequenceUniter";

  /**
   * Идентификатор журнала писателя для дефрагментации
   */
  String LOG_VALIDATOR_ID = "S5SequenceValidator";

}
