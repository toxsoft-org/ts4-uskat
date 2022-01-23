package org.toxsoft.uskat.core.api.sysdescr.dto;

import org.toxsoft.tslib.bricks.strid.IStridableParameterized;
import org.toxsoft.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.tslib.gw.IGwHardConstants;

/**
 * Information about class.
 *
 * @author goga
 */
public interface IDtoClassInfo
    extends IStridableParameterized {

  /**
   * Returns the superclass ID.
   * <p>
   * For root class (class with ID {@link IGwHardConstants#GW_ROOT_CLASS_ID} returns an empty string.
   *
   * @return String - superclass ID (an IDpath) or an empty string for root class
   */
  String parentId();

  /**
   * Returns the attribute description DTOs.
   *
   * @return IStridablesList&lt;{@link IDtoAttrInfo}&lt; - list of the attribute description DTOs
   */
  IStridablesList<IDtoAttrInfo> attrInfos();

  /**
   * Returns the rivet description DTOs.
   *
   * @return IStridablesList&lt;{@link IDtoRivetInfo}&lt; - list of the rivet description DTOs
   */
  IStridablesList<IDtoRivetInfo> rivetInfos();

  /**
   * Returns the RTdata description DTOs.
   *
   * @return IStridablesList&lt;{@link IDtoRtdataInfo}&lt; - list of the RTdata description DTOs
   */
  IStridablesList<IDtoRtdataInfo> rtdataInfos();

  /**
   * Returns the link description DTOs.
   *
   * @return IStridablesList&lt;{@link IDtoLinkInfo}&lt; - list of the link description DTOs
   */
  IStridablesList<IDtoLinkInfo> linkInfos();

  /**
   * Returns the command description DTOs.
   *
   * @return IStridablesList&lt;{@link IDtoCmdInfo}&lt; - list of the command description DTOs
   */
  IStridablesList<IDtoCmdInfo> cmdInfos();

  /**
   * Returns the event description DTOs.
   *
   * @return IStridablesList&lt;{@link IDtoEventInfo}&lt; - list of the event description DTOs
   */
  IStridablesList<IDtoEventInfo> eventInfos();

  /**
   * Returns the CLOB description DTOs.
   *
   * @return IStridablesList&lt;{@link IDtoEventInfo}&lt; - list of the CLOB description DTOs
   */
  IStridablesList<IDtoClobInfo> clobInfos();

}
