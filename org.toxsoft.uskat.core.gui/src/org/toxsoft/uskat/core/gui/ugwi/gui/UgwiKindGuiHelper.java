package org.toxsoft.uskat.core.gui.ugwi.gui;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.panels.generic.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.ugwis.*;

/**
 * Base implementation of {@link IUgwiKindGuiHelper}.
 *
 * @author hazard157
 * @param <T> - the UGWI content type
 */
public non-sealed class UgwiKindGuiHelper<T>
    implements IUgwiKindGuiHelper {

  private final AbstractUgwiKind<T> kind;

  /**
   * Constructor.
   *
   * @param aKind {@link AbstractUgwiKind}&lt;T&gt; - the UGWI kind
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException kind of the specified ID is not registered
   */
  public UgwiKindGuiHelper( AbstractUgwiKind<T> aKind ) {
    kind = TsNullArgumentRtException.checkNull( aKind );
  }

  // ------------------------------------------------------------------------------------
  // IStridable
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

  // ------------------------------------------------------------------------------------
  // IParameterized
  //

  @Override
  final public IOptionSet params() {
    return kind.params();
  }

  // ------------------------------------------------------------------------------------
  // IIconIdable
  //

  @Override
  final public String iconId() {
    return kind.iconId();
  }

  // ------------------------------------------------------------------------------------
  // IUgwiKindGuiHelper
  //

  @Override
  final public AbstractUgwiKind<T> kind() {
    return kind;
  }

  @Override
  final public IGenericEntityEditPanel<Ugwi> createUgwiEntityPanel( ITsGuiContext aTsContext, boolean aViewer ) {
    TsNullArgumentRtException.checkNull( aTsContext );
    IGenericEntityEditPanel<Ugwi> p = doCreateEntityPanel( aTsContext, aViewer );
    TsInternalErrorRtException.checkNull( p );
    return p;
  }

  @Override
  final public IGenericSelectorPanel<Ugwi> createUgwiSelectorPanel( ITsGuiContext aTsContext ) {
    TsNullArgumentRtException.checkNull( aTsContext );
    IGenericSelectorPanel<Ugwi> p = doCreateSelectorPanel( aTsContext, true );
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

}
