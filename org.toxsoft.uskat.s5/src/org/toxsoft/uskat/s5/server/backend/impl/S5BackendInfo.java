package org.toxsoft.uskat.s5.server.backend.impl;

import org.toxsoft.core.tslib.bricks.strid.IStridableParameterized;
import org.toxsoft.core.tslib.bricks.strid.impl.StridableParameterizedSer;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.bricks.strio.chario.impl.CharOutputStreamAppendable;
import org.toxsoft.core.tslib.bricks.strio.impl.StrioWriter;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.api.ISkBackendInfo;
import org.toxsoft.uskat.s5.server.IS5ServerHardConstants;

/**
 * {@link ISkBackendInfo} s5 implementation.
 *
 * @author mvk
 */
public class S5BackendInfo
    extends StridableParameterizedSer
    implements ISkBackendInfo {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор.
   *
   * @param aStridable {@link IStridableParameterized} - backend stridable
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BackendInfo( IStridableParameterized aStridable ) {
    super( aStridable );
  }

  // ------------------------------------------------------------------------------------
  // ISkBackendInfo
  //

  @Override
  public long startTime() {
    return IS5ServerHardConstants.OP_BACKEND_START_TIME.getValue( params() ).asLong();
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    IStrioWriter sw = new StrioWriter( new CharOutputStreamAppendable( sb ) );
    sw.pl( "BackendID=%s {", id() ); //$NON-NLS-1$
    for( String opid : params().keys() ) {
      sw.pl( "  %s = %s", opid, params().getValue( opid ).asString() ); //$NON-NLS-1$
    }
    sw.pl( "}" ); //$NON-NLS-1$
    return sb.toString();
  }

}
