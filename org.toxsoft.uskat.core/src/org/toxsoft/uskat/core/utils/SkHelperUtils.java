package org.toxsoft.uskat.core.utils;

import java.security.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
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
    ISkClassInfo cinf = aCoreApi.sysdescr().getClassInfo( aSkid.classId() );
    // for existing object return it's DTO
    ISkObject skObj = aCoreApi.objService().find( aSkid );
    if( skObj != null ) {
      return DtoObject.createFromSk( skObj, cinf );
    }
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
