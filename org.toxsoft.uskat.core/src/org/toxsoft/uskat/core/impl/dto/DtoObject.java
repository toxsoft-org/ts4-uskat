package org.toxsoft.uskat.core.impl.dto;

import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;

import java.io.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * {@link IDtoObject} implementation.
 *
 * @author hazard157
 */
public final class DtoObject
    implements IDtoObject, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * The keeper singleton.
   */
  public static final IEntityKeeper<IDtoObject> KEEPER =
      new AbstractEntityKeeper<>( IDtoObject.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, IDtoObject aEntity ) {
          Skid.KEEPER.write( aSw, aEntity.skid() );
          aSw.writeSeparatorChar();
          OptionSetKeeper.KEEPER.write( aSw, aEntity.attrs() );
          aSw.writeSeparatorChar();
          MappedSkids.KEEPER.write( aSw, aEntity.rivets() );
        }

        @Override
        protected IDtoObject doRead( IStrioReader aSr ) {
          Skid skid = Skid.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          OptionSet attrs = (OptionSet)OptionSetKeeper.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          MappedSkids ms = (MappedSkids)MappedSkids.KEEPER.read( aSr );
          return new DtoObject( 0, skid, attrs, ms );
        }
      };

  private final Skid           skid;
  private final IOptionSetEdit attrs;
  private final MappedSkids    rivets;

  /**
   * Constructor.
   *
   * @param aSkid {@link Skid} - object skid
   * @param aAttrs {@link IOptionSet} - attributes values
   * @param aRivets {@link IStringMap}&lt;{@link ISkidList}&gt; - rivets
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public DtoObject( Skid aSkid, IOptionSet aAttrs, IStringMap<ISkidList> aRivets ) {
    skid = TsNullArgumentRtException.checkNull( aSkid );
    attrs = new OptionSet();
    attrs.addAll( aAttrs );
    rivets = new MappedSkids();
    rivets.setAll( aRivets );
  }

  /**
   * Package-private constructor for keeper.
   *
   * @param aFoo int - unsed argument for unique constructor signature
   * @param aSkid {@link Skid} - object skid
   * @param aAttrs {@link OptionSet} - attributes values
   * @param aRivets {@link MappedSkids} - reivets values
   */
  DtoObject( int aFoo, Skid aSkid, IOptionSetEdit aAttrs, MappedSkids aRivets ) {
    skid = aSkid;
    attrs = aAttrs;
    rivets = aRivets;
  }

  /**
   * Creates {@link DtoObject} instance based on {@link ISkObject} of cvlass {@link ISkClassInfo}.
   * <p>
   * Created instance does not have system attribute values in the {@link IDtoObject#attrs()} set.
   *
   * @param aSkObj {@link ISkObject} - the source
   * @param aSkClass {@link ISkClassInfo} - description of the object's class
   * @return {@link DtoObject} - cretaed instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException class description is not of obnject class
   */
  public static DtoObject createFromSk( ISkObject aSkObj, ISkClassInfo aSkClass ) {
    TsNullArgumentRtException.checkNulls( aSkObj, aSkClass );
    TsIllegalArgumentRtException.checkFalse( aSkClass.id().equals( aSkObj.classId() ) );
    DtoObject dtoObj = new DtoObject( aSkObj.skid(), IOptionSet.NULL, aSkObj.rivets().map() );
    // copy all but system attributes values
    for( IDtoAttrInfo ainf : aSkClass.attrs().list() ) {
      if( !ISkHardConstants.isSkSysAttr( ainf ) ) {
        dtoObj.attrs().setValue( ainf.id(), aSkObj.attrs().getValue( ainf.id() ) );
      }
    }
    return dtoObj;
  }

  // ------------------------------------------------------------------------------------
  // IDtoObject
  //

  @Override
  public Skid skid() {
    return skid;
  }

  @Override
  public IOptionSetEdit attrs() {
    return attrs;
  }

  @Override
  public MappedSkids rivets() {
    return rivets;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Writes an {@link DtoObject} without class ID for storage size minimization.
   *
   * @param aSw {@link IStrioWriter} - character output stream
   * @param aEntity {@link DtoObject} - an instance to write
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static void writeShortened( IStrioWriter aSw, IDtoObject aEntity ) {
    TsNullArgumentRtException.checkNulls( aSw, aEntity );
    aSw.writeChar( CHAR_SET_BEGIN );
    aSw.writeAsIs( aEntity.strid() );
    aSw.writeSeparatorChar();
    OptionSetKeeper.KEEPER.write( aSw, aEntity.attrs() );
    aSw.writeSeparatorChar();
    MappedSkids.KEEPER.write( aSw, aEntity.rivets() );
    aSw.writeChar( CHAR_SET_END );
  }

  /**
   * Reads shortened {@link DtoObject} previously written by {@link #writeShortened(IStrioWriter, IDtoObject)}.
   *
   * @param aSr {@link IStrioReader} - char input stream
   * @param aClassId String - the class ID of the object to read
   * @return {@link DtoObject} - the read instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static DtoObject readShortened( IStrioReader aSr, String aClassId ) {
    TsNullArgumentRtException.checkNulls( aSr, aClassId );
    aSr.ensureChar( CHAR_SET_BEGIN );
    String strid = aSr.readIdPath();
    Skid skid = new Skid( aClassId, strid );
    aSr.ensureSeparatorChar();
    OptionSet attrs = (OptionSet)OptionSetKeeper.KEEPER.read( aSr );
    aSr.ensureSeparatorChar();
    MappedSkids rivets = (MappedSkids)MappedSkids.KEEPER.read( aSr );
    aSr.ensureChar( CHAR_SET_END );
    return new DtoObject( 0, skid, attrs, rivets );
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @Override
  public String toString() {
    return skid.toString();
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof DtoObject that ) {
      return skid.equals( that.skid ) && attrs.equals( that.attrs ) && rivets.equals( that.rivets );
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = INITIAL_HASH_CODE;
    result = PRIME * result + skid.hashCode();
    result = PRIME * result + attrs.hashCode();
    result = PRIME * result + rivets.hashCode();
    return result;
  }

}
