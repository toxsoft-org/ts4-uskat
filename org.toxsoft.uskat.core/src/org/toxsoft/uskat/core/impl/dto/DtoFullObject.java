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
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

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
    dtoObject = new DtoObject( aSkid );
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

  /**
   * Creates editable {@link DtoFullObject} with all properties filles.
   * <p>
   * If object with specified SKID exists then result will be filled by exiting properties, otherwise default or an
   * empty values will be applied.
   * <p>
   * Created instance does not have system attribute values in the {@link IDtoObject#attrs()} set.
   *
   * @param aSkid {@link Skid} - objecvt SKID
   * @param aCoreApi {@link ISkSysdescr} - class info provider
   * @return {@link DtoFullObject} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such class as {@link Skid#classId()}
   */
  // TODO maybe move this method to CoreAPI ?
  public static DtoFullObject createDtoFullObject( Skid aSkid, ISkCoreApi aCoreApi ) {
    TsNullArgumentRtException.checkNulls( aSkid, aCoreApi );
    ISkClassInfo classInfo = aCoreApi.sysdescr().getClassInfo( aSkid.classId() );
    DtoFullObject dto = new DtoFullObject( aSkid );
    // for existing object return it's DTO
    ISkObject skObj = aCoreApi.objService().find( aSkid );
    if( skObj != null ) {
      // attributes - copy all but system attributes values
      for( IDtoAttrInfo ainf : classInfo.attrs().list() ) {
        if( !ISkHardConstants.isSkSysAttr( ainf ) ) {
          dto.attrs().setValue( ainf.id(), skObj.attrs().getValue( ainf.id() ) );
        }
      }
      // rivets
      dto.rivets().setAll( skObj.rivets().map() );
      // clobs
      for( IDtoClobInfo cinf : classInfo.clobs().list() ) {
        Gwid gwid = Gwid.createClob( aSkid.classId(), aSkid.strid(), cinf.id() );
        String content = aCoreApi.clobService().readClob( gwid );
        dto.clobs().put( cinf.id(), content );
      }
      // links
      for( IDtoLinkInfo linf : classInfo.links().list() ) {
        Gwid gwid = Gwid.createLink( aSkid.classId(), aSkid.strid(), linf.id() );
        IDtoLinkFwd lf = aCoreApi.linkService().getLinkFwd( gwid );
        dto.links().map().put( linf.id(), lf.rightSkids() );
      }
      return dto;
    }
    // init new DTO with properties default values
    // attributes - default values for all but system attributes values
    for( IDtoAttrInfo ainf : classInfo.attrs().list() ) {
      if( !ISkHardConstants.isSkSysAttr( ainf ) ) {
        dto.attrs().setValue( ainf.id(), ainf.dataType().defaultValue() );
      }
    }
    // rivets - fill with Skid.NONE
    for( IDtoRivetInfo rinf : classInfo.rivets().list() ) {
      dto.rivets().ensureSkidList( rinf.id(), rinf.count() );
    }
    // clobs - empty strings
    for( IDtoClobInfo cinf : classInfo.clobs().list() ) {
      dto.clobs().put( cinf.id(), TsLibUtils.EMPTY_STRING );
    }
    // links - fill with Skid.NONE
    for( IDtoLinkInfo linf : classInfo.links().list() ) {
      ISkidList rights = ISkidList.EMPTY;
      if( linf.linkConstraint().isExactCount() ) {
        SkidList sl = new SkidList();
        for( int i = 0; i < linf.linkConstraint().maxCount(); i++ ) {
          sl.add( Skid.NONE );
        }
        rights = sl;
      }
      else {
        if( linf.linkConstraint().isEmptyProhibited() ) {
          rights = new SkidList( Skid.NONE );
        }
      }
      dto.links().map().put( linf.id(), rights );
    }
    return dto;
  }

  /**
   * Updates specified object and all specified properties.
   *
   * @param <T> - expected type of the object
   * @param aCoreApi {@link ISkCoreApi} - core API
   * @param aDtoObj {@link IDtoFullObject} - object with additional properties
   * @return &lt;T&gt; - created/updated object
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  // TODO maybe move this method to CoreAPI ?
  @SuppressWarnings( "unchecked" )
  public static <T extends ISkObject> T defineFullObject( ISkCoreApi aCoreApi, IDtoFullObject aDtoObj ) {
    TsNullArgumentRtException.checkNulls( aCoreApi, aDtoObj );
    // object with attrs and rivets
    ISkObject skObj = aCoreApi.objService().defineObject( aDtoObj );
    // CLOBs
    for( String clobId : aDtoObj.clobs().keys() ) {
      String content = aDtoObj.clobs().getByKey( clobId );
      Gwid gwid = Gwid.createClob( aDtoObj.skid().classId(), aDtoObj.skid().strid(), clobId );
      aCoreApi.clobService().writeClob( gwid, content );
    }
    // links
    for( String linkId : aDtoObj.links().map().keys() ) {
      ISkidList rightSkids = aDtoObj.links().map().getByKey( linkId );
      aCoreApi.linkService().setLink( aDtoObj.skid(), linkId, rightSkids );
    }
    return (T)skObj;
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
