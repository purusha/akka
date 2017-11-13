package it.at.akka;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.actor.Props;
import scala.concurrent.duration.Duration;

public class ServerSystem {

	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			System.setProperty("akka.remote.netty.tcp.port", args[0]);
		}

		final ActorSystem actorSystem = ActorSystem.create("server");
		final ActorRef actor = actorSystem.actorOf(Props.create(MasterActor.class, actorSystem));
		
		System.out.println("actor is up & running on " + actor.path());
	}

	static class MasterActor extends AbstractActor {
		private static final String WAKE_UP = "wakeUp";
		
		private Selector selector;
		
		private final Cancellable cancellable;		
		private final InetSocketAddress listenAddress;
		private final Map<SocketChannel, List<?>> dataMapper;	
		
		public MasterActor(ActorSystem system) {
			this.listenAddress = new InetSocketAddress("localhost", 8090);
			this.dataMapper = new HashMap<SocketChannel, List<?>>();
			
			this.cancellable = system.scheduler().schedule(
				Duration.Zero(), Duration.create(5, TimeUnit.MILLISECONDS), 
				getSelf(), WAKE_UP, system.dispatcher(), ActorRef.noSender()
			);						
		}

		@Override
		public void preStart() throws Exception {
			super.preStart();
			
			this.selector = Selector.open();
			
			final ServerSocketChannel serverChannel = ServerSocketChannel.open();
			serverChannel.configureBlocking(false);
			serverChannel.socket().bind(listenAddress);
			serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);
			System.out.println("Server started...");		
		}
		
		@Override
		public void postStop() throws Exception {
			super.postStop();
			
			cancellable.cancel();			
			selector.close();
		}
		
		@Override
		public Receive createReceive() {
			return 
			receiveBuilder()
				.matchEquals(WAKE_UP, msg -> {							
					try{						
						// wait for events
						this.selector.select(5l);

						// work on selected keys
						final Iterator<?> keys = this.selector.selectedKeys().iterator();
						
						while (keys.hasNext()) {
							final SelectionKey key = (SelectionKey) keys.next();

							// this is necessary to prevent the same key from coming up
							// again the next time around.
							keys.remove();

							if (!key.isValid()) {
								continue;
							}

							if (key.isAcceptable()) {
								accept(key);
							} else if (key.isReadable()) {
								handle(key);
							}
						}		
					} catch (Exception e) {
						System.err.println(e);
					}
					
				})
				.build();
		}
		
		// accept a connection made to this channel's socket
		private void accept(SelectionKey key) throws IOException {
			final ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
			
			final SocketChannel channel = serverChannel.accept();
			channel.configureBlocking(false);
					
			final SocketAddress remoteAddr = channel.socket().getRemoteSocketAddress();
			System.out.println("Connected to: " + remoteAddr);

			// register channel with selector for further IO
			dataMapper.put(channel, new ArrayList<>());		
			channel.register(this.selector, SelectionKey.OP_READ);
		}

		// read from the socket channel & respond with the same data to the client
		private void handle(SelectionKey key) throws IOException {
			final SocketChannel channel = (SocketChannel) key.channel();
			final ByteBuffer buffer = ByteBuffer.allocate(1024);		
			final int numRead = channel.read(buffer);

			if (numRead == -1) {
				this.dataMapper.remove(channel);
				
				final SocketAddress remoteAddr = channel.socket().getRemoteSocketAddress();			
				System.out.println("Connection closed by client: " + remoteAddr);
				channel.close();
				key.cancel();
				
				return;			
			}
			
			byte[] data = new byte[numRead];
			System.arraycopy(buffer.array(), 0, data, 0, numRead);
			System.out.println("Got: " + new String(data));		
					
			// respond to the client
			channel.write(ByteBuffer.wrap(data));				
		}
		
	}	
}
