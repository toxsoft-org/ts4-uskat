package org.toxsoft.uskat.core.utils.msgen;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.ugwis.*;

/**
 * Helper methods for USkat formatted message generation.
 *
 * @author hazard157
 */
public class SkMessageUtils {

  /**
   * Returns a formatted string using the specified format string and UGWI addressed values as the arguments.
   * <p>
   * The {@link OptionSetUtils#format(String, IOptionSet, IMap)} is used for text formatting so the same formating rules
   * applies to the <code>aFmtStr</code> argument.
   * <p>
   * For argument values retrieval {@link ISkUgwiKind#getAtomicValue(Ugwi)} is used.
   *
   * @param aFmtStr String - the format string
   * @param aUsedUgwies {@link IStringMap}&lt;{@link Ugwi}&gt; - map "format argument ID" - "value UGWI address"
   * @param aCoreApi {@link ISkCoreApi} - the USkat Core API to retrieve argument values
   * @return String - a formatted string
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static String format( String aFmtStr, IStringMap<Ugwi> aUsedUgwies, ISkCoreApi aCoreApi ) {
    TsNullArgumentRtException.checkNulls( aFmtStr, aUsedUgwies, aCoreApi );
    // make option set for aUsedUgwies values
    IOptionSetEdit args = new OptionSet();
    IStringMapEdit<IDataType> defs = new StringMap<>();
    for( String key : aUsedUgwies.keys() ) {
      Ugwi ugwi = aUsedUgwies.getByKey( key );
      IAtomicValue av = IAtomicValue.NULL;
      ISkUgwiKind uKind = aCoreApi.ugwiService().listKinds().findByKey( ugwi.kindId() );
      if( uKind != null ) {
        av = uKind.getAtomicValue( ugwi );
        defs.put( key, uKind.getAtomicValueDataType( ugwi ) );
      }
      args.setValue( key, av );
    }
    // format text
    return OptionSetUtils.format( aFmtStr, args, defs );
  }

  /**
   * No subclasses.
   */
  private SkMessageUtils() {
    // nop
  }

}
