package org.toxsoft.uskat.core.impl;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.impl.ISkResources.*;

import java.util.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * USkat core implementation-specific constants and options.
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
      TSID_KEEPER_ID, LocaleKeeper.KEEPER_ID, //
      TSID_DEFAULT_VALUE, avValobj( Locale.getDefault(), LocaleKeeper.KEEPER, LocaleKeeper.KEEPER_ID ) //
  );

  /**
   * {@link ISkConnection#open(ITsContextRo)} argument: Default log messages lowest severity to be logged. <br>
   * Usage: optional argument to specify which severity of the log messages will be logged. All messages of the
   * specified and higher severity will be logged. Thus specifying this option to {@link ELogSeverity#DEBUG} will log
   * all messages, while {@link ELogSeverity#ERROR} will log only errors. Note that error messages can not be hidden.
   */
  IDataDef OPDEF_DEF_CORE_LOG_SEVERITY = DataDef.create( SK_ID + "DefaultCoreLogSeverity", VALOBJ, //$NON-NLS-1$
      TSID_NAME, STR_N_OP_DEF_CORE_LOG_SEVERITY, //
      TSID_DESCRIPTION, STR_N_OP_DEF_CORE_LOG_SEVERITY, //
      TSID_KEEPER_ID, ELogSeverity.KEEPER_ID, //
      TSID_DEFAULT_VALUE, avValobj( ELogSeverity.DEBUG, ELogSeverity.KEEPER, ELogSeverity.KEEPER_ID ) //
  );

  /**
   * All USkat core implementation-specific connection opening arguments.
   */
  IStridablesList<IDataDef> ALL_SK_CORE_CFG_PARAMS = new StridablesList<>( //
      OPDEF_DEF_CORE_LOG_SEVERITY, //
      OPDEF_LOCALE, //
      OPDEF_L10N_FILES_DIR //
  );

  /**
   * {@link ISkConnection#open(ITsContextRo)} argument: the backend privider. <br>
   * Usage: this is mandatory option to create concrete backend of connection.
   */
  ITsContextRefDef<ISkBackendProvider> REFDEF_BACKEND_PROVIDER = TsContextRefDef.create( ISkBackendProvider.class, //
      TSID_NAME, STR_N_REF_BACKEND_PROVIDER, //
      TSID_DESCRIPTION, STR_D_REF_BACKEND_PROVIDER, //
      TSID_IS_MANDATORY, AV_TRUE //
  );

  /**
   * {@link ISkConnection#open(ITsContextRo)} argument: user-specified thread separator service creator. <br>
   * Usage: TODO:
   */
  @SuppressWarnings( "rawtypes" )
  ITsContextRefDef<ISkServiceCreator> REFDEF_THREAD_SEPARATOR = TsContextRefDef.create( ISkServiceCreator.class, //
      TSID_NAME, STR_N_REF_THREAD_SEPARATOR, //
      TSID_DESCRIPTION, STR_D_REF_THREAD_SEPARATOR, //
      TSID_IS_MANDATORY, AV_FALSE //
  );
}
