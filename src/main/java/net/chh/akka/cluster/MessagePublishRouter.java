package net.chh.akka.cluster;

import akka.actor.ActorSystem;

import net.chh.akka.message.persist.AcknowledgementPersist;

public class MessagePublishRouter implements ClusterMessageRouter {

  private final ActorSystem system;

  public MessagePublishRouter(ActorSystem system) {
    this.system = system;
  }

  @Override
  public void route(AcknowledgementPersist event) {
    system.eventStream().publish(event.getPayload());
  }

}
