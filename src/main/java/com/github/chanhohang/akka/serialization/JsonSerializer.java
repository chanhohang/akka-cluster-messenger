package com.github.chanhohang.akka.serialization;

import akka.serialization.SerializerWithStringManifest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.chanhohang.akka.AkkaClusterMessengerConstants;
import com.github.chanhohang.akka.AkkaClusterMessengerConstants.Message;
import com.github.chanhohang.akka.message.Acknowledgement;
import com.github.chanhohang.akka.message.AkkaEnvelope;
import com.github.chanhohang.akka.message.MessageEnvelopeImpl;
import com.github.chanhohang.akka.message.persist.AcknowledgementPersist;
import com.github.chanhohang.akka.message.persist.MessagePersist;
import com.github.chanhohang.akka.message.resend.GapFillMessage;
import com.github.chanhohang.akka.message.resend.ResendComplete;
import com.github.chanhohang.akka.message.resend.ResendRequest;
import com.github.chanhohang.akka.message.reset.ResetSequenceNumberRequest;

import java.io.IOException;

/**
 * Json Serializer for the MessageEnvelope.
 */
public class JsonSerializer extends SerializerWithStringManifest {

  private ObjectMapper mapper = new ObjectMapper();

  public JsonSerializer() {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Override
  public Object fromBinary(byte[] bytes, String manifest) {
    try {
      // @formatter:off
      switch (manifest) {
        case Message.MessageEnvelopeImpl: {
          return mapper.readValue(bytes, MessageEnvelopeImpl.class);
        }
        case Message.MessagePersist: {
          return mapper.readValue(bytes, MessagePersist.class);
        }
        case Message.Acknowledgement: {
          return mapper.readValue(bytes, Acknowledgement.class);
        }
        case Message.AcknowledgementPersist: {
          return mapper.readValue(bytes, AcknowledgementPersist.class);
        }
        case Message.ResendComplete: {
          return mapper.readValue(bytes, ResendComplete.class);
        }
        case Message.ResendRequest: {
          return mapper.readValue(bytes, ResendRequest.class);
        }
        case Message.ResetSequenceNumberRequest: {
          return mapper.readValue(bytes, ResetSequenceNumberRequest.class);
        }
        case Message.GapFillMessage: {
          return mapper.readValue(bytes, GapFillMessage.class);
        }
        default: {
          throw new IllegalArgumentException("Unknown manifest: " + manifest);
        }
      }
      // @formatter:on
    } catch (IOException exception) {
      throw new RuntimeException(exception.getMessage(), exception);
    }
  }

  @Override
  public int identifier() {
    return AkkaClusterMessengerConstants.Identifier;
  }

  @Override
  public String manifest(Object obj) {
    if (obj instanceof AkkaEnvelope) {
      return ((AkkaEnvelope) obj).getManifest();
    } else {
      throw new IllegalArgumentException("Unknown type: " + obj);
    }
  }

  @Override
  public byte[] toBinary(Object obj) {
    try {
      if (obj instanceof AkkaEnvelope) {
        return mapper.writeValueAsBytes(obj);
      } else {
        throw new IllegalArgumentException("Unknown type: " + obj);
      }
    } catch (JsonProcessingException exception) {
      throw new RuntimeException(exception.getMessage(), exception);
    }
  }

}
