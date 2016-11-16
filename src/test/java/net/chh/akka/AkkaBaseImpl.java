package net.chh.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import net.chh.akka.spring.SpringAkkaBase;

import org.springframework.stereotype.Component;

@Component
public class AkkaBaseImpl extends SpringAkkaBase {

  private final ActorSystem actorSystem;

  public AkkaBaseImpl() {
    actorSystem = ActorSystem.create("akka");
  }

  @Override
  public ActorSystem getActorSystem() {
    return actorSystem;
  }

  @Override
  public ActorRef getActor(String actorId) {
    return getActor(actorSystem, actorId);
  }

  @Override
  public void initialize() {

  }

}
