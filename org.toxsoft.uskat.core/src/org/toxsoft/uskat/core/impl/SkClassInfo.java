package org.toxsoft.uskat.core.impl;

import java.io.*;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * {@link ISkClassInfo} implementation.
 *
 * @author hazard157
 */
class SkClassInfo
    extends StridableParameterizedSer
    implements ISkClassInfo {

  private static final long serialVersionUID = 7297489386958250583L;

  /**
   * Inner implementation of {@link ISkClassProps}.
   *
   * @author hazard157
   * @param <T> - the property type
   */
  class SkClassProps<T extends IDtoClassPropInfoBase>
      implements ISkClassProps<T>, Serializable {

    private static final long serialVersionUID = 6796911409980831795L;

    private final ESkClassPropKind   kind;
    private final IStridablesList<T> itemsAll;
    private final IStridablesList<T> itemsSelf;

    SkClassProps( ESkClassPropKind aKind, IStridablesList<IDtoClassInfo> aList ) {
      IDtoClassInfo thisDto = aList.getByKey( id() );
      // self properties
      IStridablesListEdit<T> llSelf = new StridablesList<>();
      llSelf.addAll( thisDto.propInfos( aKind ) );
      // ancestors properties
      IStridablesListEdit<T> llAll = new StridablesList<>();
      IDtoClassInfo dtoParent = aList.findByKey( thisDto.parentId() );
      while( dtoParent != null ) {
        llAll.insertAll( 0, dtoParent.propInfos( aKind ) );
        dtoParent = aList.findByKey( dtoParent.parentId() );
      }
      kind = aKind;
      itemsAll = llAll;
      itemsSelf = llSelf;
    }

    @Override
    public ESkClassPropKind kind() {
      return kind;
    }

    @Override
    public IStridablesList<T> list() {
      return itemsAll;
    }

    @Override
    public IStridablesList<T> listSelf() {
      return itemsSelf;
    }

    @Override
    public ISkClassInfo findSuperDeclarer( String aPropId ) {
      if( itemsSelf.hasKey( aPropId ) ) {
        return SkClassInfo.this;
      }
      ISkClassInfo p = parent();
      if( p != null ) {
        return p.props( kind ).findSuperDeclarer( aPropId );
      }
      return null;
    }

    @Override
    public IStridablesList<ISkClassInfo> findSubDeclarers( String aPropId ) {
      IStridablesListEdit<ISkClassInfo> ll = new StridablesList<>();
      for( ISkClassInfo cinf : listSubclasses( false, false ) ) {
        if( cinf.props( kind ).listSelf().hasKey( aPropId ) ) {
          ll.add( cinf );
        }
      }
      return ll;
    }

    @Override
    public IStridablesList<T> makeCopy( boolean aOnlySelf ) {
      IStridablesList<T> src = aOnlySelf ? itemsSelf : itemsAll;
      IStridablesListEdit<T> ll = new StridablesList<>();
      for( T t : src ) {
        ll.add( t.makeCopy() );
      }
      return ll;
    }

  }

  private IStridablesList<ISkClassInfo> descendantChildsNoSelf   = null;
  private IStridablesList<ISkClassInfo> descendantChildsWithSelf = null;
  private IStridablesList<ISkClassInfo> descendantsAllNoSelf     = null;
  private IStridablesList<ISkClassInfo> descendantsAllWithSelf   = null;
  private IStridablesList<ISkClassInfo> ancestorsNoSelf          = null;
  private IStridablesList<ISkClassInfo> ancestorsWithSelf        = null;

  private final ISkClassInfo                             parentInfo;
  private final ISkClassProps<IDtoAttrInfo>              attrs;
  private final ISkClassProps<IDtoRivetInfo>             rivets;
  private final ISkClassProps<IDtoClobInfo>              clobs;
  private final ISkClassProps<IDtoRtdataInfo>            rtdata;
  private final ISkClassProps<IDtoLinkInfo>              links;
  private final ISkClassProps<IDtoCmdInfo>               cmds;
  private final ISkClassProps<IDtoEventInfo>             events;
  private final IMap<ESkClassPropKind, ISkClassProps<?>> propsMap;

  /**
   * Constructor.
   * <p>
   * Attention: after all class are created and before any use of them method
   * {@link #papiInitClassHierarchy(IStridablesList)} must be called on all classes in the order of inheritance
   * (subclasses after superclasses).
   *
   * @param aClassDto {@link IDtoClassInfo} - source for instance creation
   * @param aParent {@link ISkClassInfo} - parent class
   * @param aList {@link IStridablesList}&lt;{@link IDtoClassInfo}&gt; - list of valid tree of classes
   */
  public SkClassInfo( IDtoClassInfo aClassDto, ISkClassInfo aParent, IStridablesList<IDtoClassInfo> aList ) {
    super( aClassDto.id(), aClassDto.params() );
    parentInfo = aParent;
    attrs = new SkClassProps<>( ESkClassPropKind.ATTR, aList );
    rivets = new SkClassProps<>( ESkClassPropKind.RIVET, aList );
    clobs = new SkClassProps<>( ESkClassPropKind.CLOB, aList );
    rtdata = new SkClassProps<>( ESkClassPropKind.RTDATA, aList );
    links = new SkClassProps<>( ESkClassPropKind.LINK, aList );
    cmds = new SkClassProps<>( ESkClassPropKind.CMD, aList );
    events = new SkClassProps<>( ESkClassPropKind.EVENT, aList );
    IMapEdit<ESkClassPropKind, ISkClassProps<?>> map = new ElemMap<>();
    map.put( ESkClassPropKind.ATTR, attrs );
    map.put( ESkClassPropKind.RIVET, rivets );
    map.put( ESkClassPropKind.CLOB, clobs );
    map.put( ESkClassPropKind.RTDATA, rtdata );
    map.put( ESkClassPropKind.LINK, links );
    map.put( ESkClassPropKind.CMD, cmds );
    map.put( ESkClassPropKind.EVENT, events );
    propsMap = map;
  }

  /**
   * This method is first one to be called after constructor.
   * <p>
   * Method must be called <b>after</b> all instances of {@link ISkClassInfo} are created but <b>before</b> any use of
   * created classes.
   *
   * @param aAll {@link IStridablesList}&lt;{@link ISkClassInfo}&gt; - list of valid tree of classes
   */
  void papiInitClassHierarchy( IStridablesList<ISkClassInfo> aAll ) {
    // предки
    IStridablesListEdit<ISkClassInfo> a2 = new StridablesList<>();
    ISkClassInfo cinf = this;
    while( !cinf.parentId().isEmpty() ) {
      cinf = aAll.getByKey( cinf.parentId() );
      a2.insert( 0, cinf );
    }
    ancestorsNoSelf = a2;
    IStridablesListEdit<ISkClassInfo> a1 = new StridablesList<>( ancestorsNoSelf );
    a1.add( this );
    ancestorsWithSelf = a1;
    // наследники
    IStridablesListEdit<ISkClassInfo> d1 = new StridablesList<>(); // only childs, without self
    IStridablesListEdit<ISkClassInfo> d2 = new StridablesList<>(); // all descendants, without self
    for( ISkClassInfo cInfo : aAll ) {
      // добавим непосредственных детей в d1
      if( cInfo.parentId().equals( id() ) ) {
        d1.add( cInfo );
      }
      // в d2 добавим всех потомков
      if( !ancestorsWithSelf.hasKey( cInfo.id() ) ) { // оптимизация: убедимся, что класс не сам, и не родитель
        if( internalIsDescendant( cInfo, id(), aAll ) ) {
          d2.add( cInfo );
        }
      }
    }
    descendantChildsNoSelf = d1;
    descendantsAllNoSelf = d2;
    IStridablesListEdit<ISkClassInfo> d0 = new StridablesList<>( this );
    d0.addAll( d1 );
    descendantChildsWithSelf = d0;
    d0 = new StridablesList<>( this );
    d0.addAll( d2 );
    descendantsAllWithSelf = d0;
  }

  /**
   * Determines if <code>aClassInfo</code> id descendant of the class ID <code>aParentId</code>.
   * <p>
   * This method does not consider the class to inherit from itself, so if argument's refer to the same class method
   * returns <code>false</code>.
   *
   * @param aClassInfo {@link ISkClassInfo} - the probable descendant
   * @param aParentId String - the probable ancestor ID
   * @param aAll {@link IStridablesList}&lt;{@link ISkClassInfo}&gt; - list of valid tree of classes
   * @return boolean - <code>true</code> if aClassInfo class has aParentId in his ancestors
   */
  private static boolean internalIsDescendant( ISkClassInfo aClassInfo, String aParentId,
      IStridablesList<ISkClassInfo> aAll ) {
    ISkClassInfo cinf = aClassInfo;
    while( !cinf.parentId().isEmpty() ) {
      cinf = aAll.getByKey( cinf.parentId() );
      if( aParentId.equals( cinf.id() ) ) {
        return true;
      }
    }
    return false;
  }

  // ------------------------------------------------------------------------------------
  // ISkClassInfo
  //

  @Override
  public ISkClassInfo parent() {
    return parentInfo;
  }

  @Override
  public ISkClassProps<IDtoAttrInfo> attrs() {
    return attrs;
  }

  @Override
  public ISkClassProps<IDtoRivetInfo> rivets() {
    return rivets;
  }

  @Override
  public ISkClassProps<IDtoClobInfo> clobs() {
    return clobs;
  }

  @Override
  public ISkClassProps<IDtoLinkInfo> links() {
    return links;
  }

  @Override
  public ISkClassProps<IDtoRtdataInfo> rtdata() {
    return rtdata;
  }

  @Override
  public ISkClassProps<IDtoEventInfo> events() {
    return events;
  }

  @Override
  public ISkClassProps<IDtoCmdInfo> cmds() {
    return cmds;
  }

  @Override
  public IStridablesList<ISkClassInfo> listSubclasses( boolean aOnlyChilds, boolean aIncludeSelf ) {
    if( aOnlyChilds ) {
      if( aIncludeSelf ) {
        return descendantChildsWithSelf;
      }
      return descendantChildsNoSelf;
    }
    if( aIncludeSelf ) {
      return descendantsAllWithSelf;
    }
    return descendantsAllNoSelf;
  }

  @Override
  public IStridablesList<ISkClassInfo> listSuperclasses( boolean aIncludeSelf ) {
    if( aIncludeSelf ) {
      return ancestorsWithSelf;
    }
    return ancestorsNoSelf;
  }

  @Override
  public boolean isSuperclassOf( String aSubclassId ) {
    return descendantsAllNoSelf.ids().hasElem( aSubclassId );
  }

  @Override
  public boolean isAssignableFrom( String aSubclassId ) {
    return descendantsAllWithSelf.ids().hasElem( aSubclassId );
  }

  @Override
  public boolean isSubclassOf( String aSuperclassId ) {
    return ancestorsNoSelf.ids().hasElem( aSuperclassId );
  }

  @Override
  public boolean isAssignableTo( String aSuperclassId ) {
    return ancestorsWithSelf.ids().hasElem( aSuperclassId );
  }

  @Override
  public boolean isOfClass( IStringList aClassIdsList ) {
    TsNullArgumentRtException.checkNull( aClassIdsList );
    return TsCollectionsUtils.intersects( aClassIdsList, ancestorsWithSelf.ids() );
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public <T extends IDtoClassPropInfoBase> ISkClassProps<T> props( ESkClassPropKind aKind ) {
    return (ISkClassProps)propsMap.getByKey( aKind );
  }

}
