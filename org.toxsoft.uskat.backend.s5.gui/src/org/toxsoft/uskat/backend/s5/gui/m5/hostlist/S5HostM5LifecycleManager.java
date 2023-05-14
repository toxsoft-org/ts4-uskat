package org.toxsoft.uskat.backend.s5.gui.m5.hostlist;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.common.*;

/**
 * LM for {@link S5HostM5Model}.
 *
 * @author hazard157
 */
class S5HostM5LifecycleManager
    extends M5LifecycleManager<S5Host, S5HostList> {

  public S5HostM5LifecycleManager( IM5Model<S5Host> aModel, S5HostList aMaster ) {
    super( aModel, true, true, true, true, aMaster );
    TsNullArgumentRtException.checkNull( aMaster );
  }

  @Override
  protected ValidationResult doBeforeCreate( IM5Bunch<S5Host> aValues ) {
    String address = aValues.getAsAv( S5HostM5Model.FID_ADDRESS ).asString();
    int port = aValues.getAsAv( S5HostM5Model.FID_PORT ).asInt();
    return S5Host.validateS5HostArgs( address, port );
  }

  @Override
  protected S5Host doCreate( IM5Bunch<S5Host> aValues ) {
    String address = aValues.getAsAv( S5HostM5Model.FID_ADDRESS ).asString();
    int port = aValues.getAsAv( S5HostM5Model.FID_PORT ).asInt();
    S5Host h = new S5Host( address, port );
    master().add( h );
    return h;
  }

  @Override
  protected ValidationResult doBeforeEdit( IM5Bunch<S5Host> aValues ) {
    String address = aValues.getAsAv( S5HostM5Model.FID_ADDRESS ).asString();
    int port = aValues.getAsAv( S5HostM5Model.FID_PORT ).asInt();
    return S5Host.validateS5HostArgs( address, port );
  }

  @Override
  protected S5Host doEdit( IM5Bunch<S5Host> aValues ) {
    String address = aValues.getAsAv( S5HostM5Model.FID_ADDRESS ).asString();
    int port = aValues.getAsAv( S5HostM5Model.FID_PORT ).asInt();
    S5Host h = new S5Host( address, port );
    int index = master().indexOf( aValues.originalEntity() );
    master().set( index, h );
    return h;
  }

  @Override
  protected ValidationResult doBeforeRemove( S5Host aEntity ) {
    return ValidationResult.SUCCESS;
  }

  @Override
  protected void doRemove( S5Host aEntity ) {
    master().remove( aEntity );
  }

  @Override
  protected IList<S5Host> doListEntities() {
    return master();
  }

}
