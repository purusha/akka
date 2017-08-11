package it.at.akka.actors.proto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pong implements Serializable {
	private static final long serialVersionUID = 6283430359505131720L;
	
	private String message;
}
