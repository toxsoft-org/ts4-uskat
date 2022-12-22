package org.toxsoft.uskat.refbooks.lib;

import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * Information about refbook to be created.
 * <p>
 * This is much like {@link IDtoClassInfo} but without unneeded real-time properties and the {@link #id()} is refbook
 * identifier, not the refbook or items class identifier.
 *
 * @author goga
 */
public interface IDtoRefbookInfo
    extends IStridableParameterized {

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
   * Returns the link description DTOs.
   *
   * @return IStridablesList&lt;{@link IDtoLinkInfo}&lt; - list of the link description DTOs
   */
  IStridablesList<IDtoLinkInfo> linkInfos();

  /**
   * Returns the CLOB description DTOs.
   *
   * @return IStridablesList&lt;{@link IDtoEventInfo}&lt; - list of the CLOB description DTOs
   */
  IStridablesList<IDtoClobInfo> clobInfos();

  /**
   * Returns asked properties list of this class.
   *
   * @param <T> - the property type
   * @param aKind {@link ESkClassPropKind} - the requested kind of properties
   * @return IStridablesList&lt;T&lt; - list of the properties
   */
  <T extends IDtoClassPropInfoBase> IStridablesList<T> propInfos( ESkClassPropKind aKind );

}
