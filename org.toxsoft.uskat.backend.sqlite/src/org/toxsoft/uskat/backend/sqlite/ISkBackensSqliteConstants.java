package org.toxsoft.uskat.backend.sqlite;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.backend.sqlite.l10n.ISkBackendSqliteSharedResources.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.uskat.core.backend.*;

/**
 * Backend constants.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface ISkBackensSqliteConstants {

  /**
   * Backend ID prefix for subclass implementations.
   */
  String BACKEND_ID = ISkBackendHardConstant.SKB_ID + ".sqlite"; //$NON-NLS-1$

  String DEFAULT_DB_FILE_NAME = "USkatDb.sqlite"; //$NON-NLS-1$

  String OPID_DB_FILE_NAME = "SqltiteDatabaseFile"; //$NON-NLS-1$

  IDataDef OPDEF_DB_FILE_NAME = DataDef.create( OPID_DB_FILE_NAME, STRING, //
      TSID_NAME, STR_OP_DB_FILE_NAME, //
      TSID_DESCRIPTION, STR_OP_DB_FILE_NAME_D, //

      // TODO --- we don't want to include
      "org.toxsoft.valed.option.EditorFactoryName", "ts.valed.AvStringFile", //
      "org.toxsoft.valed.option.File.IsOpenDialog", AV_TRUE, //

      // ---

      TSID_DEFAULT_VALUE, avStr( DEFAULT_DB_FILE_NAME ) //
  );

}
