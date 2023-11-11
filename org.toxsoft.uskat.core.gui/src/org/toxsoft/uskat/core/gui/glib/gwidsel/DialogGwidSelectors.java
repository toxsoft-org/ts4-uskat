package org.toxsoft.uskat.core.gui.glib.gwidsel;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.dialogs.datarec.*;
import org.toxsoft.core.tsgui.utils.layout.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Invokes various GWID selection dialogs.
 *
 * @author hazard157
 */
public class DialogGwidSelectors {

  /**
   * Panel for {@link DialogGwidSelectors#selectSinglePropGwid(ITsDialogInfo, Gwid, ESkClassPropKind, ISkConnection)}.
   *
   * @author hazard157
   */
  static class SingleConcretePropGwidPanel
      extends AbstractTsDialogPanel<Gwid, ISkConnection> {

    private final IPanelSingleConcreteGwidSelector panel;

    SingleConcretePropGwidPanel( Composite aParent, TsDialog<Gwid, ISkConnection> aOwnerDialog,
        ESkClassPropKind aKind ) {
      super( aParent, aOwnerDialog );
      ITsGuiContext ctx = new TsGuiContext( tsContext() );
      IGwidSelectorConstants.OPDEF_CLASS_PROP_KIND.setValue( ctx.params(), avValobj( aKind ) );
      panel = new PanelSingleConcreteGwidSelector( ctx );
      //
      this.setLayout( new BorderLayout() );
      panel.createControl( this );
      panel.getControl().setLayoutData( BorderLayout.CENTER );
    }

    @Override
    protected void doSetDataRecord( Gwid aData ) {
      panel.setEntity( aData );
    }

    @Override
    protected ValidationResult doValidate() {
      return panel.canGetEntity();
    }

    @Override
    protected Gwid doGetDataRecord() {
      return panel.getEntity();
    }

  }

  /**
   * No subclasses
   */
  private DialogGwidSelectors() {
    // nop
  }

  /**
   * Invokes single concrete class property GWID selection dialog.
   *
   * @param aDialogInfo {@link ITsDialogInfo} - dialog window properties
   * @param aInital {@link Gwid} - initially displayed value or <code>null</code>
   * @param aKind {@link ESkClassPropKind} - the class property kind to select GWID
   * @param aSkConn {@link ISkConnection} - the Sk-connection used for GWID selection
   * @return {@link Gwid} - selected GWID or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static Gwid selectSinglePropGwid( ITsDialogInfo aDialogInfo, Gwid aInital, ESkClassPropKind aKind,
      ISkConnection aSkConn ) {
    TsNullArgumentRtException.checkNulls( aDialogInfo, aKind, aSkConn );
    IDialogPanelCreator<Gwid, ISkConnection> creator = ( p, dlg ) -> new SingleConcretePropGwidPanel( p, dlg, aKind );
    TsDialog<Gwid, ISkConnection> d = new TsDialog<>( aDialogInfo, aInital, aSkConn, creator );
    return d.execData();
  }

}
