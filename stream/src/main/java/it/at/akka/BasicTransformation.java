package it.at.akka;

import java.util.Arrays;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Source;

public class BasicTransformation {
    public static void main(String[] args) {

        final ActorSystem system = ActorSystem.create("Sys");
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        final String text = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. " +
                "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, " +
                "when an unknown printer took a galley of type and scrambled it to make a type " +
                "specimen book.";

        Source
            .from(Arrays.asList(text.split("\\s")))// transform
            .map(e -> e.toUpperCase())
            .runForeach(System.out::println, materializer)
            .handle((done, failure) -> {
                system.terminate();
                return NotUsed.getInstance();
            });

    }
}
