package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;

/**
 * Messages generated by {@link IBaClasses} addon to the frontend.
 *
 * @author hazard157
 */
public interface IBaClassesMessages {

  // FIXME move to BaMsgXxx

  // ------------------------------------------------------------------------------------
  // Message: any change in the classes

  /**
   * The message ID.
   */
  String MSGID_SYSDESCR_CHANGE = "SysdescrChange"; //$NON-NLS-1$

  /**
   * ID of the option {@link #OPDEF_CRUD_OP}.
   */
  String OPID_CRUD_OP = "CrudOp"; //$NON-NLS-1$

  /**
   * {@link GtMessage#args()} option: stores {@link ECrudOp}, the change kind
   */
  IDataDef OPDEF_CRUD_OP = DataDef.create( OPID_CRUD_OP, VALOBJ, //
      TSID_IS_MANDATORY, AV_TRUE, //
      TSID_KEEPER_ID, ECrudOp.KEEPER_ID //
  );

  /**
   * ID of the option {@link #OPDEF_CLASS_ID}.
   */
  String OPID_CLASS_ID = "ClassId"; //$NON-NLS-1$

  /**
   * {@link GtMessage#args()} option: stores affected class ID or not specified for {@link ECrudOp#LIST}.
   */
  IDataDef OPDEF_CLASS_ID = DataDef.create( OPID_CLASS_ID, STRING, //
      TSID_IS_MANDATORY, AV_FALSE //
  );

  /**
   * Creates the {@link GtMessage} for any change in the classes.
   *
   * @param aOp {@link ECrudOp} - the change kind
   * @param aClassId String - affected class ID, assumed <code>null</code> for {@link ECrudOp#LIST}
   * @return {@link GtMessage} - created instance to send to the frontend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  static GtMessage makeMessage( ECrudOp aOp, String aClassId ) {
    TsNullArgumentRtException.checkNull( aOp );
    GtMessage msg;
    if( aOp != ECrudOp.LIST ) {
      TsNullArgumentRtException.checkNull( aOp );
      msg = new GtMessage( ISkSysdescr.SERVICE_ID, MSGID_SYSDESCR_CHANGE, //
          OPID_CRUD_OP, avValobj( aOp ), //
          OPID_CLASS_ID, aClassId );
    }
    else {
      msg = new GtMessage( ISkSysdescr.SERVICE_ID, MSGID_SYSDESCR_CHANGE, //
          OPID_CRUD_OP, avValobj( ECrudOp.LIST ) );
    }
    return msg;
  }

  /**
   * Extracts {@link #OPDEF_CRUD_OP} value from the message with ID {@link #MSGID_SYSDESCR_CHANGE}.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return {@link ECrudOp} - retreived value
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException message is not {@link #MSGID_SYSDESCR_CHANGE}
   * @throws TsItemNotFoundRtException option {@link #OPID_CRUD_OP} is missed in {@link GenericMessage#args()}
   */
  static ECrudOp extractCrudOp( GenericMessage aMsg ) {
    TsNullArgumentRtException.checkNull( aMsg );
    TsIllegalArgumentRtException.checkFalse( aMsg.messageId().equals( MSGID_SYSDESCR_CHANGE ) );
    return OPDEF_CRUD_OP.getValue( aMsg.args() ).asValobj();
  }

  /**
   * Extracts {@link #OPDEF_CLASS_ID} value from the message with ID {@link #MSGID_SYSDESCR_CHANGE}.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return String - retreived value, may be <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException message is not {@link #MSGID_SYSDESCR_CHANGE}
   * @throws TsIllegalArgumentRtException message is invalid
   */
  static String extractClassId( GenericMessage aMsg ) {
    ECrudOp op = extractCrudOp( aMsg );
    String classId = aMsg.args().getStr( OPID_CLASS_ID, null );
    if( classId != null ) {
      TsIllegalArgumentRtException.checkTrue( op == ECrudOp.LIST );
    }
    else {
      TsIllegalArgumentRtException.checkTrue( op != ECrudOp.LIST );
    }
    return classId;
  }

}
