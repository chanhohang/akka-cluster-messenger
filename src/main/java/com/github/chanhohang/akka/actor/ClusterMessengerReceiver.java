package com.github.chanhohang.akka.actor;

import akka.Done;
import akka.NotUsed;
import akka.persistence.RecoveryCompleted;
import akka.persistence.UntypedPersistentActor;
import akka.persistence.query.EventEnvelope;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Source;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.chanhohang.akka.AkkaClusterMessengerConstants;
import com.github.chanhohang.akka.cluster.ClusterMessageRouter;
import com.github.chanhohang.akka.cluster.MessagePublishRouter;
import com.github.chanhohang.akka.journal.ClusterJdbcReadJournal;
import com.github.chanhohang.akka.journal.ClusterReadJournal;
import com.github.chanhohang.akka.message.Acknowledgement;
import com.github.chanhohang.akka.message.MessageEnvelopeImpl;
import com.github.chanhohang.akka.message.persist.AcknowledgementPersist;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Component(AkkaClusterMessengerConstants.PersistentId.Cluster.Receiver)
@Lazy
public class ClusterMessengerReceiver extends UntypedPersistentActor {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  private ObjectMapper mapper = new ObjectMapper();

  private Map<String, AtomicLong> sequenceNumberMap = new HashMap<>();

  @Value("${actp.akka.role}")
  private String role;

  @Autowired(required = false)
  private ClusterMessageRouter router;

  @Autowired(required = false)
  private ClusterReadJournal readJournal;

  private boolean resendTrigger = false;

  @PostConstruct
  private void postConstruct() {
    if (readJournal == null) {
      readJournal = new ClusterJdbcReadJournal();
    }
    if (router == null) {
      router = new MessagePublishRouter(context().system());
    }
  }

  @Override
  public String persistenceId() {
    return role + "." + AkkaClusterMessengerConstants.PersistentId.Cluster.Receiver;
  }

  @Override
  public void onReceiveCommand(Object message) throws Exception {

    try {
      log.info("onReceiveCommand {} from sender {}", message, getSender());

      if (message instanceof ResendComplete) {
        handleResendComplete((ResendComplete) message);
      } else if (message instanceof ResetSequenceNumberRequest) {
        handleResetSequenceNumber((ResetSequenceNumberRequest) message);
      } else if (message instanceof MessageEnvelopeImpl) {
        handleMessage((MessageEnvelopeImpl) message);
      } else {
        unhandled(message);
      }
    } catch (Exception exception) {
      log.error("Unexpected Exception.", exception);
      try {
        getSender().tell(exception, self());
      } catch (Exception replyException) {
        log.error("Unexpected replyException.", replyException);
      }
    }

  }

  private void handleMessage(MessageEnvelopeImpl msg)
      throws JsonParseException, JsonMappingException, IOException {

    sendAcknowledgement(msg, role);

    String role = msg.getRole();

    AtomicLong sequenceNumber = getSequenceNumber(role);
    if (msg.getSequenceNumber() <= sequenceNumber.get()) {
      // Message Already Processed.
      log.info("Sequence Number {} from {} is already processed.", sequenceNumber, role);
    } else if (msg.getSequenceNumber() > sequenceNumber.get() + 1) {
      // Gap Detected.
      if (resendTrigger == false) {
        resendTrigger = true;
        ResendRequest resendRequest = new ResendRequest(sequenceNumber.get() + 1,
            msg.getSequenceNumber());
        resendRequest.setTargetRole(getRole());
        log.info("start resend. {}", resendRequest);
        getSender().tell(resendRequest, getSelf());
      } else {
        log.info("resend from {} is in progressed.", role);
      }

    } else {
      // Normal Message.
      sequenceNumber.set(msg.getSequenceNumber());

      Object event = null;
      if (msg.getPayload() != null && msg.getPayloadClass() != null) {
        event = mapper.readValue(msg.getPayload(), msg.getPayloadClass());
      } else {
        log.info("Skip parsing unrecognized payloadClass {} and payload {}", msg.getPayloadClass(),
            msg.getPayload());
      }
      AcknowledgementPersist ackPersist = new AcknowledgementPersist();
      ackPersist.setRole(role);
      ackPersist.setSequenceNumber(msg.getSequenceNumber());
      ackPersist.setPayloadClass(msg.getPayloadClass());
      ackPersist.setPayload(event);
      ackPersist.setTargetRole(msg.getRole());

      persistAsync(ackPersist, evt -> {

        if (evt.getPayload() != null) {
          router.route(evt);
        }
      });
    }
  }

  private void sendAcknowledgement(MessageEnvelopeImpl msg, String role) {
    Acknowledgement ack = new Acknowledgement();
    ack.setDeliveryId(msg.getDeliveryId());
    ack.setTargetRole(role);
    ack.setRole(getRole());
    log.info("Sending Acknowledgement {} to sender {}", ack, getSender());
    getSender().tell(ack, getSelf());
  }

  private void handleResetSequenceNumber(ResetSequenceNumberRequest message) {
    long resetSequenceNumber = message.getSequenceNumber();
    AtomicLong currentSequenceNumber = getSequenceNumber(message.getRole());
    String role = message.getRole();

    List<Long> deleteList = new ArrayList<>();

    if (currentSequenceNumber.get() == resetSequenceNumber) {
      log.info("handleResetSequenceNumber already reset {}, role {}", resetSequenceNumber, role);
    } else {
      log.info("handleResetSequenceNumber for reset sequence number {}, role {}, "
          + "current sequence number {}", resetSequenceNumber, role, currentSequenceNumber);

      currentSequenceNumber.set(resetSequenceNumber);

      Source<EventEnvelope, NotUsed> events = readJournal
          .currentEventsByPersistenceId(getContext().system(), persistenceId(), 0, Long.MAX_VALUE);
      Materializer materializer = ActorMaterializer.create(getContext().system());

      CompletionStage<Done> stage = events.runForeach(eventEnvelope -> {
        if (eventEnvelope.event() instanceof AcknowledgementPersist) {
          AcknowledgementPersist msgPersist = (AcknowledgementPersist) eventEnvelope.event();
          if (message.getRole() == null || message.getRole().equals(msgPersist.getTargetRole())) {
            long seqNo = eventEnvelope.sequenceNr();
            if (msgPersist.getSequenceNumber() >= resetSequenceNumber) {
              deleteList.add(seqNo);
            }
          }
        }
      }, materializer);

      stage.thenRun(() -> {

        log.info("Delete Event Store Sequence Number {}", deleteList);

        for (long i : deleteList) {
          deleteMessages(i);
        }

      });

    }
    sendAcknowledgement(message, message.getRole());
  }

  private void handleResendComplete(ResendComplete message) {
    long endSequenceNumber = message.getEndSequenceNumber();
    AtomicLong sequenceNumber = getSequenceNumber(message.getRole());
    if (sequenceNumber.get() == endSequenceNumber) {
      log.info("handleResendComplete Complete for end sequence number {}, role {}",
          endSequenceNumber, message.getRole());
    } else {
      log.info("handleResendComplete incomplete and reset sequence number to {}, role {}, ",
          endSequenceNumber, message.getRole());
      sequenceNumber.set(endSequenceNumber);
    }
    resendTrigger = false;
    sendAcknowledgement(message, message.getRole());
  }

  private AtomicLong getSequenceNumber(String role) {
    AtomicLong sequenceNumber = sequenceNumberMap.get(role);
    if (sequenceNumber == null) {
      sequenceNumber = new AtomicLong();
      sequenceNumberMap.put(role, sequenceNumber);
    }
    return sequenceNumber;
  }

  @Override
  public void onReceiveRecover(Object event) {
    log.info("onReceiveRecover {}", event);
    if (event instanceof AcknowledgementPersist) {
      AcknowledgementPersist msg = (AcknowledgementPersist) event;
      String role = msg.getRole();
      AtomicLong sequenceNumber = getSequenceNumber(role);
      if (sequenceNumber.get() < msg.getSequenceNumber()) {
        sequenceNumber.set(msg.getSequenceNumber());
      }
    } else if (event instanceof RecoveryCompleted) {
      log.info("sequenceNumberMap {}", sequenceNumberMap);
    }
  }

}
