package com.github.chanhohang.akka.message.reset;

import com.github.chanhohang.akka.AkkaClusterMessengerConstants;
import com.github.chanhohang.akka.message.MessageEnvelopeImpl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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