package net.chh.akka.message;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import net.chh.akka.AkkaClusterMessengerConstants.Message;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Acknowledgement extends MessageEnvelopeImpl {

  private static final long serialVersionUID = 1L;

  @Override
  public String getManifest() {
    return Message.Acknowledgement;
  }
}
