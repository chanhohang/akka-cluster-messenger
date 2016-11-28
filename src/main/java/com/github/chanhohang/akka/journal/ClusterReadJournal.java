package com.github.chanhohang.akka.journal;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.persistence.query.EventEnvelope;
import akka.stream.javadsl.Source;

import com.github.chanhohang.akka.AkkaClusterMessengerConstants;

public interface ClusterReadJournal {

  /**
   * Generate {@link Source} from read Journal.
   * 
   * @param system
   *          see {@link ActorSystem}
   * @param persistenceId
   *          see {@link AkkaClusterMessengerConstants.PersistentId}
   * @param startSequnceNumber
   *          {@link Long}
   * @param endSequenceNumber
   *          {@link Long}
   * @return {@link Source} to iterate the Events stored in journal.
   */
  Source<EventEnvelope, NotUsed> currentEventsByPersistenceId(ActorSystem system,
      String persistenceId, long startSequnceNumber, long endSequenceNumber);

}
