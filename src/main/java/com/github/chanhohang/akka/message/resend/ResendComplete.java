package com.github.chanhohang.akka.message.resend;

import com.github.chanhohang.akka.AkkaClusterMessengerConstants;
import com.github.chanhohang.akka.message.MessageEnvelopeImpl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class ResendComplete extends MessageEnvelopeImpl {

  private static final long serialVersionUID = 1L;

  private long endSequenceNumber;

  @Override
  public String getManifest() {
    return AkkaClusterMessengerConstants.Message.ResendComplete;
  }
}
