package org.toxsoft.uskat.core.gui.utils.ugwi;

import java.util.*;

import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.panels.generic.*;
import org.toxsoft.core.tsgui.panels.lazy.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.utils.ugwi.*;

/**
 * {@link DefaultGenericUgwiEditorPanel} implementation of {@link IGenericSelectorPanel} to select UGWI.
 * <p>
 * Uses {@link DefaultGenericUgwiEditorPanel} to specify UGWI as a selected one.
 * <p>
 * Instance of this class is returned by the default implementation of
 * {@link IUgwiKindGuiHelper#createSelectorPanel(ITsGuiContext, boolean)}.
 *
 * @author hazard157
 */
public class DefaultGenericUgwiSelectorPanel
    extends AbstractTsStdEventsProducerLazyPanel<Ugwi, Control>
    implements IGenericSelectorPanel<Ugwi> {

  private final DefaultGenericUgwiEditorPanel ugwiPanel;

  /**
   * Constructor.
   * <p>
   * Constructor stores reference to the context, does not creates copy.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @param aViewer boolean - the flag to create the viewer, not an editor
   * @param aUgwiKindGuiHelper {@link IUgwiKindGuiHelper} - panel creator UGWI kind
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public DefaultGenericUgwiSelectorPanel( ITsGuiContext aContext, boolean aViewer, IUgwiKindGuiHelper aUgwiKindGuiHelper ) {
    super( aContext );
    ugwiPanel = new DefaultGenericUgwiEditorPanel( tsContext(), aViewer, aUgwiKindGuiHelper );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private void whenUgwiPanelChange() {
    Ugwi sel = selectedItem();
    genericChangeEventer.fireChangeEvent();
    selectionChangeEventHelper.fireTsSelectionEvent( sel );
  }

  // ------------------------------------------------------------------------------------
  // AbstractTsStdEventsProducerLazyPanel
  //

  @Override
  protected Control doCreateControl( Composite aParent ) {
    ugwiPanel.createControl( aParent );
    ugwiPanel.genericChangeEventer().addListener( aSource -> whenUgwiPanelChange() );
    return ugwiPanel.getControl();
  }

  @Override
  public Ugwi selectedItem() {
    if( !ugwiPanel.canGetEntity().isError() ) {
      return ugwiPanel.getEntity();
    }
    return null;
  }

  @Override
  public void setSelectedItem( Ugwi aItem ) {
    ugwiPanel.setEntity( aItem );
    Ugwi sel = selectedItem();
    if( !Objects.equals( aItem, sel ) ) {
      whenUgwiPanelChange();
    }
  }

  // ------------------------------------------------------------------------------------
  // IGenericContentPanel
  //

  @Override
  public boolean isViewer() {
    return ugwiPanel.isViewer();
  }

  // ------------------------------------------------------------------------------------
  // IGenericSelectorPanel
  //

  @Override
  public void refresh() {
    // nop
  }

}
