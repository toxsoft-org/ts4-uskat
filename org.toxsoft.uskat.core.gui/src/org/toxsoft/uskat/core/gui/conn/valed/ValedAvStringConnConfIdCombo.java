package org.toxsoft.uskat.core.gui.conn.valed;
//public class ValedAvStringConnConfIdCombo {

import static org.toxsoft.core.tsgui.valed.api.IValedControlConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.controls.av.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Wraps {@link ValedConnConfIdCombo} to edit {@link EAtomicType#STRING} atomic value.
 *
 * @author hazard157
 */
public class ValedAvStringConnConfIdCombo
    extends AbstractAvWrapperValedControl<String> {

  /**
   * The factory name.
   */
  public static final String FACTORY_NAME = VALED_EDNAME_PREFIX + ".AvStringConnConfIdCombo"; //$NON-NLS-1$

  /**
   * The factory class.
   *
   * @author hazard157
   */
  static class Factory
      extends AbstractValedControlFactory {

    protected Factory() {
      super( FACTORY_NAME );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected IValedControl<IAtomicValue> doCreateEditor( ITsGuiContext aContext ) {
      return new ValedAvStringConnConfIdCombo( aContext );
    }

  }

  /**
   * The factory singleton.
   */
  public static final AbstractValedControlFactory FACTORY = new Factory();

  /**
   * Constructor.
   *
   * @param aTsContext {@link ITsGuiContext} - the editor context
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public ValedAvStringConnConfIdCombo( ITsGuiContext aTsContext ) {
    super( aTsContext, EAtomicType.STRING, ValedConnConfIdCombo.FACTORY );
  }

  // ------------------------------------------------------------------------------------
  // AbstractAvWrapperValedControl
  //

  @Override
  protected IAtomicValue tv2av( String aTypedValue ) {
    return avStr( aTypedValue );
  }

  @Override
  protected String av2tv( IAtomicValue aAtomicValue ) {
    return aAtomicValue.asString();
  }

}
