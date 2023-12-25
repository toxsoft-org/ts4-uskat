package org.toxsoft.uskat.core.gui.conn;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
interface ISkResources {

  /**
   * {@link SkConnectionSupplier}
   */
  String FMT_ERR_CONN_ID_EXISTS      = Messages.getString( "FMT_ERR_CONN_ID_EXISTS" );      //$NON-NLS-1$
  String MSG_ERR_CANT_REMOVE_NULL_ID = Messages.getString( "MSG_ERR_CANT_REMOVE_NULL_ID" ); //$NON-NLS-1$
  String FMT_WARN_NO_SUCH_CONN_ID    = Messages.getString( "FMT_WARN_NO_SUCH_CONN_ID" );    //$NON-NLS-1$

  /**
   * {@link SkSwtThreadSeparator}
   */
  String ERR_GUI_DISPLAY_UNDEF = "GUI display not defined (SkSwtThreadSeparator.REF_DISPLAY = ???)";                   //$NON-NLS-1$
  String ERR_WRONG_GUI_THREAD  = "Wrong GUI thread (ISkCoreConfigConstants.REFDEF_API_THREAD != Display.getThread())"; //$NON-NLS-1$

}
