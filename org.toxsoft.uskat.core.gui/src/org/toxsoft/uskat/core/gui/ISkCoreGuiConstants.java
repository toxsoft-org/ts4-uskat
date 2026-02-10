package org.toxsoft.uskat.core.gui;

import static org.toxsoft.core.tsgui.graphics.icons.ITsStdIconIds.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.gui.km5.sded.ISkSdedKm5SharedResources.*;

import org.eclipse.e4.core.contexts.*;
import org.toxsoft.core.tsgui.bricks.actions.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.graphics.icons.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
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

  String PREFIX_OF_ICON_FIELD_NAME  = "ICONID_";             //$NON-NLS-1$
  String ICONID_SDED_CLASS          = "sded-class";          //$NON-NLS-1$
  String ICONID_SDED_CLASSES_LIST   = "sded-classes-list";   //$NON-NLS-1$
  String ICONID_SDED_CLASS_ATTR     = "sded-class-attr";     //$NON-NLS-1$
  String ICONID_SDED_CLASS_CLOB     = "sded-class-clob";     //$NON-NLS-1$
  String ICONID_SDED_CLASS_RIVET    = "sded-class-rivet";    //$NON-NLS-1$
  String ICONID_SDED_CLASS_LINK     = "sded-class-link";     //$NON-NLS-1$
  String ICONID_SDED_CLASS_DATA     = "sded-class-data";     //$NON-NLS-1$
  String ICONID_SDED_CLASS_CMD      = "sded-class-cmd";      //$NON-NLS-1$
  String ICONID_SDED_CLASS_EVENT    = "sded-class-event";    //$NON-NLS-1$
  String ICONID_SDED_OBJ            = "sded-obj";            //$NON-NLS-1$
  String ICONID_SDED_OBJS_LIST      = "sded-objs-list";      //$NON-NLS-1$
  String ICONID_SDED_OBJ_ATTR       = "sded-obj-attr";       //$NON-NLS-1$
  String ICONID_SDED_OBJ_CLOB       = "sded-obj-clob";       //$NON-NLS-1$
  String ICONID_SDED_OBJ_RIVET      = "sded-obj-rivet";      //$NON-NLS-1$
  String ICONID_SDED_OBJ_LINK       = "sded-obj-link";       //$NON-NLS-1$
  String ICONID_SDED_OBJ_DATA       = "sded-obj-data";       //$NON-NLS-1$
  String ICONID_SDED_OBJ_CMD        = "sded-obj-cmd";        //$NON-NLS-1$
  String ICONID_SDED_OBJ_EVENT      = "sded-obj-event";      //$NON-NLS-1$
  String ICONID_USKAT_SERVER        = "uskat-server";        //$NON-NLS-1$
  String ICONID_USKAT_CONNECT       = "uskat-connect";       //$NON-NLS-1$
  String ICONID_USKAT_DISCONNECT    = "uskat-disconnect";    //$NON-NLS-1$
  String ICONID_COLORED_WORLD_BLUE  = "colored-world-blue";  //$NON-NLS-1$
  String ICONID_COLORED_WORLD_GREEN = "colored-world-green"; //$NON-NLS-1$
  String ICONID_COLORED_WORLD_RED   = "colored-world-red";   //$NON-NLS-1$

  static String getClassPropKindIconId( ESkClassPropKind aKind ) {
    return switch( aKind ) {
      case ATTR -> ICONID_SDED_CLASS_ATTR;
      case CLOB -> ICONID_SDED_CLASS_CLOB;
      case CMD -> ICONID_SDED_CLASS_CMD;
      case EVENT -> ICONID_SDED_CLASS_EVENT;
      case LINK -> ICONID_SDED_CLASS_LINK;
      case RIVET -> ICONID_SDED_CLASS_RIVET;
      case RTDATA -> ICONID_SDED_CLASS_DATA;
      default -> null;
    };
  }

  // ------------------------------------------------------------------------------------
  // TODO ???
  //

  /**
   * The reference key of the {@link #REFDEF_SUPPLIED_SK_CONN_ID}.
   */
  String REFID_SUPPLIED_SK_CONN_ID = USKAT_FULL_ID + ".gui.ctx_ref.SuppliedSkConnId"; //$NON-NLS-1$

  /**
   * For all GUI components this reference contains {@link IdChain} to get {@link ISkConnection} to work with.
   * <p>
   * The connection must be retrieved by {@link ISkConnectionSupplier#getConn(IdChain)}.
   * <p>
   * <b>Attention:</b> use methods {@link #skConnCtx(ITsGuiContext)} and {@link #skCoreApi(ITsGuiContext)} to get and
   * {@link #setCtxSkConnKey(ITsGuiContext, IdChain)} to set reference value, do not use this reference directly.
   */
  ITsGuiContextRefDef<IdChain> REFDEF_SUPPLIED_SK_CONN_ID = TsGuiContextRefDef.create( REFID_SUPPLIED_SK_CONN_ID, //
      IdChain.class, //
      TSID_IS_MANDATORY, AV_TRUE, //
      TSID_IS_NULL_ALLOWED, AV_FALSE //
  );

  /**
   * Returns Core API reference from the context using the {@link #REFDEF_SUPPLIED_SK_CONN_ID}.
   * <p>
   * This is convenience method designed to simplify usage of the option {@link #REFDEF_SUPPLIED_SK_CONN_ID}.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @return {@link ISkCoreApi} - the core API of the {@link ISkConnection} found by key
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such key in the context
   * @throws TsItemNotFoundRtException no such connection in {@link ISkConnectionSupplier}
   */
  static ISkCoreApi skCoreApi( ITsGuiContext aContext ) {
    TsNullArgumentRtException.checkNull( aContext );
    IdChain key = REFDEF_SUPPLIED_SK_CONN_ID.getRef( aContext );
    ISkConnectionSupplier cs = aContext.get( ISkConnectionSupplier.class );
    ISkConnection skConn = cs.getConn( key );
    return skConn.coreApi();
  }

  /**
   * Returns Sk-connection reference from the context using the {@link #REFDEF_SUPPLIED_SK_CONN_ID}.
   * <p>
   * This is convenience method designed to simplify usage of the {@link #REFDEF_SUPPLIED_SK_CONN_ID}.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @return {@link ISkConnection} - the {@link ISkConnection} found by key
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such key in the context
   * @throws TsItemNotFoundRtException no such connection in {@link ISkConnectionSupplier}
   */
  static ISkConnection skConnCtx( ITsGuiContext aContext ) {
    TsNullArgumentRtException.checkNull( aContext );
    IdChain key = REFDEF_SUPPLIED_SK_CONN_ID.getRef( aContext );
    ISkConnectionSupplier cs = aContext.get( ISkConnectionSupplier.class );
    return cs.getConn( key );
  }

  /**
   * Returns Sk-connection key from the context using the {@link #REFDEF_SUPPLIED_SK_CONN_ID}.
   * <p>
   * This is convenience method designed to simplify usage of the {@link #REFDEF_SUPPLIED_SK_CONN_ID}.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @return {@link IdChain} - the Sk-connection key
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such key in the context
   * @throws TsItemNotFoundRtException no such connection in {@link ISkConnectionSupplier}
   */
  static IdChain skConnKeyCtx( ITsGuiContext aContext ) {
    TsNullArgumentRtException.checkNull( aContext );
    return REFDEF_SUPPLIED_SK_CONN_ID.getRef( aContext );
  }

  /**
   * Stores the the connection key in the context.
   * <p>
   * This is convenience method designed to simplify usage of the {@link #REFDEF_SUPPLIED_SK_CONN_ID}.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @param aSkConnKey {@link IdChain} - the key to be used with {@link ISkConnectionSupplier#getConn(IdChain)}
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  static void setCtxSkConnKey( ITsGuiContext aContext, IdChain aSkConnKey ) {
    TsNullArgumentRtException.checkNulls( aContext, aSkConnKey );
    REFDEF_SUPPLIED_SK_CONN_ID.setRef( aContext, aSkConnKey );
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
