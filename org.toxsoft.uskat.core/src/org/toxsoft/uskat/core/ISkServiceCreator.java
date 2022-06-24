package org.toxsoft.uskat.core;

import org.toxsoft.uskat.core.devapi.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * {@link AbstractSkCoreService} creator is used to dynamically add services to the USkat core at runtime.
 *
 * @author hazard157
 * @param <S> - the type of the service
 */
public interface ISkServiceCreator<S extends AbstractSkService> {

  /**
   * Creates the service instance.
   *
   * @param aCoreApi {@link IDevCoreApi} - internal (extended) core API for services
   * @return &lt;S&gt; - created service instance
   */
  S createService( IDevCoreApi aCoreApi );

}
