package org.toxsoft.uskat.core.impl.dto;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;

import java.io.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * {@link IDtoFullObject} editable implementation.
 *
 * @author hazard157
 */
public final class DtoFullObject
    implements IDtoFullObject, Serializable {

  private static final long serialVersionUID = -4180840585239680350L;

  /**
   * The keeper singleton.
   */
  public static final IEntityKeeper<IDtoFullObject> KEEPER =
      new AbstractEntityKeeper<>( IDtoFullObject.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, IDtoFullObject aEntity ) {
          Skid.KEEPER.write( aSw, aEntity.skid() );
          aSw.writeSeparatorChar();
          OptionSetKeeper.KEEPER.write( aSw, aEntity.attrs() );
          aSw.writeSeparatorChar();
          MappedSkids.KEEPER.write( aSw, aEntity.rivets() );
          aSw.writeSeparatorChar();
          StrioUtils.writeStringMap( aSw, EMPTY_STRING, aEntity.clobs(), StringKeeper.KEEPER, true );
          aSw.writeSeparatorChar();
          MappedSkids.KEEPER.write( aSw, aEntity.links() );
        }

        @Override
        protected IDtoFullObject doRead( IStrioReader aSr ) {
          Skid skid = Skid.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          OptionSet attrs = (OptionSet)OptionSetKeeper.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          MappedSkids ms = (MappedSkids)MappedSkids.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          IStringMapEdit<String> clobs = StrioUtils.readStringMap( aSr, EMPTY_STRING, StringKeeper.KEEPER );
          aSr.ensureSeparatorChar();
          MappedSkids links = (MappedSkids)MappedSkids.KEEPER.read( aSr );
          return new DtoFullObject( skid, attrs, ms, clobs, links );
        }
      };

  private final DtoObject              dtoObject;
  private final IStringMapEdit<String> clobs;
  private final MappedSkids            links;

  /**
   * Constructor.
   *
   * @param aSkid {@link Skid} - the object SKID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public DtoFullObject( Skid aSkid ) {
    dtoObject = new DtoObject( aSkid, IOptionSet.NULL, IStringMap.EMPTY );
    clobs = new StringMap<>();
    links = new MappedSkids();
  }

  /**
   * Constructor.
   *
   * @param aObject {@link IDtoObject} - the source object data
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public DtoFullObject( IDtoObject aObject ) {
    this( aObject, IStringMap.EMPTY, IStringMap.EMPTY );
  }

  /**
   * Constructor.
   *
   * @param aObject {@link IDtoObject} - the source object data
   * @param aClobs {@link IStringMap}&lt;String&gt; - CLOBs values
   * @param aLinks {@link IStringMap}&lt;{@link ISkidList}&lt; - links values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public DtoFullObject( IDtoObject aObject, IStringMap<String> aClobs, IStringMap<ISkidList> aLinks ) {
    TsNullArgumentRtException.checkNulls( aObject, aClobs, aLinks );
    dtoObject = new DtoObject( aObject.skid(), aObject.attrs(), aObject.rivets().map() );
    clobs = new StringMap<>( aClobs );
    links = new MappedSkids();
    links.setAll( aLinks );
  }

  private DtoFullObject( Skid aSkid, IOptionSetEdit aAttrs, MappedSkids aRivets, IStringMapEdit<String> aClobs,
      MappedSkids aLinks ) {
    dtoObject = new DtoObject( 0, aSkid, aAttrs, aRivets );
    clobs = aClobs;
    links = aLinks;
  }

  // ------------------------------------------------------------------------------------
  // IDtoFullObject
  //

  @Override
  public Skid skid() {
    return dtoObject.skid();
  }

  @Override
  public IOptionSetEdit attrs() {
    return dtoObject.attrs();
  }

  @Override
  public MappedSkids rivets() {
    return dtoObject.rivets();
  }

  @Override
  public IStringMapEdit<String> clobs() {
    return clobs;
  }

  @Override
  public MappedSkids links() {
    return links;
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + ':' + dtoObject.toString();
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof DtoFullObject that ) {
      return dtoObject.equals( that.dtoObject ) && this.clobs.equals( that.clobs ) && this.links.equals( that.links );
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = INITIAL_HASH_CODE;
    result = PRIME * result + dtoObject.hashCode();
    result = PRIME * result + clobs.hashCode();
    result = PRIME * result + links.hashCode();
    return result;
  }

}
