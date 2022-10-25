package org.toxsoft.uskat.mnemo.gui.glib;

import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.panels.lazy.*;
import org.toxsoft.core.tsgui.utils.anim.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.uskat.mnemo.gui.tmp_mws_plugin.*;

/**
 * The mnemoscheme dispay panel.
 *
 * @author hazard157
 */
public interface IMnemoPanel
    extends ILazyControl<Control>, //
    IPausableAnimation {

  void showMnemo( MnemoScreenId aMnemoGwid );

  MnemoScreenId getMnemoScreenId();

  default void clearMnemo() {
    showMnemo( null );
  }

  void addExternalActionHandler( ITsExternalActionHandler aHandler );

  // FIXME create GenericCommand and it's framework

  IStridablesList<IGenericCommandDef> listCommandDefs();

  IGenericCommandExecutor commandExecutor();

}
