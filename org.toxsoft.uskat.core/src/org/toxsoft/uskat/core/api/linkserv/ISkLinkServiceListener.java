package org.toxsoft.uskat.core.api.linkserv;

import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.gw.skid.Skid;

// TODO TRANSLATE

/**
 * Слушатель изменений в связях между объектами.
 *
 * @author goga
 */
public interface ISkLinkServiceListener {

  /**
   * Вызывается при измененииях в связи.
   *
   * @param aChangedLinks {@link IStringMap}&lt;{@link Skid}&gt; - карта "Ид связи" - "ИД левого объекта"
   */
  void onLinkChanged( IStringMap<Skid> aChangedLinks );

}
