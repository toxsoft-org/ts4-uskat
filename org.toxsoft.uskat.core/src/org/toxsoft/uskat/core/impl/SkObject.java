package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.uskat.core.ISkCoreApi;
import org.toxsoft.uskat.core.api.objserv.ISkObject;


public class SkObject
    implements ISkObject {

  @Override
  public String id() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String nmName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String description() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Skid skid() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IOptionSet attrs() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IStringMap<ISkidList> rivets() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ISkCoreApi coreApi() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Skid getSingleLinkSkid( String aLinkId ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends ISkObject> T getSingleLink( String aLinkId ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ISkidList getLinkSkids( String aLinkId ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends ISkObject> IMap<Skid, T> getLink( String aLinkId ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends ISkObject> IList<T> getLinkObjs( String aLinkId ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ISkidList getLinkRevSkids( String aClassId, String aLinkId ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends ISkObject> IMap<Skid, T> getLinkRev( String aClassId, String aLinkId ) {
    // TODO Auto-generated method stub
    return null;
  }

}
