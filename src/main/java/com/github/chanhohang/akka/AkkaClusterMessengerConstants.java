package com.github.chanhohang.akka;

public interface AkkaClusterMessengerConstants {
  String Profile = "AkkaClusterMessenger";
  int Identifier = 689;

  interface PersistentId {
    public static final String SEPARATOR = ".";
    
    interface Cluster {
      String node = "cluster";
      String Receiver = node + SEPARATOR + "receiver";
      String Sender = node + SEPARATOR + "sender";
      String Listener = node + SEPARATOR + "listener";
    }
  }

  interface Message {
    String MessageEnvelopeImpl = "MessageEnvelopeImpl";
    String MessagePersist = "MessagePersist";
    String AcknowledgementPersist = "AcknowledgementPersist";
    String Acknowledgement = "Acknowledgement";
    String ResendRequest = "ResendRequest";
    String ResendComplete = "ResendComplete";
    String ResetSequenceNumberRequest = "ResetSequenceNumberRequest";
    String GapFillMessage = "GapFillMessage";
  }
}
