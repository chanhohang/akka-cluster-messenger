package com.github.chanhohang.akka.message;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

public interface AkkaEnvelope extends Serializable {

  @JsonIgnore
  public abstract String getManifest();

}
