package net.chh.akka.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public abstract class BaseMessageEnvelope<T> implements AkkaEnvelope {

  private static final long serialVersionUID = 1L;

  private long sequenceNumber;

  private Class<?> payloadClass;

  private T payload;

  private String role;

  private String targetRole;

}
