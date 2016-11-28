package com.github.chanhohang.akka.message;

import com.github.chanhohang.akka.AkkaClusterMessengerConstants.Message;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Acknowledgement extends MessageEnvelopeImpl {

  private static final long serialVersionUID = 1L;

  @Override
  public String getManifest() {
    return Message.Acknowledgement;
  }
}
