package org.toxsoft.uskat.base.gui.conn;

import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * {@link ISkConnectionSupplier} implementation.
 *
 * @author hazard157
 */
public class SkConnectionSupplier
    implements ISkConnectionSupplier {

  private final IMapEdit<IdChain, ISkConnection> connsMap = new ElemMap<>();

  private IdChain defKey = IdChain.NULL;

  /**
   * Constructor.
   */
  public SkConnectionSupplier() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // ISkConnectionSupplier
  //

  @Override
  public ISkConnection defConn() {
    return connsMap.findByKey( defKey );
  }

  @Override
  public ISkConnection setDefaultConnection( IdChain aKey ) {
    TsNullArgumentRtException.checkNull( aKey );
    TsItemNotFoundRtException.checkFalse( connsMap.hasKey( aKey ) );
    defKey = aKey;
    return defConn();
  }

  @Override
  public IdChain getDefaultConnectionKey() {
    return defKey;
  }

  @Override
  public ISkConnection createConnection( IdChain aKey ) {
    TsNullArgumentRtException.checkNull( aKey );
    TsIllegalArgumentRtException.checkTrue( aKey == IdChain.NULL );
    TsItemAlreadyExistsRtException.checkTrue( connsMap.hasKey( aKey ) );
    ISkConnection conn = SkCoreUtils.createConnection();
    connsMap.put( aKey, conn );
    return conn;
  }

  @Override
  public IMap<IdChain, ISkConnection> allConns() {
    return connsMap;
  }

  @Override
  public void removeConnection( IdChain aKey ) {
    ISkConnection conn = connsMap.findByKey( aKey );
    if( conn == null ) {
      return;
    }
    connsMap.removeByKey( aKey );
    if( defKey.equals( aKey ) ) {
      defKey = IdChain.NULL;
    }
  }

}
