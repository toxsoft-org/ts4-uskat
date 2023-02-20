package org.toxsoft.uskat.backend.memtext;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.metainf.*;

/**
 * Base of {@link ISkBackendMetaInfo} implementation for all memtext backends.
 *
 * @author hazard157
 */
public abstract class MtbAbstractMetaInfo
    extends SkBackendMetaInfo {

  /**
   * Constructor.
   *
   * @param aId String - provider ID (an IDpath)
   * @param aName String - short name
   * @param aDescription String - description
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public MtbAbstractMetaInfo( String aId, String aName, String aDescription ) {
    super( aId, aName, aDescription );
  }

  @Override
  final protected ValidationResult doCheckArguments( ITsContextRo aArgs ) {
    // TODO warn if OPDEF_AUTO_SAVE_SECS has bad value (too big or too small)
    // TODO warn if OPDEF_MAX_EVENTS_COUNT is out of range (if OPDEF_IS_EVENTS_STORED is set)
    // TODO warn if OPDEF_MAX_CMDS_COUNT is out of range (if OPDEF_IS_CMDS_STORED is set)
    // TODO warn if OPDEF_HISTORY_DEPTH_HOURS is out of range
    // TODO warn if OPDEF_CURR_DATA_10MS_TICKS is out of range
    return doDoCheckArguments( aArgs );
  }

  protected abstract ValidationResult doDoCheckArguments( ITsContextRo aArgs );

}
