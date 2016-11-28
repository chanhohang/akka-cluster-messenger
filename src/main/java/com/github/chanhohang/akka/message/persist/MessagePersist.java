package com.github.chanhohang.akka.message.persist;

import com.github.chanhohang.akka.AkkaClusterMessengerConstants;
import com.github.chanhohang.akka.message.BaseMessageEnvelope;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MessagePersist extends BaseMessageEnvelope<Object> {

  private static final long serialVersionUID = 1L;

  @Override
  public String getManifest() {
    return AkkaClusterMessengerConstants.Message.MessagePersist;
  }
}
