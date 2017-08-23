package it.at.akka.actors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import akka.actor.ActorPath;
import akka.actor.ActorPaths;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.cluster.client.ClusterClient;
import akka.cluster.client.ClusterClient.Send;
import akka.cluster.client.ClusterClientSettings;
import it.at.akka.guice.GuiceAbstractActor;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.duration.FiniteDuration;

@Slf4j
public class MasterActor extends GuiceAbstractActor {
	private final static String SCHEDULATION_CHECK = "SchedulationsCheck";
	
	private final Cancellable schedule;
	private final ActorRef workerProxy;
//    private final ActorRef remoteActor;
	
	@Inject
	public MasterActor() {
		final ActorSystem system = getContext().system();
		
		schedule = system.scheduler().schedule(
			FiniteDuration.create(1, TimeUnit.SECONDS), 
			FiniteDuration.create(1, TimeUnit.SECONDS), 
			getSelf(), SCHEDULATION_CHECK, 
			system.dispatcher(), getSelf()
		); 		
		
		//workerProxy = system.actorSelection("akka.tcp://NodoAMineCluster@127.0.0.1:1337/user/masterA");
		workerProxy = system.actorOf(
	        ClusterClient.props(
                ClusterClientSettings.create(system).withInitialContacts(initialContacts())
            ));
	}
	
	private Set<ActorPath> initialContacts() {
	    return new HashSet<ActorPath>(Arrays.asList(
            ActorPaths.fromString("akka.tcp://NodoAMineCluster@127.0.0.1:1337/system/receptionist")
	    ));
    }
	

	@Override
	public void postStop() throws Exception {
		super.postStop();
		log.info("end {} ", getSelf().path());
		
		schedule.cancel();
	}
	
	@Override
	public void preStart() throws Exception {
		super.preStart();
		log.info("start {} with parent {}", getSelf().path(), getContext().parent());
}	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.matchEquals(SCHEDULATION_CHECK, sc -> { 
			    final Send send = new ClusterClient.Send("/user/masterA", "PING from " + getSelf().path(), true);
				workerProxy.tell(send, getSelf());
				
				log.info("sent message to {}", workerProxy);				
			})
            .match(String.class, pong -> {
                if (StringUtils.startsWith(pong, "PONG")) {
                    log.info("{}", pong);
                } else {
                    log.error("unhandled message {}", pong);
                }
            })                      			
			.build();			
	}

}
