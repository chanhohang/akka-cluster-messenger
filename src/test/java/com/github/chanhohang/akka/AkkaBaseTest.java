package com.github.chanhohang.akka;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.Assert.assertNotNull;

import com.google.common.collect.Sets;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Address;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.Member;
import akka.cluster.MemberStatus;

import com.github.chanhohang.akka.AkkaClusterMessengerConstants.PersistentId;
import com.github.chanhohang.akka.cluster.IClusterStore;
import com.github.chanhohang.akka.message.MessageEnvelopeImpl;
import com.github.chanhohang.akka.spring.SpringAkkaBase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@ContextConfiguration(locations = { "classpath:test-db-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(value = { AkkaClusterMessengerConstants.Profile, "test" })
public class AkkaBaseTest {

  @Autowired
  private SpringAkkaBase akkaBase;

  @Autowired
  private IClusterStore clusterStore;

  @Test
  public void test() throws InterruptedException {
    assertThat(akkaBase).isNotNull();
    ActorRef receiver = akkaBase.getActor(PersistentId.Cluster.Receiver);

    assertNotNull(receiver);
    receiver.tell(MessageEnvelopeImpl.builder().deliveryId(1).build(), ActorRef.noSender());


    Member member = Mockito.mock(Member.class);
    Mockito.when(member.getRoles()).thenReturn(Sets.newHashSet("TEST"));
    Mockito.when(member.address()).thenReturn(Address.apply("akka.tcp", "127.0.0.1"));
    Mockito.when(member.status()).thenReturn(MemberStatus.up());
    MemberUp up = new MemberUp(member);

    ActorRef listener = akkaBase.getActor(PersistentId.Cluster.Listener);
    listener.tell(up, ActorRef.noSender());
    List<ActorSelection> actor = clusterStore.getActor("TEST", PersistentId.Cluster.Listener);
    assertThat(actor).isNotNull();
    // assertThat(actor.size()).isEqualTo(1);

  }

}
