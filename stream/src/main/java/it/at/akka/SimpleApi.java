package it.at.akka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletionStage;
import akka.Done;
import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class SimpleApi {
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        
        //Source

        Source<Integer, NotUsed> range = Source.range(1,  100);
        Source<Integer, NotUsed> list = Source.from(new ArrayList<>(Arrays.asList(1, 2, 3, 42)));
        Source<Integer, NotUsed> single = Source.single(9);
        Source<Integer, NotUsed> repeat = Source.repeat(13);
        
        //Sink
        
        Sink<Integer, CompletionStage<Integer>> fold = Sink.<Integer, Integer>fold(0, (next, total) -> total + next);
        Sink<Object, CompletionStage<Done>> foreach = Sink.foreach(e -> System.out.print(e));
        
        ActorRef ref = null;
        Object onCompleteMessage = null;
        Sink<Object, NotUsed> actorRef = Sink.actorRef(ref, onCompleteMessage);
        
        //RunnableGraph
        
        RunnableGraph<CompletionStage<Integer>> right = list.toMat(fold, Keep.right());
        RunnableGraph<Pair<NotUsed, CompletionStage<Integer>>> both = list.toMat(fold, Keep.both());
        RunnableGraph<NotUsed> left = list.toMat(fold, Keep.left());        
        
        //Start
        
        System.out.println("starting ActorSystem");
        
        final ActorSystem actorSystem = ActorSystem.create();
        final Materializer materializer = ActorMaterializer.create(actorSystem);
        
        right.run(materializer).thenAccept(sum -> {
            System.out.println(sum);
            
            actorSystem.terminate();
        });
        
    }
}
