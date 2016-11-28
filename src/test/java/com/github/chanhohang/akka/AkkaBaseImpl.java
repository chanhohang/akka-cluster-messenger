package com.github.chanhohang.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import com.github.chanhohang.akka.AkkaClusterMessengerConstants.PersistentId;
import com.github.chanhohang.akka.spring.SpringAkkaBase;

import org.springframework.stereotype.Component;

@Component
public class AkkaBaseImpl extends SpringAkkaBase {

  private final ActorSystem actorSystem;

  private ActorRef clusterListener;
  private ActorRef clusterMessagingReceiver;

  public AkkaBaseImpl() {
    actorSystem = ActorSystem.create("akka");
  }

  @Override
  public ActorSystem getActorSystem() {
    return actorSystem;
  }

  @Override
  public ActorRef getActor(String actorId) {
    // @formatter:off
    switch (actorId) {
      case PersistentId.Cluster.Listener:
        return clusterListener;
      case PersistentId.Cluster.Receiver:
        return clusterMessagingReceiver;
      default:
        return getActor(actorSystem, actorId);    
    }
    // @formatter:on

  }

  @Override
  public void initialize() {

    clusterListener = getActor(actorSystem, PersistentId.Cluster.Listener);
    clusterMessagingReceiver = getActor(actorSystem, PersistentId.Cluster.Receiver);

  }

}
