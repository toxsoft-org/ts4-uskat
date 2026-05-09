package org.toxsoft.uskat.core.gui.valed.std;

import static org.toxsoft.uskat.core.gui.valed.std.ISkResources.*;

import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.dialogs.datarec.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.gui.valed.std.clsid.*;

/**
 * Helper methods to work with USkat standard VALEDs.
 *
 * @author hazard157
 */
public class SkStdValedUtils {

  /**
   * Invokes USkat class ID selection dialog.
   * <p>
   * {@link ITsDialogInfo#tsContext()} is used as a VALED creation context.
   *
   * @param aInitClassId String - initially selected class ID or <code>null</code>
   * @param aDialogInfo {@link ITsDialogInfo} - the dialog window parameters
   * @return String - selected class ID or <code>null</code> if no selection was done
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static String selectClassId( String aInitClassId, ITsDialogInfo aDialogInfo ) {
    TsNullArgumentRtException.checkNull( aDialogInfo );
    IValedControlFactory factory = ValedSkClassIdSelector.FACTORY;
    Pair<String, Boolean> p = ValedControlUtils.invokeAsModalDialog( aInitClassId, factory, aDialogInfo );
    return p.right().booleanValue() ? p.left() : null;
  }

  /**
   * Invokes USkat class ID selection modal dialog with default dialog window parameters.
   *
   * @param aContext {@link ITsGuiContext} - the VALED creation context
   * @param aInitClassId String - initially selected class ID or <code>null</code>
   * @return String - selected class ID or <code>null</code> if no selection was done
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static String selectClassId( ITsGuiContext aContext, String aInitClassId ) {
    TsNullArgumentRtException.checkNull( aContext );
    Shell shell = aContext.get( Shell.class );
    ITsDialogInfo dialogInfo =
        new TsDialogInfo( aContext, shell, STR_DLG_CLASS_ID_SELECTION, STR_DLG_CLASS_ID_SELECTION_D, 0 );
    return selectClassId( aInitClassId, dialogInfo );
  }

  /**
   * No subclasses.
   */
  private SkStdValedUtils() {
    // nop
  }

}
