package it.at.akka.apps;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.actor.Props;
import it.at.akka.actors.MasterActor;
import it.at.akka.guice.GuiceExtension;
import it.at.akka.guice.GuiceExtensionImpl;

public class SimpleApplication {
	private final Injector injector;	

	@Inject
	public SimpleApplication(Injector injector) {
		this.injector = injector;
	}

	public void run() {		
        //create system
        final ActorSystem system = ActorSystem.create("NodoAMineCluster", ConfigFactory.load());
        system.registerExtension(GuiceExtension.provider);

        //configure Guice
        final GuiceExtensionImpl guiceExtension = GuiceExtension.provider.get(system);
        guiceExtension.setInjector(injector);         
        
        //clusterSettings
//        final ClusterSingletonManagerSettings settings = ClusterSingletonManagerSettings.create(system);
        
        system.actorOf(
//        	ClusterSingletonManager.props(
//				Props.create(MasterActor.class), PoisonPill.class, settings), "masterA"
        		
    		Props.create(MasterActor.class), "masterA"
		);
        
//        system.actorOf(    		
//    		ClusterSingletonManager.props(    			
//				new RandomPool(5).props(Props.create(WorkerActor.class)), PoisonPill.class, settings), "worker"    		
//		);
        
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
}
