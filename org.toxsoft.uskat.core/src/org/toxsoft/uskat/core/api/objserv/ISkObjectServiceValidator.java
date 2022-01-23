package org.toxsoft.uskat.core.api.objserv;

import org.toxsoft.tslib.bricks.validator.ValidationResult;
import org.toxsoft.tslib.coll.IList;
import org.toxsoft.tslib.gw.skid.ISkidList;
import org.toxsoft.tslib.gw.skid.Skid;
import org.toxsoft.tslib.utils.errors.TsNullArgumentRtException;

/**
 * {@link ISkObjectService} service validator.
 *
 * @author goga
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
  ValidationResult canCreateObjects( IList<IDtoObject> aDtoObjects );

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
  ValidationResult canRemoveObjects( ISkidList aSkids );

}
