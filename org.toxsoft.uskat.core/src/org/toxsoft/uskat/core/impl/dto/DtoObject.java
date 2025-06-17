package org.toxsoft.uskat.core.impl.dto;

import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;

import java.io.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.impl.*;

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
          aSw.writeSeparatorChar();
          StrioUtils.writeStringMap( aSw, EMPTY_STRING, aEntity.rivetRevs(), MappedSkids.KEEPER, true );
        }

        @Override
        protected IDtoObject doRead( IStrioReader aSr ) {
          Skid skid = Skid.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          OptionSet attrs = (OptionSet)OptionSetKeeper.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          MappedSkids ms = (MappedSkids)MappedSkids.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          IStringMapEdit<IMappedSkids> rr = StrioUtils.readStringMap( aSr, EMPTY_STRING, MappedSkids.KEEPER );
          return new DtoObject( 0, skid, attrs, ms, rr );
        }
      };

  private final Skid                   skid;
  private final IOptionSetEdit         attrs;
  private final MappedSkids            rivets;
  private IStringMapEdit<IMappedSkids> rivetRevs;

  /**
   * Constructor.
   *
   * @param aSkid {@link Skid} - object skid
   * @param aAttrs {@link IOptionSet} - attributes values
   * @param aRivets {@link IStringMap}&lt;{@link ISkidList}&gt; - rivets
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public DtoObject( Skid aSkid, IOptionSet aAttrs, IStringMap<ISkidList> aRivets ) {
    this( aSkid, aAttrs, aRivets, IStringMap.EMPTY );
  }

  /**
   * Constructor with empty fields.
   *
   * @param aSkid {@link Skid} - object skid
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public DtoObject( Skid aSkid ) {
    this( aSkid, IOptionSet.NULL, IStringMap.EMPTY, IStringMap.EMPTY );
  }

  /**
   * Constructor.
   * <p>
   * Package-private constructor for {@link CoreL10n}.
   *
   * @param aSkid {@link Skid} - object skid
   * @param aAttrs {@link IOptionSet} - attributes values
   * @param aRivets {@link IStringMap}&lt;{@link ISkidList}&gt; - rivets
   * @param aRivetRevs {@link IStringMap}&lt;{@link MappedSkids};&gt; SKIDs map of reverse rivets where: <br>
   *          - {@link IStringMap} key is "rivet class ID";<br>
   *          - {@link IMappedSkids} key is "rivet ID";<br>
   *          - {@link IMappedSkids} values are "SKIDs list of the left objects which have this object riveted".
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  DtoObject( Skid aSkid, IOptionSet aAttrs, IStringMap<ISkidList> aRivets, IStringMap<IMappedSkids> aRivetRevs ) {
    TsNullArgumentRtException.checkNulls( aSkid, aAttrs, aRivets, aRivetRevs );
    skid = aSkid;
    attrs = new OptionSet();
    attrs.addAll( aAttrs );
    rivets = new MappedSkids();
    rivets.setAll( aRivets );
    rivetRevs = new StringMap<>();
    rivetRevs.setAll( aRivetRevs );
  }

  /**
   * Package-private constructor for keeper & {@link DtoFullObject}.
   *
   * @param aFoo int - unsed argument for unique constructor signature
   * @param aSkid {@link Skid} - object skid
   * @param aAttrs {@link OptionSet} - attributes values
   * @param aRivets {@link MappedSkids} - reivets values
   */
  DtoObject( int aFoo, Skid aSkid, IOptionSetEdit aAttrs, MappedSkids aRivets,
      IStringMapEdit<IMappedSkids> aRivetRevs ) {
    skid = aSkid;
    attrs = aAttrs;
    rivets = aRivets;
    rivetRevs = aRivetRevs;
  }

  /**
   * Creates {@link DtoObject} instance based on {@link ISkObject}.
   * <p>
   * Created instance does not have system attribute values in the {@link IDtoObject#attrs()} set.
   *
   * @param aSkObj {@link ISkObject} - the source
   * @param aCoreApi {@link ISkCoreApi} - core API
   * @return {@link DtoObject} - cretaed instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException there is no {@link ISkClassInfo} in this core API for specified object
   */
  public static DtoObject createFromSk( ISkObject aSkObj, ISkCoreApi aCoreApi ) {
    TsNullArgumentRtException.checkNulls( aSkObj, aCoreApi );
    DtoObject dtoObj = new DtoObject( aSkObj.skid() );
    ISkClassInfo cinf = aCoreApi.sysdescr().getClassInfo( aSkObj.classId() );
    // copy all but system attributes values
    for( IDtoAttrInfo ainf : cinf.attrs().list() ) {
      if( !ISkHardConstants.isSkSysAttr( ainf ) ) {
        dtoObj.attrs().setValue( ainf.id(), aSkObj.attrs().getValue( ainf.id() ) );
      }
    }
    return dtoObj;
  }

  /**
   * Creates editable {@link DtoObject} with all attrs and rivets inited by defaults.
   * <p>
   * If object with specified SKID exists then result will be filled by exiting properties, otherwise default attribute
   * values and {@link Skid#NONE} rivets will be applied.
   * <p>
   * Created instance does not have system attribute values in the {@link IDtoObject#attrs()} set.
   *
   * @param aSkid {@link Skid} - objecvt SKID
   * @param aCoreApi {@link ISkSysdescr} - class info provider
   * @return {@link DtoObject} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such class as {@link Skid#classId()}
   */
  public static DtoObject createDtoObject( Skid aSkid, ISkCoreApi aCoreApi ) {
    TsNullArgumentRtException.checkNulls( aSkid, aCoreApi );
    // for existing object return it's DTO
    ISkObject skObj = aCoreApi.objService().find( aSkid );
    if( skObj != null ) {
      return DtoObject.createFromSk( skObj, aCoreApi );
    }
    ISkClassInfo cinf = aCoreApi.sysdescr().getClassInfo( aSkid.classId() );
    // create new DTO with properties default values
    DtoObject dto = new DtoObject( aSkid );
    for( IDtoAttrInfo ainf : cinf.attrs().list() ) {
      if( !ISkHardConstants.isSkSysAttr( ainf ) ) {
        dto.attrs().setValue( ainf.id(), ainf.dataType().defaultValue() );
      }
    }
    for( IDtoRivetInfo rinf : cinf.rivets().list() ) {
      dto.rivets().ensureSkidList( rinf.id(), rinf.count() );
    }
    // Метод createDtoObject позволяет создавать только НОВЫЕ объекты клиента, список обратных склепок всегда пустой (он
    // формируется бекендом)
    dto.rivetRevs().clear();

    return dto;
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

  @Override
  public IStringMapEdit<IMappedSkids> rivetRevs() {
    return rivetRevs;
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
    aSw.writeSeparatorChar();
    StrioUtils.writeStringMap( aSw, EMPTY_STRING, aEntity.rivetRevs(), MappedSkids.KEEPER, true );
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
    // 2025-06-15 mvk trick is temporarily, while rivet revs is being developed
    IStringMapEdit<IMappedSkids> rr = IStringMap.EMPTY;
    char c = aSr.peekChar();
    if( c == CHAR_ITEM_SEPARATOR ) {
      aSr.ensureSeparatorChar();
      rr = StrioUtils.readStringMap( aSr, EMPTY_STRING, MappedSkids.KEEPER );
    }
    aSr.ensureChar( CHAR_SET_END );
    return new DtoObject( 0, skid, attrs, rivets, rr );
  }

  /**
   * Set object reverse rivets.
   * <p>
   * The method is used by {@link AbstractSkObjectManager} & {@link SkCoreServObject}.
   *
   * @param aDtoObj {@link DtoObject} updating object
   * @param aRivetRevs {@link IStringMap}&lt;{@link MappedSkids};&gt; SKIDs map of reverse rivets where: <br>
   *          - {@link IStringMap} key is "rivet class ID";<br>
   *          - {@link IMappedSkids} key is "rivet ID";<br>
   *          - {@link IMappedSkids} values are "SKIDs list of the left objects which have this object riveted".
   * @throws TsNullArgumentRtException any arg = null
   */
  public static void setRivetRevs( DtoObject aDtoObj, IStringMap<IMappedSkids> aRivetRevs ) {
    TsNullArgumentRtException.checkNulls( aDtoObj, aRivetRevs );
    aDtoObj.rivetRevs = new StringMap<>( aRivetRevs );
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
