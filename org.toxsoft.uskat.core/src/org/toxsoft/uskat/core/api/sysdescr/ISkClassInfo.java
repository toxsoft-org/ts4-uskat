package org.toxsoft.uskat.core.api.sysdescr;

import org.toxsoft.core.tslib.bricks.strid.IStridableParameterized;
import org.toxsoft.core.tslib.gw.IGwHardConstants;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * Information about class.
 *
 * @author hazard157
 */
public interface ISkClassInfo
    extends IStridableParameterized {

  /**
   * Returns the class herarchy information.
   *
   * @return {@link ISkClassHierarchyInfo} - herarchy information
   */
  ISkClassHierarchyInfo hierarchy();

  /**
   * Returns complete info about class attributes.
   *
   * @return {@link ISkClassProps}&lt;{@link IDtoAttrInfo}&gt; - information about attributes
   */
  ISkClassProps<IDtoAttrInfo> attrs();

  /**
   * Returns complete info about class rivets.
   *
   * @return {@link ISkClassProps}&lt;{@link IDtoRivetInfo}&gt; - information about rivets
   */
  ISkClassProps<IDtoRivetInfo> rivets();

  /**
   * Returns complete info about class CLOBs.
   *
   * @return {@link ISkClassProps}&lt;{@link IDtoClobInfo}&gt; - information about CLOBs
   */
  ISkClassProps<IDtoClobInfo> clobs();

  /**
   * Returns complete info about class links.
   *
   * @return {@link ISkClassProps}&lt;{@link IDtoLinkInfo}&gt; - information about links
   */
  ISkClassProps<IDtoLinkInfo> links();

  /**
   * Returns complete info about class RTdata.
   *
   * @return {@link ISkClassProps}&lt;{@link IDtoRtdataInfo}&gt; - information about RTdata
   */
  ISkClassProps<IDtoRtdataInfo> rtdata();

  /**
   * Returns complete info about class events.
   *
   * @return {@link ISkClassProps}&lt;{@link IDtoEventInfo}&gt; - information about events
   */
  ISkClassProps<IDtoEventInfo> events();

  /**
   * Returns complete info about class commands.
   *
   * @return {@link ISkClassProps}&lt;{@link IDtoCmdInfo}&gt; - information about commands
   */
  ISkClassProps<IDtoCmdInfo> cmds();

  // ------------------------------------------------------------------------------------
  // Convinience inline methods

  /**
   * Returns the superclass ID.
   * <p>
   * For root class (class with ID {@link IGwHardConstants#GW_ROOT_CLASS_ID} returns an empty string.
   *
   * @return String - superclass ID (an IDpath) or an empty string for root class
   */
  default String parentId() {
    return hierarchy().parent() != null ? hierarchy().parent().id() : TsLibUtils.EMPTY_STRING;
  }

  /**
   * Returns information about specified kinf of class properties.
   *
   * @param <T> - concrete type of property info
   * @param aKind {@link ESkClassPropKind} - the requested kind of properties
   * @return {@link ISkClassProps}&lt;T&gt; - requested properties
   */
  @SuppressWarnings( { "unchecked", "rawtypes" } )
  default <T extends IDtoClassPropInfoBase> ISkClassProps<T> props( ESkClassPropKind aKind ) {
    return switch( aKind ) {
      case ATTR -> (ISkClassProps)attrs();
      case RIVET -> (ISkClassProps)rivets();
      case CLOB -> (ISkClassProps)clobs();
      case LINK -> (ISkClassProps)links();
      case RTDATA -> (ISkClassProps)rtdata();
      case EVENT -> (ISkClassProps)events();
      case CMD -> (ISkClassProps)cmds();
    };
  }

}
