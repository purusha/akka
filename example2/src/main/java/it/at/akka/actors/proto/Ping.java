package it.at.akka.actors.proto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Ping implements Serializable {
	private static final long serialVersionUID = 5814263473275012525L;
	
	private String message;
}
