package org.toxsoft.uskat.core.gui.valed;

import static org.toxsoft.uskat.core.gui.ISkCoreGuiConstants.*;

import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * Base class for VALEDs using USkat API.
 * <p>
 * Determines the way how VALED gets references to {@link ISkConnection} and hence, to {@link ISkCoreApi}. The reference
 * {@link ISkCoreGuiConstants#REFDEF_SUPPLIED_SK_CONN_ID} is used to provide Sk-connection to the VALED. Implements
 * {@link ISkConnected}.
 *
 * @author hazard157
 * @param <V> - the edited value type
 */
public abstract class AbstractSkValedControl<V>
    extends AbstractValedControl<V, Control>
    implements ISkConnected {

  private final ISkConnection skConn; // never is null

  /**
   * Constructor.
   *
   * @param aContext {@link ITsGuiContext} - the VALED context
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  protected AbstractSkValedControl( ITsGuiContext aContext ) {
    super( aContext );
    skConn = skConnCtx( tsContext() );
  }

  // ------------------------------------------------------------------------------------
  // API for subclasses
  //

  @Override
  final public ISkConnection skConn() {
    return skConn;
  }

}
