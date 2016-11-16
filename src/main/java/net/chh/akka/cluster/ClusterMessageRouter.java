package net.chh.akka.cluster;

import net.chh.akka.actor.ClusterMessengerReceiver;
import net.chh.akka.message.persist.AcknowledgementPersist;

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
