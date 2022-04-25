package org.toxsoft.uskat.core.impl;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;

import java.io.*;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.chario.impl.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.api.*;

/**
 * {@link ISkBackendInfo} implementation.
 *
 * @author hazard157
 */
public class SkBackendInfo
    extends StridableParameterized
    implements ISkBackendInfo, Serializable {

  private static final long serialVersionUID = 157157L;

  private static final IDataDef OP_START_TIME   = create( SK_ID + ".StartTime", TIMESTAMP ); //$NON-NLS-1$
  private static final IDataDef OP_SESSION_SKID = create( SK_ID + ".SessionSkid", VALOBJ );  //$NON-NLS-1$

  /**
   * Конструктор.
   *
   * @param aId String - backend ID
   * @param aStartTime long - the server start time (millisecons after epoch)
   * @param aSessionId {@link Skid} - session ID or {@link Skid#NONE} authentification is not supported
   * @param aParams {@link IOptionSet} - {@link ISkBackendInfo#params()} initial values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SkBackendInfo( String aId, long aStartTime, Skid aSessionId, IOptionSet aParams ) {
    super( aId, aParams );
    OP_START_TIME.setValue( params(), avTimestamp( aStartTime ) );
    OP_SESSION_SKID.setValue( params(), avValobj( aSessionId ) );
  }

  /**
   * Конструктор.
   *
   * @param aId String - backend ID
   * @param aStartTime long - the server start time (millisecons after epoch)
   * @param aSessionId {@link Skid} - session ID or {@link Skid#NONE} authentification is not supported
   * @param aIdsAndValues Object[] - {@link ISkBackendInfo#params()} initial values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SkBackendInfo( String aId, long aStartTime, Skid aSessionId, Object... aIdsAndValues ) {
    super( aId, OptionSetUtils.createOpSet( aIdsAndValues ) );
    OP_START_TIME.setValue( params(), avTimestamp( aStartTime ) );
    OP_SESSION_SKID.setValue( params(), avValobj( aSessionId ) );
  }

  // ------------------------------------------------------------------------------------
  // ISkBackendInfo
  //

  @Override
  public long startTime() {
    return OP_START_TIME.getValue( params() ).asLong();
  }

  @Override
  public Skid sessionId() {
    return OP_SESSION_SKID.getValue( params() ).asValobj();
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
