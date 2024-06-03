package org.toxsoft.uskat.core.gui.ugwi.gui;

import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.panels.generic.*;
import org.toxsoft.core.tsgui.panels.lazy.*;
import org.toxsoft.core.tslib.bricks.events.change.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Textual representation UGWI viewer/editor implementation of {@link IGenericEntityEditPanel} to display UGWI.
 * <p>
 * Contains simple {@link Text} allwomg to view/edit canonical representation {@link Ugwi#canonicalString()}.
 * <p>
 * Instance of this class is returned by the default implementation of
 * {@link IUgwiKindGuiHelper#createUgwiEntityPanel(ITsGuiContext, boolean)}.
 *
 * @author hazard157
 */
public class DefaultGenericUgwiEditorPanel
    extends AbstractLazyPanel<Control>
    implements IGenericEntityEditPanel<Ugwi> {

  private final GenericChangeEventer genericChangeEventer;

  private final IUgwiKindGuiHelper ugwiGuiHelper;
  private final boolean            isViewer;

  private Text text;
  private Ugwi ugwi = Ugwi.NONE;

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
  public DefaultGenericUgwiEditorPanel( ITsGuiContext aContext, boolean aViewer,
      IUgwiKindGuiHelper aUgwiKindGuiHelper ) {
    super( aContext );
    ugwiGuiHelper = TsNullArgumentRtException.checkNull( aUgwiKindGuiHelper );
    isViewer = aViewer;
    genericChangeEventer = new GenericChangeEventer( this );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private void updateTextWidget() {
    if( text != null ) {
      text.setText( ugwi.canonicalString() );
    }
  }

  // ------------------------------------------------------------------------------------
  // IGenericEntityEditPanel
  //

  @Override
  public void setEntity( Ugwi aEntity ) {
    ugwi = aEntity != null ? aEntity : Ugwi.NONE;
    updateTextWidget();
  }

  @Override
  public boolean isViewer() {
    return isViewer;
  }

  @Override
  public Ugwi getEntity() {
    TsValidationFailedRtException.checkError( canGetEntity() );
    return ugwi;
  }

  // method also updates field #ugwi
  @Override
  public ValidationResult canGetEntity() {
    String s = text.getText();
    if( s.isEmpty() ) {
      ugwi = Ugwi.NONE;
      return ValidationResult.SUCCESS;
    }
    ValidationResult vr = Ugwi.validateCanonicalString( s );
    if( !vr.isError() ) {
      Ugwi u = Ugwi.fromCanonicalString( s );
      vr = ValidationResult.firstNonOk( vr, ugwiGuiHelper.kind().ugwiKind().validateUgwi( u ) );
      if( !vr.isError() ) {
        ugwi = u;
      }
    }
    return vr;
  }

  // ------------------------------------------------------------------------------------
  // AbstractLazyPanel
  //

  @Override
  protected Control doCreateControl( Composite aParent ) {
    Composite backplane = new Composite( aParent, SWT.NONE );
    backplane.setLayout( new GridLayout( 1, false ) );
    int swtStyle = SWT.SINGLE | SWT.BORDER;
    if( isViewer ) {
      swtStyle |= SWT.READ_ONLY;
    }
    text = new Text( backplane, swtStyle );
    text.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );
    updateTextWidget();
    if( !isViewer ) {
      text.addModifyListener( aEvent -> genericChangeEventer.fireChangeEvent() );
    }
    return backplane;
  }

  // ------------------------------------------------------------------------------------
  // IGenericChangeEventCapable
  //

  @Override
  public IGenericChangeEventer genericChangeEventer() {
    return genericChangeEventer;
  }

}
