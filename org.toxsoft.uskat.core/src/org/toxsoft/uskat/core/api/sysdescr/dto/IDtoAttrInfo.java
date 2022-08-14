package org.toxsoft.uskat.core.api.sysdescr.dto;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Information about attribute property of class.
 *
 * @author hazard157
 */
public interface IDtoAttrInfo
    extends IDtoClassPropInfoBase {

  /**
   * Returns the data type of the attribute.
   *
   * @return {@link IDataType} - the data type
   */
  IDataType dataType();

  /**
   * Returns the attribute parameter value from {@link #params()} or data type.
   * <p>
   * {@link IDtoAttrInfo#params()} overrides {@link IDataType#params()} of {@link #dataType()}. This method returns
   * paraeter of attribute if it exists otherwise returns parameter of data type. Returns <code>aDefaultValue</code> if
   * searched parameter not found.
   *
   * @param aParamId String - ID of parameter
   * @param aDefaultValue {@link IAtomicValue} - value if no such param, may be <code>null</code>
   * @return {@link IAtomicValue} - parameter value or <code>null</code> if none exists
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  default IAtomicValue getParamValue( String aParamId, IAtomicValue aDefaultValue ) {
    if( params().hasKey( aParamId ) ) {
      return params().getValue( aParamId );
    }
    if( dataType().params().hasKey( aParamId ) ) {
      return dataType().params().getValue( aParamId );
    }
    return aDefaultValue;
  }

  /**
   * Returns the attribute parameter value from {@link #params()} or data type.
   * <p>
   * {@link IDtoAttrInfo#params()} overrides {@link IDataType#params()} of {@link #dataType()}. This method returns
   * paraeter of attribute if it exists otherwise returns parameter of data type. Returns
   * {@link IDataDef#defaultValue()} if searched parameter not found.
   *
   * @param aParamDef {@link IDataDef} - parameter definition
   * @return {@link IAtomicValue} - parameter value or <code>null</code> if none exists
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  default IAtomicValue getParamValue( IDataDef aParamDef ) {
    TsNullArgumentRtException.checkNull( aParamDef );
    return getParamValue( aParamDef.id(), aParamDef.defaultValue() );
  }

}
