package net.chh.akka.spring;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import net.chh.akka.AkkaClusterMessengerConstants;

public abstract class SpringAkkaBase {

  /**
   * return the Akka Actor System.
   * 
   * @return see {@link ActorSystem}
   */
  public abstract ActorSystem getActorSystem();

  public abstract ActorRef getActor(String actorId);

  /**
   * Retrieve Actor for processing.
   * 
   * @param system
   *          see {@link ActorSystem}
   * @param actorId
   *          String representation of Actor Id. Please refer to
   *          {@link AkkaClusterMessengerConstants}
   * @return see {@link ActorRef}
   */
  protected ActorRef getActor(ActorSystem system, String actorId) {
    Props props = SpringExtensions.springExtProvider.get(system).props(actorId);
    return system.actorOf(props, actorId);

  }

  /**
   * Initialize Persistent Actor instance.
   */
  public abstract void initialize();
}
