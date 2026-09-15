package org.toxsoft.uskat.devel.mws.e4.uiparts;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.dialogs.*;
import org.toxsoft.core.tsgui.widgets.*;
import org.toxsoft.uskat.core.gui.e4.uiparts.*;

/**
 * Demonstrates UI and usage of the SK-entities selector VALEDs from the package
 * <code>org.toxsoft.uskat.core.gui.valed.std.Valed*</code>.
 *
 * @author hazard157
 */
public class UipartSelectorValedsDemo
    extends SkMwsAbstractPart {

  @Override
  protected void doCreateContent( TsComposite aParent ) {

    // TODO UipartSelectorValedsDemo.doCreateContent()

    Button b = new Button( aParent, SWT.PUSH );
    b.setText( getClass().getName() );
    b.addSelectionListener( new SelectionAdapter() {

      @Override
      public void widgetSelected( SelectionEvent e ) {
        TsDialogUtils.underDevelopment( getShell() );
      }

    } );

  }

}
