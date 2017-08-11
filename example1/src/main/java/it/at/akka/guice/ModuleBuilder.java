package it.at.akka.guice;

import com.google.inject.Module;

public class ModuleBuilder {
	public static Module build() {
		return new AkkaModule();
	}
}
