package net.chh.akka.message.persist;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import net.chh.akka.AkkaClusterMessengerConstants;
import net.chh.akka.message.BaseMessageEnvelope;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AcknowledgementPersist extends BaseMessageEnvelope<Object> {

  private static final long serialVersionUID = 1L;

  @Override
  public String getManifest() {
    return AkkaClusterMessengerConstants.Message.AcknowledgementPersist;
  }

}
