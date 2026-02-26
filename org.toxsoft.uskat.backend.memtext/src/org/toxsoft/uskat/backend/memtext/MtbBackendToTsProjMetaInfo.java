package org.toxsoft.uskat.backend.memtext;

import static org.toxsoft.uskat.backend.memtext.ISkResources.*;
import static org.toxsoft.uskat.backend.memtext.MtbBackendToTsProj.*;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.uskat.core.backend.metainf.*;

/**
 * {@link ISkBackendMetaInfo} implementation for {@link MtbBackendToFile}.
 *
 * @author hazard157
 */
class MtbBackendToTsProjMetaInfo
    extends MtbAbstractMetaInfo {

  /**
   * The instance singleton.
   */
  static final ISkBackendMetaInfo INSTANCE = new MtbBackendToTsProjMetaInfo();

  private MtbBackendToTsProjMetaInfo() {
    super( BACKEND_ID, STR_BACKEND_MEMTEXT_TO_FILE, STR_BACKEND_MEMTEXT_TO_FILE_D );
    argOps().add( MtbBackendToTsProj.OPDEF_PDU_ID );
    argOps().add( IBackendMemtextConstants.OPDEF_NOT_STORED_OBJ_CLASS_IDS );
    argOps().add( IBackendMemtextConstants.OPDEF_IS_EVENTS_STORED );
    argOps().add( IBackendMemtextConstants.OPDEF_MAX_EVENTS_COUNT );
    argOps().add( IBackendMemtextConstants.OPDEF_IS_CMDS_STORED );
    argOps().add( IBackendMemtextConstants.OPDEF_MAX_CMDS_COUNT );
    argOps().add( IBackendMemtextConstants.OPDEF_HISTORY_DEPTH_HOURS );
    argOps().add( IBackendMemtextConstants.OPDEF_CURR_DATA_10MS_TICKS );
    argRefs().put( MtbBackendToTsProj.REFDEF_PROJECT.refKey(), MtbBackendToTsProj.REFDEF_PROJECT );
  }

  @Override
  protected ValidationResult doDoCheckArguments( ITsContextRo aArgs ) {
    // TODO error if file OPDEF_PDU_ID is not an IDpath
    return ValidationResult.SUCCESS;
  }

}
