package org.toxsoft.uskat.core;

import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * The USkate core change Event.
 *
 * @author hazard157
 * @param op {@link ECrudOp} - the opration kind, never is {@link ECrudOp#LIST}
 * @param gwid {@link Gwid} - affected entity GWID
 */
public record SkCoreEvent ( ECrudOp op, Gwid gwid ) {

  @SuppressWarnings( "javadoc" )
  public SkCoreEvent( ECrudOp op, Gwid gwid ) {
    TsNullArgumentRtException.checkNulls( op, gwid );
    TsIllegalArgumentRtException.checkTrue( op == ECrudOp.LIST );
    this.op = op;
    this.gwid = gwid;
  }

}
