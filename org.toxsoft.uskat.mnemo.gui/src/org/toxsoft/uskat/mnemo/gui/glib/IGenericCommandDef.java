package org.toxsoft.uskat.mnemo.gui.glib;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;

public interface IGenericCommandDef
    extends IStridableParameterized {

  IStridablesList<IDataDef> listArgs();

}
