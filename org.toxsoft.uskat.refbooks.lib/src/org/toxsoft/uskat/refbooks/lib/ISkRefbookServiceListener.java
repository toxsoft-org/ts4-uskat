package org.toxsoft.uskat.refbooks.lib;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.uskat.core.api.evserv.*;

/**
 * {@link ISkRefbookService} changes listener.
 *
 * @author hazard157
 */
public interface ISkRefbookServiceListener {

  /**
   * Informs ablut changes in refbooks list (not changed in items).
   *
   * @param aOp {@link ECrudOp} - the kind of changes
   * @param aRefbookId String - identifier of the changed refbook or <code>null</code> on batch changes
   */
  void onRefbookChanged( ECrudOp aOp, String aRefbookId );

  /**
   * Informs about any changes in items of the specified refbook.
   * <p>
   * List contains events as defined by {@link ISkRefbookServiceHardConstants#EVDTO_REFBOOK_EDIT}.
   *
   * @param aRefbookId String - identifier of the refbook with items changed
   * @param aEvents {@link IList}&lt;{@link SkEvent}&gt; - refbook items change event
   */
  void onRefbookItemsChanged( String aRefbookId, IList<SkEvent> aEvents );

}
