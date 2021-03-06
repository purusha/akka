package it.at.akka;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Framing;
import akka.stream.javadsl.FramingTruncation;
import akka.util.ByteString;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupLogFile {
    private static final Pattern LOGLEVEL = Pattern.compile(".*\\[(DEBUG|INFO|WARN|ERROR)\\].*");
    
    public static void main(String[] args) throws IOException {
  
        final ActorSystem system = ActorSystem.create("Sys");
        final ActorMaterializer materializer = ActorMaterializer.create(system);
    
        // read lines from a log file
        final File inputFile = new File("src/main/resources/logfile.txt");
    
        final Map<String, PrintWriter> outputs = new HashMap<>();
        for (String level : Arrays.asList("DEBUG", "INFO", "WARN", "ERROR", "UNKNOWN")) {
            final String outPath = "target/log-" + level + ".txt";
            final PrintWriter output = new PrintWriter(new FileOutputStream(outPath), true);
            
            outputs.put(level, output);
        }
    
        FileIO.fromFile(inputFile)
            // parse bytestrings (chunks of data) to lines
            .via(Framing.delimiter(ByteString.fromString(System.lineSeparator()), 512, FramingTruncation.ALLOW))
            
            .map(ByteString::utf8String)
            
            // group them by log level
            .map((line) -> {
                final Matcher matcher = LOGLEVEL.matcher(line);
                
                return matcher.find() ? new Pair<>(matcher.group(1), line) : new Pair<>("UNKNOWN", line);
            })
            
            // write lines of each group to a separate file
            .runForeach((levelProducerPair) -> {
                outputs.get(levelProducerPair.first()).println(levelProducerPair.second());
            }, materializer)
            
            .handle((ignored, failure) -> {
                outputs.forEach((key, writer) -> {
                  writer.close();
                });
                system.terminate();
                return NotUsed.getInstance();
            });
        
    }
}
