package it.at.akka.guice;

import com.google.inject.Injector;

import akka.actor.AbstractActor;

//import akka.actor.UntypedActor;
//
//public abstract class GuiceUntypedActor extends UntypedActor {
//	
//    public Injector getInjector() {
//        return GuiceExtension.provider.get(getContext().system()).getInjector();
//    }
//
//    public Props makeGuiceProps(Class<?> clazz, Object ... arguments) {
//        return Props.create(GuiceActorProducer.class, getInjector(), clazz, arguments);
//    }
//    
//}

public abstract class GuiceAbstractActor extends AbstractActor {

	public Injector getInjector() {
        return GuiceExtension.provider.get(getContext().system()).getInjector();
    }
	
}