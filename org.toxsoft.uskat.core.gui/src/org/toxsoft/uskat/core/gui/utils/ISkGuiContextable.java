package org.toxsoft.uskat.core.gui.utils;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * Mixin interface of entities havind access to ISkConnection.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface ISkGuiContextable
    extends ITsGuiContextable, ISkConnected {

  /**
   * Returns {@link ISkConnection} - connection to the USkat system.
   * <p>
   * This is the only method to be implementaed by subclass.
   *
   * @return {@link ISkConnection} - connection to USkat.
   */
  @Override
  ISkConnection skConn();

  default ISkConnectionSupplier connectionSupplier() {
    return tsContext().get( ISkConnectionSupplier.class );
  }

  default <S extends ISkService> S skService( String aServiceId ) {
    return coreApi().getService( aServiceId );
  }

  @Override
  default IM5Domain m5() {
    return skConn().scope().get( IM5Domain.class );
  }

  // HERE add more convinience methods

}
