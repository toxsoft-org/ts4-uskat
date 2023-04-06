package org.toxsoft.uskat.core.gui.km5;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * {@link KM5AbstractContributor} instance creator to be registered.
 *
 * @author hazard157
 */
public interface IKM5ContributorCreator {

  /**
   * Creates the instance.
   *
   * @param aConn {@link ISkConnection} - the connection
   * @param aDomain {@link IM5Domain} - connection domain
   * @return {@link KM5AbstractContributor} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  KM5AbstractContributor create( ISkConnection aConn, IM5Domain aDomain );

}
