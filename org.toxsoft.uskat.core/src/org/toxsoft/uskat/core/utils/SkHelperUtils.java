package org.toxsoft.uskat.core.utils;

import java.security.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * Helper methods to woth with USkat core.
 * <p>
 * Methods of this class perform common tasks for applications using {@link ISkCoreApi}.
 *
 * @author hazard157
 */
public class SkHelperUtils {

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
    DtoObject dto = new DtoObject( aSkid, IOptionSet.NULL, IStringMap.EMPTY );
    for( IDtoAttrInfo ainf : cinf.attrs().list() ) {
      if( !ISkHardConstants.isSkSysAttr( ainf ) ) {
        dto.attrs().setValue( ainf.id(), ainf.dataType().defaultValue() );
      }
    }
    for( IDtoRivetInfo rinf : cinf.rivets().list() ) {
      SkidList sl = dto.rivets().ensureSkidList( rinf.id() );
      for( int i = 0; i < rinf.count(); i++ ) {
        sl.add( Skid.NONE );
      }
    }
    return dto;
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
      SkidList sl = dto.rivets().ensureSkidList( rinf.id() );
      for( int i = 0; i < rinf.count(); i++ ) {
        sl.add( Skid.NONE );
      }
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

  /**
   * Returns hexadecimal string representation of the argument MD5 digest.
   *
   * @param aPassword String - non-blank password string
   * @return String - hexadecimal string representation of the hash digets
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException argument is a blank string
   */
  public static String getPasswordHashCode( String aPassword ) {
    TsErrorUtils.checkNonBlank( aPassword );
    MessageDigest md;
    try {
      md = MessageDigest.getInstance( "MD5" ); //$NON-NLS-1$
    }
    catch( NoSuchAlgorithmException e ) {
      throw new TsInternalErrorRtException( e );
    }
    md.update( aPassword.getBytes() );
    return TsMiscUtils.bytesToHexStr( md.digest() );
  }

  /**
   * Prohibit inheritance.
   */
  private SkHelperUtils() {
    // nop
  }

}
