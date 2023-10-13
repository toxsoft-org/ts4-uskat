package org.toxsoft.uskat.s5.server.backend.supports.events;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.s5.utils.IS5HardConstants.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.s5.server.backend.supports.events.impl.S5EventBlob;
import org.toxsoft.uskat.s5.server.backend.supports.events.impl.S5EventBlock;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceHardConstants;
import org.toxsoft.uskat.s5.utils.IS5HardConstants;

/**
 * Константы по умолчанию определяющие работу механизма событий.
 *
 * @author mvk
 */
public interface IS5EventHardConstants
    extends IS5HardConstants {

  // ------------------------------------------------------------------------------------
  // Опции для параметризованного описания типов данных:
  // ({@link ISkClassInfo} = aTypeInfo {@link IParameterized})
  //
  /**
   * String prefix of the all s5 events identifiers.
   */
  String EVENTS_PREFIX = "s5.events"; //$NON-NLS-1$

  /**
   * Опция {@link ISkClassInfo#params()}: полное имя java-класса реализации блока.
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_BLOCK_IMPL_CLASS = createOption( EVENTS_PREFIX, IS5SequenceHardConstants.OP_BLOCK_IMPL_CLASS,
      avStr( S5EventBlock.class.getName() ) );

  /**
   * Опция {@link ISkClassInfo#params()}: полное имя java-класса реализации blob значений.
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_BLOB_IMPL_CLASS =
      createOption( EVENTS_PREFIX, IS5SequenceHardConstants.OP_BLOB_IMPL_CLASS, avStr( S5EventBlob.class.getName() ) );

  /**
   * Опция {@link ISkClassInfo#params()}: количество значений в одном блоке.
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  IDataDef OP_BLOCK_SIZE_MAX = createOption( EVENTS_PREFIX, IS5SequenceHardConstants.OP_BLOCK_SIZE_MAX, avInt( 500 ) );

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
  IDataDef OP_VALUE_SIZE_MAX = createOption( EVENTS_PREFIX, IS5SequenceHardConstants.OP_VALUE_SIZE_MAX, avInt( 100 ) );
}
