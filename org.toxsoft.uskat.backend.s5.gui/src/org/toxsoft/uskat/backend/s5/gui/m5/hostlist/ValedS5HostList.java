package org.toxsoft.uskat.backend.s5.gui.m5.hostlist;

import static org.toxsoft.core.tsgui.valed.api.IValedControlConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.backend.s5.gui.m5.hostlist.ISkResources.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.dialogs.datarec.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.controls.helpers.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.uskat.s5.common.*;

/**
 * VALED to edit {@link S5HostList}.
 *
 * @author hazard157
 */
public class ValedS5HostList
    extends AbstractValedTextAndButton<S5HostList> {

  /**
   * The factory class.
   *
   * @author hazard157
   */
  public static class Factory
      extends AbstractValedControlFactory {

    /**
     * Constructor.
     */
    public Factory() {
      super( FACTORY_NAME );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected IValedControl<S5HostList> doCreateEditor( ITsGuiContext aContext ) {
      return new ValedS5HostList( aContext );
    }

    @Override
    protected boolean isSuitableRawEditor( Class<?> aValueClass, ITsGuiContext aEditorContext ) {
      return aValueClass.equals( S5HostList.class );
    }

  }

  /**
   * The registered factory name.
   */
  public static final String FACTORY_NAME = VALED_EDNAME_PREFIX + ".ValedHostList"; //$NON-NLS-1$

  /**
   * The factory singleton.
   */
  public static final AbstractValedControlFactory FACTORY = new Factory();

  private final S5HostList hostList = new S5HostList();

  ValedS5HostList( ITsGuiContext aContext ) {
    super( aContext );
    setParamIfNull( OPDEF_IS_HEIGHT_FIXED, AV_TRUE );
  }

  // ------------------------------------------------------------------------------------
  // AbstractValedTextAndButton
  //

  @Override
  protected boolean doProcessButtonPress() {
    ITsDialogInfo di = new TsDialogInfo( tsContext(), DLG_C_EDITS_HOSTLIST, DLG_T_EDITS_HOSTLIST );
    IM5Model<S5Host> model = m5().getModel( S5HostM5Model.MODEL_ID, S5Host.class );
    IM5LifecycleManager<S5Host> lm = model.getLifecycleManager( hostList );
    IList<S5Host> ll = M5GuiUtils.editModownColl( tsContext(), model, di, lm );
    if( ll != null ) {
      hostList.setAll( ll );
      return true;
    }
    return false;
  }

  @Override
  protected void doUpdateTextControl() {
    String s = S5HostList.hostsToString( hostList, true );
    getTextControl().setText( s );
  }

  @Override
  protected S5HostList doGetUnvalidatedValue() {
    return new S5HostList( hostList );
  }

  @Override
  protected void doDoSetUnvalidatedValue( S5HostList aValue ) {
    if( aValue == null ) {
      hostList.clear();
    }
    else {
      hostList.setAll( aValue );
    }
  }

}
