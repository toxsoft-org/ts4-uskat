package org.toxsoft.uskat.s5.server.backend.impl;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import java.io.Serializable;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.strid.impl.StridableParameterized;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.bricks.strio.chario.impl.CharOutputStreamAppendable;
import org.toxsoft.core.tslib.bricks.strio.impl.StrioWriter;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.api.ISkBackendInfo;
import org.toxsoft.uskat.s5.common.sessions.IS5SessionInfo;
import org.toxsoft.uskat.s5.server.IS5ServerHardConstants;

/**
 * {@link ISkBackendInfo} implementation.
 *
 * @author hazard157
 */
public class S5BackendInfo
    extends StridableParameterized
    implements ISkBackendInfo, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор.
   *
   * @param aId String - backend ID
   * @param aSessionInfo {@link IS5SessionInfo} информация о текущей сессии пользователя
   * @param aParams {@link IOptionSet} - {@link ISkBackendInfo#params()} initial values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BackendInfo( String aId, IS5SessionInfo aSessionInfo, IOptionSet aParams ) {
    super( aId, aParams );
    IS5ServerHardConstants.OP_BACKEND_SESSION_INFO.setValue( params(), avValobj( aSessionInfo ) );
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
