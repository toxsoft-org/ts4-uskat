package org.toxsoft.uskat.core.api.sysdescr.dto;

/**
 * Information about CLOB property of class.
 *
 * @author hazard157
 */
public interface IDtoClobInfo
    extends IDtoClassPropInfoBase {

  /**
   * Returns the maximual allowed length of the CLOB.
   * <p>
   * CLOB may contaiin any number of <code>char</code> symbols. However Java {@link String} has resriction of maxumum up
   * to {@link Integer#MAX_VALUE} chars. If length of the CLOB exceeds {@link String} limitation, CLOB can not be read
   * as {@link String} value.
   *
   * @return long - maximal number of <code>char</code>s in CLOB
   */
  long maxCharsCount();

}
