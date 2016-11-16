package net.chh.akka.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import net.chh.akka.AkkaClusterMessengerConstants;

import javax.annotation.concurrent.NotThreadSafe;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@NotThreadSafe
@ToString(callSuper = true)
@Builder
public class MessageEnvelopeImpl extends BaseMessageEnvelope<String> {

  private static final long serialVersionUID = 1L;

  private long deliveryId;

  @Override
  public String getManifest() {
    return AkkaClusterMessengerConstants.Message.MessageEnvelopeImpl;
  }
}
