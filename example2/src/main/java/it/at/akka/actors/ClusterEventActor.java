package it.at.akka.actors;

import com.google.inject.Inject;

import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.cluster.Member;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Router;
import it.at.akka.actors.proto.Ping;
import it.at.akka.guice.GuiceAbstractActor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClusterEventActor extends GuiceAbstractActor {
	
	private static final String USER_RESOLVER = "/user/resolver";
	private Router router;
	
	@Inject
	public ClusterEventActor(ActorSystem system) {
		Cluster
			.get(system)
			.subscribe(getSelf(), MemberEvent.class, UnreachableMember.class);
		
		router = new Router(new RoundRobinRoutingLogic())
			.addRoutee(getContext().actorSelection(USER_RESOLVER));
	}

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
			.match(MemberUp.class, mUp -> {
				final Member member = mUp.member();
				
				router = router.addRoutee(getContext().actorSelection(member.address() + USER_RESOLVER));				
				log.info("added routee on {}", member.address());
		     })
			.match(UnreachableMember.class, um -> {
				final Member member = um.member();
				
				router = router.removeRoutee(getContext().actorSelection(member.address() + USER_RESOLVER));
				log.info("delete routee on {}", member.address());
		     })			
			.match(Ping.class, ping -> {
				
				router.route(ping, getSender());
				log.info("CONSUME {}", ping);
				
		     })			
			.matchAny(o -> {
				log.warn("not handled message {}", o);
			})
			.build();					
	}

}
