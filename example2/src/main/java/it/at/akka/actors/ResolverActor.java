package it.at.akka.actors;

import it.at.akka.actors.proto.Ping;
import it.at.akka.actors.proto.Pong;
import it.at.akka.guice.GuiceAbstractActor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResolverActor extends GuiceAbstractActor {

	@Override
	public void postStop() throws Exception {
		super.postStop();
		log.info("end {} ", getSelf().path());
	}
	
	@Override
	public void preStart() throws Exception {
		super.preStart();
		log.info("start {} with parent {}", getSelf().path(), getContext().parent());
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(Ping.class, ping -> {
				
				log.info("HANDLED {}", ping);
				
				final Pong pong = new Pong("PONG of " + ping.getMessage());				
				getSender().tell(pong, getSelf());
				
				log.info("RESPOND {}", pong);
				
			})
			.build();			
	}
}
