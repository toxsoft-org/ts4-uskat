package org.toxsoft.uskat.core.utils;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * The {@link IDataType} implementation with external {@link IOptionSet} as {@link #params()} override.
 * <p>
 * This implementation has it's own {@link #params()} and reference to the external {@link IOptionSet} used as
 * parameters of higher priority. Higher priority means that when asked for option {@link IDataType#params()} will
 * return value from {@link #priorityParams}. If {@link #priorityParams} does not has asked value the value from
 * internal {@link #chainedParams} will be returned.
 * <p>
 * Designed to be used in {@link IDtoAttrInfo}, {@link IDtoRtdataInfo}, etc.
 *
 * @author hazard157
 */
public class PriorityDataType
    implements IDataType {

  private final EAtomicType atomicType;
  private final IOptionSet  priorityParams;

  private final IOptionSetEdit chainedParams = new OptionSet() {

    @Override
    protected IAtomicValue doInternalFind( String aId ) {
      IAtomicValue av = priorityParams.findByKey( aId );
      if( av != null ) {
        return av;
      }
      return map.findByKey( aId );
    }

  };

  /**
   * Constructor.
   *
   * @param aAtomicType {@link EAtomicType} - atomic type
   * @param aPriorytyOpsetRef {@link IOptionSet} - reference to the params of higher priority
   * @param aParams {@link IOptionSet} - initial values of {@link #params()}
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public PriorityDataType( EAtomicType aAtomicType, IOptionSet aPriorytyOpsetRef, IOptionSet aParams ) {
    TsNullArgumentRtException.checkNulls( aAtomicType, aPriorytyOpsetRef, aParams );
    atomicType = aAtomicType;
    chainedParams.setAll( aParams );
    priorityParams = aPriorytyOpsetRef;
  }

  // ------------------------------------------------------------------------------------
  // IDataType
  //

  @Override
  public EAtomicType atomicType() {
    return atomicType;
  }

  @Override
  public IOptionSet params() {
    return chainedParams;
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + ": " + atomicType.id(); //$NON-NLS-1$
  }

}
