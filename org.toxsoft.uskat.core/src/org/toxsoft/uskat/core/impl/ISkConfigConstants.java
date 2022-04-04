package org.toxsoft.uskat.core.impl;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * USkat core configuration constants and options.
 *
 * @author hazard157
 */
public interface ISkConfigConstants {

  /**
   * {@link ISkConnection#open(ITsContextRo)} argument: localization files root directory.
   */
  IDataDef OPDEF_L10N_FILES_DIR = DataDef.create( SK_ID + "L10nFileDir", STRING, //$NON-NLS-1$
      TSID_NAME, STR_N_OP_L10N_FILES_DIR, //
      TSID_DESCRIPTION, STR_N_OP_L10N_FILES_DIR, //
      TSID_DEFAULT_VALUE, avStr( "uskat-l10n" ) //$NON-NLS-1$
  );

  /**
   * {@link ISkConnection#open(ITsContextRo)} argument: locale for core entitties localization.
   */
  IDataDef OPDEF_LOCALE = DataDef.create( SK_ID + "Locale", VALOBJ, //$NON-NLS-1$
      TSID_NAME, STR_N_OP_LOCALE, //
      TSID_DESCRIPTION, STR_N_OP_LOCALE, //
      TSID_KEEPER_ID, LocaleKeeper.KEEPER_ID //
  );

}
