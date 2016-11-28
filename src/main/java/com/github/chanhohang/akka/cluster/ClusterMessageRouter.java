package com.github.chanhohang.akka.cluster;

import com.github.chanhohang.akka.actor.ClusterMessengerReceiver;
import com.github.chanhohang.akka.message.persist.AcknowledgementPersist;

/**
 * Route message interface when message receive in {@link ClusterMessengerReceiver}.
 */
public interface ClusterMessageRouter {

  /**
   * Route {@link AcknowledgementPersist} payload to the designated target.
   * 
   * @param event
   *          see {@link AcknowledgementPersist}
   */
  void route(AcknowledgementPersist event);
}
