package org.toxsoft.uskat.core.utils.ugwi;

import java.io.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.basis.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * {@link IUgwiList} editable implementation.
 *
 * @author hazard157
 */
public final class UgwiList
    implements IUgwiList, IKeepableEntity, Serializable {

  /**
   * Registered keeper ID.
   */
  public static final String KEEPER_ID = "UgwiList"; //$NON-NLS-1$

  /**
   * The keeper singleton (indenting keeper).
   */
  public static final IEntityKeeper<IUgwiList> KEEPER_INDENTED =
      new AbstractEntityKeeper<>( IUgwiList.class, EEncloseMode.ENCLOSES_KEEPER_IMPLEMENTATION, IUgwiList.EMPTY ) {

        @Override
        protected void doWrite( IStrioWriter aSw, IUgwiList aEntity ) {
          Ugwi.KEEPER.writeColl( aSw, aEntity.items(), true );
        }

        @Override
        protected IUgwiList doRead( IStrioReader aSr ) {
          IListEdit<Ugwi> ll = Ugwi.KEEPER.readColl( aSr );
          return UgwiList.createDirect( ll );
        }

      };

  /**
   * The keeper singleton (non-indenting keeper).
   */
  public static final IEntityKeeper<IUgwiList> KEEPER =
      new AbstractEntityKeeper<>( IUgwiList.class, EEncloseMode.ENCLOSES_KEEPER_IMPLEMENTATION, IUgwiList.EMPTY ) {

        @Override
        protected void doWrite( IStrioWriter aSw, IUgwiList aEntity ) {
          Ugwi.KEEPER.writeColl( aSw, aEntity.items(), false );
        }

        @Override
        protected IUgwiList doRead( IStrioReader aSr ) {
          IListEdit<Ugwi> ll = Ugwi.KEEPER.readColl( aSr );
          return UgwiList.createDirect( ll );
        }

      };

  private static final long serialVersionUID = 157157L;

  private final IListEdit<Ugwi> itemsList;

  /**
   * Constructor.
   */
  public UgwiList() {
    itemsList = new ElemLinkedBundleList<>();
  }

  /**
   * Constructor.
   *
   * @param aSource {@link IList}&lt;{@link Ugwi}&gt; - initlial content of list
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public UgwiList( ITsCollection<Ugwi> aSource ) {
    this();
    itemsList.setAll( aSource );
  }

  private UgwiList( IListEdit<Ugwi> aList, @SuppressWarnings( "unused" ) boolean FooParam ) {
    itemsList = aList;
  }

  /**
   * Creates the instance directly storing reference to the list.
   *
   * @param aList {@link IListEdit} - the implementing list
   * @return {@link UgwiList} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException argument does not implements {@link ITsFastIndexListTag}
   */
  public static UgwiList createDirect( IListEdit<Ugwi> aList ) {
    TsNullArgumentRtException.checkNull( aList );
    TsIllegalArgumentRtException.checkFalse( aList instanceof ITsFastIndexListTag );
    return new UgwiList( aList, false );
  }

  // ------------------------------------------------------------------------------------
  // IUgwiList
  //

  @Override
  public IList<Ugwi> items() {
    return itemsList;
  }

  @Override
  public IStringList listKindIds() {
    if( itemsList.isEmpty() ) {
      return IStringList.EMPTY;
    }
    IStringListEdit ll = new StringArrayList();
    for( Ugwi u : itemsList ) {
      if( !ll.hasElem( u.kindId() ) ) {
        ll.add( u.kindId() );
      }
    }
    return ll;
  }

  @Override
  public IList<Ugwi> listByKindId( String aKindId ) {
    TsNullArgumentRtException.checkNull( aKindId );
    if( itemsList.isEmpty() || !StridUtils.isValidIdPath( aKindId ) ) {
      return IList.EMPTY;
    }
    IListEdit<Ugwi> ll = new ElemArrayList<>();
    for( Ugwi u : itemsList ) {
      if( u.kindId().equals( aKindId ) ) {
        ll.add( u );
      }
    }
    return ll;
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @Override
  public String toString() {
    return TsCollectionsUtils.countableCollectionToString( itemsList );
  }

  @Override
  public int hashCode() {
    return itemsList.hashCode();
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof IUgwiList that ) {
      return this.itemsList.equals( that.items() );
    }
    return false;
  }

  // ------------------------------------------------------------------------------------
  // IKeepableEntity
  //

  @Override
  public void write( IStrioWriter aDw ) {
    Ugwi.KEEPER.writeColl( aDw, itemsList, false );
  }

  @Override
  public void read( IStrioReader aDr ) {
    Ugwi.KEEPER.readColl( aDr, itemsList );
  }

}
