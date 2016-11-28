package com.github.chanhohang.akka.spring;

import akka.actor.ActorSystem;

import com.github.chanhohang.akka.AkkaClusterMessengerConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile(AkkaClusterMessengerConstants.Profile)
public class SpringConfiguration {
  // the application context is needed to initialize the Akka Spring Extension
  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private SpringAkkaBase core;

  /**
   * Actor system singleton for this application.
   * 
   * @return see {@link ActorSystem}
   */
  @Bean
  public ActorSystem actorSystem() {
    ActorSystem system = core.getActorSystem();
    // initialize the application context in the Akka Spring Extension
    SpringExtensions.springExtProvider.get(system).initialize(applicationContext);

    core.initialize();
    return system;
  }
}
