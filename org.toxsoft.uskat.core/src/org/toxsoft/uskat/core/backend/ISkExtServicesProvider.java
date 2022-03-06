package org.toxsoft.uskat.core.backend;

import java.io.*;

import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.devapi.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Extension services provider.
 *
 * @author hazard157
 */
public interface ISkExtServicesProvider {

  /**
   * Instance rpovides no services.
   */
  ISkExtServicesProvider NULL = new InternalNullExtServicesProvider();

  /**
   * Creates and returns the addon extension and addon services.
   *
   * @param aCoreApi {@link IDevCoreApi} - core API for service developers
   * @return {@link IStringMap}&lt;{@link ISkService}&gt; - the created services map "service ID" - "service instance"
   */
  IStringMap<AbstractSkService> createExtServices( IDevCoreApi aCoreApi );

}

/**
 * {@link ISkExtServicesProvider#NULL} special-case implementation.
 */
class InternalNullExtServicesProvider
    implements ISkExtServicesProvider, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Method correctly deserializes {@link ISkExtServicesProvider#NULL} value.
   *
   * @return {@link ObjectStreamException} - {@link ISkExtServicesProvider#NULL}
   * @throws ObjectStreamException is declared but newer thrown by this method
   */
  @SuppressWarnings( { "static-method" } )
  private Object readResolve()
      throws ObjectStreamException {
    return ISkExtServicesProvider.NULL;
  }

  // ------------------------------------------------------------------------------------
  // ISkExtServicesProvider
  //

  @Override
  public IStringMap<AbstractSkService> createExtServices( IDevCoreApi aCoreApi ) {
    return IStringMap.EMPTY;
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @Override
  public int hashCode() {
    return TsLibUtils.INITIAL_HASH_CODE;
  }

  @Override
  public boolean equals( Object obj ) {
    return obj == this;
  }

  @Override
  public String toString() {
    return ISkExtServicesProvider.class.getSimpleName() + ".NULL"; //$NON-NLS-1$
  }

}
