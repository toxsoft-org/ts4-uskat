package org.toxsoft.uskat.core.msgapi;

import org.toxsoft.core.tslib.bricks.events.msg.*;

public interface ISkMessageApi {

  record Packet ( long queryId, GtMessage msg ) {
  }

  void requestToCore( Packet aRequest );

  interface IClientPush {

    void resposeFromCore( Packet aResponse );

  }

}
