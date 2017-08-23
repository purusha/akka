package it.at.akka.actors;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.cluster.Member;
import akka.cluster.client.ClusterClientReceptionist;
import it.at.akka.guice.GuiceAbstractActor;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.duration.FiniteDuration;

@Slf4j
public class MasterActor extends GuiceAbstractActor {
	private final static String SCHEDULATION_CHECK = "SchedulationsCheck";
	
	private final Cancellable schedule;
	
	@Inject
	public MasterActor() {
		final ActorSystem system = getContext().system();
		
		schedule = system.scheduler().schedule(
			FiniteDuration.create(10, TimeUnit.SECONDS), 
			FiniteDuration.create(10, TimeUnit.SECONDS), 
			getSelf(), SCHEDULATION_CHECK, 
			system.dispatcher(), getSelf()
		); 	
		
		 Cluster.get(system).subscribe(getSelf(), MemberEvent.class, UnreachableMember.class);
		 
		 ClusterClientReceptionist.get(system).registerService(getSelf());
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
				log.info("i am here {}", getSender().path());				
			})
			.match(MemberUp.class, mUp -> {
		        Member member = mUp.member();
		        log.info("MemberUp message {}", member);
			})
            .match(String.class, ping -> {
                if (StringUtils.startsWith(ping, "PING")) {
                    log.info("{}", ping);
                    getSender().tell("PONG of " + ping, getSelf());
                } else {
                    log.error("unhandled message {}", ping);
                }
            })          			
			.matchAny(o -> {
				log.error("unhandled message {}", o);
			})
			.build();			
	}

}
