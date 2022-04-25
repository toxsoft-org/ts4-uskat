package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.coll.derivative.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.*;

/**
 * Реализация {@link ISkFrontendRear}, развязывающий потоки выполнения бекенда и фронтенда.
 *
 * @author hazard157
 * @author mvk
 */
class SkFrontendRearThreadSeparatedWrapper
    implements ISkFrontendRear, ICooperativeMultiTaskable {

  /**
   * Max number of messages dispatcher p[er single call of {@link #doJob()}.
   */
  private static final int DOJOB_MESSAGE_COUNT = 16;

  /**
   * The queu of messages from backend to be sent to the frontend.
   */
  private IQueue<GtMessage> msgQueue = new SynchronizedQueueWrapper<>( new Queue<>() );

  /**
   * The wrapped frontend.
   */
  private final ISkFrontendRear frontend;

  /**
   * Constructor.
   *
   * @param aFrontend {@link ISkFrontendRear} - the wrapped frontend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  SkFrontendRearThreadSeparatedWrapper( ISkFrontendRear aFrontend ) {
    TsNullArgumentRtException.checkNull( aFrontend );
    frontend = aFrontend;
  }

  // ------------------------------------------------------------------------------------
  // ISkFrontendRear
  //

  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    msgQueue.putTail( aMessage );
  }

  // ------------------------------------------------------------------------------------
  // ICooperativeMultiTaskable
  //

  @Override
  public void doJob() {
    int count = 0;
    while( count++ < DOJOB_MESSAGE_COUNT ) {
      GtMessage msg = msgQueue.getHeadOrNull();
      if( msg == null ) {
        break;
      }
      frontend.onBackendMessage( msg );
    }
  }

}
