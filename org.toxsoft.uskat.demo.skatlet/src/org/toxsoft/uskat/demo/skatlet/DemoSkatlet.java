package org.toxsoft.uskat.demo.skatlet;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.opset.impl.OptionSetUtils;
import org.toxsoft.uskat.core.impl.SkatletBase;

/**
 * Demo skatlet.
 *
 * @author mvk
 */
public class DemoSkatlet
    extends SkatletBase {

  /**
   * Constructor.
   */
  public DemoSkatlet() {
    super( "Demo", OptionSetUtils.createOpSet( //$NON-NLS-1$
        TSID_NAME, "Demo skatlet", //$NON-NLS-1$
        TSID_DESCRIPTION, "This is a demo skatlet" //$NON-NLS-1$
    ) );
  }

  // ------------------------------------------------------------------------------------
  // SkatletBase
  //
}
