package net.chh.akka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import akka.actor.ActorRef;

import net.chh.akka.AkkaClusterMessengerConstants.PersistentId;
import net.chh.akka.message.MessageEnvelopeImpl;
import net.chh.akka.spring.SpringAkkaBase;

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
