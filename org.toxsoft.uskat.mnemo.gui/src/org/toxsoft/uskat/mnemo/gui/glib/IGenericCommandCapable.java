package org.toxsoft.uskat.mnemo.gui.glib;

import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;

public interface IGenericCommandCapable
    extends IStridableParameterized, IGenericCommandExecutor {

  IStridablesList<IGenericCommandDef> listCommandDefs();

}
