package org.toxsoft.uskat.core.gui.glib.widgets;

import static org.toxsoft.core.tsgui.graphics.icons.ITsStdIconIds.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.core.gui.glib.widgets.ISkResources.*;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.graphics.icons.*;
import org.toxsoft.core.tsgui.panels.lazy.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Uneditable {@link Text} to display single GWID, with the button to copy GWID to clipboard.
 *
 * @author hazard157
 */
public class GwidViewWidget
    extends AbstractLazyPanel<Control> {

  private final Clipboard clipboard;

  private Gwid   gwid           = null;
  private Text   text           = null;
  private Button btnToClipboard = null;

  private final SelectionAdapter copyToClipboardSelectionListener = new SelectionAdapter() {
    @Override
    public void widgetSelected( SelectionEvent e ) {
      copyGwidToClipboard();
    }
  };

  /**
   * Constructor.
   * <p>
   * Constructor stores reference to the context, does not creates copy.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public GwidViewWidget( ITsGuiContext aContext ) {
    super( aContext );
    clipboard = new Clipboard( getDisplay() );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private void showTooltipOnCopyButton( String aText ) {
    Point location = btnToClipboard.getParent().toDisplay( btnToClipboard.getLocation() );
    Point size = btnToClipboard.getSize();
    location.x += size.x / 2;
    location.y += size.y / 2;
    ToolTip toolTip = new ToolTip( getShell(), SWT.BALLOON | SWT.ICON_INFORMATION );
    // ToolTip toolTip = new ToolTip( getShell(), SWT.ICON_INFORMATION );
    toolTip.setMessage( aText );
    toolTip.setAutoHide( false ); // Prevents the tooltip from hiding automatically
    toolTip.setLocation( location.x, location.y );
    // disable hover tooltip
    String hoverToolTipText = btnToClipboard.getToolTipText();
    btnToClipboard.setToolTipText( null );
    // Show the tooltip
    toolTip.setVisible( true );
    // hide it after a delay
    getDisplay().timerExec( 2000, () -> {
      if( toolTip.isVisible() ) {
        toolTip.setVisible( false );
        toolTip.dispose();
        // restore hover tooltip
        btnToClipboard.setToolTipText( hoverToolTipText );
      }
    } );
  }

  private void copyGwidToClipboard() {
    String s = text.getText();
    if( s.isBlank() ) { // nothing to copy
      showTooltipOnCopyButton( STR_MSG_NO_GWID_TO_COPY );
      return;
    }
    // write data to clipboard
    TextTransfer textTransfer = TextTransfer.getInstance();
    clipboard.setContents( new Object[] { s }, new Transfer[] { textTransfer } );
    showTooltipOnCopyButton( STR_MSG_GWID_COPIED_TO_CLIPBOARD );
  }

  // ------------------------------------------------------------------------------------
  // AbstractLazyPanel
  //

  @Override
  protected Control doCreateControl( Composite aParent ) {
    Composite backplane = new Composite( aParent, SWT.NONE );
    backplane.setLayout( new BorderLayout() );
    // left label
    CLabel label = new CLabel( backplane, SWT.CENTER );
    label.setText( STR_LABEL_GWID );
    label.setLayoutData( new BorderData( SWT.LEFT ) );
    // text
    text = new Text( backplane, SWT.BORDER );
    text.setEditable( false );
    text.setLayoutData( new BorderData( SWT.CENTER ) );
    // btnToClipboard
    btnToClipboard = new Button( backplane, SWT.PUSH | SWT.FLAT );
    btnToClipboard.setImage( iconManager().loadStdIcon( ICONID_EDIT_COPY, EIconSize.IS_16X16 ) );
    btnToClipboard.setToolTipText( STR_COPY_GWID_TO_CLIPBOARD_D );
    btnToClipboard.addSelectionListener( copyToClipboardSelectionListener );
    btnToClipboard.setLayoutData( new BorderData( SWT.RIGHT ) );
    btnToClipboard.setEnabled( gwid != null );
    return backplane;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns then currently displayed GWID.
   *
   * @return {@link Gwid} - displayed {@link Gwid} or <code>null</code> if no Gwid is set
   */
  public Gwid getGwid() {
    return gwid;
  }

  /**
   * Returns displayed GWID string.
   * <p>
   * If no GWID is set (when {@link #getGwid()} = <code>null</code>) then returns an empty string.
   *
   * @return String - {@link Gwid#canonicalString()} or an empty string
   */
  public String getGwidString() {
    return text.getText();
  }

  /**
   * Sets the GWID to be displayed.
   *
   * @param aGwid {@link Gwid} - gwid to be displayed or <code>null</code>
   */
  public void setGwid( Gwid aGwid ) {
    gwid = aGwid;
    if( aGwid != null ) {
      text.setText( gwid.canonicalString() );
    }
    else {
      text.setText( EMPTY_STRING );
    }
    text.setEnabled( gwid != null );
    btnToClipboard.setEnabled( gwid != null );
  }

}
