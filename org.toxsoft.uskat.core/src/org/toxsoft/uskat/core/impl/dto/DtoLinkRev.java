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
 * Редактируемая реализация {@link IDtoLinkRev}.
 *
 * @author goga
 */
public final class DtoLinkRev
    implements IDtoLinkRev, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Keeper singlton.
   */
  public static final IEntityKeeper<IDtoLinkRev> KEEPER =
      new AbstractEntityKeeper<>( IDtoLinkRev.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, IDtoLinkRev aEntity ) {
          Gwid.KEEPER.write( aSw, aEntity.gwid() );
          aSw.writeSeparatorChar();
          Skid.KEEPER.write( aSw, aEntity.rightSkid() );
          aSw.writeSeparatorChar();
          SkidListKeeper.KEEPER.write( aSw, aEntity.leftSkids() );
        }

        @Override
        protected IDtoLinkRev doRead( IStrioReader aSr ) {
          Gwid gwid = Gwid.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          Skid rightObjId = Skid.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          ISkidList leftObjIds = SkidListKeeper.KEEPER.read( aSr );
          return new DtoLinkRev( gwid, rightObjId, leftObjIds );
        }
      };

  private final Gwid     gwid;
  private final Skid     rightSkid;
  private final SkidList leftObjIds;

  /**
   * Constructor.
   *
   * @param aGwid {@link Gwid} - abstract GWID of this link
   * @param aRightObjId {@link Skid} - SKID of the right object
   * @param aLeftObjIds {@link ISkidList} - the SKIDs list of the left objects
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException {@link Gwid} is not of kind {@link EGwidKind#GW_LINK}
   * @throws TsIllegalArgumentRtException link {@link Gwid} is not abstract
   * @throws TsIllegalArgumentRtException link {@link Gwid} is multi GWID
   */
  public DtoLinkRev( Gwid aGwid, Skid aRightObjId, ISkidList aLeftObjIds ) {
    TsNullArgumentRtException.checkNulls( aGwid, aRightObjId, aLeftObjIds );
    TsIllegalArgumentRtException.checkFalse( aGwid.isAbstract() );
    TsIllegalArgumentRtException.checkTrue( aGwid.isMulti() );
    TsIllegalArgumentRtException.checkTrue( aGwid.kind() != EGwidKind.GW_LINK );
    gwid = aGwid;
    rightSkid = aRightObjId;
    leftObjIds = new SkidList( aLeftObjIds );
  }

  /**
   * Static copy constructor.
   *
   * @param aSource {@link IDtoLinkRev} - источник
   * @return {@link DtoLinkRev} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static DtoLinkRev createCopy( IDtoLinkRev aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    return new DtoLinkRev( aSource.gwid(), aSource.rightSkid(), aSource.leftSkids() );
  }

  private DtoLinkRev( Gwid aGwid, Skid aRightObjId, SkidList aLeftObjIds ) {
    gwid = aGwid;
    rightSkid = aRightObjId;
    leftObjIds = aLeftObjIds;
  }

  /**
   * Static constructor.
   * <p>
   * WARNING: constructor is unsafe but fast. Arguments are stored without any check and without defensive copy
   * creation.
   *
   * @param aGwid {@link Gwid} - concrete link GWID
   * @param aRightObjId {@link Skid} - SKID of the right object
   * @param aLeftObjIds {@link ISkidList} - the SKIDs list of the left objects
   * @return {@link DtoLinkRev} - created instance
   */
  public static DtoLinkRev createFastUnsafe( Gwid aGwid, Skid aRightObjId, SkidList aLeftObjIds ) {
    return new DtoLinkRev( aGwid, aRightObjId, aLeftObjIds );
  }

  // ------------------------------------------------------------------------------------
  // IDtoReverseLink
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
  public Skid rightSkid() {
    return rightSkid;
  }

  @Override
  public SkidList leftSkids() {
    return leftObjIds;
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @Override
  public String toString() {
    return gwid.toString() + " <-- " + rightSkid.toString(); //$NON-NLS-1$
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof DtoLinkRev that ) {
      return gwid.equals( that.gwid ) //
          && rightSkid.equals( that.rightSkid ) //
          && leftObjIds.equals( that.leftObjIds );
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = INITIAL_HASH_CODE;
    result = PRIME * result + gwid.hashCode();
    result = PRIME * result + rightSkid.hashCode();
    result = PRIME * result + leftObjIds.hashCode();
    return result;
  }

}
