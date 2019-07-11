package com.streamtest;

import java.util.*;
import org.apache.spark.SparkConf;
import org.apache.spark.TaskContext;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka010.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import scala.Tuple2;
/*import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
*/




public class Main {

    public static void main(String[] args) {
        System.out.println("Starting up server ....");

        SparkConf conf = new SparkConf().setAppName("MyTestAPP").setMaster("spark://master:7077");
        JavaStreamingContext ssc = new JavaStreamingContext(conf, new Duration(1000));

        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", "localhost:9092");
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", "0");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);

        Collection<String> topics = Arrays.asList("topicA", "topicB");

        JavaInputDStream<ConsumerRecord<String, String>> messages =
          KafkaUtils.createDirectStream(
            ssc,
            LocationStrategies.PreferConsistent(),
            ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams)
          );
/*
        stream.mapToPair(
          new PairFunction<ConsumerRecord<String, String>, String, String>() {
            @Override
            public Tuple2<String, String> call(ConsumerRecord<String, String> record) {
              return new Tuple2<>(record.key(), record.value());
            }
          });
*/
        
        JavaPairDStream<String, String> results = messages.mapToPair( 
                    record -> new Tuple2<>(record.key(), record.value())
                );
        JavaDStream<String> lines = results.map(
            tuple2 -> tuple2._2()
            );
         JavaDStream<String> words = lines.flatMap(
             x -> Arrays.asList(x.split("\\s+")).iterator()
             );
      JavaPairDStream<String, Integer> wordCounts = words.mapToPair(
            s -> new Tuple2<>(s, 1)
        ).reduceByKey(
            (i1, i2) -> i1 + i2
          );
        
      ssc.start();
      try {
        ssc.awaitTermination();
    } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
        
        
        System.out.println("Server execution finished");
    }
}
