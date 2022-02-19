package org.toxsoft.uskat.core.api.objserv;

import java.io.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
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
   * @return {@link IStringMap}&lt;{@link ISkidList}&gt; - the map "rivet ID" - "riveted right objects SKIDs list"
   */
  IStringMap<ISkidList> rivets();

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
   * Always returns {@link Skid#strid() skid().strid()}.
   *
   * @return String - the object strid
   */
  String strid();

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

  // TODO TRANSLATE

  // ------------------------------------------------------------------------------------
  //

  /**
   * Возвращает идентификатор единственного (или первого) объекта по связи.
   *
   * @param aLinkId String - идентификатор связи
   * @return {@link Skid} - идентификатор связанного объекта или <code>null</code>
   */
  Skid getSingleLinkSkid( String aLinkId );

  /**
   * Возвращает единственный (или первый) объект по связи.
   *
   * @param <T> - конкретный тип возвращаемого объекта
   * @param aLinkId String - идентификатор связи
   * @return {@link ISkObject} - связанный объект или <code>null</code>
   */
  <T extends ISkObject> T getSingleLink( String aLinkId );

  /**
   * Возвращает идентификаторы объектов по связи.
   *
   * @param aLinkId String - идентификатор связи
   * @return {@link ISkidList} - список идентификаторов объектов
   */
  ISkidList getLinkSkids( String aLinkId );

  /**
   * Возвращает объекты по связи.
   *
   * @param <T> - конкретный тип возвращаемых объектов
   * @param aLinkId String - идентификатор связи
   * @return {@link IList}&lt;{@link ISkObject}&gt; - список связанный объектов
   */
  <T extends ISkObject> IMap<Skid, T> getLink( String aLinkId );

  /**
   * Возвращает объекты по связи.
   *
   * @param <T> - конкретный тип возвращаемых объектов
   * @param aLinkId String - идентификатор связи
   * @return {@link IMap}&lt;{@link Skid},{@link ISkObject}&gt; - карта связанный объектов
   */
  <T extends ISkObject> IList<T> getLinkObjs( String aLinkId );

  /**
   * Возвращает идентификаторы объектов по обратной связи.
   *
   * @param aClassId String - идентификатор класса связи
   * @param aLinkId String - идентификатор связи
   * @return {@link ISkidList} - список идентификаторов объектов
   */
  ISkidList getLinkRevSkids( String aClassId, String aLinkId );

  /**
   * Возвращает объекты по обратной связи.
   *
   * @param <T> - конкретный тип возвращаемых объектов
   * @param aClassId String - идентификатор класса связи
   * @param aLinkId String - идентификатор связи
   * @return {@link ISkidList} - список идентификаторов объектов
   */
  <T extends ISkObject> IMap<Skid, T> getLinkRev( String aClassId, String aLinkId );

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
  public IStringMap<ISkidList> rivets() {
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
  public <T extends ISkObject> T getSingleLink( String aLinkId ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public ISkidList getLinkSkids( String aLinkId ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public <T extends ISkObject> IMap<Skid, T> getLink( String aLinkId ) {
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
  public <T extends ISkObject> IMap<Skid, T> getLinkRev( String aClassId, String aLinkId ) {
    throw new TsNullObjectErrorRtException();
  }

}
