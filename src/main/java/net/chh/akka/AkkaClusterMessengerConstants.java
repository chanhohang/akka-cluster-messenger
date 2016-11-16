package net.chh.akka;

public interface AkkaClusterMessengerConstants {
  String Profile = "AkkaClusterMessenger";
  int Identifier = 689;
  
  interface PersistentId {
    interface Cluster {
      String node = "cluster";
      String Receiver = node + "." + "receiver";
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
  }
}
