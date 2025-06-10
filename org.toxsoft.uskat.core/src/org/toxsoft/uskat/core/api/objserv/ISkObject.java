package org.toxsoft.uskat.core.api.objserv;

import java.io.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Green world (application domain) object.
 * <p>
 * Stridable identifier {@link #id()} returns object {@link #strid()}.
 * <p>
 * Notes:
 * <ul>
 * <li>any user implementation of {@link ISkObject} must be subclassed from {@link SkObject};</li>
 * <li>method {@link SkObject#equals(Object)} is <code>final</code> and checks if two instances point to the same object
 * in Green World.</li>
 * </ul>
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
   * <p>
   * The map always contains all rivets from {@link ISkClassInfo#rivets()}. All lists in map always contains
   * {@link IDtoRivetInfo#count()} items.
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
   * Returns the core API of connection to the objects source.
   *
   * @return {@link ISkCoreApi} - USkat core API
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
   * Returns SKIDs of the rivet.
   *
   * @param aRivetId String - the rivet ID
   * @return {@rivet ISkidList} - riveted object SKIDs
   * @throws TsItemNotFoundRtException no such rivet or object exists
   */
  default ISkidList getMultiRivetSkids( String aRivetId ) {
    return rivets().map().getByKey( aRivetId );
  }

  /**
   * Returns objects of the rivet.
   *
   * @param <T> - expected type of the object
   * @param aRivetId String - the rivet ID
   * @return {@rivet ISkObject} - riveted objects
   * @throws TsItemNotFoundRtException no such rivet or object exists
   */
  @SuppressWarnings( { "unchecked", "rawtypes" } )
  default <T extends ISkObject> IList<T> getMultiRivets( String aRivetId ) {
    return (IList)coreApi().objService().getObjs( getMultiRivetSkids( aRivetId ) );
  }

  /**
   * Returns SKID of single rivet (rivet with max 1 object riveted).
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
   * Returns object of single rivet (rivet with max 1 object riveted).
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
   * @param <T> - expected type of the objects
   * @param aClassId String - rivet class ID
   * @param aRivetId String - rivet ID
   * @return {@rivet IList}&lt;T&gt; - list of objects
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  <T extends ISkObject> IList<T> getRivetRevObjs( String aClassId, String aRivetId );

  // ------------------------------------------------------------------------------------
  // CLOBs

  /**
   * Returns the CLOB value.
   * <p>
   * If CLOB was never written or is an empty String then returns <code>aDefaultValue</code>.
   *
   * @param aClobId String - the ClOB ID
   * @param aDefaultValue String - default value may be <code>null</code>
   * @return String - the CLOB value or <code>aDefaultValue</code> (hence may be <code>null</code>)
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such CLOB is declared
   * @throws TsIllegalStateRtException CLOB is too big for reading
   */
  String getClob( String aClobId, String aDefaultValue );

  /**
   * Returns the CLOB value.
   * <p>
   * Simple calls {@link #getClob(String, String)} with an empty string as <code>aDefaultValue</code>.
   *
   * @param aClobId String - the ClOB ID
   * @return String - the CLOB value or an empty String
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such CLOB is declared
   * @throws TsIllegalStateRtException CLOB is too big for reading
   */
  default String getClob( String aClobId ) {
    return getClob( aClobId, TsLibUtils.EMPTY_STRING );
  }

  // ------------------------------------------------------------------------------------
  // RTdata

  /**
   * Returns current value of the RTdat if respective channel is open.
   * <p>
   * This method uses an open {@link ISkReadCurrDataChannel} to read the value. If channel is not open, simply returns
   * <code>null</code>.
   * <p>
   * This method is intended to be used when someone else opens/closes the channel.
   *
   * @param aRtdataId String - the ID of the RTdata
   * @return {@link IAtomicValue} - current value or <code>null</code>
   */
  IAtomicValue readRtdataIfOpen( String aRtdataId );

  /**
   * Sets current value of RTdata if write channel is open.
   * <p>
   * This method uses an open {@link ISkWriteCurrDataChannel} to read the value. If channel is not open, simply returns
   * <code>null</code>.
   * <p>
   * This method is intended to be used when someone else opens/closes the channel.
   *
   * @param aRtdataId String - the ID of the RTdata
   * @param aValue {@link IAtomicValue} - current value
   * @return boolean - the sign that channel was open and value written
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  boolean writeRtdataIfOpen( String aRtdataId, IAtomicValue aValue );

  // ------------------------------------------------------------------------------------
  // Links

  /**
   * Returns SKID of single link (link with max 1 object linked).
   * <p>
   * If link is empty or there are more than 1 linked object, method returns <code>null</code>.
   *
   * @param aLinkId String - the link ID
   * @return {@link Skid} - single linked object SKID or <code>null</code>
   * @throws TsItemNotFoundRtException no such link or object exists
   */
  Skid getSingleLinkSkid( String aLinkId );

  /**
   * Returns object of single link (link with max 1 object linked).
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
   * @param <T> - expected type of the linked objects
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
  public IStringMap<IMappedSkids> rivetRevs() {
    return IStringMap.EMPTY;
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

  @Override
  public String getClob( String aClobId, String aDefaultValue ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public IAtomicValue readRtdataIfOpen( String aRtdataId ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public boolean writeRtdataIfOpen( String aRtdataId, IAtomicValue aValue ) {
    throw new TsNullObjectErrorRtException();
  }

}
