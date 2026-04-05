package org.toxsoft.uskat.backend.s5.gui.utils;

import org.eclipse.swt.widgets.*;
import org.toxsoft.uskat.core.gui.glib.query.*;

class S5ConnectDialog
    extends SkProgressDialog {

  boolean needCancel = false;

  S5ConnectDialog( Shell aShell, String aDialogName, long aTimeout ) {
    super( aShell, aDialogName, aTimeout );
    setCancelHandler( () -> {
      // Пользователь отменил операцию
      cancel();
    } );
  }

  boolean isNeedCancel() {
    return needCancel;
  }

  void cancel() {
    needCancel = true;
  }
}
