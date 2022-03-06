package org.toxsoft.uskat.s5.utils.collections;

import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.gw.skid.Skid;

/**
 * Карта у которой ключом и значением являются идентификаторы объектов {@link Skid}
 *
 * @author mvk
 */
public interface ISkidMap
    extends IMap<Skid, Skid> {
  // nop
}
