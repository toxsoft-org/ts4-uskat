package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.devapi.*;

public class SkCoreApi
    implements IDevCoreApi, ISkFrontendRear, ICloseable {

  SkCoreApi( ITsContextRo aArgs, SkConnection aConn ) {
    // TODO Auto-generated constructor stub
  }

  // ------------------------------------------------------------------------------------
  // Package API
  //

  void papiCheckIsOpen() {

  }

  @Override
  public ISkSysdescr sysdescr() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void close() {
    // TODO Auto-generated method stub

  }

  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    // TODO Auto-generated method stub

  }

  @Override
  public <T> T getBackendAddon( Class<T> aAddonInterface ) {
    // TODO Auto-generated method stub
    return null;
  }

}
