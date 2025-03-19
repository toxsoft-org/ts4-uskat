package org.toxsoft.uskat.virtdata;

import static org.toxsoft.uskat.virtdata.ISkResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.bricks.events.change.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.rtdserv.*;

/**
 * Currdata reader for a virtual data.
 *
 * @author mvk
 */
public class SkVirtDataCurrDataReader
    implements ISkCurrDataChangeListener, ICloseable {

  private final ISkCoreApi                             coreApi;
  private final Skid                                   objId;
  private final IMapEdit<Gwid, ISkReadCurrDataChannel> readChannels = new ElemMap<>();
  private final IGenericChangeListener                 changeListener;

  /**
   * Constructor.
   *
   * @param aCoreApi {@link ISkCoreApi} connection API.
   * @param aObjId {@link Skid} ID of the object of the written data.
   * @param aReadDataIds {@link IStringList} list of read data IDs.
   * @param aChangeListener {@link IGenericChangeListener} input data change listener
   * @throws TsNullArgumentRtException any argument = null.
   * @throws TsIllegalArgumentRtException object of another class.
   */
  public SkVirtDataCurrDataReader( ISkCoreApi aCoreApi, Skid aObjId, IStringList aReadDataIds,
      IGenericChangeListener aChangeListener ) {
    TsNullArgumentRtException.checkNulls( aCoreApi, aObjId, aReadDataIds, aChangeListener );
    coreApi = aCoreApi;
    objId = aObjId;
    changeListener = aChangeListener;
    ISkRtdataService rtdService = coreApi.rtdService();
    rtdService.eventer().addListener( this );
    // Register read currdata
    GwidList readGwids = new GwidList();
    for( String dataId : aReadDataIds ) {
      readGwids.add( Gwid.createRtdata( objId.classId(), objId.strid(), dataId ) );
    }
    addReadData( readGwids );
  }

  // ------------------------------------------------------------------------------------
  // Public API
  //
  /**
   * Add read data
   *
   * @param aGwids {@link IStringList} list of read data IDs.
   * @throws TsNullArgumentRtException any argument = null
   */
  public final void addReadData( IGwidList aGwids ) {
    TsNullArgumentRtException.checkNull( aGwids );
    readChannels.putAll( coreApi.rtdService().createReadCurrDataChannels( aGwids ) );
    for( Gwid gwid : aGwids ) {
      if( !readChannels.hasKey( gwid ) ) {
        throw new TsIllegalArgumentRtException( ERR_NOT_FOUND_READ_DATA, gwid );
      }
    }
  }

  /**
   * Returns a list of parameters that have no value.
   *
   * @return {@link IGwidList} list identifiers of unassigned parameters.
   */
  public final IGwidList listUnassigned() {
    GwidList retValue = new GwidList();
    for( Gwid dataId : readChannels.keys() ) {
      if( !readChannels.getByKey( dataId ).getValue().isAssigned() ) {
        retValue.add( dataId );
      }
    }
    return retValue;
  }

  /**
   * Read value.
   *
   * @param aDataId String ID of read data.
   * @return boolean value.
   * @throws TsNullArgumentRtException argument = null.
   * @throws TsItemNotFoundRtException read channel does not exist.
   */
  public final boolean getBool( String aDataId ) {
    TsNullArgumentRtException.checkNull( aDataId );
    return getBool( Gwid.createRtdata( objId.classId(), objId.strid(), aDataId ) );
  }

  /**
   * Read value.
   *
   * @param aGwid {@link Gwid} ID of read data.
   * @return boolean value.
   * @throws TsNullArgumentRtException argument = null.
   * @throws TsItemNotFoundRtException read channel does not exist.
   */
  public final boolean getBool( Gwid aGwid ) {
    TsNullArgumentRtException.checkNull( aGwid );
    return readChannels.getByKey( aGwid ).getValue().asBool();
  }

  /**
   * Read value.
   *
   * @param aDataId String ID of read data.
   * @return int value.
   * @throws TsNullArgumentRtException argument = null.
   * @throws TsItemNotFoundRtException read channel does not exist.
   */
  public final int getInt( String aDataId ) {
    TsNullArgumentRtException.checkNull( aDataId );
    return getInt( Gwid.createRtdata( objId.classId(), objId.strid(), aDataId ) );
  }

  /**
   * Read value.
   *
   * @param aGwid String ID of read data.
   * @return int value.
   * @throws TsNullArgumentRtException argument = null.
   * @throws TsItemNotFoundRtException read channel does not exist.
   */
  public final int getInt( Gwid aGwid ) {
    TsNullArgumentRtException.checkNull( aGwid );
    return readChannels.getByKey( aGwid ).getValue().asInt();
  }

  /**
   * Read value.
   *
   * @param aDataId String ID of read data.
   * @return float value.
   * @throws TsNullArgumentRtException argument = null.
   * @throws TsItemNotFoundRtException read channel does not exist.
   */
  public final float getFloat( String aDataId ) {
    TsNullArgumentRtException.checkNull( aDataId );
    return getFloat( Gwid.createRtdata( objId.classId(), objId.strid(), aDataId ) );
  }

  /**
   * Read value.
   *
   * @param aGwid String ID of read data.
   * @return float value.
   * @throws TsNullArgumentRtException argument = null.
   * @throws TsItemNotFoundRtException read channel does not exist.
   */
  public final float getFloat( Gwid aGwid ) {
    TsNullArgumentRtException.checkNull( aGwid );
    return readChannels.getByKey( aGwid ).getValue().asFloat();
  }

  /**
   * Read value.
   *
   * @param aDataId String ID of read data.
   * @return String value.
   * @throws TsNullArgumentRtException argument = null.
   * @throws TsItemNotFoundRtException read channel does not exist.
   */
  public final String getStr( String aDataId ) {
    TsNullArgumentRtException.checkNull( aDataId );
    return getStr( Gwid.createRtdata( objId.classId(), objId.strid(), aDataId ) );
  }

  /**
   * Read value.
   *
   * @param aGwid String ID of read data.
   * @return String value.
   * @throws TsNullArgumentRtException argument = null.
   * @throws TsItemNotFoundRtException read channel does not exist.
   */
  public final String getStr( Gwid aGwid ) {
    TsNullArgumentRtException.checkNull( aGwid );
    return readChannels.getByKey( aGwid ).getValue().asString();
  }

  /**
   * Read value.
   *
   * @param aDataId String ID of read data.
   * @return T value.
   * @throws TsNullArgumentRtException argument = null.
   * @throws TsItemNotFoundRtException read channel does not exist.
   */
  public final <T> T getValobj( String aDataId ) {
    TsNullArgumentRtException.checkNull( aDataId );
    return getValobj( Gwid.createRtdata( objId.classId(), objId.strid(), aDataId ) );
  }

  /**
   * Read value.
   *
   * @param aGwid String ID of read data.
   * @return T value.
   * @throws TsNullArgumentRtException argument = null.
   * @throws TsItemNotFoundRtException read channel does not exist.
   */
  public final <T> T getValobj( Gwid aGwid ) {
    TsNullArgumentRtException.checkNull( aGwid );
    return readChannels.getByKey( aGwid ).getValue().asValobj();
  }

  /**
   * Read value.
   *
   * @param aDataId String ID of read data.
   * @return {@link IAtomicValue} value.
   * @throws TsNullArgumentRtException argument = null.
   * @throws TsItemNotFoundRtException read channel does not exist.
   */
  public final IAtomicValue get( String aDataId ) {
    TsNullArgumentRtException.checkNull( aDataId );
    return get( Gwid.createRtdata( objId.classId(), objId.strid(), aDataId ) );
  }

  /**
   * Read value.
   *
   * @param aGwid String ID of read data.
   * @return {@link IAtomicValue} value.
   * @throws TsNullArgumentRtException argument = null.
   * @throws TsItemNotFoundRtException read channel does not exist.
   */
  public final IAtomicValue get( Gwid aGwid ) {
    TsNullArgumentRtException.checkNull( aGwid );
    return readChannels.getByKey( aGwid ).getValue();
  }

  // ------------------------------------------------------------------------------------
  // ISkCurrDataChangeListener
  //
  @Override
  public void onCurrData( IMap<Gwid, IAtomicValue> aNewValues ) {
    for( Gwid gwid : aNewValues.keys() ) {
      if( readChannels.hasKey( gwid ) ) {
        changeListener.onGenericChangeEvent( this );
        return;
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // ICloseable
  //
  @Override
  public void close() {
    coreApi.rtdService().eventer().removeListener( this );
    for( ISkReadCurrDataChannel channel : readChannels ) {
      channel.close();
    }
    readChannels.clear();
  }

  // ------------------------------------------------------------------------------------
  // API for subclasses
  //
  /**
   * @return {@link ISkCoreApi}
   */
  protected final ISkCoreApi coreApi() {
    return coreApi;
  }

  /**
   * @return {@link Skid} objId
   */
  protected final Skid objId() {
    return objId;
  }
}
