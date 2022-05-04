package org.toxsoft.uskat.sysext.refbooks;

import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;

import ru.uskat.common.dpu.IDpuSdAttrInfo;
import ru.uskat.common.dpu.IDpuSdLinkInfo;

/**
 * DPU of refbook meta information (without items).
 * <p>
 * The {@link #id()} is refbook identifier, not the refbook or items class identifier.
 *
 * @author goga
 */
public interface ISkRefbookDpuInfo
    extends IStridable {

  /**
   * Returns the attribute DPUs.
   *
   * @return IStridablesList&lt;{@link IDpuSdAttrInfo}&lt; - list of the attribute DPUs
   */
  IStridablesList<IDpuSdAttrInfo> itemAttrInfos();

  /**
   * Returns the link DPUs.
   *
   * @return IStridablesList&lt;{@link IDpuSdLinkInfo}&lt; - list of the link DPUs
   */
  IStridablesList<IDpuSdLinkInfo> itemLinkInfos();

}
