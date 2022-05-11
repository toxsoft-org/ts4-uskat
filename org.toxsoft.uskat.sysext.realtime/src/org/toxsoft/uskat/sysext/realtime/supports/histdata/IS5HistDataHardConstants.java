package org.toxsoft.uskat.sysext.realtime.supports.histdata;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.s5.utils.IS5HardConstants.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceHardConstants;
import org.toxsoft.uskat.s5.utils.IS5HardConstants;

import ru.uskat.backend.ISkFrontendRear;
import ru.uskat.backend.messages.SkMessageHistDataQueryFinished;
import ru.uskat.core.api.sysdescr.ISkRtdataInfo;

/**
 * Константы по умолчанию определяющие работу механизма хранимых данных.
 *
 * @author mvk
 */
public interface IS5HistDataHardConstants
    extends IS5HardConstants {

  /**
   * Код ошибки {@link SkMessageHistDataQueryFinished#sendError(ISkFrontendRear, String, int, String)}: Отмена
   * пользователем выполнения запроса исторических данных
   */
  int HDQUERY_CANCEL = 1;

  /**
   * Код ошибки {@link SkMessageHistDataQueryFinished#sendError(ISkFrontendRear, String, int, String)}: Неожиданная
   * ошибка выполнения запроса
   */
  int HDQUERY_UNEXPECTED_ERROR = 2;

  // ------------------------------------------------------------------------------------
  // Опции для параметризованного описания типов данных:
  // ({@link ISkRtdataInfo#params()} = aTypeInfo {@link IParameterized})
  //
  /**
   * String prefix of the all s5 histdata identifiers.
   */
  String HISTDATA_PREFIX = "s5.histdata"; //$NON-NLS-1$

  /**
   * Опция {@link ISkRtdataInfo#params()}: полное имя java-класса реализации блока.
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_BLOCK_IMPL_CLASS = createOption( HISTDATA_PREFIX, IS5SequenceHardConstants.OP_BLOB_IMPL_CLASS );

  /**
   * Опция {@link ISkRtdataInfo#params()}: полное имя java-класса реализации blob значений.
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_BLOB_IMPL_CLASS = createOption( HISTDATA_PREFIX, IS5SequenceHardConstants.OP_BLOB_IMPL_CLASS );

  /**
   * Опция {@link ISkRtdataInfo#params()}: количество значений в одном блоке.
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  IDataDef OP_BLOCK_SIZE_MAX =
      createOption( HISTDATA_PREFIX, IS5SequenceHardConstants.OP_BLOCK_SIZE_MAX, avInt( 5000 ) );

  /**
   * Опция {@link ISkRtdataInfo#params()}: максимальный размер одного значения (байты).
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   * <p>
   * При использовании характеристики следует учитывать, что для асинхронных значений фактический размер значения будет
   * определяться как: {@link #OP_VALUE_SIZE_MAX} + 8(количество байт в метке времени)
   */
  IDataDef OP_VALUE_SIZE_MAX = createOption( HISTDATA_PREFIX, IS5SequenceHardConstants.OP_VALUE_SIZE_MAX, avInt( 8 ) );
}
