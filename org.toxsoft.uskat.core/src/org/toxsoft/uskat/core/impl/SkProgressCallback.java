package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * The long operation progress callback with the ability to replace the executor.
 *
 * @author mvk
 */
public final class SkProgressCallback
    implements ILongOpProgressCallback {

  private ILongOpProgressCallback callbackExecutor = ILongOpProgressCallback.NONE;

  // ------------------------------------------------------------------------------------
  // public API
  //

  /**
   * Returns the callback executor.
   *
   * @return {@link ILongOpProgressCallback} callback
   */
  public ILongOpProgressCallback callbackExecutor() {
    return callbackExecutor;
  }

  /**
   * Defines the callback executor.
   *
   * @param aExecutor {@link ILongOpProgressCallback} new executor
   * @return {@link ILongOpProgressCallback} previous executor
   * @throws TsNullArgumentRtException arg = null
   */
  public ILongOpProgressCallback setCallbackExecutor( ILongOpProgressCallback aExecutor ) {
    TsNullArgumentRtException.checkNull( aExecutor );
    ILongOpProgressCallback retValue = callbackExecutor;
    callbackExecutor = aExecutor;
    return retValue;
  }

  // ------------------------------------------------------------------------------------
  // ILongOpProgressCallback
  //

  @Override
  public boolean startWork( String aName, boolean aUndefined ) {
    return callbackExecutor.startWork( aName, aUndefined );
  }

  @Override
  public boolean updateWorkProgress( String aName, double aDonePercents ) {
    return callbackExecutor.updateWorkProgress( aName, aDonePercents );
  }

  @Override
  public void finished( ValidationResult aStatus ) {
    callbackExecutor.finished( aStatus );
  }

}
