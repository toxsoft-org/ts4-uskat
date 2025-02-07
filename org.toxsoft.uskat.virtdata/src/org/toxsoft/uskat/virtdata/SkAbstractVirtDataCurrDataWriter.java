package org.toxsoft.uskat.virtdata;

import static org.toxsoft.uskat.virtdata.ISkResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.bricks.events.change.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.rtdserv.*;

/**
 * An abstract implementation of a virtual data writer.
 *
 * @author mvk
 */
public abstract class SkAbstractVirtDataCurrDataWriter
    implements IGenericChangeListener, ICloseable {

  private final ISkCoreApi              coreApi;
  private final ISkWriteCurrDataChannel writeChannel;
  private final ILogger                 logger;

  /**
   * Constructor.
   *
   * @param aCoreApi {@link ISkCoreApi} connection API.
   * @param aWriteDataId {@link Gwid} ID of write data.
   * @throws TsNullArgumentRtException any argument = null.
   * @throws TsIllegalArgumentRtException object of another class.
   */
  protected SkAbstractVirtDataCurrDataWriter( ISkCoreApi aCoreApi, Gwid aWriteDataId ) {
    this( aCoreApi, aWriteDataId, LoggerUtils.defaultLogger() );
  }

  /**
   * Constructor.
   *
   * @param aCoreApi {@link ISkCoreApi} connection API.
   * @param aWriteDataId {@link Gwid} ID of write data.
   * @throws TsNullArgumentRtException any argument = null.
   * @throws TsIllegalArgumentRtException object of another class.
   */
  protected SkAbstractVirtDataCurrDataWriter( ISkCoreApi aCoreApi, Gwid aWriteDataId, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aCoreApi, aWriteDataId, aLogger );
    coreApi = aCoreApi;
    ISkRtdataService rtdService = coreApi.rtdService();
    GwidList writeDataIds = new GwidList();
    writeDataIds.add( aWriteDataId );
    writeChannel = rtdService.createWriteCurrDataChannels( writeDataIds ).values().first();
    if( writeChannel == null ) {
      throw new TsIllegalArgumentRtException( ERR_NOT_FOUND_WRITE_DATA, writeDataIds.first() );
    }
    logger = aLogger;
  }

  // ------------------------------------------------------------------------------------
  // public API
  //
  /**
   * @return ID of write data
   */
  public final Gwid writeDataId() {
    return writeChannel.gwid();
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
   * Check for unassigned inputs.
   *
   * @param aUnassignedInputs {@link IGwidList} inputs to be checked.
   * @return <b>true</b> there is unassigned inputs; <b>false</b> there isn't unassigned inputs.
   */
  protected final boolean checkUnassignedInputs( IGwidList... aUnassignedInputs ) {
    GwidList unassignedInputs = new GwidList();
    for( IGwidList unassignedInput : aUnassignedInputs ) {
      unassignedInputs.addAll( unassignedInput );
    }
    if( unassignedInputs.size() > 0 ) {
      logger.warning( ERR_UNASSIGNED_INPUTS, writeDataId(), unassignedInputs );
      return true;
    }
    return false;
  }

  // ------------------------------------------------------------------------------------
  // IGenericChangeListener
  //
  @Override
  public final void onGenericChangeEvent( Object aSource ) {
    // The value on one (or more) channels has changed.
    writeChannel.setValue( doCalculateValue() );
  }

  // ------------------------------------------------------------------------------------
  // ICloseable
  //
  @Override
  public final void close() {
    doClose();
    writeChannel.close();
  }

  // ------------------------------------------------------------------------------------
  // To implement
  //
  /**
   * Calculate a value for write.
   *
   * @return {@link IAtomicValue} value to write to channel
   */
  protected abstract IAtomicValue doCalculateValue();

  /**
   * Close resources.
   */
  protected abstract void doClose();
}
