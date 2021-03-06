package it.at.akka.guice;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.Injector;

import akka.actor.Actor;
import akka.actor.IndirectActorProducer;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class GuiceActorProducer implements IndirectActorProducer {
    
    private final Injector injector;
    private final Class<? extends Actor> actorClass;
    private final List<Object> arguments;

    public GuiceActorProducer(Injector injector, Class<? extends Actor> actorClass, Object ... arguments) {
        this.injector = injector;
        this.actorClass = actorClass;
        this.arguments = Lists.newArrayList(arguments);
    }

    @Override
    public Actor produce() {
//        log.debug("##########################################");
//        log.debug("build an instance of {}", actorClass);
//        log.debug("with parameters:");
//        for(Object arg : arguments) {
//            log.debug("{}", arg.getClass());
//        }
//        log.debug("##########################################");
                
//        if (actorClass.equals(SenderProcessor.class)) {
//        	SenderProcessorFactory instance = injector.getInstance(SenderProcessor.SenderProcessorFactory.class);
//        	
//        	return instance.create((DeliveryContext)arguments.get(0));
//        	return null;
//        } else {        
        	return injector.getInstance(actorClass);        	
//        }
    }

    @Override
    public Class<? extends Actor> actorClass() {
        return actorClass;
    }
}