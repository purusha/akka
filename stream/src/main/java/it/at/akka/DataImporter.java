package it.at.akka;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class DataImporter {
    private static final String CONTENT = "1;2;3;4;5;6;7;8;9;10;20;30;40;50;100;101"; //;199.1
    
    public static void main(String[] args) {
        
        final ActorSystem actorSystem = ActorSystem.create();
        
        new Importer(actorSystem).calculateAverage(CONTENT)
            .whenComplete((d, e) -> {
                actorSystem.terminate();
            });

    }
}

class Importer {
    private final ActorMaterializer materializer;
    private final AverageRepository averageRepository;

    public Importer(ActorSystem actorSystem) {
        this.materializer = ActorMaterializer.create(actorSystem);
        this.averageRepository = new AverageRepository();
    }

    private List<Integer> parseLine(String line) {
        final String[] fields = line.split(";");
        
        return 
            Arrays
                .stream(fields)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private Flow<String, Integer, NotUsed> parseContent() {
        return Flow.of(String.class).mapConcat(this::parseLine);
    }

    private Flow<Integer, Double, NotUsed> computeAverage() {
        return 
            Flow.of(Integer.class)
                .grouped(2)
                .mapAsyncUnordered(8, integers ->
                    CompletableFuture.supplyAsync(() -> integers
                        .stream()
                        .mapToDouble(v -> v)
                        .average()
                        .orElse(-1.0)
                    )
                );
    }

    private Flow<String, Double, NotUsed> calculateAverage() {
        return 
            Flow
                .of(String.class)
                .via(parseContent())
                .via(computeAverage());
    }

    private Sink<Double, CompletionStage<Done>> storeAverages() {
        return 
            Flow
                .of(Double.class)
                .mapAsyncUnordered(4, averageRepository::save)
                .toMat(Sink.ignore(), Keep.right());
    }


    public CompletionStage<Done> calculateAverage(String content) {
        return                
            Source
                .single(content)
                .via(calculateAverage())
                .runWith(storeAverages(), materializer)
                .whenComplete((d, e) -> {
                    if (d != null) {
                        System.out.println("Import finished !");
                    } else {
                        e.printStackTrace();
                    }
                });
    }

}

class AverageRepository {
    CompletionStage<Double> save(Double average) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("saving average: " + average);
            
            return average;
        });
    }
}
