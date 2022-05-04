package org.toxsoft.uskat.sysext.refbooks;

import static ru.uskat.common.ISkHardConstants.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.utils.TsLibUtils;

import ru.uskat.core.common.skobject.ISkObject;

/**
 * DPU of the {@link ISkRefbookItem}.
 *
 * @author goga
 */
public interface ISkRefbookDpuItemInfo
    extends IStridable {

  /**
   * Returns the item {@link ISkObject#strid()}.
   *
   * @return String - the object strid
   */
  String strid();

  /**
   * Returns the item attribute values.
   *
   * @return {@link IOptionSet} - the item attribute values
   */
  IOptionSet attrs();

  @Override
  default String id() {
    return strid();
  }

  @Override
  default String nmName() {
    return attrs().getStr( AID_NAME, TsLibUtils.EMPTY_STRING );
  }

  @Override
  default String description() {
    return attrs().getStr( AID_DESCRIPTION, TsLibUtils.EMPTY_STRING );
  }

}
