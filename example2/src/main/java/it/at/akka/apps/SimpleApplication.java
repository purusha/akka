package it.at.akka.apps;

import com.google.inject.Inject;
import com.google.inject.Injector;

import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import it.at.akka.actors.ClusterEventActor;
import it.at.akka.actors.NodeActor;
import it.at.akka.actors.ResolverActor;
import it.at.akka.guice.GuiceActorUtils;
import it.at.akka.guice.GuiceExtension;
import it.at.akka.guice.GuiceExtensionImpl;

public class SimpleApplication {
	private final Injector injector;
	private final ActorSystem system;	

	@Inject
	public SimpleApplication(Injector injector, ActorSystem system) {
		this.injector = injector;
		this.system = system;
	}

	public void run() {		
        //register Guice provider
        system.registerExtension(GuiceExtension.provider);

        //configure Guice provider
        final GuiceExtensionImpl guiceExtension = GuiceExtension.provider.get(system);
        guiceExtension.setInjector(injector);         
        
        //clusterSettings
        final ClusterSingletonManagerSettings settings = ClusterSingletonManagerSettings.create(system).withRole("boooo");
        
        system.actorOf(
    		build(ResolverActor.class), "resolver"
		);
        
        system.actorOf(
    		build(NodeActor.class)
		);
        
//        system.actorOf(    		
//    		ClusterSingletonManager.props(    			
//				new RandomPool(5).props(build(WorkerActor.class)), PoisonPill.class, settings), "worker"    		
//		);
        
        system.actorOf(
        	ClusterSingletonManager.props(
    			build(ClusterEventActor.class), PoisonPill.class, settings), "cluster-event"
		);
        
        
//        final ClusterSingletonProxySettings deliveryMasterSettings = ClusterSingletonProxySettings.create(system).withRole("master");        
//        final ActorRef deliveryMaster = system.actorOf(ClusterSingletonProxy.props("/user/delivery-master", deliveryMasterSettings), "delivery-master-proxy");
                
        //create instance of DeliveryMaster
//        system.actorOf(
//    		ClusterSingletonManager.props(
//				GuiceActorUtils.makeProps(system, DeliveryMaster.class), PoisonPill.class, settings), "delivery-master"
//		); 
        
//        final Config engineConf = injector.getInstance(Config.class);        
//        final String hostBinding = engineConf.getString("engine.http.listener.host"); 
        
        //HTTP Listener's  
        //this listener should be removed ... (is used only for DEV Environment) 
//        system.actorOf(
//    		ClusterSingletonManager.props(
//				HTTPDeliveryListener.create(
//					deliveryMaster, hostBinding + engineConf.getString("engine.http.listener.delivery.path")), PoisonPill.class, settings), "delivery-listener"
//		);

        //show status of: which delivery is in process ?
//        system.actorOf(
//    		ClusterSingletonManager.props(
//				GuiceActorUtils.makeProps(system, ShowDeliveryInProcessActor.class), PoisonPill.class, settings), "show-delivery-in-progress"
//		); 
        
//		new HTTPListener(
//			system, engineConf.getString("engine.http.listener.health.host"), engineConf.getInt("engine.http.listener.health.port")
//		);
		
//		final Camel camel = CamelExtension.get(system);

		//polling Request's Table for retrieve delivery to be processed
//        system.actorOf(
//    		ClusterSingletonManager.props(
//				EngineRouteBuilderActor.create(
//					camel, deliveryMaster), PoisonPill.class, settings), "request-table-poller"	
//		);

        //print machine Infos
//        system.actorOf(
//    		GuiceActorUtils.makeProps(system, MachineStatusInfoActor.class)
//		);

        //booo??
//        system.actorOf(
//			ClusterSingletonManager.props(
//				Props.create(ExclusionListEventProducer.class), PoisonPill.class, settings), "exclusionlist-events-producer"
//		);
				
	}
	
	public Props build(Class<?> clazz) {
		return GuiceActorUtils.makeProps(system, clazz);
	}
	
}
