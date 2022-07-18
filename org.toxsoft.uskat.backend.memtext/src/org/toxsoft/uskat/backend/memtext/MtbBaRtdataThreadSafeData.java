package org.toxsoft.uskat.backend.memtext;

import java.util.concurrent.locks.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.temporal.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.uskat.core.backend.api.*;

/**
 * Thread-safe data holder for {@link MtbBaRtdata}.
 * <p>
 * Data holder is accessed from frontend from it's own thread and from internal thread of RTdata addon. Internal thread
 * does the following:
 * <ul>
 * <li>periodically generates {@link BaMsgRtdataCurrData} message to frontend;</li>
 * <li>periodically removes from memory an outdated history data;</li>
 * </ul>
 *
 * @author hazard157
 */
class MtbBaRtdataThreadSafeData {

  private final long earliestTimeToStore = 0L;

  private final ReentrantReadWriteLock       cdLock       = new ReentrantReadWriteLock();
  private final GwidList                     cdReadGwids  = new GwidList();
  private final GwidList                     cdWriteGwids = new GwidList();
  private final IMapEdit<Gwid, IAtomicValue> cdMap        = new ElemMap<>();
  private final IMapEdit<Gwid, IAtomicValue> newValues    = new ElemMap<>();

  private final ReentrantReadWriteLock                          hdLock = new ReentrantReadWriteLock();
  private final IMapEdit<Gwid, TimedList<ITemporalAtomicValue>> hdMap  = new ElemMap<>();

  public MtbBaRtdataThreadSafeData( long aHistrolyDepthMsecs ) {
    // TODO Auto-generated constructor stub
  }

}
