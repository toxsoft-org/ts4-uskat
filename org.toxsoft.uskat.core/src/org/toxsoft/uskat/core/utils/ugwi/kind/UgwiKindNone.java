package org.toxsoft.uskat.core.utils.ugwi.kind;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.utils.ugwi.kind.ITsResources.*;

import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.uskat.core.utils.ugwi.*;

/**
 * The UGWI kind for special case {@link Ugwi#NONE}.
 * <p>
 * Has no useful public API.
 *
 * @author hazard157
 */
public class UgwiKindNone
    extends UgwiKind {

  /**
   * The registered kind ID.
   */
  public static final String KIND_ID = "none"; //$NON-NLS-1$

  /**
   * The singleton instance.
   */
  public static final IUgwiKind INSTANCE = new UgwiKindNone();

  /**
   * Constructor.
   */
  public UgwiKindNone() {
    super( KIND_ID, OptionSetUtils.createOpSet( //
        TSID_NAME, STR_UK_NONE, //
        TSID_DESCRIPTION, STR_UK_NONE_D //
    ) );
  }

  // ------------------------------------------------------------------------------------
  // AbstractUgwiKind
  //

}
