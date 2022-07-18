package org.toxsoft.uskat.backend.memtext;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.temporal.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;

/**
 * {@link IBaRtdata} implementation.
 *
 * @author hazard157
 */
public class MtbBaRtdata
    extends MtbAbstractAddon
    implements IBaRtdata {

  /**
   * TODOs for addon implementation:
   * <ol>
   * <li>send current data update messages to frontend;</li>
   * <li>once in hour/day/??? check stored history depth and remove old data;</li>
   * </ol>
   */

  int foo;

  // FIXME usage? config option? min/max values, etc...
  private final long earliestTimeToStore = 0L;

  private final GwidList                     cdReadGwids  = new GwidList();
  private final GwidList                     cdWriteGwids = new GwidList();
  private final IMapEdit<Gwid, IAtomicValue> cdMap        = new ElemMap<>();
  private final IMapEdit<Gwid, IAtomicValue> newValues    = new ElemMap<>();

  private final IMapEdit<Gwid, TimedList<ITemporalAtomicValue>> hdMap = new ElemMap<>();

  /**
   * Constructor.
   *
   * @param aOwner {@link MtbAbstractBackend} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public MtbBaRtdata( MtbAbstractBackend aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_RTDATA );
  }

  // ------------------------------------------------------------------------------------
  // MtbAbstractAddon
  //

  @Override
  public void close() {
    // TODO close channels ???
  }

  @Override
  public void clear() {
    // nop
  }

  @Override
  protected void doWrite( IStrioWriter aSw ) {

    // TODO Auto-generated method stub

  }

  @Override
  protected void doRead( IStrioReader aSr ) {

    // TODO Auto-generated method stub

  }

  // ------------------------------------------------------------------------------------
  // Package API
  //

  @Override
  void papiRemoveEntitiesOfClassIdsBeforeSave( IStringList aClassIds ) {
    IListEdit<Gwid> toRemove = new ElemArrayList<>();
    for( Gwid g : hdMap.keys() ) {
      if( aClassIds.hasElem( g.classId() ) ) {
        toRemove.add( g );
      }
    }
    for( Gwid g : toRemove ) {
      hdMap.removeByKey( g );
    }
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private static boolean isRtdataGwidValid( Gwid aGwid ) {
    if( aGwid.isAbstract() ) {
      return false;
    }
    if( aGwid.isMulti() ) {
      return false;
    }
    if( aGwid.kind() != EGwidKind.GW_RTDATA ) {
      return false;
    }
    return true;
  }

  /**
   * Configures {@link #cdMap} to contain union of {@link #cdReadGwids} and {@link #cdWriteGwids}.
   */
  private void internalConfigureCurrDataReadWrite() {
    // add new GWIDs ro read
    for( Gwid g : cdReadGwids ) {
      if( isRtdataGwidValid( g ) ) {
        if( !cdMap.hasKey( g ) ) {
          cdMap.put( g, IAtomicValue.NULL );
        }
      }
    }
    // add new GWIDs ro write
    for( Gwid g : cdWriteGwids ) {
      if( isRtdataGwidValid( g ) ) {
        if( !cdMap.hasKey( g ) ) {
          cdMap.put( g, IAtomicValue.NULL );
        }
      }
    }
    // remove outdated GWIDs
    IListEdit<Gwid> toRemove = new ElemArrayList<>();
    for( Gwid g : cdMap.keys() ) {
      if( !cdReadGwids.hasElem( g ) && !cdWriteGwids.hasElem( g ) ) {
        toRemove.add( g );
      }
    }
    for( Gwid g : toRemove ) {
      cdMap.removeByKey( g );
    }
  }

  /**
   * Writes to destination list historical values.
   * <p>
   * Note on implementation:
   * <ul>
   * <li>ignores values out of interval {@link #earliestTimeToStore} .. now;</li>
   * <li>replaces values with the same timestamp by new one;</li>
   * <li>if source contains duplicate timestamps only last value will be stored.</li>
   * </ul>
   *
   * @param aDest {@link TimedList}&lt;{@link ITemporalAtomicValue}&gt; - destination list
   * @param aSource {@link ITimedList}&lt;{@link ITemporalAtomicValue}&gt; - source wof history data values
   */
  private void internalUpdateHistory( TimedList<ITemporalAtomicValue> aDest,
      ITimedList<ITemporalAtomicValue> aSource ) {
    long now = System.currentTimeMillis();
    for( ITemporalAtomicValue e : aSource ) {
      if( e.timestamp() >= earliestTimeToStore && e.timestamp() <= now ) {
        aDest.replaceByTimestamp( e );
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // IBaRtdata
  //

  @Override
  public void configureCurrDataReader( IList<Gwid> aRtdGwids ) {
    cdReadGwids.setAll( aRtdGwids );
    internalConfigureCurrDataReadWrite();
  }

  @Override
  public void configureCurrDataWriter( IList<Gwid> aRtdGwids ) {
    cdWriteGwids.setAll( aRtdGwids );
    internalConfigureCurrDataReadWrite();
  }

  @Override
  public void writeCurrData( Gwid aGwid, IAtomicValue aValue ) {
    if( !cdMap.hasElem( aValue ) ) {
      return;
    }
    cdMap.put( aGwid, aValue );
    newValues.put( aGwid, aValue );
    // TODO when to send meassage to frontend?
  }

  @Override
  public void writeHistData( Gwid aGwid, ITimeInterval aInterval, ITimedList<ITemporalAtomicValue> aValues ) {
    if( !isRtdataGwidValid( aGwid ) ) {
      return;
    }
    TimedList<ITemporalAtomicValue> ll = hdMap.findByKey( aGwid );
    if( ll == null ) {
      ll = new TimedList<>();
      hdMap.put( aGwid, ll );
    }
    internalUpdateHistory( ll, aValues );
  }

  @Override
  public ITimedList<ITemporalAtomicValue> queryObjRtdata( IQueryInterval aInterval, Gwid aGwid ) {
    ITimedList<ITemporalAtomicValue> ll = hdMap.findByKey( aGwid );
    if( ll == null ) {
      return new TimedList<>(); // TODO we need ITimedList.EMPTY !
    }
    return ll.selectInterval( aInterval );
  }

}
