package org.toxsoft.uskat.s5.server.backend.supports.histdata;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5Resources.*;
import static org.toxsoft.uskat.s5.utils.IS5HardConstants.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoRtdataInfo;
import org.toxsoft.uskat.s5.server.sequences.IS5Sequence;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceHardConstants;
import org.toxsoft.uskat.s5.utils.IS5HardConstants;

/**
 * Константы по умолчанию определяющие работу механизма хранимых данных.
 *
 * @author mvk
 */
public interface IS5HistDataHardConstants
    extends IS5HardConstants {

  // ------------------------------------------------------------------------------------
  // Опции для параметризованного описания типов данных:
  // ({@link IDtoRtdataInfo#params()} = aTypeInfo {@link IParameterized})
  //
  /**
   * String prefix of the all s5 histdata identifiers.
   */
  String HISTDATA_PREFIX = "s5.histdata"; //$NON-NLS-1$

  /**
   * Опция {@link IS5Sequence#typeInfo()}: атомарный тип значений блока.
   * <p>
   * Тип: {@link EAtomicType#VALOBJ}({@link EAtomicType})
   */
  IDataDef OP_ATOMIC_TYPE = create( HISTDATA_PREFIX + ".atomicType", EAtomicType.VALOBJ, // //$NON-NLS-1$
      TSID_NAME, STR_N_ATOMIC_TYPE, //
      TSID_DESCRIPTION, STR_D_ATOMIC_TYPE, //
      TSID_IS_NULL_ALLOWED, AV_FALSE );

  /**
   * Опция {@link IDtoRtdataInfo#params()}: полное имя java-класса реализации блока.
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_BLOCK_IMPL_CLASS = createOption( HISTDATA_PREFIX, IS5SequenceHardConstants.OP_BLOB_IMPL_CLASS );

  /**
   * Опция {@link IDtoRtdataInfo#params()}: полное имя java-класса реализации blob значений.
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_BLOB_IMPL_CLASS = createOption( HISTDATA_PREFIX, IS5SequenceHardConstants.OP_BLOB_IMPL_CLASS );

  /**
   * Опция {@link IDtoRtdataInfo#params()}: минимальное количество значений в одном дефрагментированном блоке.
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  IDataDef OP_BLOCK_SIZE_MIN =
      createOption( HISTDATA_PREFIX, IS5SequenceHardConstants.OP_BLOCK_SIZE_MIN, avInt( 5000 ) );

  /**
   * Опция {@link ISkClassInfo#params()}: максимальное количество значений в одном дефрагментированном блоке.
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  IDataDef OP_BLOCK_SIZE_MAX =
      createOption( HISTDATA_PREFIX, IS5SequenceHardConstants.OP_BLOCK_SIZE_MAX, avInt( 50000 ) );

  /**
   * Опция {@link IDtoRtdataInfo#params()}: максимальный размер одного значения (байты).
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   * <p>
   * При использовании характеристики следует учитывать, что для асинхронных значений фактический размер значения будет
   * определяться как: {@link #OP_VALUE_SIZE_MAX} + 8(количество байт в метке времени)
   */
  IDataDef OP_VALUE_SIZE_MAX = createOption( HISTDATA_PREFIX, IS5SequenceHardConstants.OP_VALUE_SIZE_MAX, avInt( 8 ) );
}
