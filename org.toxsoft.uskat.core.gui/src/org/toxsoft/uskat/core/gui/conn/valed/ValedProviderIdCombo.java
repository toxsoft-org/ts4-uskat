package org.toxsoft.uskat.core.gui.conn.valed;

import static org.toxsoft.core.tsgui.valed.api.IValedControlConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.utils.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.controls.basic.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.gui.conn.cfg.*;

/**
 * Combo box to select ID from the {@link IConnectionConfigService#listProviders()}.
 *
 * @author hazard157
 */
public class ValedProviderIdCombo
    extends ValedComboSelector<String> {

  /**
   * The factory name.
   */
  @SuppressWarnings( "hiding" )
  public static final String FACTORY_NAME = VALED_EDNAME_PREFIX + ".ValedProviderIdCombo"; //$NON-NLS-1$

  /**
   * The factory singleton.
   */
  @SuppressWarnings( "hiding" )
  public static final AbstractValedControlFactory FACTORY = new AbstractValedControlFactory( FACTORY_NAME ) {

    @SuppressWarnings( "unchecked" )
    @Override
    protected IValedControl<String> doCreateEditor( ITsGuiContext aContext ) {
      AbstractValedControl<String, Combo> e = new ValedProviderIdCombo( aContext );
      e.setParamIfNull( OPDEF_IS_HEIGHT_FIXED, AV_TRUE );
      e.setParamIfNull( OPDEF_IS_WIDTH_FIXED, AV_FALSE );
      return e;
    }

  };

  /**
   * Constructor.
   *
   * @param aContext {@link ITsGuiContext} - the editor context
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public ValedProviderIdCombo( ITsGuiContext aContext ) {
    super( prerpareContext( aContext ) );
  }

  private static ITsGuiContext prerpareContext( ITsGuiContext aContext ) {
    IConnectionConfigService ccs = aContext.get( IConnectionConfigService.class );
    REFDEF_VALUE_VISUALS_PROVIDER.setRef( aContext, (ITsVisualsProvider<String>)aItem -> {
      if( aItem == null ) {
        return TsLibUtils.EMPTY_STRING;
      }
      IConnectionConfigProvider p = ccs.listProviders().findByKey( aItem );
      if( p != null ) {
        return StridUtils.printf( StridUtils.FORMAT_ID_NAME, p );
      }
      return aItem;
    } );
    REFDEF_ITEMS_PROVIDER.setRef( aContext, () -> ccs.listProviders().keys() );
    return aContext;
  }

}
