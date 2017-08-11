package it.at.akka;

import com.google.inject.Guice;
import com.google.inject.Injector;

import it.at.akka.apps.SimpleApplication;
import it.at.akka.guice.ModuleBuilder;

public class StartSystem {
	public static Injector injector;
	
	public static void main(String[] args) {		
		
		if (args.length > 0) {
			System.setProperty("akka.remote.netty.tcp.port", args[0]);
		}		
		
		injector = Guice.createInjector(ModuleBuilder.build());

		injector.getInstance(SimpleApplication.class).run();
		
	}
}
