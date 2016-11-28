package com.github.chanhohang.akka.actor;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorPath;
import akka.actor.ActorSelection;
import akka.japi.Function;
import akka.persistence.RecoveryCompleted;
import akka.persistence.UntypedPersistentActorWithAtLeastOnceDelivery;
import akka.persistence.jdbc.query.javadsl.JdbcReadJournal;
import akka.persistence.query.EventEnvelope;
import akka.persistence.query.PersistenceQuery;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Source;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.chanhohang.akka.AkkaClusterMessengerConstants;
import com.github.chanhohang.akka.cluster.IClusterStore;
import com.github.chanhohang.akka.message.Acknowledgement;
import com.github.chanhohang.akka.message.BaseMessageEnvelope;
import com.github.chanhohang.akka.message.MessageEnvelopeImpl;
import com.github.chanhohang.akka.message.persist.MessagePersist;
import com.github.chanhohang.akka.message.resend.GapFillMessage;
import com.github.chanhohang.akka.message.resend.ResendComplete;
import com.github.chanhohang.akka.message.resend.ResendRequest;
import com.github.chanhohang.akka.message.reset.ResetSequenceNumberRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Component(AkkaClusterMessengerConstants.PersistentId.Cluster.Sender)
@Lazy
public class ClusterMessengerSender extends UntypedPersistentActorWithAtLeastOnceDelivery {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private IClusterStore clusterStore;

  private ObjectMapper objectMapper = new ObjectMapper();

  private AtomicLong sequenceNumber = new AtomicLong();

  private Map<String, Boolean> resendMap = new HashMap<>();

  @Value("${actp.akka.role}")
  private String role;

  @Override
  public String persistenceId() {
    return role + "." + AkkaClusterMessengerConstants.PersistentId.Cluster.Sender;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void onReceiveCommand(Object message) throws Throwable {

    log.info("onReceiveCommand {}", message);

    try {
      if (message instanceof Acknowledgement) {
        Acknowledgement ack = (Acknowledgement) message;
        confirmDelivery(ack.getDeliveryId());

      } else if (message instanceof ResetSequenceNumberRequest) {

        handleResetSequenceNumber((ResetSequenceNumberRequest) message);

      } else if (message instanceof ResendRequest) {

        handleResendRequest((ResendRequest) message);

      } else if (message instanceof BaseMessageEnvelope) {

        handleGenericEvent((BaseMessageEnvelope) message);

      } else {
        unhandled(message);
      }
    } catch (Exception exception) {
      log.error("Unexpected Exception", exception);
    }
  }

  private void handleResetSequenceNumber(ResetSequenceNumberRequest message) {
    final long sequenceNumber = message.getSequenceNumber();
    final JdbcReadJournal readJournal = PersistenceQuery.get(getContext().system())
        .getReadJournalFor(JdbcReadJournal.class, JdbcReadJournal.Identifier());

    Source<EventEnvelope, NotUsed> events = readJournal
        .currentEventsByPersistenceId(persistenceId(), 0, Long.MAX_VALUE);

    AtomicLong eventsSeqNo = new AtomicLong();
    AtomicLong lastSequenceNumber = new AtomicLong();

    this.sequenceNumber.set(sequenceNumber);
    List<Long> deleteList = new ArrayList<>();

    Materializer materializer = ActorMaterializer.create(getContext().system());
    CompletionStage<Done> stage = events.runForeach(eventEnvelope -> {
      if (eventEnvelope.event() instanceof MessagePersist) {
        MessagePersist msgPersist = (MessagePersist) eventEnvelope.event();
        long seqNo = eventEnvelope.sequenceNr();
        lastSequenceNumber.set(seqNo);
        if (msgPersist.getSequenceNumber() >= sequenceNumber) {
          deleteList.add(seqNo);
          eventsSeqNo.set(seqNo);
        }
      }
    }, materializer);

    stage.thenRun(() -> {
      log.info("Delete Event Store {}, lastSequenceNumber {}", deleteList);

      for (long i : deleteList) {
        deleteMessages(i);
      }

      List<ActorSelection> destinations = clusterStore.getActor(null,
          AkkaClusterMessengerConstants.PersistentId.Cluster.Receiver);
      for (ActorSelection destination : destinations) {

        ClusterMessengerSender.this.deliver(destination, new Function<Long, Object>() {
          @Override
          public Object apply(Long deliveryId) throws Exception {
            ResetSequenceNumberRequest outMsg = new ResetSequenceNumberRequest();
            outMsg.setSequenceNumber(message.getSequenceNumber());
            outMsg.setDeliveryId(deliveryId);
            outMsg.setRole(getRole());
            log.info("sending ResetSequenceNumber to destination {}, Message={}", destination,
                outMsg);
            return outMsg;
          }
        });
      }
    });

  }

  private void handleResendRequest(ResendRequest message)
      throws InterruptedException, ExecutionException {
    String targetRole = message.getTargetRole();

    if (resendMap.get(targetRole) == null || !resendMap.get(targetRole).booleanValue()) {
      log.info("start handle resend. {}, role {}", message, message.getTargetRole());
      resendMap.put(targetRole, Boolean.TRUE);
      long startSequenceNumber = message.getStartSequenceNumber();

      final JdbcReadJournal readJournal = PersistenceQuery.get(getContext().system())
          .getReadJournalFor(JdbcReadJournal.class, JdbcReadJournal.Identifier());

      Source<EventEnvelope, NotUsed> events = readJournal
          .currentEventsByPersistenceId(persistenceId(), 0, Long.MAX_VALUE);

      Materializer materializer = ActorMaterializer.create(getContext().system());
      ActorPath destination = getSender().path();

      AtomicLong lastSequenceNubmer = new AtomicLong();

      CompletionStage<Done> stage = events.runForeach(eventEnvelope -> {
        if (eventEnvelope.event() instanceof MessagePersist) {
          MessagePersist msgPersist = (MessagePersist) eventEnvelope.event();
          log.info("Check Message {}", msgPersist);
          lastSequenceNubmer.set(msgPersist.getSequenceNumber());
          if (msgPersist.getSequenceNumber() >= startSequenceNumber) {
            if (isTarget(msgPersist, targetRole)) {
              ClusterMessengerSender.this.deliver(destination, new Function<Long, Object>() {
                @Override
                public Object apply(Long deliveryId) throws Exception {
                  MessageEnvelopeImpl out = send(deliveryId, msgPersist);
                  log.info("sending message to destination {}, Message={}", destination, out);
                  return out;
                }
              });
            } else {
              ClusterMessengerSender.this.deliver(destination, new Function<Long, Object>() {
                @Override
                public Object apply(Long deliveryId) throws Exception {
                  GapFillMessage out = new GapFillMessage();
                  out.setSequenceNumber(msgPersist.getSequenceNumber());
                  out.setDeliveryId(deliveryId);
                  out.setRole(getRole());
                  log.info("sending message to destination {}, Message={}", destination, out);
                  return out;
                }
              });
            }
          } else {
            log.info("Sequence Number {} is already sent.", msgPersist.getSequenceNumber());
          }
        } else {
          log.info("Unknown event {}", eventEnvelope);
        }

      }, materializer);

      stage.thenRun(() -> {
        ResendComplete resendComplete = new ResendComplete();
        resendComplete.setRole(getRole());
        resendComplete.setEndSequenceNumber(lastSequenceNubmer.get());
        deliver(destination, deliveryId -> {
          resendMap.put(targetRole, Boolean.FALSE);
          resendComplete.setDeliveryId(deliveryId);
          return resendComplete;
        });
      });

      // Cannot handle concurrent resend request.
      Done done = stage.toCompletableFuture().get();
      log.info("Resend Completed. {}", done);
    } else {
      log.info("resend in progress. {}, role {}", message, targetRole);
    }
  }

  @SuppressWarnings("rawtypes")
  private void handleGenericEvent(BaseMessageEnvelope message) {
    final MessagePersist msgPersist = new MessagePersist();
    msgPersist.setRole(getRole());
    msgPersist.setPayload(message);
    msgPersist.setPayloadClass(message.getClass());
    msgPersist.setSequenceNumber(sequenceNumber.incrementAndGet());

    final String targetRole = message.getTargetRole();
    if (targetRole != null) {
      msgPersist.setTargetRole(targetRole);
    }

    persist(msgPersist, evt -> {
      List<ActorSelection> destinations = clusterStore.getActor(targetRole,
          AkkaClusterMessengerConstants.PersistentId.Cluster.Receiver);

      for (ActorSelection destination : destinations) {

        ClusterMessengerSender.this.deliver(destination, new Function<Long, Object>() {
          @Override
          public Object apply(Long deliveryId) throws Exception {
            MessageEnvelopeImpl out = send(deliveryId, msgPersist);
            log.info("sending message to destination {}, Message={}", destination, out);
            return out;
          }
        });
      }
    });
  }

  @Override
  public void onReceiveRecover(Object event) throws Throwable {
    log.info("onReceiveRecover {}", event);
    if (event instanceof MessagePersist) {
      MessagePersist msg = (MessagePersist) event;
      sequenceNumber.set(msg.getSequenceNumber());
    } else if (event instanceof RecoveryCompleted) {
      log.info("sequenceNumber {}", sequenceNumber);
    }

  }

  private boolean isTarget(MessagePersist msgPersist, String targetRole) {
    if (msgPersist.getTargetRole() == null) {
      return true;
    }
    if (msgPersist.getTargetRole().equals(targetRole)) {
      return true;
    }
    return false;
  }

  private MessageEnvelopeImpl send(long deliveryId, MessagePersist msgPersist)
      throws JsonProcessingException {
    Object payload = msgPersist.getPayload();
    Class<?> payloadClass = msgPersist.getPayloadClass();
    String content = objectMapper.writeValueAsString(payload);
    String role = msgPersist.getRole();
    String targetRole = msgPersist.getTargetRole();

    MessageEnvelopeImpl outMsg = new MessageEnvelopeImpl(deliveryId);
    outMsg.setSequenceNumber(msgPersist.getSequenceNumber());
    outMsg.setPayloadClass(payloadClass);
    outMsg.setPayload(content);
    outMsg.setRole(role);
    outMsg.setTargetRole(targetRole);

    return outMsg;
  }

}
