package org.toxsoft.uskat.core.gui.km5;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * M5-field for {@link ISkObject} attribute.
 *
 * @author hazard157
 * @param <T> - modeled entity type
 */
public class KM5AttributeFieldDef<T extends ISkObject>
    extends M5AttributeFieldDef<T> {

  /**
   * Constructor from {@link IDtoAttrInfo}.
   *
   * @param aAttrInfo {@link IDtoAttrInfo} - the attribute info
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public KM5AttributeFieldDef( IDtoAttrInfo aAttrInfo ) {
    this( aAttrInfo.id(), aAttrInfo.dataType() );
    params().addAll( aAttrInfo.params() );
    setNameAndDescription( aAttrInfo.nmName(), aAttrInfo.description() );
  }

  /**
   * Constructor for fields not existing in {@link ISkObject}.
   *
   * @param aId String - the field ID
   * @param aAttrType {@link IDataType} - the attribute data type
   * @param aIdsAndValues Object[] - identifier / value pairs overrides {@link IDataType#params()}
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public KM5AttributeFieldDef( String aId, IDataType aAttrType, Object... aIdsAndValues ) {
    super( aId, aAttrType, aIdsAndValues );
    // TODO do we need this hack?
    // if no default format string for boolean AV, set it as check mark
    if( atomicType() == EAtomicType.BOOLEAN ) {
      if( !params().hasValue( TSID_FORMAT_STRING ) ) {
        params().setStr( TSID_FORMAT_STRING, FMT_BOOL_CHECK );
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // M5FieldDef
  //

  @Override
  protected IAtomicValue doGetFieldValue( T aEntity ) {
    return aEntity.attrs().getValue( id() );
  }

}
