package it.at.akka.guice;

import com.google.inject.Injector;

import akka.actor.ActorSystem;
import akka.actor.Props;

public class GuiceActorUtils {

	private static Injector getInjector(ActorSystem actorSystem) {
        return GuiceExtension.provider.get(actorSystem).getInjector();
    }

    public static Props makeProps(ActorSystem actorSystem, Class<?> clazz) {
        return Props.create(GuiceActorProducer.class, getInjector(actorSystem), clazz, new Object[]{});
    }
}
