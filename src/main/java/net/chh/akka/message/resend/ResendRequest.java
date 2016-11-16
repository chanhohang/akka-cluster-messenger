package net.chh.akka.message.resend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import net.chh.akka.AkkaClusterMessengerConstants;
import net.chh.akka.message.MessageEnvelopeImpl;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class ResendRequest extends MessageEnvelopeImpl {

  private static final long serialVersionUID = 1L;

  private long startSequenceNumber;

  private long endSequenceNumber;

  @Override
  public String getManifest() {
    return AkkaClusterMessengerConstants.Message.ResendRequest;
  }
}
