package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.utils.valobj.*;
import org.toxsoft.uskat.core.api.events.*;

public class SkUtils {

  public static void initialize() {
    TsValobjUtils.registerKeeper( SkEvent.KEEPER_ID, SkEvent.KEEPER );
  }

}
