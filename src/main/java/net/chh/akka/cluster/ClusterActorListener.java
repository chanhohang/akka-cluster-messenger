package net.chh.akka.cluster;

import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.ReachableMember;
import akka.cluster.ClusterEvent.UnreachableMember;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import net.chh.akka.AkkaClusterMessengerConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Component(AkkaClusterMessengerConstants.PersistentId.Cluster.Listener)
@Lazy
public class ClusterActorListener extends UntypedActor {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  private Cluster cluster = Cluster.get(getContext().system());

  @Autowired
  private IClusterStore clusterStore;

  // subscribe to cluster changes
  @Override
  public void preStart() {
    // #subscribe
    cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class,
        UnreachableMember.class);
    // #subscribe
    clusterStore.preStart(this);
  }

  @Override
  public void postStop() {
    cluster.unsubscribe(getSelf());
    clusterStore.postStop(this);
  }

  @Override
  public void onReceive(Object message) {

    if (message instanceof MemberEvent) {
      MemberEvent memberEvent = (MemberEvent) message;
      if (memberEvent.member().address().equals(cluster.selfAddress())) {
        log.info("Self Node Event {}", memberEvent);
        clusterStore.ownNodeEvent(memberEvent);
        return;
      }
    }

    if (message instanceof MemberUp) {
      MemberUp memberUp = (MemberUp) message;
      log.info("Member is Up: {}", memberUp.member());

      clusterStore.onMemberUp(memberUp);

    } else if (message instanceof UnreachableMember) {
      UnreachableMember unreachableMember = (UnreachableMember) message;
      log.info("Member detected as unreachable: {}", unreachableMember.member());
      clusterStore.onUnreachableMember(unreachableMember);

    } else if (message instanceof ReachableMember) {
      ReachableMember reachableMember = (ReachableMember) message;
      log.info("Member detected as reachable: {}", reachableMember);
      clusterStore.onReachableMember(reachableMember);

    } else if (message instanceof MemberRemoved) {
      MemberRemoved memberRemoved = (MemberRemoved) message;
      log.info("Member is Removed: {}", memberRemoved.member());
      clusterStore.onMemberRemoved(memberRemoved);

    } else if (message instanceof MemberEvent) {
      // ignore

    } else {
      unhandled(message);
    }

  }
}