package org.toxsoft.uskat.core.api.objserv;

import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.skid.*;

/**
 * Encapsulated information about object.
 *
 * @author hazard157
 */
public interface IDtoObject
    extends IStridable {

  /**
   * Returns the object SKID.
   *
   * @return {@link Skid} - the object SKID
   */
  Skid skid();

  /**
   * Returns the attributes values.
   *
   * @return {@link IOptionSet} - the attributes
   */
  IOptionSet attrs();

  /**
   * Returns the riveted objects SKIDs.
   *
   * @return {@link IMappedSkids} - the map "rivet ID" - "riveted right objects SKIDs list"
   */
  IMappedSkids rivets();

  /**
   * Returns SKIDs map of reverse rivets (left objects which have this object riveted).
   *
   * @return {@link IStringMap}&lt;{@link IMappedSkids}&gt; SKIDs map of reverse rivets where: <br>
   *         - {@link IStringMap} key is "rivet class ID";<br>
   *         - {@link IMappedSkids} key is "rivet ID" - ;<br>
   *         - {@link IMappedSkids} values are "SKIDs list of the left objects which have this object riveted".
   */
  IStringMap<IMappedSkids> rivetRevs();

  // ------------------------------------------------------------------------------------
  // IStridable
  //

  @Override
  default String id() {
    return skid().strid();
  }

  @Override
  default String nmName() {
    return attrs().getStr( AID_NAME );
  }

  @Override
  default String description() {
    return attrs().getStr( AID_DESCRIPTION );
  }

  // ------------------------------------------------------------------------------------
  // inline methods for convenience

  @SuppressWarnings( "javadoc" )
  default String classId() {
    return skid().classId();
  }

  @SuppressWarnings( "javadoc" )
  default String strid() {
    return skid().strid();
  }

  @SuppressWarnings( "javadoc" )
  default String readableName() {
    return nmName().isBlank() ? id() : nmName();
  }

}
