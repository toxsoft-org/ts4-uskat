package org.toxsoft.uskat.core.utils.virtdata;

import static org.toxsoft.uskat.core.utils.virtdata.ISkResources.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.coll.IMapEdit;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.GwidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.ISkCoreApi;
import org.toxsoft.uskat.core.api.rtdserv.*;

/**
 * An abstract implementation of a virtual data writer.
 *
 * @author mvk
 */
public abstract class AbstractVirtDataWriter
    implements ISkCurrDataChangeListener, ICloseable {

  private final ISkCoreApi                             coreApi;
  private final String                                 classId;
  private final String                                 objId;
  private final IMapEdit<Gwid, ISkReadCurrDataChannel> readChannels = new ElemMap<>();
  private final ISkWriteCurrDataChannel                writeChannel;

  /**
   * Constructor.
   *
   * @param aCoreApi {@link ISkCoreApi} connection API.
   * @param aClassId String ID ID of the object class of the written data.
   * @param aObjId {@link Skid} ID of the object of the written data.
   * @param aReadDataIds {@link IStringList} list of read data IDs.
   * @param aWriteDataId String ID of write data.
   * @throws TsNullArgumentRtException any argument = null.
   * @throws TsIllegalArgumentRtException object of another class.
   */
  protected AbstractVirtDataWriter( ISkCoreApi aCoreApi, String aClassId, Skid aObjId, IStringList aReadDataIds,
      String aWriteDataId ) {
    TsNullArgumentRtException.checkNulls( aCoreApi, aClassId, aObjId, aReadDataIds, aWriteDataId );
    TsIllegalArgumentRtException
        .checkFalse( aCoreApi.sysdescr().hierarchy().isAssignableFrom( aClassId, aObjId.classId() ) );
    coreApi = aCoreApi;
    classId = aClassId;
    objId = aObjId.strid();
    GwidList readGwids = new GwidList();
    for( String dataId : aReadDataIds ) {
      readGwids.add( Gwid.createRtdata( classId, objId, dataId ) );
    }
    ISkRtdataService rtdService = coreApi.rtdService();
    rtdService.eventer().addListener( this );
    GwidList writeDataIds = new GwidList();
    writeDataIds.add( Gwid.createRtdata( classId, objId, aWriteDataId ) );
    readChannels.putAll( rtdService.createReadCurrDataChannels( readGwids ) );
    writeChannel = rtdService.createWriteCurrDataChannels( writeDataIds ).values().first();
    for( Gwid gwid : readGwids ) {
      if( !readChannels.hasKey( gwid ) ) {
        throw new TsIllegalArgumentRtException( ERR_NOT_FOUND_READ_DATA, gwid );
      }
    }
    if( writeChannel == null ) {
      throw new TsIllegalArgumentRtException( ERR_NOT_FOUND_WRITE_DATA, writeDataIds.first() );
    }
    // Write current value
    writeValue();
  }

  // ------------------------------------------------------------------------------------
  // API for subclasses
  //
  /**
   * Read value.
   *
   * @param aDataId String ID of read data.
   * @return boolean value.
   * @throws TsNullArgumentRtException argument = null.
   * @throws TsItemNotFoundRtException read channel does not exist.
   */
  protected final boolean getBool( String aDataId ) {
    TsNullArgumentRtException.checkNull( aDataId );
    return getBool( Gwid.createRtdata( classId, objId, aDataId ) );
  }

  /**
   * Read value.
   *
   * @param aGwid {@link Gwid} ID of read data.
   * @return boolean value.
   * @throws TsNullArgumentRtException argument = null.
   * @throws TsItemNotFoundRtException read channel does not exist.
   */
  protected final boolean getBool( Gwid aGwid ) {
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
  protected final int getInt( String aDataId ) {
    TsNullArgumentRtException.checkNull( aDataId );
    return getInt( Gwid.createRtdata( classId, objId, aDataId ) );
  }

  /**
   * Read value.
   *
   * @param aGwid String ID of read data.
   * @return int value.
   * @throws TsNullArgumentRtException argument = null.
   * @throws TsItemNotFoundRtException read channel does not exist.
   */
  protected final int getInt( Gwid aGwid ) {
    TsNullArgumentRtException.checkNull( aGwid );
    return readChannels.getByKey( aGwid ).getValue().asInt();
  }

  // ------------------------------------------------------------------------------------
  // ISkCurrDataChangeListener
  //
  @Override
  public void onCurrData( IMap<Gwid, IAtomicValue> aNewValues ) {
    for( Gwid gwid : aNewValues.keys() ) {
      if( readChannels.hasKey( gwid ) ) {
        // The value on one (or more) channels has changed.
        writeChannel.setValue( writeValue() );
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
    writeChannel.close();
  }

  // ------------------------------------------------------------------------------------
  // To implement
  //
  /**
   * Create a value for write
   *
   * @return {@link IAtomicValue} value to write to channel
   */
  protected abstract int doWriteValue();

  // ------------------------------------------------------------------------------------
  // private methods
  //
  private IAtomicValue writeValue() {
    for( ISkReadCurrDataChannel channel : readChannels ) {
      if( !channel.getValue().isAssigned() ) {
        // One of the values ​​is not assigned
        return IAtomicValue.NULL;
      }
    }
    return AvUtils.avInt( doWriteValue() );
  }
}
