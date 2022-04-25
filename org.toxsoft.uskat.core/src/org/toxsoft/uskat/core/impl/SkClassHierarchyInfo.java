package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;

/**
 * {@link ISkClassHierarchyInfo} implementation.
 *
 * @author hazard157
 */
class SkClassHierarchyInfo
    implements ISkClassHierarchyInfo {

  private final SkClassInfo  owner;
  private final ISkClassInfo parent;

  public SkClassHierarchyInfo( SkClassInfo aOwner, ISkClassInfo aParent ) {
    TsNullArgumentRtException.checkNull( aOwner );
    owner = aOwner;
    parent = aParent;
  }

  // ------------------------------------------------------------------------------------
  // package API
  //

  void papiClearCache() {
    // TODO SkClassHierarchyInfo.papiClearCache()
  }

  // ------------------------------------------------------------------------------------
  // ISkClassHierarchyInfo
  //

  @Override
  public ISkClassInfo parent() {
    return parent;
  }

  @Override
  public IStridablesList<ISkClassInfo> listSubclasses( boolean aOnlyChilds, boolean aIncludeSelf ) {
    // TODO реализовать SkClassHierarchyInfo.listSubclasses()
    throw new TsUnderDevelopmentRtException( "SkClassHierarchyInfo.listSubclasses()" );
  }

  @Override
  public IStridablesList<ISkClassInfo> listSuperclasses( boolean aIncludeSelf ) {
    // TODO реализовать SkClassHierarchyInfo.listSuperclasses()
    throw new TsUnderDevelopmentRtException( "SkClassHierarchyInfo.listSuperclasses()" );
  }

  @Override
  public boolean isSuperclassOf( String aSubclassId ) {
    // TODO реализовать SkClassHierarchyInfo.isSuperclassOf()
    throw new TsUnderDevelopmentRtException( "SkClassHierarchyInfo.isSuperclassOf()" );
  }

  @Override
  public boolean isAssignableFrom( String aSubclassId ) {
    // TODO реализовать SkClassHierarchyInfo.isAssignableFrom()
    throw new TsUnderDevelopmentRtException( "SkClassHierarchyInfo.isAssignableFrom()" );
  }

  @Override
  public boolean isSubclassOf( String aSuperclassId ) {
    // TODO реализовать SkClassHierarchyInfo.isSubclassOf()
    throw new TsUnderDevelopmentRtException( "SkClassHierarchyInfo.isSubclassOf()" );
  }

  @Override
  public boolean isAssignableTo( String aSuperclassId ) {
    // TODO реализовать SkClassHierarchyInfo.isAssignableTo()
    throw new TsUnderDevelopmentRtException( "SkClassHierarchyInfo.isAssignableTo()" );
  }

  @Override
  public boolean isOfClass( IStringList aClassIdsList ) {
    // TODO реализовать SkClassHierarchyInfo.isOfClass()
    throw new TsUnderDevelopmentRtException( "SkClassHierarchyInfo.isOfClass()" );
  }

}
