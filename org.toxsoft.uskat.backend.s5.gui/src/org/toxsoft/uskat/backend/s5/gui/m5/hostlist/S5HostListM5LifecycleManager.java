package org.toxsoft.uskat.backend.s5.gui.m5.hostlist;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.uskat.s5.common.*;

/**
 * LM for {@link S5HostListM5Model}.
 *
 * @author hazard157
 */
class S5HostListM5LifecycleManager
    extends M5LifecycleManager<S5HostList, Object> {

  public S5HostListM5LifecycleManager( IM5Model<S5HostList> aModel ) {
    super( aModel, true, true, true, false, null );
  }

  @Override
  protected S5HostList doCreate( IM5Bunch<S5HostList> aValues ) {
    IList<S5Host> ll = aValues.getAs( S5HostListM5Model.FID_HOSTS, IList.class );
    S5HostList hostsList = new S5HostList();
    hostsList.setAll( ll );
    return hostsList;
  }

  @Override
  protected S5HostList doEdit( IM5Bunch<S5HostList> aValues ) {
    IList<S5Host> ll = aValues.getAs( S5HostListM5Model.FID_HOSTS, IList.class );
    S5HostList hostsList = new S5HostList();
    hostsList.setAll( ll );
    return hostsList;
  }

  @Override
  protected void doRemove( S5HostList aEntity ) {
    // nop
  }

}
