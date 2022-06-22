package org.toxsoft.uskat.core.api.cmdserv;

import org.toxsoft.core.tslib.gw.gwid.*;

/**
 * Command service listener.
 *
 * @author hazard157
 */
public interface ISkCommandServiceListener {

  /**
   * Called then executed commands list {@link ISkCommandService#listExecutableCommandGwids()} changes.
   *
   * @param aExecutableCommandGwids {@link IGwidList} - the same as
   *          {@link ISkCommandService#listExecutableCommandGwids()}
   */
  void onExecutableCommandGwidsChanged( IGwidList aExecutableCommandGwids );

}
