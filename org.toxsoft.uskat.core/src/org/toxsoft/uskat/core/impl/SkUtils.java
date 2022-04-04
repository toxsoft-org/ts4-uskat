package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.utils.valobj.*;
import org.toxsoft.uskat.core.api.events.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * USkat helper methods and the point of entry.
 *
 * @author hazard157
 */
public class SkUtils {

  /**
   * Initializes static stuff, must be called once before any USkat usage.
   */
  public static void initialize() {
    TsValobjUtils.registerKeeper( SkEvent.KEEPER_ID, SkEvent.KEEPER );
  }

  /**
   * Creates the instance of the single threaded {@link ISkConnection}.
   *
   * @return {@link ISkConnection} - instance of the connection in {@link ESkConnState#CLOSED CLOSED} state
   */
  public static ISkConnection createConnection() {
    return new SkConnection();
  }

}
