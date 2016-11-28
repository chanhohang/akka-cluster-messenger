package com.github.chanhohang.akka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import akka.actor.ActorRef;

import com.github.chanhohang.akka.AkkaClusterMessengerConstants;
import com.github.chanhohang.akka.AkkaClusterMessengerConstants.PersistentId;
import com.github.chanhohang.akka.message.MessageEnvelopeImpl;
import com.github.chanhohang.akka.spring.SpringAkkaBase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = { "classpath:test-db-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(value = { AkkaClusterMessengerConstants.Profile, "test" })
public class AkkaBaseTest {

  @Autowired
  private SpringAkkaBase akkaBase;

  @Test
  public void test() throws InterruptedException {
    assertThat(akkaBase).isNotNull();
    ActorRef receiver = akkaBase.getActor(PersistentId.Cluster.Receiver);

    assertNotNull(receiver);
    receiver.tell(MessageEnvelopeImpl.builder().deliveryId(1).build(), ActorRef.noSender());

  }

}
