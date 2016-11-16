package net.chh.akka.cluster;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.ReachableMember;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.cluster.Member;

import net.chh.akka.AkkaClusterMessengerConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Lazy
@Profile(AkkaClusterMessengerConstants.Profile)
public class ClusterStore implements IClusterStore {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  private ClusterActorListener actor;

  private Map<Member, List<ActorSelection>> clusterMessengerReceiver = new HashMap<>();

  @Override
  public void preStart(ClusterActorListener clusterActorListener) {
    this.actor = clusterActorListener;
  }

  @Override
  public void onMemberUp(MemberUp memberUp) {
    Member member = memberUp.member();
    registerActor(member, AkkaClusterMessengerConstants.PersistentId.Cluster.Receiver,
        clusterMessengerReceiver);
  }

  @Override
  public void onUnreachableMember(UnreachableMember unreacheableMember) {
    Member member = unreacheableMember.member();
    unRegisterActor(member, clusterMessengerReceiver);
  }

  @Override
  public void onReachableMember(ReachableMember reachableMember) {
    Member member = reachableMember.member();
    registerActor(member, AkkaClusterMessengerConstants.PersistentId.Cluster.Receiver,
        clusterMessengerReceiver);
  }

  @Override
  public void onMemberRemoved(MemberRemoved memberRemoved) {
    Member member = memberRemoved.member();
    unRegisterActor(member, clusterMessengerReceiver);
  }

  @Override
  public void postStop(ClusterActorListener clusterActorListener) {

    clusterMessengerReceiver.clear();
  }

  private void registerActor(Member member, String actorId,
      Map<Member, List<ActorSelection>> actorList) {
    log.info("Start registering actor from {}", member);
    ActorSelection actorSelection = actor.getContext()
        .actorSelection(member.address().toString() + "/user/" + actorId);
    log.info("Registering {} from {}, roles {}", actorSelection, member, member.roles());
    // Check the actor exists.
    Future<ActorRef> result = actorSelection.resolveOne(FiniteDuration.create(5, TimeUnit.SECONDS));
    if (result.value().get().orElse(null) != null) {
      List<ActorSelection> list = getList(member, actorList);
      list.add(actorSelection);
    }
  }

  private List<ActorSelection> getList(Member member, Map<Member, List<ActorSelection>> actorList) {
    List<ActorSelection> list = actorList.get(member);
    if (list == null) {
      list = new ArrayList<>();
      actorList.put(member, list);
    }
    return list;
  }

  private void unRegisterActor(Member member, Map<Member, List<ActorSelection>> actorList) {
    ActorSelection selectedActor = null;
    List<ActorSelection> list = getList(member, actorList);
    for (ActorSelection actor : list) {
      if (member.address().equals(actor.anchorPath().address())) {
        selectedActor = actor;
        break;
      }
    }

    if (selectedActor != null) {
      log.info("Start unregistering actor from {}, roles {} , actor {}", member, member.roles(),
          selectedActor);
      list.remove(selectedActor);
    }

  }

  @Override
  public List<ActorSelection> getActor(String role, String actorId) {

    List<ActorSelection> result;
    Stream<Entry<Member, List<ActorSelection>>> stream = clusterMessengerReceiver.entrySet()
        .stream();
    if (role != null) {
      stream = stream.filter(entry -> entry.getKey().getRoles().contains(role));
    }
    Stream<ActorSelection> listStream = stream.map(entry -> entry.getValue()).flatMap(List::stream);

    if (actorId != null) {
      listStream = listStream.filter(p -> p.pathString().contains(actorId));
    }
    result = listStream.collect(Collectors.toList());

    return result;
  }

  @Override
  public void ownNodeEvent(MemberEvent memberEvent) {
    log.info("Self Cluster Node Event {}", memberEvent);
  }

}
