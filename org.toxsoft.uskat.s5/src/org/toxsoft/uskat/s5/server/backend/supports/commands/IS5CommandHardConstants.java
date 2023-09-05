package org.toxsoft.uskat.s5.server.backend.supports.commands;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.commands.IS5Resources.*;
import static org.toxsoft.uskat.s5.utils.IS5HardConstants.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.s5.server.backend.supports.commands.impl.S5CommandBlob;
import org.toxsoft.uskat.s5.server.backend.supports.commands.impl.S5CommandBlock;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceHardConstants;
import org.toxsoft.uskat.s5.utils.IS5HardConstants;

/**
 * Константы по умолчанию определяющие работу механизма команд.
 *
 * @author mvk
 */
public interface IS5CommandHardConstants
    extends IS5HardConstants {

  // ------------------------------------------------------------------------------------
  // Опции для параметризованного описания типов данных:
  // ({@link ISkClassInfo} = aTypeInfo {@link IParameterized})
  //
  /**
   * String prefix of the all s5 commands identifiers.
   */
  String COMMANDS_PREFIX = "s5.commands"; //$NON-NLS-1$

  /**
   * Опция {@link ISkClassInfo#params()}: время ожидания завершения выполнения команды (мсек).
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  IDataDef OP_EXECUTION_TIMEOUT = create( COMMANDS_PREFIX + ".executionTimeout", EAtomicType.INTEGER, //$NON-NLS-1$
      TSID_NAME, STR_N_EXECUTION_TIMEOUT, //
      TSID_DESCRIPTION, STR_D_EXECUTION_TIMEOUT, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avInt( 10000 ) );

  /**
   * Опция {@link ISkClassInfo#params()}: полное имя java-класса реализации блока.
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_BLOCK_IMPL_CLASS = createOption( COMMANDS_PREFIX, IS5SequenceHardConstants.OP_BLOCK_IMPL_CLASS,
      avStr( S5CommandBlock.class.getName() ) );

  /**
   * Опция {@link ISkClassInfo#params()}: полное имя java-класса реализации blob значений.
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_BLOB_IMPL_CLASS = createOption( COMMANDS_PREFIX, IS5SequenceHardConstants.OP_BLOB_IMPL_CLASS,
      avStr( S5CommandBlob.class.getName() ) );

  /**
   * Опция {@link ISkClassInfo#params()}: количество значений в одном блоке.
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  IDataDef OP_BLOCK_SIZE_MAX =
      createOption( COMMANDS_PREFIX, IS5SequenceHardConstants.OP_BLOCK_SIZE_MAX, avInt( 500 ) );

  /**
   * Опция {@link ISkClassInfo#params()}: максимальный размер одного значения (байты).
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   * <p>
   * Значение указывается как наиболее вероятное и не требует точного определения.
   * <p>
   * При использовании характеристики следует учитывать, что для асинхронных значений фактический размер значения будет
   * определяться как: {@link #OP_VALUE_SIZE_MAX} + 8(количество байт в метке времени)
   */
  IDataDef OP_VALUE_SIZE_MAX =
      createOption( COMMANDS_PREFIX, IS5SequenceHardConstants.OP_VALUE_SIZE_MAX, avInt( 100 ) );

  /**
   * Опция {@link ISkClassInfo#params()} : гарантированное время (сутки) хранения истории команд.
   * <p>
   * Определяет время хранения истории команд. По факту система может хранить данные более долгий период (определяется
   * реализацией), но не меньший.
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  IDataDef OP_VALUE_STORAGE_DEPTH =
      createOption( COMMANDS_PREFIX, IS5SequenceHardConstants.OP_VALUE_STORAGE_DEPTH, avInt( 365 * 10 ) ); // по
                                                                                                           // умолчанию
}
