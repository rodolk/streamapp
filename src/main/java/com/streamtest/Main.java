package com.streamtest;

import java.util.*;
import org.apache.spark.SparkConf;
import org.apache.spark.TaskContext;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Seconds;
import org.apache.spark.streaming.StreamingContext;
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

        SparkConf conf = new SparkConf().setAppName("MyTestAPP").setMaster("spark://rodolk-Spark:7077");
        JavaStreamingContext ssc = new JavaStreamingContext(conf, Seconds.apply(1));

        Map<String, Object> kafkaParams = new HashMap<>();
//        kafkaParams.put("bootstrap.servers", "localhost:9092");
        kafkaParams.put("bootstrap.servers", "172.26.113.97:9092");
                
        kafkaParams.put("ssl.endpoint.identification.algorithm", "");
        kafkaParams.put("security.protocol", "SSL");
        kafkaParams.put("ssl.truststore.location", "/home/rodolk/work/streamapp/certs/kafka.serverTest.truststore.jks");
        kafkaParams.put("ssl.truststore.password", "intel123");
        kafkaParams.put("ssl.keystore.location", "/home/rodolk/work/streamapp/certs/kafka.mykeystore.jks");
        kafkaParams.put("ssl.keystore.password", "intel123");
        kafkaParams.put("ssl.key.password", "intel123");
        
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", "rodolk");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);

        Collection<String> topics = Arrays.asList("public_test1");

        JavaInputDStream<ConsumerRecord<String, String>> messages =
          KafkaUtils.createDirectStream(
            ssc,
            LocationStrategies.PreferConsistent(),
            ConsumerStrategies.Subscribe(topics, kafkaParams)
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
/*         JavaDStream<String> words = lines.flatMap(
             x -> Arrays.asList(x.split("\\s+")).iterator()
             );
      JavaPairDStream<String, Integer> wordCounts = words.mapToPair(
            s -> new Tuple2<>(s, 1)
        ).reduceByKey(
            (i1, i2) -> i1 + i2
          );*/
      lines.print();
      
 //     lines.foreachRDD(
   // 		    javaRdd -> {
    //		        System.out.println("ACA-2");
/*    		      Map<String, Integer> wordCountMap = javaRdd.collectAsMap();
    		      for (String key : wordCountMap.keySet()) {
    		    	  System.out.println(key +":" + wordCountMap.get(key));
    		      }*/
//    		    }
 //   		  );

     // System.out.println("ACA-1");

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
