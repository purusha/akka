package it.at.akka.guice;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AkkaModule implements Module {
	
	@Override
	public void configure(Binder binder) {
						
		final ActorSystem system = ActorSystem.create("MineCluster", ConfigFactory.load());
		log.info("created system {}", system);
		
		binder
			.bind(ActorSystem.class)
			.toInstance(system);
		
		
	}	
}
