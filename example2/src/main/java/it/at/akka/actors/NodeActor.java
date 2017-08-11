package it.at.akka.actors;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.inject.Inject;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.cluster.singleton.ClusterSingletonProxy;
import akka.cluster.singleton.ClusterSingletonProxySettings;
import it.at.akka.actors.proto.Ping;
import it.at.akka.guice.GuiceAbstractActor;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.duration.FiniteDuration;

@Slf4j
public class NodeActor extends GuiceAbstractActor {
	private final static String SCHEDULATION_CHECK = "SchedulationsCheck";
	
	private final Cancellable schedule;
	private final ActorRef eventProxy;
	private final UUID mine;
	
	@Inject
	public NodeActor() {
		final ActorSystem system = getContext().system();
		
		schedule = system.scheduler().schedule(
			FiniteDuration.create(5, TimeUnit.SECONDS), 
			FiniteDuration.create(45, TimeUnit.SECONDS), 
			getSelf(), SCHEDULATION_CHECK, 
			system.dispatcher(), getSelf()
		); 		
		
		final ClusterSingletonProxySettings deliveryMasterSettings = ClusterSingletonProxySettings.create(system);        		
		eventProxy = system.actorOf(ClusterSingletonProxy.props("/user/cluster-event", deliveryMasterSettings));		
		
		mine = UUID.randomUUID();
		log.info("mine uuid is {}", mine);
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
				
				final Ping ping = new Ping("PING [" + getSender().path() + " @ " + mine + "]");				
				eventProxy.tell(ping, getSelf());
				log.info("  {}", ping);
				
			})
			.build();			
	}

}
