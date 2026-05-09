package org.toxsoft.uskat.core.gui.valed.std.clsid;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.controls.av.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.inner.*;

/**
 * Chooses class ID from the {@link ISkSysdescr#listClasses()} as {@link EAtomicType#STRING}.
 * <p>
 * Wraps over {@link ValedSkClassIdSelector} with the same options respected.
 *
 * @author hazard157
 */
public class ValedAvSkClassIdSelector
    extends AbstractAvWrapperValedControl<String> {

  /**
   * The registered factory ID.
   */
  public static final String FACTORY_NAME = ISkCoreGuiInnerSharedConstants.SKCGC_VALED_AV_CLASS_ID_SELECTOR;

  /**
   * The factory singleton.
   */
  @SuppressWarnings( "unchecked" )
  public static final IValedControlFactory FACTORY = new AbstractValedControlFactory( FACTORY_NAME ) {

    @Override
    protected IValedControl<IAtomicValue> doCreateEditor( ITsGuiContext aContext ) {
      return new ValedAvSkClassIdSelector( aContext );
    }

  };

  /**
   * Constructor.
   *
   * @param aContext {@link ITsGuiContext} - the VALED context
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public ValedAvSkClassIdSelector( ITsGuiContext aContext ) {
    super( aContext, EAtomicType.STRING, ValedSkClassIdSelector.FACTORY );
  }

  @Override
  protected IAtomicValue tv2av( String aTypedValue ) {
    return avStr( aTypedValue );
  }

  @Override
  protected String av2tv( IAtomicValue aAtomicValue ) {
    return aAtomicValue.asString();
  }

}
