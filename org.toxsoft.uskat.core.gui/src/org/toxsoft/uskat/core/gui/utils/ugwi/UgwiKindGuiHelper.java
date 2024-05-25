package org.toxsoft.uskat.core.gui.utils.ugwi;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.panels.generic.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.utils.ugwi.*;

/**
 * Base implementation of {@link IUgwiKindGuiHelper}.
 *
 * @author hazard157
 */
public non-sealed class UgwiKindGuiHelper
    implements IUgwiKindGuiHelper {

  private final IUgwiKind kind;

  /**
   * Constructor.
   *
   * @param aKindId String - the registered kind ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException kind of the specified ID is not registered
   */
  public UgwiKindGuiHelper( String aKindId ) {
    kind = UgwiUtils.getKind( aKindId );
  }

  // ------------------------------------------------------------------------------------
  // IStridableParameterized
  //

  @Override
  final public String id() {
    return kind.id();
  }

  @Override
  final public String nmName() {
    return kind.nmName();
  }

  @Override
  final public String description() {
    return kind.description();
  }

  @Override
  final public IOptionSet params() {
    return kind.params();
  }

  @Override
  final public String iconId() {
    return kind.iconId();
  }

  // ------------------------------------------------------------------------------------
  // IUgwiKindGuiHelper
  //

  @SuppressWarnings( "unchecked" )
  @Override
  final public <T extends IUgwiKind> T kind() {
    return (T)kind;
  }

  @Override
  final public IGenericEntityEditPanel<Ugwi> createEntityPanel( ITsGuiContext aTsContext, boolean aViewer ) {
    TsNullArgumentRtException.checkNull( aTsContext );
    IGenericEntityEditPanel<Ugwi> p = doCreateEntityPanel( aTsContext, aViewer );
    TsInternalErrorRtException.checkNull( p );
    return p;
  }

  @Override
  final public IGenericSelectorPanel<Ugwi> createSelectorPanel( ITsGuiContext aTsContext, boolean aViewer ) {
    TsNullArgumentRtException.checkNull( aTsContext );
    IGenericSelectorPanel<Ugwi> p = doCreateSelectorPanel( aTsContext, aViewer );
    TsInternalErrorRtException.checkNull( p );
    return p;
  }

  // ------------------------------------------------------------------------------------
  // To override/implement
  //

  /**
   * Implementation must create UGWI editor/viewer panel.
   * <p>
   * In the base class returns new instance of {@link DefaultGenericUgwiEditorPanel}. There is no need to call
   * superclass method when overriding.
   *
   * @param aTsContext {@link ITsGuiContext} - the context, never is <code>null</code>
   * @param aViewer boolean - <code>true</code> to create editor, <code>false</code> - the viewer
   * @return {@link IGenericEntityEditPanel}&lt;{@link Ugwi}&lt; - created panel
   */
  protected IGenericEntityEditPanel<Ugwi> doCreateEntityPanel( ITsGuiContext aTsContext, boolean aViewer ) {
    return new DefaultGenericUgwiEditorPanel( aTsContext, aViewer, this );
  }

  /**
   * Implementation must the panel to select the UGWI of this kind.
   * <p>
   * In the base class returns new instance of {@link DefaultGenericUgwiSelectorPanel}. There is no need to call
   * superclass method when overriding.
   *
   * @param aTsContext {@link ITsGuiContext} - the context, never is <code>null</code>
   * @param aViewer boolean - <code>true</code> to create editor, <code>false</code> - the viewer
   * @return {@link IGenericSelectorPanel}&lt;{@link Ugwi}&lt; - created panel
   */
  protected IGenericSelectorPanel<Ugwi> doCreateSelectorPanel( ITsGuiContext aTsContext, boolean aViewer ) {
    return new DefaultGenericUgwiSelectorPanel( aTsContext, aViewer, this );
  }

  /**
   * Implementation must check UGWI for syntactical validity.
   * <p>
   * In the base class returns {@link ValidationResult#SUCCESS}, there is no need to call superclass method when
   * overriding.
   *
   * @param aUgwi {@link Ugwi} - the UGWI of this kind, never is <code>null</code>
   * @return {@link Object} - found (created) entity or <code>null</code>
   */
  protected ValidationResult doValidateUgwi( Ugwi aUgwi ) {
    return ValidationResult.SUCCESS;
  }

}
