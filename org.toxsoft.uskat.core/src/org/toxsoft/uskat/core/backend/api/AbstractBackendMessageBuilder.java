package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.errors.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Backend to frontend message builder classed base implementation.
 *
 * @author hazard157
 */
public class AbstractBackendMessageBuilder {

  private final String serviceId;
  private final String messageId;

  private final IStridablesListEdit<IDataDef> argDefs = new StridablesList<>();

  protected AbstractBackendMessageBuilder( String aServiceId, String aMessageId ) {
    serviceId = StridUtils.checkValidIdPath( aServiceId );
    messageId = StridUtils.checkValidIdPath( aMessageId );
  }

  // ------------------------------------------------------------------------------------
  // For subclasses
  //

  protected IDataDef defineArgNonValobj( String aArgId, EAtomicType aAtomicType, boolean aMandatory,
      Object... aIdsAndValues ) {
    StridUtils.checkValidIdPath( aArgId );
    TsNullArgumentRtException.checkNulls( aAtomicType, aIdsAndValues );
    TsIllegalArgumentRtException.checkTrue( aAtomicType == EAtomicType.VALOBJ );
    TsItemAlreadyExistsRtException.checkTrue( argDefs.hasKey( aArgId ) );
    DataDef dd = DataDef.create( aArgId, aAtomicType, aIdsAndValues );
    dd.params().setBool( TSID_IS_MANDATORY, aMandatory );
    argDefs.add( dd );
    return dd;
  }

  protected IDataDef defineArgValobj( String aArgId, String aKeeperId, boolean aMandatory, Object... aIdsAndValues ) {
    StridUtils.checkValidIdPath( aArgId );
    StridUtils.checkValidIdPath( aKeeperId );
    TsNullArgumentRtException.checkNull( aIdsAndValues );
    TsItemAlreadyExistsRtException.checkTrue( argDefs.hasKey( aArgId ) );
    DataDef dd = DataDef.create( aArgId, EAtomicType.VALOBJ, aIdsAndValues );
    dd.params().setBool( TSID_IS_MANDATORY, aMandatory );
    dd.params().setStr( TSID_KEEPER_ID, aKeeperId );
    argDefs.add( dd );
    return dd;
  }

  protected IAtomicValue getArg( GenericMessage aMsg, String aArgId ) {
    IDataDef dd = argDefs.getByKey( aArgId );
    TsItemNotFoundRtException.checkNull( dd );
    return dd.getValue( aMsg.args() );
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Creates message with arguments specified as
   *
   * @param aIdsAndValues Object[] - identifier / value pairs of the {@link GtMessage#args()}
   * @return {@link GtMessage} - created message
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException absent mandatoey arguments
   * @throws AvTypeCastRtException incompatibe value type
   */
  public GtMessage makeMessageVarargs( Object... aIdsAndValues ) {
    IOptionSet args = OptionSetUtils.createOpSet( aIdsAndValues );
    // check args agains definitions
    for( IDataDef dd : argDefs ) {
      IAtomicValue argVal = args.findByKey( dd.id() );
      TsIllegalArgumentRtException.checkTrue( dd.isMandatory() && argVal == null );
      if( argVal != null ) {
        AvTypeCastRtException.canAssign( dd.atomicType(), argVal.atomicType() );
      }
    }
    return new GtMessage( serviceId, messageId, args );
  }

}
