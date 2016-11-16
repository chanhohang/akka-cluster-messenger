package net.chh.akka.cluster;

import akka.actor.ActorSelection;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.ReachableMember;
import akka.cluster.ClusterEvent.UnreachableMember;

import java.util.List;

/**
 * Cluster Store implemented by the micro service in how to handle the cluster event.
 */
public interface IClusterStore {

  /**
   * please see {@link ClusterActorListener#preStart()}.
   * 
   * @param clusterActorListener
   *          see {@link ClusterActorListener} the caller.
   */
  void preStart(ClusterActorListener clusterActorListener);

  /**
   * please see {@link ClusterActorListener#onReceive(Object)}.
   * 
   * @param memberUp
   *          see {@link MemberUp} event from cluster.
   */
  void onMemberUp(MemberUp memberUp);

  /**
   * please see {@link ClusterActorListener#onReceive(Object)}.
   * 
   * @param unreacheableMember
   *          see {@link UnreachableMember} event from cluster.
   */
  void onUnreachableMember(UnreachableMember unreacheableMember);

  /**
   * please see {@link ClusterActorListener#onReceive(Object)}.
   * 
   * @param reachableMember
   *          see {@link ReachableMember} event from cluster.
   */
  void onReachableMember(ReachableMember reachableMember);

  /**
   * please see {@link ClusterActorListener#onReceive(Object)}.
   * 
   * @param memberRemoved
   *          see {@link MemberRemoved} event from cluster.
   */
  void onMemberRemoved(MemberRemoved memberRemoved);

  /**
   * please see {@link ClusterActorListener#postStop()}.
   * 
   * @param clusterActorListener
   *          see {@link ClusterActorListener} the caller.
   */
  void postStop(ClusterActorListener clusterActorListener);

  /**
   * Get Actor from this Cluster Store.
   * 
   * @param role
   *          specify which cluster role to lookup. When Null specified, getting the actorId across
   *          all roles.
   * @param actorId
   *          the Id of the actor.
   * @return {@link List} of {@link ActorSelection}.
   */
  List<ActorSelection> getActor(String role, String actorId);

  void ownNodeEvent(MemberEvent memberEvent);

}