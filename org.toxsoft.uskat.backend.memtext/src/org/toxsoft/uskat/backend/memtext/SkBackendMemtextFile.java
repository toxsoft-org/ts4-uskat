package org.toxsoft.uskat.backend.memtext;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.backend.memtext.ISkResources.*;

import java.io.*;
import java.nio.charset.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;

/**
 * {@link AbstractSkBackendMemtext} implementation which stores data in {@link File}.
 *
 * @author hazard157
 */
public class SkBackendMemtextFile
    extends AbstractSkBackendMemtext {

  /**
   * ID of this backend returned as {@link ISkBackendInfo#id()}.
   */
  public static final String BACKEND_ID = SKB_ID_MEMTEXT + ".file"; //$NON-NLS-1$

  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  /**
   * Имя текстового файла хранения содержимого.
   */
  public static final IDataDef OP_FILE_NAME =
      DataDef.create( SkBackendMemtextFile.class.getSimpleName() + ".FileName", STRING, //$NON-NLS-1$
          TSID_DEFAULT_VALUE, AV_STR_EMPTY, //
          TSID_NAME, STR_N_OP_FILE_NAME, //
          TSID_DESCRIPTION, STR_D_OP_FILE_NAME //
      );

  /**
   * Секунды между автосохранением содержимого, <= 0 - нет автосохранения.
   */
  public static final IDataDef OP_AUTO_SAVE_SECS =
      DataDef.create( SkBackendMemtextFile.class.getSimpleName() + ".AutoSaveSecs", INTEGER, //$NON-NLS-1$
          TSID_DEFAULT_VALUE, avInt( 20 ), //
          TSID_NAME, STR_N_OP_AUTO_SAVE_SECS, //
          TSID_DESCRIPTION, STR_D_OP_AUTO_SAVE_SECS //
      );

  /**
   * Список идентификаторов классов, объекты которых не хранятся в файле.
   * <p>
   * Точнее, перед закрытием бекенда, файлы объекты этих классов удаляются из хранилища.
   */
  public static final IDataDef OP_NOT_STORED_OBJ_CLASS_IDS =
      DataDef.create( SkBackendMemtextFile.class.getSimpleName() + ".NotStoredObjClassIds", VALOBJ, //$NON-NLS-1$
          TSID_DEFAULT_VALUE, avValobj( IStringList.EMPTY ), //
          TSID_KEEPER_ID, avStr( StringListKeeper.KEEPER_ID ), //
          TSID_NAME, STR_N_NOT_STORED_OBJ_CLASS_IDS, //
          TSID_DESCRIPTION, STR_D_NOT_STORED_OBJ_CLASS_IDS //
      );

  /**
   * Синглтон поставщика бекенда.
   */
  public static final ISkBackendProvider PROVIDER = SkBackendMemtextFile::new;

  public SkBackendMemtextFile( ISkFrontendRear aFrontend, ITsContextRo aArgs ) {
    super( aFrontend, aArgs, BACKEND_ID, IOptionSet.NULL );
    // TODO Auto-generated constructor stub
  }

  @Override
  protected void doInternalCheck() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void doClose() {
    // TODO Auto-generated method stub

  }

}
