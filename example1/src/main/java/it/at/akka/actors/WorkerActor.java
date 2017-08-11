package it.at.akka.actors;

import it.at.akka.guice.GuiceAbstractActor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WorkerActor extends GuiceAbstractActor {
	
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
			.matchAny(o -> {
				log.warn("not handled message {}", o);
			})
			.build();			
	}

}
