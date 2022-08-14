package org.toxsoft.uskat.core.utils;

import java.security.*;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.basis.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * Helper methods to woth with USkat core.
 * <p>
 * Methods of this class perform common tasks for applications using {@link ISkCoreApi}.
 *
 * @author hazard157
 */
public class SkHelperUtils {

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
   * Prohibit inheritance.
   */
  private SkHelperUtils() {
    // nop
  }

}
