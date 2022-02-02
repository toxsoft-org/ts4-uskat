package org.toxsoft.uskat.core.api.objserv;

import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.uskat.core.impl.SkObject;

/**
 * Sk-object creator to be registered with {@link ISkObjectService#registerObjectCreator(String, ISkObjectCreator)}.
 *
 * @author hazard157
 * @param <T> - Java-type of Sk-object
 */
public interface ISkObjectCreator<T extends SkObject> {

  /**
   * Implementation must create Sk-object of {@link Skid#classId()} class
   *
   * @param aSkid {@link Skid} - ID of object to be created
   * @return &lt;T&gt; - created instance
   */
  T createObject( Skid aSkid );

}
