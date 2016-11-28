package com.github.chanhohang.akka.journal;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.persistence.inmemory.query.journal.javadsl.InMemoryReadJournal;
import akka.persistence.query.EventEnvelope;
import akka.persistence.query.PersistenceQuery;
import akka.stream.javadsl.Source;

import com.github.chanhohang.akka.journal.ClusterReadJournal;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class TestReadJournal implements ClusterReadJournal {

  @Override
  public Source<EventEnvelope, NotUsed> currentEventsByPersistenceId(ActorSystem system,
      String persistenceId, long startSequnceNumber, long endSequenceNumber) {
    final InMemoryReadJournal readJournal = PersistenceQuery.get(system)
        .getReadJournalFor(InMemoryReadJournal.class, InMemoryReadJournal.Identifier());

    return readJournal.currentEventsByPersistenceId(persistenceId, startSequnceNumber,
        endSequenceNumber);
  }

}
