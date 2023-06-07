package org.toxsoft.uskat.core.gui.e4.handlers;

import static org.toxsoft.uskat.core.gui.conn.cfg.m5.IConnectionConfigM5Constants.*;
import static org.toxsoft.uskat.core.gui.e4.handlers.ISkResources.*;

import org.eclipse.e4.core.contexts.*;
import org.eclipse.e4.core.di.annotations.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.dialogs.datarec.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.uskat.core.gui.conn.cfg.*;

/**
 * Command: edit list of connection {@link IConnectionConfigService#listConfigs()}.
 *
 * @author hazard157
 */
public class CmdEditConnConfigs {

  @Execute
  void exec( IEclipseContext aContext, IM5Domain aM5, IConnectionConfigService aCcService ) {
    ITsGuiContext ctx = new TsGuiContext( aContext );
    IM5Model<IConnectionConfig> model = aM5.getModel( MID_SK_CONN_CFG, IConnectionConfig.class );
    ITsDialogInfo di = new TsDialogInfo( ctx, DLG_EDIT_CONN_CONFIGS, DLG_EDIT_CONN_CONFIGS_D );
    IM5LifecycleManager<IConnectionConfig> lm = model.getLifecycleManager( aCcService );
    M5GuiUtils.editModownColl( ctx, model, di, lm );
  }

}
