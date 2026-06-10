package org.toxsoft.uskat.core.backend.api;

import java.io.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Edition of RTdata values.
 *
 * @author mvk
 */
public final class BaRtDataEdition
    implements Serializable {

  private static final long serialVersionUID = 157157L;

  private final int                      editionNo;
  private final IMap<Gwid, IAtomicValue> values;

  /**
   * Constructor.
   *
   * @param aEditionNo int edition number
   * @param aValues {@link IMap}&lt;{@link Gwid},{@link IAtomicValue}&gt; current RTdata values;<br>
   *          Key: {@link Gwid} concrete GWID;<br>
   *          Value: {@link IAtomicValue} сurrent value.
   * @throws TsNullArgumentRtException arg = null
   */
  public BaRtDataEdition( int aEditionNo, IMap<Gwid, IAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNull( aValues );
    editionNo = aEditionNo;
    values = aValues;
  }

  /**
   * Returns edition number of current RTdata values.
   *
   * @return int edition number
   */
  public int editionNo() {
    return editionNo;
  }

  /**
   * Returns RTdata values.
   *
   * @return {@link IMap}&lt;{@link Gwid},{@link IAtomicValue}&gt; current RTdata values;<br>
   *         Key: {@link Gwid} concrete GWID;<br>
   *         Value: {@link IAtomicValue} сurrent value.
   */
  public IMap<Gwid, IAtomicValue> values() {
    return values;
  }

  /**
   * Comparison of edition versions.
   *
   * @param aEditionNo1 int edition number 1
   * @param aEditionNo2 int edition number 2
   * @return int 0: equals, > 0: aEditionNo1 is is newer than aEditionNo2; 0 < aEditionNo1 is older than aEditionNo2
   */
  public static int compareEditionNo( int aEditionNo1, int aEditionNo2 ) {
    return aEditionNo1 - aEditionNo2;
  }
}
