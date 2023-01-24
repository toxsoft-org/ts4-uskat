package org.toxsoft.uskat.sded.gui.glib;

import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.panels.inpled.*;
import org.toxsoft.core.tslib.bricks.events.change.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * {@link AbstractInplaceContentPanel} implementation wraping owver {@link IM5EntityPanel}.
 *
 * @author hazard157
 * @param <T> - modelled entity type
 */
public class InplaceM5EntityPanelWrapper<T>
    extends AbstractInplaceContentPanel {

  private final IGenericChangeListener m5PanelChangeListener = aSource -> {
    changed = true;
    genericChangeEventer().fireChangeEvent();
  };

  private final IM5EntityPanel<T> m5Panel;

  private boolean changed = false;

  /**
   * Constructor.
   * <p>
   * Constructos stores reference to the context, does not creates copy.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @param aM5Panel {@link IM5EntityPanel} - the wrapped panel
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException wrapped panel has created SWT widget
   */
  public InplaceM5EntityPanelWrapper( ITsGuiContext aContext, IM5EntityPanel<T> aM5Panel ) {
    super( aContext );
    m5Panel = TsNullArgumentRtException.checkNull( aM5Panel );
    TsIllegalArgumentRtException.checkNoNull( aM5Panel.getControl() );
    m5Panel.genericChangeEventer().addListener( m5PanelChangeListener );
  }

  @Override
  protected Control doCreateControl( Composite aParent ) {
    return m5Panel.createControl( aParent );
  }

  @Override
  public boolean isViewer() {
    return m5Panel.isViewer() && m5Panel.lifecycleManager() != null;
  }

  @Override
  public boolean isEditing() {
    return m5Panel.isEditable();
  }

  @Override
  public boolean isChanged() {
    return changed;
  }

  @Override
  public ValidationResult canStartEditing() {
    return validate();
  }

  @Override
  public void setEditMode( boolean aMode ) {
    m5Panel.setEditable( aMode );
    changed = false;
  }

  @Override
  public ValidationResult validate() {
    ValidationResult vr = m5Panel.canGetValues();
    IM5LifecycleManager<T> lm = m5Panel.lifecycleManager();
    if( !vr.isError() && lm != null ) {
      IM5Bunch<T> bunch = m5Panel.getValues();
      T entity = m5Panel.lastValues().originalEntity();
      if( entity != null ) {
        vr = ValidationResult.firstNonOk( vr, lm.canEdit( bunch ) );
      }
      else {
        vr = ValidationResult.firstNonOk( vr, lm.canCreate( bunch ) );
      }
    }
    return vr;
  }

  @Override
  public void doApplyChanges() {
    IM5LifecycleManager<T> lm = m5Panel.lifecycleManager();
    IM5Bunch<T> bunch = m5Panel.getValues();
    T originalEntity = m5Panel.lastValues().originalEntity();
    T entity;
    if( originalEntity != null ) {
      entity = lm.edit( bunch );
    }
    else {
      entity = lm.create( bunch );
    }
    m5Panel.setEntity( entity );
    changed = false;
  }

  @Override
  public void revertChanges() {
    m5Panel.setEntity( m5Panel.getValues().originalEntity() );
    changed = false;
  }

}
