package net.chh.akka.serialization;

import akka.serialization.SerializerWithStringManifest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.chh.akka.AkkaClusterMessengerConstants;
import net.chh.akka.AkkaClusterMessengerConstants.Message;
import net.chh.akka.message.AkkaEnvelope;
import net.chh.akka.message.MessageEnvelopeImpl;
import net.chh.akka.message.persist.MessagePersist;

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
