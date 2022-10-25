package org.toxsoft.uskat.mnemo.gui.glib;

import org.toxsoft.core.tslib.bricks.validator.*;

public interface IGenericCommandExecutor {

  ValidationResult execGenericCommand( IGenericCommand aCommand );

}
