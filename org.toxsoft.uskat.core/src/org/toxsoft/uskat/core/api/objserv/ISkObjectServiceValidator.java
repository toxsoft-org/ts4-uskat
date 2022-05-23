package org.toxsoft.uskat.core.api.objserv;

import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * {@link ISkObjectService} service validator.
 *
 * @author hazard157
 */
public interface ISkObjectServiceValidator {

  /**
   * Checks if new object can be created.
   *
   * @param aDtoObject {@link IDtoObject} - information of the new object
   * @return {@link ValidationResult} - the validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canCreateObject( IDtoObject aDtoObject );

  /**
   * Checks if multiple objects can be created/edited simultaneously.
   *
   * @param aDtoObjects {@link IList}&lt;{@link IDtoObject}&gt; - information of the new/edited objects
   * @return {@link ValidationResult} - the validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  default ValidationResult canCreateObjects( IList<IDtoObject> aDtoObjects ) {
    TsNullArgumentRtException.checkNull( aDtoObjects );
    ValidationResult vr = ValidationResult.SUCCESS;
    for( IDtoObject o : aDtoObjects ) {
      vr = ValidationResult.firstNonOk( vr, canCreateObject( o ) );
      if( vr.isError() ) {
        break;
      }
    }
    return vr;
  }

  /**
   * Проверят, можно ли создать отредактировать существующий объект.
   *
   * @param aNewObject {@link IDtoObject} - new information of the object
   * @param aOldObject {@link ISkObject} - existing information of the object
   * @return {@link ValidationResult} - the validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canEditObject( IDtoObject aNewObject, ISkObject aOldObject );

  /**
   * Checks if object may be removed (deleted).
   *
   * @param aSkid {@link Skid} - SKID of the object to be deleted
   * @return {@link ValidationResult} - the validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canRemoveObject( Skid aSkid );

  /**
   * Checks if multiple objects may be removed simultaneously.
   *
   * @param aSkids {@link ISkidList} - list SKID of the objects to be deleted
   * @return {@link ValidationResult} - the validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  default ValidationResult canRemoveObjects( ISkidList aSkids ) {
    TsNullArgumentRtException.checkNull( aSkids );
    ValidationResult vr = ValidationResult.SUCCESS;
    for( Skid s : aSkids ) {
      vr = ValidationResult.firstNonOk( vr, canRemoveObject( s ) );
      if( vr.isError() ) {
        break;
      }
    }
    return vr;
  }

}
