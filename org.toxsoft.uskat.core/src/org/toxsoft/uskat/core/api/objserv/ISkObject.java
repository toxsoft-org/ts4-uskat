package org.toxsoft.uskat.core.api.objserv;

import java.io.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Green world (application domain) object.
 * <p>
 * Stridable identifier {@link #id()} returns object {@link #strid()}.
 * <p>
 * Note: any user implementation of {@link ISkObject} must be subclassed from {@link SkObject}.
 *
 * @author hazard157
 */
public interface ISkObject
    extends IStridable {

  /**
   * Non-existing object single ton with identifier {@link Skid#NONE}.
   */
  ISkObject NONE = new InternalNoneSkObject();

  /**
   * Returns the objct SKID.
   *
   * @return {@link Skid} - the objct SKID
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
   * <p>
   * The map always contains all rivets from {@link ISkClassInfo#rivets()}. All lists in map always contains
   * {@link IDtoRivetInfo#count()} items.
   *
   * @return {@link IMappedSkids} - the map "rivet ID" - "riveted right objects SKIDs list"
   */
  IMappedSkids rivets();

  /**
   * Returns the object class ID.
   * <p>
   * Always returns {@link Skid#classId() skid().classId()}.
   *
   * @return String - the class ID
   */
  String classId();

  /**
   * Returns the object string identifier.
   * <p>
   * For anyISkObject {@link #id()} and {@link #strid()} returns the same value as {@link Skid#strid() skid().strid()}.
   *
   * @return String - the object strid
   */
  String strid();

  /**
   * For anyISkObject {@link #id()} and {@link #strid()} returns the same value as {@link Skid#strid() skid().strid()}.
   * <p>
   * {@inheritDoc}
   */
  @Override
  String id();

  /**
   * Returns human-readable short name of the object.
   * <p>
   * By default if it is non-blank returns {@link #nmName()} otherwise returns {@link #id()}. Implementation may
   * override.
   *
   * @return String - human-readable non-blank name
   */
  String readableName();

  /**
   * Returns the core API of connnection to the objects source.
   *
   * @return {@link ISkCoreApi} - Skat core API
   */
  ISkCoreApi coreApi();

  /**
   * Returns the information of the object class.
   *
   * @return {@link ISkClassInfo} - class information
   */
  default ISkClassInfo classInfo() {
    return coreApi().sysdescr().getClassInfo( skid().classId() );
  }

  /**
   * Return SKID of single rivet (rivet with max 1 object riveted).
   * <p>
   * If rivet is empty or there are more than 1 riveted object, method returns <code>null</code>.
   *
   * @param aRivetId String - the rivet ID
   * @return {@rivet Skid} - single riveted object SKID or <code>null</code>
   * @throws TsItemNotFoundRtException no such rivet or object exists
   */
  default Skid getSingleRivetSkid( String aRivetId ) {
    return rivets().map().getByKey( aRivetId ).first();
  }

  /**
   * Return object of single rivet (rivet with max 1 object riveted).
   * <p>
   * If rivet is empty or there are more than 1 riveted object, method returns <code>null</code>. Also returns
   * <code>null</code> if object of riveted SKID does not exists.
   *
   * @param <T> - expected type of the object
   * @param aRivetId String - the rivet ID
   * @return {@rivet ISkObject} - single riveted object or <code>null</code>
   * @throws TsItemNotFoundRtException no such rivet or object exists
   */
  @SuppressWarnings( "unchecked" )
  default <T extends ISkObject> T getSingleRivet( String aRivetId ) {
    Skid skid = getSingleRivetSkid( aRivetId );
    return (T)coreApi().objService().find( skid );
  }

  /**
   * Returns SKIDs of reverse rivet (left objects which have this object riveted).
   *
   * @param aClassId String - rivet class ID
   * @param aRivetId String - rivet ID
   * @return {@rivet ISkidList} - list of left objects SKIDs
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ISkidList getRivetRevSkids( String aClassId, String aRivetId );

  /**
   * Returns objects of reverse rivet (left objects which have this object riveted).
   *
   * @param <T> - конкретный тип возвращаемых объектов
   * @param aClassId String - rivet class ID
   * @param aRivetId String - rivet ID
   * @return {@rivet IList}&lt;T&gt; - list of objects
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  <T extends ISkObject> IList<T> getRivetRevObjs( String aClassId, String aRivetId );

  // ------------------------------------------------------------------------------------
  //

  /**
   * Return SKID of single link (link with max 1 object linked).
   * <p>
   * If link is empty or there are more than 1 linked object, method returns <code>null</code>.
   *
   * @param aLinkId String - the link ID
   * @return {@link Skid} - single linked object SKID or <code>null</code>
   * @throws TsItemNotFoundRtException no such link or object exists
   */
  Skid getSingleLinkSkid( String aLinkId );

  /**
   * Return object of single link (link with max 1 object linked).
   * <p>
   * If link is empty or there are more than 1 linked object, method returns <code>null</code>. Also returns
   * <code>null</code> if object of linked SKID does not exists.
   *
   * @param <T> - expected type of the object
   * @param aLinkId String - the link ID
   * @return {@link ISkObject} - single linked object or <code>null</code>
   * @throws TsItemNotFoundRtException no such link or object exists
   */
  <T extends ISkObject> T getSingleLinkObj( String aLinkId );

  /**
   * Returns SKIDs of linked objects.
   *
   * @param aLinkId String - the link ID
   * @return {@link ISkidList} - list of linked objects SKIDs
   * @throws TsItemNotFoundRtException no such link or object exists
   */
  ISkidList getLinkSkids( String aLinkId );

  /**
   * Returns linked objects.
   *
   * @param <T> - expected type of the linked objects
   * @param aLinkId String - the link ID
   * @return {@link IList}&lt;T&gt; - list of objects
   * @throws TsItemNotFoundRtException no such link or object exists
   */
  <T extends ISkObject> IList<T> getLinkObjs( String aLinkId );

  /**
   * Returns SKIDs of reverse link (left objects which have this object linked).
   *
   * @param aClassId String - link class ID
   * @param aLinkId String - link ID
   * @return {@link ISkidList} - list of left objects SKIDs
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ISkidList getLinkRevSkids( String aClassId, String aLinkId );

  /**
   * Returns objects of reverse link (left objects which have this object linked).
   *
   * @param <T> - конкретный тип возвращаемых объектов
   * @param aClassId String - link class ID
   * @param aLinkId String - link ID
   * @return {@link IList}&lt;T&gt; - list of objects
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  <T extends ISkObject> IList<T> getLinkRevObjs( String aClassId, String aLinkId );

}

class InternalNoneSkObject
    implements ISkObject, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Method correctly deserializes {@link ISkObject#NONE} value.
   *
   * @return {@link ObjectStreamException} - {@link ISkObject#NONE}
   * @throws ObjectStreamException is declared but newer thrown by this method
   */
  @SuppressWarnings( { "static-method" } )
  private Object readResolve()
      throws ObjectStreamException {
    return ISkObject.NONE;
  }

  @Override
  public Skid skid() {
    return Skid.NONE;
  }

  @Override
  public String nmName() {
    return TsLibUtils.EMPTY_STRING;
  }

  @Override
  public String id() {
    return skid().strid();
  }

  @Override
  public String description() {
    return TsLibUtils.EMPTY_STRING;
  }

  @Override
  public IOptionSet attrs() {
    return IOptionSet.NULL;
  }

  @Override
  public IMappedSkids rivets() {
    return IMappedSkids.EMPTY;
  }

  @Override
  public String classId() {
    return skid().classId();
  }

  @Override
  public String strid() {
    return skid().strid();
  }

  @Override
  public String readableName() {
    return skid().strid();
  }

  @Override
  public ISkCoreApi coreApi() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public Skid getSingleLinkSkid( String aLinkId ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public <T extends ISkObject> T getSingleLinkObj( String aLinkId ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public ISkidList getLinkSkids( String aLinkId ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public <T extends ISkObject> IList<T> getLinkObjs( String aLinkId ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public ISkidList getLinkRevSkids( String aClassId, String aLinkId ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public <T extends ISkObject> IList<T> getLinkRevObjs( String aClassId, String aLinkId ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public ISkidList getRivetRevSkids( String aClassId, String aRivetId ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public <T extends ISkObject> IList<T> getRivetRevObjs( String aClassId, String aRivetId ) {
    throw new TsNullObjectErrorRtException();
  }

}
