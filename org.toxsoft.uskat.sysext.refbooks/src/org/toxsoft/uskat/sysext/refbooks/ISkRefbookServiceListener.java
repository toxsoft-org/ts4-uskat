package org.toxsoft.uskat.sysext.refbooks;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.helpers.ECrudOp;

import ru.uskat.common.dpu.rt.events.SkEvent;

/**
 * {@link ISkRefbookService} changes listener.
 *
 * @author goga
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
   * List contains events as defined by {@link ISkRefbookServiceHardConstants#EVDPU_REFBOOK_ITEM_CHANGE}.
   *
   * @param aRefbookId String - identifier of the refbook with items changed
   * @param aEvents {@link IList}&lt;{@link SkEvent}&gt; - refbook items change event
   */
  void onRefbookItemsChanged( String aRefbookId, IList<SkEvent> aEvents );

  /**
   * Informs about any changes in items of the specified refbook.
   *
   * @param aRefbookId String - identifier of the refbook with items changed
   * @deprecated use {@link #onRefbookItemsChanged(String, IList)} instead
   */
  @Deprecated
  void onRefbookItemsChanged( String aRefbookId );

}
