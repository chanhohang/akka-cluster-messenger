package net.chh.akka.message.reset;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import net.chh.akka.AkkaClusterMessengerConstants;
import net.chh.akka.message.MessageEnvelopeImpl;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ResetSequenceNumberRequest extends MessageEnvelopeImpl {
  private static final long serialVersionUID = 1L;

  @Override
  public String getManifest() {
    return AkkaClusterMessengerConstants.Message.ResetSequenceNumberRequest;
  }

}