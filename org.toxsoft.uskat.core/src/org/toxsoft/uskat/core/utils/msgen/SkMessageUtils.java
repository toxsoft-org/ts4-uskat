package org.toxsoft.uskat.core.utils.msgen;

import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.utils.ugwi.*;

/**
 * Helper methods for USkat formatted message generation.
 *
 * @author hazard157
 */
public class SkMessageUtils {

  public static String format( ISkMessageInfo aInfo, ISkCoreApi aCoreApi ) {
    TsNullArgumentRtException.checkNulls( aInfo, aCoreApi );
    return format( aInfo.fmtStr(), aInfo.usedUgwies(), aCoreApi );
  }

  public static String format( String aFmtStr, IStringMap<Ugwi> aUsedUgwies, ISkCoreApi aCoreApi ) {
    TsNullArgumentRtException.checkNulls( aFmtStr, aUsedUgwies, aCoreApi );
    // make option set for aUsedUgwies values

    // FIXME move SkUgwiXxx to uskat.core

    // IOptionSetEdit args = new OptionSet();
    // for( String key : aUsedUgwies.keys() ) {
    // Ugwi ugwi = aUsedUgwies.getByKey( key );
    // SkUgwiKind uKind = SkUgwiUtils.getKind( ugwi.kindId() );
    // IAtomicValue av;
    // if( !uKind.hasAtomicValue() ) {
    // Object obj = uKind.getUgwiValue( ugwi, aCoreApi );
    // av = avStr( Objects.toString( obj ) );
    // }
    // else {
    // av = uKind.getUgwiValueAsAtomic( ugwi, aCoreApi );
    // }
    // args.setValue( key, av );
    // }
    // format string
    // return OptionSetUtils.format( aFmtStr, args );

    // TODO реализовать SkMessageUtils.format()
    throw new TsUnderDevelopmentRtException( "SkMessageUtils.format()" );
  }

  /**
   * No subclasses.
   */
  private SkMessageUtils() {
    // nop
  }

}
