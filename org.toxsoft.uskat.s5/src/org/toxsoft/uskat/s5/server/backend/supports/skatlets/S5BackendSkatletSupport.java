package org.toxsoft.uskat.s5.server.backend.supports.skatlets;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.devapi.*;
import org.toxsoft.uskat.s5.client.local.*;

/**
 * Реализация {@link ISkatletSupport}
 */
class S5BackendSkatletSupport
    implements ISkatletSupport {

  private final IS5LocalConnectionSingleton connectionFactory;
  private final ILogger                     logger;

  /**
   * Constructor.
   *
   * @param aConnectionFactory {@link IS5LocalConnectionSingleton} фабрика соединений
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5BackendSkatletSupport( IS5LocalConnectionSingleton aConnectionFactory, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aConnectionFactory, aLogger );
    connectionFactory = aConnectionFactory;
    logger = aLogger;
  }

  // ------------------------------------------------------------------------------------
  // ISkatletSupport
  //
  @Override
  public ISkConnection createConnection( String aName, ITsContextRo aArgs ) {
    TsNullArgumentRtException.checkNulls( aName, aArgs );
    return connectionFactory.open( aName, aArgs );
  }

  @Override
  public ILogger logger() {
    return logger;
  }

}
