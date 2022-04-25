package org.toxsoft.uskat.core.impl;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * USkat core configuration constants and options.
 * <p>
 * Most of this options/references is used as connection arguments in {@link ISkConnection#open(ITsContextRo)}.
 *
 * @author hazard157
 */
public interface ISkCoreConfigConstants {

  /**
   * {@link ISkConnection#open(ITsContextRo)} argument: localization files root directory. <br>
   * Usage: Optional argument allows to specify different (non-default) directory for USkat connection localization
   * files.
   */
  IDataDef OPDEF_L10N_FILES_DIR = DataDef.create( SK_ID + "L10nFileDir", STRING, //$NON-NLS-1$
      TSID_NAME, STR_N_OP_L10N_FILES_DIR, //
      TSID_DESCRIPTION, STR_N_OP_L10N_FILES_DIR, //
      TSID_DEFAULT_VALUE, avStr( "uskat-l10n" ) //$NON-NLS-1$
  );

  /**
   * {@link ISkConnection#open(ITsContextRo)} argument: locale for core entities localization. <br>
   * Usage: optional argument to specify connection locale. If not specified connection locale will be default system
   * locale.
   */
  IDataDef OPDEF_LOCALE = DataDef.create( SK_ID + "Locale", VALOBJ, //$NON-NLS-1$
      TSID_NAME, STR_N_OP_LOCALE, //
      TSID_DESCRIPTION, STR_N_OP_LOCALE, //
      TSID_KEEPER_ID, LocaleKeeper.KEEPER_ID //
  );

  /**
   * {@link ISkConnection#open(ITsContextRo)} argument: the backend privider. <br>
   * Usage: this is mandatory oprion to create concrete backend of connection.
   */
  ITsContextRefDef<ISkBackendProvider> REFDEF_BACKEND_PROVIDER = TsContextRefDef.create( ISkBackendProvider.class, //
      TSID_NAME, STR_N_REF_BACKEND_PROVIDER, //
      TSID_DESCRIPTION, STR_D_REF_BACKEND_PROVIDER, //
      TSID_IS_MANDATORY, AV_TRUE //
  );

  /**
   * {@link ISkConnection#open(ITsContextRo)} argument: frontend-backend thread separator.<br>
   * Usage: this argument is mandatory for backends with flag
   * {@link ISkBackendHardConstant#OPDEF_SKBI_NEEDS_THREAD_SEPARATOR} set. Must contain reference to the
   * {@link SkBackendThreadSeparator} instance which {@link SkBackendThreadSeparator#doJob()} is called from the same
   * execution thread as {@link ISkCoreApi} calls.
   */
  ITsContextRefDef<SkBackendThreadSeparator> REFDEF_BACKEND_THREAD_SEPARATOR =
      TsContextRefDef.create( SkBackendThreadSeparator.class );

}
