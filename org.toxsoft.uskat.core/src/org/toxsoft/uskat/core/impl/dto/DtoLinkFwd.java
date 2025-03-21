package org.toxsoft.uskat.core.impl.dto;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;

import java.io.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.linkserv.*;

/**
 * {@link IDtoLinkFwd} partially editable implementation.
 * <p>
 * Note that {@link #rightSkids()} returns an editable {@link SkidList} while {@link #gwid()} can not be edited.
 *
 * @author hazard157
 */
public final class DtoLinkFwd
    implements IDtoLinkFwd, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Синглтон хранителя.
   */
  public static final IEntityKeeper<IDtoLinkFwd> KEEPER =
      new AbstractEntityKeeper<>( IDtoLinkFwd.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, IDtoLinkFwd aEntity ) {
          Gwid.KEEPER.write( aSw, aEntity.gwid() );
          aSw.writeSeparatorChar();
          Skid.KEEPER.write( aSw, aEntity.leftSkid() );
          aSw.writeSeparatorChar();
          SkidListKeeper.KEEPER.write( aSw, aEntity.rightSkids() );
        }

        @Override
        protected IDtoLinkFwd doRead( IStrioReader aSr ) {
          Gwid gwid = Gwid.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          Skid leftSkid = Skid.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          SkidList rightObjIds = (SkidList)SkidListKeeper.KEEPER.read( aSr );
          return new DtoLinkFwd( 0, gwid, leftSkid, rightObjIds );
        }
      };

  private final Gwid     gwid;
  private final Skid     leftObj;
  private final SkidList rightSkids;

  /**
   * Constructor.
   * <p>
   * Note: link GWID must contain ID of the link declaring class while left SKID contains class ID of the object.
   *
   * @param aGwid {@link Gwid} - abstract GWID of the link
   * @param aLeftSkid {@link Skid} - SKID of the left object
   * @param aRightObjIds {@link ISkidList} - right objects SKIDs initial values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException {@link Gwid} is not of kind {@link EGwidKind#GW_LINK}
   * @throws TsIllegalArgumentRtException link {@link Gwid} is concrete
   * @throws TsIllegalArgumentRtException link {@link Gwid} is multi GWID
   */
  public DtoLinkFwd( Gwid aGwid, Skid aLeftSkid, ISkidList aRightObjIds ) {
    TsNullArgumentRtException.checkNulls( aGwid, aRightObjIds );
    TsIllegalArgumentRtException.checkFalse( aGwid.isAbstract() );
    TsIllegalArgumentRtException.checkTrue( aGwid.isMulti() );
    TsIllegalArgumentRtException.checkTrue( aGwid.kind() != EGwidKind.GW_LINK );
    gwid = aGwid;
    leftObj = aLeftSkid;
    rightSkids = new SkidList( aRightObjIds );
  }

  /**
   * Static copy constructor.
   *
   * @param aSource {@link IDtoLinkFwd} - источник
   * @return {@link DtoLinkFwd} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static DtoLinkFwd createCopy( IDtoLinkFwd aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    return new DtoLinkFwd( aSource.gwid(), aSource.leftSkid(), aSource.rightSkids() );
  }

  private DtoLinkFwd( @SuppressWarnings( "unused" ) int aFoo, Gwid aGwid, Skid aLeftSkid, SkidList aRightObjIds ) {
    gwid = aGwid;
    leftObj = aLeftSkid;
    rightSkids = aRightObjIds;
  }

  /**
   * Constructor that uses arguments for internal storage, without any checks and defensive copy creation.
   * <p>
   * Warning: must be used only when loading from storage for optimization purposes.
   *
   * @param aGwid {@link Gwid} - abstract GWID of the link
   * @param aLeftSkid {@link Skid} - SKID of the left object
   * @param aRightObjIds {@link ISkidList} - right objects SKIDs initial values
   * @return {@link DtoLinkFwd} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static DtoLinkFwd createDirect( Gwid aGwid, Skid aLeftSkid, SkidList aRightObjIds ) {
    TsNullArgumentRtException.checkNulls( aGwid, aLeftSkid, aRightObjIds );
    return new DtoLinkFwd( 0, aGwid, aLeftSkid, aRightObjIds );
  }

  // ------------------------------------------------------------------------------------
  // Static API
  //

  /**
   * Returns new instance with given SKID removed from right objects.
   * <p>
   * Method is aimed to simplify single object removal from the link.
   * <p>
   * If given SKID is not in list then returns copy of source argument.
   *
   * @param aLink {@link IDtoLinkFwd} - source link
   * @param aRightSkidToRemove {@link Skid} - SKID obj object to remove from {@link IDtoLinkFwd#rightSkids()}
   * @return {@link DtoLinkFwd} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static DtoLinkFwd createCopyMinusSkid( IDtoLinkFwd aLink, Skid aRightSkidToRemove ) {
    TsNullArgumentRtException.checkNulls( aRightSkidToRemove );
    DtoLinkFwd lf = DtoLinkFwd.createCopy( aLink );
    lf.rightSkids.remove( aRightSkidToRemove );
    return lf;
  }

  /**
   * Returns new instance with given SKID added to right objects.
   * <p>
   * Method is aimed to simplify single object addition to the link.
   * <p>
   * If given SKID already is in list then returns copy of source argument.
   *
   * @param aLink {@link IDtoLinkFwd} - source link
   * @param aRightSkidToRemove {@link Skid} - SKID obj object to add to {@link IDtoLinkFwd#rightSkids()}
   * @return {@link DtoLinkFwd} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static DtoLinkFwd createCopyPlusSkid( IDtoLinkFwd aLink, Skid aRightSkidToRemove ) {
    TsNullArgumentRtException.checkNulls( aRightSkidToRemove );
    DtoLinkFwd lf = DtoLinkFwd.createCopy( aLink );
    if( !lf.rightSkids().hasElem( aRightSkidToRemove ) ) {
      lf.rightSkids.add( aRightSkidToRemove );
    }
    return lf;
  }

  // ------------------------------------------------------------------------------------
  // IDtoLink
  //

  @Override
  public Gwid gwid() {
    return gwid;
  }

  @Override
  public String classId() {
    return gwid.classId();
  }

  @Override
  public String linkId() {
    return gwid.propId();
  }

  @Override
  public Skid leftSkid() {
    return leftObj;
  }

  @Override
  public SkidList rightSkids() {
    return rightSkids;
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @Override
  public String toString() {
    return gwid.toString() + '-' + leftObj.toString();
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof DtoLinkFwd that ) {
      return gwid.equals( that.gwid ) && leftObj.equals( that.leftObj ) && rightSkids.equals( that.rightSkids );
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = INITIAL_HASH_CODE;
    result = PRIME * result + gwid.hashCode();
    result = PRIME * result + leftObj.hashCode();
    result = PRIME * result + rightSkids.hashCode();
    return result;
  }
}
