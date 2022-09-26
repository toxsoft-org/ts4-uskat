package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.errors.AvTypeCastRtException;
import org.toxsoft.core.tslib.av.impl.DataDef;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetUtils;
import org.toxsoft.core.tslib.bricks.events.msg.GenericMessage;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Backend to frontend message builder classed base implementation.
 *
 * @author hazard157
 */
public class AbstractBackendMessageBuilder {

  private final String topicId;
  private final String messageId;

  private final IStridablesListEdit<IDataDef> argDefs = new StridablesList<>();

  protected AbstractBackendMessageBuilder( String aTopicId, String aMessageId ) {
    topicId = StridUtils.checkValidIdPath( aTopicId );
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
    return new GtMessage( topicId, messageId, args );
  }

  /**
   * Returns an indication that the message was built by the target builder.
   *
   * @param aMessage {@link GtMessage} the message.
   * @return boolean <b>true</b> the message was built by the target builder.
   */
  public boolean isOwnMessage( GtMessage aMessage ) {
    TsNullArgumentRtException.checkNull( aMessage );
    return topicId.equals( aMessage.topicId() ) && messageId.equals( aMessage.messageId() );
  }
}
