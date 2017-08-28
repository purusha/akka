package it.at.akka;

import com.google.inject.Guice;

import it.at.akka.apps.SimpleApplication;
import it.at.akka.guice.ModuleBuilder;

public class StartSystem {
	public static void main(String[] args) {		
		
		if (args.length > 0) {
			System.setProperty("akka.remote.netty.tcp.port", args[0]);
		}		
		
		Guice
			.createInjector(ModuleBuilder.build())
			.getInstance(SimpleApplication.class).run();
		
	}
}
