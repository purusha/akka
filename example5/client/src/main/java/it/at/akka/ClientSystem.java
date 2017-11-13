package it.at.akka;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.routing.ActorRefRoutee;
import akka.routing.Router;
import akka.routing.SmallestMailboxRoutingLogic;
import scala.concurrent.duration.Duration;

public class ClientSystem {

	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			System.setProperty("akka.remote.netty.tcp.port", args[0]);
		}

		final ActorSystem actorSystem = ActorSystem.create("client");
		actorSystem.actorOf(Props.create(MasterActor.class), MasterActor.NAME);
		actorSystem.actorOf(Props.create(GeneratorActor.class, actorSystem));
	}

	static class MasterActor extends AbstractActor {		
		public static final String NAME = "master-client-actor";
		
		private Router router;

		public MasterActor() {			
			router = new Router(
				new SmallestMailboxRoutingLogic(), 
				IntStream.range(0, 5)
					.mapToObj(i -> {
						final ActorRef r = getContext().actorOf(Props.create(WorkerWithConnection.class), String.valueOf(i));
						getContext().watch(r);
						
						return new ActorRefRoutee(r);
					})
					.collect(
						Collectors.toList()
					)
			);			
		}

		@Override
		public Receive createReceive() {
			return 
			receiveBuilder()
				.match(Terminated.class, message -> {
			        final ActorRef r = getContext().actorOf(Props.create(WorkerWithConnection.class));
			        getContext().watch(r);
			        
		        	router = router.removeRoutee(message.actor()).addRoutee(new ActorRefRoutee(r));
			     })
				.matchAny(
					msg -> router.route(msg, getSender())
				)
				.build();
		}		
	}
	
	static class WorkerWithConnection extends AbstractActor {		
		private final InetSocketAddress addr = new InetSocketAddress("localhost", 8090);		
		private SocketChannel client;

		@Override
		public Receive createReceive() {
			return 
			receiveBuilder()
				.matchAny(msg -> {

					final String payload = getSelf().path() + "[" + UUID.randomUUID().toString() + "]";
		            final ByteBuffer buffer = ByteBuffer.wrap(payload.getBytes());		            
		            client.write(buffer);	            
		            System.out.println("send: " + payload);
		            
		            buffer.clear();
		            
		            client.read(buffer);
		            System.out.println("received: " + new String(buffer.array()));	            					
					
				})
				.build();
		}
		
		@Override
		public void preStart() throws Exception {
			super.preStart();
			
	        this.client = SocketChannel.open(addr); 
	        System.out.println("Client... started on " + getSelf().path());			
		}
		
		@Override
		public void postStop() throws Exception {
			super.postStop();
			
			this.client.close();
		}		
	}

	static class GeneratorActor extends AbstractActor {		
		private final Cancellable cancellable;
		
		@SuppressWarnings("deprecation")
		public GeneratorActor(ActorSystem system) {						
			final ActorRef actor = system.actorFor("/user/" + MasterActor.NAME);
			
			cancellable = system.scheduler().schedule(
				Duration.Zero(), Duration.create(10, TimeUnit.MILLISECONDS), 
				actor, "Tick", system.dispatcher(), ActorRef.noSender()
			);			
		}

		@Override
		public Receive createReceive() {
			return receiveBuilder().build();
		}
		
		@Override
		public void postStop() throws Exception {
			super.postStop();
			
			cancellable.cancel();
		}
	}

}
