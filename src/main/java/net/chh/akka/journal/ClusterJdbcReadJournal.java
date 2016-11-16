package net.chh.akka.journal;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.persistence.jdbc.query.javadsl.JdbcReadJournal;
import akka.persistence.query.EventEnvelope;
import akka.persistence.query.PersistenceQuery;
import akka.stream.javadsl.Source;

public class ClusterJdbcReadJournal implements ClusterReadJournal {

  @Override
  public Source<EventEnvelope, NotUsed> currentEventsByPersistenceId(ActorSystem system,
      String persistenceId, long startSequnceNumber, long endSequenceNumber) {

    final JdbcReadJournal readJournal = PersistenceQuery.get(system)
        .getReadJournalFor(JdbcReadJournal.class, JdbcReadJournal.Identifier());

    return readJournal.currentEventsByPersistenceId(persistenceId, startSequnceNumber,
        endSequenceNumber);

  }
}
