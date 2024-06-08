package org.toxsoft.uskat.core.gui;

import static org.toxsoft.core.tsgui.graphics.icons.ITsStdIconIds.*;
import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.gui.km5.sded.ISkSdedKm5SharedResources.*;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.bricks.actions.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.graphics.icons.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.conn.*;

/**
 * Plugin constants.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface ISkCoreGuiConstants {

  /**
   * Prefix of SDED IDs.
   */
  String SDED_ID = SK_ID + ".sded"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Icons

  String PREFIX_OF_ICON_FIELD_NAME = "ICONID_";           //$NON-NLS-1$
  String ICONID_SDED_CLASS         = "sded-class";        //$NON-NLS-1$
  String ICONID_SDED_CLASSES_LIST  = "sded-classes-list"; //$NON-NLS-1$
  String ICONID_SDED_CLASS_ATTR    = "sded-class-attr";   //$NON-NLS-1$
  String ICONID_SDED_CLASS_CLOB    = "sded-class-clob";   //$NON-NLS-1$
  String ICONID_SDED_CLASS_RIVET   = "sded-class-rivet";  //$NON-NLS-1$
  String ICONID_SDED_CLASS_LINK    = "sded-class-link";   //$NON-NLS-1$
  String ICONID_SDED_CLASS_DATA    = "sded-class-data";   //$NON-NLS-1$
  String ICONID_SDED_CLASS_CMD     = "sded-class-cmd";    //$NON-NLS-1$
  String ICONID_SDED_CLASS_EVENT   = "sded-class-event";  //$NON-NLS-1$
  String ICONID_SDED_OBJ           = "sded-obj";          //$NON-NLS-1$
  String ICONID_SDED_OBJS_LIST     = "sded-objs-list";    //$NON-NLS-1$
  String ICONID_SDED_OBJ_ATTR      = "sded-obj-attr";     //$NON-NLS-1$
  String ICONID_SDED_OBJ_CLOB      = "sded-obj-clob";     //$NON-NLS-1$
  String ICONID_SDED_OBJ_RIVET     = "sded-obj-rivet";    //$NON-NLS-1$
  String ICONID_SDED_OBJ_LINK      = "sded-obj-link";     //$NON-NLS-1$
  String ICONID_SDED_OBJ_DATA      = "sded-obj-data";     //$NON-NLS-1$
  String ICONID_SDED_OBJ_CMD       = "sded-obj-cmd";      //$NON-NLS-1$
  String ICONID_SDED_OBJ_EVENT     = "sded-obj-event";    //$NON-NLS-1$
  String ICONID_USKAT_SERVER       = "uskat-server";      //$NON-NLS-1$
  String ICONID_USKAT_CONNECT      = "uskat-connect";     //$NON-NLS-1$
  String ICONID_USKAT_DISCONNECT   = "uskat-disconnect";  //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // TODO ???
  //

  /**
   * For all GUI components this option contains {@link IdChain} to get {@link ISkConnection} to work with.
   * <p>
   * The connection must be retrieved by {@link ISkConnectionSupplier#getConn(IdChain)}.
   */
  IDataDef OPDEF_SUPPLIED_SK_CONN_ID = DataDef.create( USKAT_FULL_ID + ".gui.SuppliedSkConnId", VALOBJ, //$NON-NLS-1$
      TSID_IS_MANDATORY, AV_TRUE, //
      TSID_IS_NULL_ALLOWED, AV_FALSE //
  );

  /**
   * Returns Core API reference from the context using the key from option {@link #OPDEF_SUPPLIED_SK_CONN_ID}.
   * <p>
   * This is convenience method designed to simplify usage of the option {@link #OPDEF_SUPPLIED_SK_CONN_ID}.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @return {@link ISkCoreApi} - the core API of the {@link ISkConnection} found by key
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such key in the context
   * @throws TsItemNotFoundRtException no such connection in {@link ISkConnectionSupplier}
   */
  static ISkCoreApi skCoreApi( ITsGuiContext aContext ) {
    TsNullArgumentRtException.checkNull( aContext );
    ISkConnection skConn = OPDEF_SUPPLIED_SK_CONN_ID.getValue( aContext.params() ).asValobj();
    return skConn.coreApi();
  }

  /**
   * Stores the the connection key in the context.
   * <p>
   * This is convenience method designed to simplify usage of the option {@link #OPDEF_SUPPLIED_SK_CONN_ID}.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @param aSkConnKey {@link IdChain} - the key to be used with {@link ISkConnectionSupplier#getConn(IdChain)}
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  static void setCtxSkConnKey( ITsGuiContext aContext, IdChain aSkConnKey ) {
    TsNullArgumentRtException.checkNulls( aContext, aSkConnKey );
    OPDEF_SUPPLIED_SK_CONN_ID.setValue( aContext.params(), avValobj( aSkConnKey ) );
  }

  // ------------------------------------------------------------------------------------
  // Actions

  /**
   * ID of action {@link #ACDEF_HIDE_CLAIMED_CLASSES}.
   */
  String ACTID_HIDE_CLAIMED_CLASSES = SDED_ID + ".HideClaimedClasses"; //$NON-NLS-1$

  /**
   * Hide/show uneditable classes owned by the internal services.
   */
  TsActionDef ACDEF_HIDE_CLAIMED_CLASSES = TsActionDef.ofCheck1( ACTID_HIDE_CLAIMED_CLASSES, //
      TSID_NAME, STR_N_HIDE_CLAIMED_CLASSES, //
      TSID_DESCRIPTION, STR_D_HIDE_CLAIMED_CLASSES, //
      TSID_ICON_ID, ICONID_VIEW_FILTER //
  );

  /**
   * Constants registration.
   *
   * @param aWinContext {@link IEclipseContext} - windows level context
   */
  static void init( IEclipseContext aWinContext ) {
    ITsIconManager iconManager = aWinContext.get( ITsIconManager.class );
    iconManager.registerStdIconByIds( Activator.PLUGIN_ID, ISkCoreGuiConstants.class, PREFIX_OF_ICON_FIELD_NAME );
  }

}
