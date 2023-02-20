package org.toxsoft.uskat.backend.memtext;

import static org.toxsoft.uskat.backend.memtext.ISkResources.*;
import static org.toxsoft.uskat.backend.memtext.MtbBackendToFile.*;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.uskat.core.backend.metainf.*;

/**
 * {@link ISkBackendMetaInfo} implementation for {@link MtbBackendToFile}.
 *
 * @author hazard157
 */
public class MtbBackendToFileMetaInfo
    extends SkBackendMetaInfo {

  /**
   * The instance singleton.
   */
  public static final ISkBackendMetaInfo INSTANCE = new MtbBackendToFileMetaInfo();

  private MtbBackendToFileMetaInfo() {
    super( BACKEND_ID, STR_N_BACKEND_MEMTEXT_TO_FILE, STR_D_BACKEND_MEMTEXT_TO_FILE, PROVIDER );
    argOps().add( MtbBackendToFile.OPDEF_FILE_PATH );
    argOps().add( MtbBackendToFile.OPDEF_AUTO_SAVE_SECS );
    argOps().add( IBackendMemtextConstants.OPDEF_NOT_STORED_OBJ_CLASS_IDS );
    argOps().add( IBackendMemtextConstants.OPDEF_IS_EVENTS_STORED );
    argOps().add( IBackendMemtextConstants.OPDEF_MAX_EVENTS_COUNT );
    argOps().add( IBackendMemtextConstants.OPDEF_IS_CMDS_STORED );
    argOps().add( IBackendMemtextConstants.OPDEF_MAX_CMDS_COUNT );
    argOps().add( IBackendMemtextConstants.OPDEF_HISTORY_DEPTH_HOURS );
    argOps().add( IBackendMemtextConstants.OPDEF_CURR_DATA_10MS_TICKS );
  }

  @Override
  protected ValidationResult doCheckArguments( ITsContext aArgs ) {
    // TODO error if file OPDEF_FILE_PATH is not accessible for writing
    // TODO warn if file OPDEF_FILE_PATH does not exists
    // TODO warn if OPDEF_AUTO_SAVE_SECS has bad value (too big or too small)
    // TODO warn if OPDEF_MAX_EVENTS_COUNT is out of range (if OPDEF_IS_EVENTS_STORED is set)
    // TODO warn if OPDEF_MAX_CMDS_COUNT is out of range (if OPDEF_IS_CMDS_STORED is set)
    // TODO warn if OPDEF_HISTORY_DEPTH_HOURS is out of range
    // TODO warn if OPDEF_CURR_DATA_10MS_TICKS is out of range
    return ValidationResult.SUCCESS;
  }

}
