package org.toxsoft.uskat.core.devapi.transactions;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * Listener to the changes in {@link ISkTransactionService}.
 *
 * @author mvk
 */
public interface ISkTransactionServiceListener {

  /**
   * Called when start transaction.
   */
  default void onStart() {
    // nop
  }

  /**
   * Called after the object has been created.
   *
   * @param aDtoObj {@link IDtoObject} object
   */
  default void onObjCreated( IDtoObject aDtoObj ) {
    // nop
  }

  /**
   * Called after the object has been merged.
   *
   * @param aDtoObj {@link IDtoObject} object
   */
  default void onObjMerged( IDtoObject aDtoObj ) {
    // nop
  }

  /**
   * Called after the object has been removed.
   *
   * @param aDtoObj {@link IDtoObject} object
   */
  default void onObjRemoved( IDtoObject aDtoObj ) {
    // nop
  }

  /**
   * Called before commit transaction.
   *
   * @param aRemovingObjs {@link IList}&lt;{@link IDtoObject}&gt; removing objects
   * @param aUpdatingObjs {@link IList}&lt;{@link Pair}&lt;{@link IDtoObject},{@link IDtoObject}&gt;&gt; updating object
   *          pair list where {@link Pair#left()} is the prev object state, {@link Pair#right()} is the new object state
   * @param aCreatingObjs {@link IList}&lt;{@link IDtoObject}&gt; creating objects
   * @param aUpdatingLinks {@link IList}&lt;{@link IDtoLinkFwd}&gt; updating object link list
   */
  default void onBeforeCommit( IList<IDtoObject> aRemovingObjs, IList<Pair<IDtoObject, IDtoObject>> aUpdatingObjs,
      IList<IDtoObject> aCreatingObjs, IList<IDtoLinkFwd> aUpdatingLinks ) {
    // nop
  }

  /**
   * Called after commit transaction.
   *
   * @param aRemovingObjs {@link IList}&lt;{@link IDtoObject}&gt; removing objects
   * @param aUpdatingObjs {@link IList}&lt;{@link Pair}&lt;{@link IDtoObject},{@link IDtoObject}&gt;&gt; updating object
   *          pair list where {@link Pair#left()} is the prev object state, {@link Pair#right()} is the new object state
   * @param aCreatingObjs {@link IList}&lt;{@link IDtoObject}&gt; creating objects
   * @param aUpdatingLinks {@link IList}&lt;{@link IDtoLinkFwd}&gt; updating object link list
   */
  default void onAfterCommit( IList<IDtoObject> aRemovingObjs, IList<Pair<IDtoObject, IDtoObject>> aUpdatingObjs,
      IList<IDtoObject> aCreatingObjs, IList<IDtoLinkFwd> aUpdatingLinks ) {
    // nop
  }

  /**
   * Called when transaction rollback.
   *
   * @param aRemovingObjs {@link IList}&lt;{@link IDtoObject}&gt; removing objects
   * @param aUpdatingObjs {@link IList}&lt;{@link Pair}&lt;{@link IDtoObject},{@link IDtoObject}&gt;&gt; updating object
   *          pair list where {@link Pair#left()} is the prev object state, {@link Pair#right()} is the new object state
   * @param aCreatingObjs {@link IList}&lt;{@link IDtoObject}&gt; creating objects
   * @param aUpdatingLinks {@link IList}&lt;{@link IDtoLinkFwd}&gt; updating object link list
   */
  default void onRollback( IList<IDtoObject> aRemovingObjs, IList<Pair<IDtoObject, IDtoObject>> aUpdatingObjs,
      IList<IDtoObject> aCreatingObjs, IList<IDtoLinkFwd> aUpdatingLinks ) {
    // nop
  }

}
