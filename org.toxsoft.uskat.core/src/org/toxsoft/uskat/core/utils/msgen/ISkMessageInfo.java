package org.toxsoft.uskat.core.utils.msgen;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;

/**
 * The USkat text message definition.
 *
 * @author hazard157
 */
public interface ISkMessageInfo {

  /**
   * No message (empty string message) singleton.
   */
  ISkMessageInfo NONE = new SkMessageInfo( EMPTY_STRING );

  /**
   * Returns the format string for {@link OptionSetUtils#format(String, IOptionSet)}.
   *
   * @return String - the format string
   */
  String fmtStr();

  /**
   * Returns UGWIs used for message formating.
   * <p>
   * The format string {@link #fmtStr()}, as expected in {@link OptionSetUtils#format(String, IOptionSet)}, may contain
   * <i>format specifiers</i>. Each format specifier contains the argument ID, the key. The Returned map must contain
   * every key of the format specifiers mentioned in {@link #fmtStr()}. So for every argument ID the respective UGWI
   * must exist.
   * <p>
   * See {@link SkMessageUtils#format(String, IStringMap, ISkCoreApi)} for more information about UGWI resolution.
   *
   * @return {@link IStringMap}&lt;{@link Ugwi}&gt; - used UGWIs map "key IDpath" - "UGWI"
   */
  IStringMap<Ugwi> usedUgwies();

  /**
   * Formats the message string using {@link SkMessageUtils#format(String, IStringMap, ISkCoreApi)} method.
   *
   * @param aCoreApi {@link ISkCoreApi} - USkat API used for the UGWI resolution
   * @return String - formatted message
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  default String makeMessage( ISkCoreApi aCoreApi ) {
    return SkMessageUtils.format( fmtStr(), usedUgwies(), aCoreApi );
  }

}
