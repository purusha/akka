package it.at.akka.guice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Binder;
import com.google.inject.Module;

public class AkkaModule implements Module {
    private final static Logger log = LoggerFactory.getLogger(AkkaModule.class);
	
	@Override
	public void configure(Binder binder) {
						
	}	
}
