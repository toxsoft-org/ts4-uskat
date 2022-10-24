package org.toxsoft.uskat.mnemo.gui.tmp_mws_plugin;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;

public interface IMnemoPersp {

  /**
   * FIXME returns one of the GWIDs listed in {@link #listMainMnemos()}
   *
   * @return
   */
  MnemoScreenId getHomeMnemoGwid();

  void setHomeMnemoGwid( MnemoScreenId aGwid );

  IList<MnemoScreenId> listMainMnemos();

  IList<MnemoScreenId> listPopupMnemos( MnemoScreenId aMainMnemoGwid );

}
