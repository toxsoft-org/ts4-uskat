package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * {@link ISkClassProps} implementation.
 *
 * @author hazard157
 * @param <T> - the property type
 */
final class SkClassProps<T extends IDtoClassPropInfoBase>
    implements ISkClassProps<T> {

  private final ESkClassPropKind kind;

  private final IStridablesListEdit<T> itemsAll  = new StridablesList<>();
  private final IStridablesListEdit<T> itemsSelf = new StridablesList<>();

  /**
   * Owner class, initialized in {@link #papiInit(SkClassInfo)}.
   */
  private SkClassInfo owner = null;

  /**
   * Constructor.
   *
   * @param aKind {@link ESkClassPropKind} - the kind of property
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SkClassProps( ESkClassPropKind aKind ) {
    TsNullArgumentRtException.checkNull( aKind );
    kind = aKind;
  }

  // ------------------------------------------------------------------------------------
  // package API
  //

  /**
   * Initilize from owner class.
   * <p>
   * Warning: this methods must be the first one called after constructor.
   *
   * @param aOwner {@link SkClassInfo} - the owner class
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void papiInit( SkClassInfo aOwner ) {
    TsNullArgumentRtException.checkNull( aOwner );
    owner = aOwner;
    papiSetSelf( IStridablesList.EMPTY );
    papiClearCache();
  }

  void papiClearCache() {
    // TODO SkClassProps.papiClearCache()
  }

  // ------------------------------------------------------------------------------------
  // ISkClassProps
  //

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
      return owner;
    }
    ISkClassInfo parent = owner.hierarchy().parent();
    if( parent != null ) {
      return parent.props( kind ).findSuperDeclarer( aPropId );
    }
    return null;
  }

  @Override
  public IStridablesList<ISkClassInfo> findSubDeclarers( String aPropId ) {
    IStridablesListEdit<ISkClassInfo> ll = new StridablesList<>();

    // TODO реализовать SkClassProps.findSubDeclarers()
    throw new TsUnderDevelopmentRtException( "SkClassProps.findSubDeclarers()" );
  }

  @Override
  public IStridablesList<T> makeCopy( boolean aOnlySelf ) {
    IStridablesListEdit<T> src = aOnlySelf ? itemsSelf : itemsAll;
    IStridablesListEdit<T> ll = new StridablesList<>();
    for( T t : src ) {
      ll.add( t.makeCopy() );
    }
    return ll;
  }

  // ------------------------------------------------------------------------------------
  // package API
  //

  void papiSetSelf( IStridablesList<T> aSelfItems ) {
    TsNullArgumentRtException.checkNull( aSelfItems );
    ISkClassInfo parentInfo = owner.hierarchy().parent();
    // any of the self property must NOT have the same ID as any of the parent property
    if( parentInfo != null ) {
      ISkClassProps<T> parentProps = parentInfo.props( kind );
      TsInternalErrorRtException
          .checkTrue( TsCollectionsUtils.intersects( parentProps.list().ids(), aSelfItems.ids() ) );
      itemsAll.setAll( parentProps.list() );
    }
    itemsSelf.setAll( aSelfItems );
    itemsAll.addAll( itemsSelf );
  }

  void papiFindSubSeclarers( IStridablesListEdit<T> aList, String aPropId ) {

  }

}
