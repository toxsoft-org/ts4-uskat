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
          SkidListKeeper.KEEPER.write( aSw, aEntity.rightSkids() );
        }

        @Override
        protected IDtoLinkFwd doRead( IStrioReader aSr ) {
          Gwid gwid = Gwid.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          ISkidList rightObjIds = SkidListKeeper.KEEPER.read( aSr );
          return new DtoLinkFwd( gwid, rightObjIds );
        }
      };

  private final Gwid     gwid;
  private final SkidList rightObjIds;

  /**
   * Constructor.
   *
   * @param aGwid {@link Gwid} - concrete link GWID
   * @param aRightObjIds {@link ISkidList} - right objects SKIDs initial values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException {@link Gwid} is not of kind {@link EGwidKind#GW_LINK}
   * @throws TsIllegalArgumentRtException link {@link Gwid} is abstract
   * @throws TsIllegalArgumentRtException link {@link Gwid} is multi GWID
   */
  public DtoLinkFwd( Gwid aGwid, ISkidList aRightObjIds ) {
    TsNullArgumentRtException.checkNulls( aGwid, aRightObjIds );
    TsIllegalArgumentRtException.checkTrue( aGwid.isAbstract() );
    TsIllegalArgumentRtException.checkTrue( aGwid.isMulti() );
    TsIllegalArgumentRtException.checkTrue( aGwid.kind() != EGwidKind.GW_LINK );
    gwid = aGwid;
    rightObjIds = new SkidList( aRightObjIds );
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
    return new DtoLinkFwd( aSource.gwid(), aSource.rightSkids() );
  }

  private DtoLinkFwd( Gwid aGwid, SkidList aRightObjIds ) {
    gwid = aGwid;
    rightObjIds = aRightObjIds;
  }

  /**
   * Static constructor.
   * <p>
   * WARNING: constructor is unsafe but fast. Arguments are stored without any check and without defensive copy
   * creation.
   *
   * @param aGwid {@link Gwid} - concrete link GWID
   * @param aRightObjIds {@link ISkidList} - right objects SKIDs initial values
   * @return {@link DtoLinkFwd} - created instance
   */
  public static DtoLinkFwd createFastUnsafe( Gwid aGwid, SkidList aRightObjIds ) {
    return new DtoLinkFwd( aGwid, aRightObjIds );
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
    return gwid.skid();
  }

  @Override
  public SkidList rightSkids() {
    return rightObjIds;
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @Override
  public String toString() {
    return gwid.toString();
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof DtoLinkFwd that ) {
      return gwid.equals( that.gwid ) && rightObjIds.equals( that.rightObjIds );
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = INITIAL_HASH_CODE;
    result = PRIME * result + gwid.hashCode();
    result = PRIME * result + rightObjIds.hashCode();
    return result;
  }
}
