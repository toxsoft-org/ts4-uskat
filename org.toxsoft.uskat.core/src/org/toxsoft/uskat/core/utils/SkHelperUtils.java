package org.toxsoft.uskat.core.utils;

import java.security.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.basis.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * Helper methods to woth with USkat core.
 * <p>
 * Methods of this class perform common tasks for applications using {@link ISkCoreApi}.
 *
 * @author hazard157
 */
public class SkHelperUtils {

  /**
   * Returns the data type constraint for the attribute.
   * <p>
   * Data type constraints are stored in {@link IDataType#params()} and {@link IDtoRtdataInfo#params()}. The constraint
   * in {@link IDtoRtdataInfo#params()} has priority ("overrides") constraint with the same ID from
   * {@link IDtoAttrInfo#dataType()}.
   *
   * @param aInfo {@link IDtoAttrInfo} - the attribute info
   * @param aConstraintId String - the constraint ID
   * @return {@link IAtomicValue} - the constraint value or {@link IAtomicValue#NULL} if none found
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static IAtomicValue getConstraint( IDtoAttrInfo aInfo, String aConstraintId ) {
    TsNullArgumentRtException.checkNulls( aInfo, aConstraintId );
    IAtomicValue av = aInfo.params().getValue( aConstraintId, IAtomicValue.NULL );
    if( av == IAtomicValue.NULL ) {
      av = aInfo.dataType().params().getValue( aConstraintId, IAtomicValue.NULL );
    }
    return av;
  }

  /**
   * Returns the data type constraint for the RTdata.
   * <p>
   * Data type constraints are stored in {@link IDataType#params()} and {@link IDtoRtdataInfo#params()}. The constraint
   * in {@link IDtoRtdataInfo#params()} has priority ("overrides") constraint with the same ID from
   * {@link IDtoRtdataInfo#dataType()}.
   *
   * @param aInfo {@link IDtoRtdataInfo} - the RTdata info
   * @param aConstraintId String - the constraint ID
   * @return {@link IAtomicValue} - the constraint value or {@link IAtomicValue#NULL} if none found
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static IAtomicValue getConstraint( IDtoRtdataInfo aInfo, String aConstraintId ) {
    TsNullArgumentRtException.checkNulls( aInfo, aConstraintId );
    IAtomicValue av = aInfo.params().getValue( aConstraintId, IAtomicValue.NULL );
    if( av == IAtomicValue.NULL ) {
      av = aInfo.dataType().params().getValue( aConstraintId, IAtomicValue.NULL );
    }
    return av;
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
   * Returns a reasonable length displayable string of object names.
   * <p>
   * For an empty list an empty string is returned.
   *
   * @param aObjects {@link ITsCollection}&lt;{@link ISkObject}&gt; - objects
   * @return String - human readable string
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static String makeObjsListReadableName( ITsCollection<ISkObject> aObjects ) {
    TsNullArgumentRtException.checkNull( aObjects );
    IList<ISkObject> ll;
    if( aObjects instanceof IList ) {
      ll = (IList<ISkObject>)aObjects;
    }
    else {
      ll = new ElemArrayList<>( aObjects );
    }
    switch( ll.size() ) {
      case 0:
        return TsLibUtils.EMPTY_STRING;
      case 1:
      case 2:
      case 3: {
        StringBuilder sb = new StringBuilder();
        for( int i = 0; i < ll.size(); i++ ) {
          sb.append( ll.get( i ).readableName() );
          if( i < ll.size() - 1 ) {
            sb.append( ", " ); //$NON-NLS-1$
          }
        }
        return sb.toString();
      }
      default: {
        StringBuilder sb = new StringBuilder();
        sb.append( ll.get( 0 ).readableName() );
        sb.append( ", " ); //$NON-NLS-1$
        sb.append( ll.get( 1 ).readableName() );
        sb.append( ", ... " ); //$NON-NLS-1$
        sb.append( ll.last().readableName() );
        return sb.toString();
      }
    }
  }

  /**
   * Get SKIDs of listed objects.
   *
   * @param aObjs {@link IList}&lt;{@link ISkObject}&gt; - list of objects
   * @return {@link SkidList} - list of SKIDs
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static SkidList objsToSkids( IList<? extends ISkObject> aObjs ) {
    TsNullArgumentRtException.checkNull( aObjs );
    SkidList ll = new SkidList();
    if( aObjs instanceof ITsFastIndexListTag<? extends ISkObject> fl ) {
      for( int i = 0, n = fl.size(); i < n; i++ ) {
        ll.add( fl.get( i ).skid() );
      }
    }
    else {
      for( ISkObject o : aObjs ) {
        ll.add( o.skid() );
      }
    }
    return ll;
  }

  /**
   * Prohibit inheritance.
   */
  private SkHelperUtils() {
    // nop
  }

}
