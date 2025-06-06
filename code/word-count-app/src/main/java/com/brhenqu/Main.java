package com.brhenqu;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, getEnvOrDefault("APPLICATION_ID_CONFIG","wordcount-app"));
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, getEnvOrDefault("BOOTSTRAP_SERVERS_CONFIG","localhost:9092"));
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, getEnvOrDefault("AUTO_OFFSET_RESET_CONFIG","earliest"));
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String,String> wordCountInput = builder.stream("word-count-input");

        KTable<String, Long> wordCounts = wordCountInput.mapValues(textLine -> textLine
                        .toLowerCase())
                        .flatMapValues(lowerCasedTextLine -> Arrays.asList(lowerCasedTextLine.split(" ")))
                        .selectKey((ignoredKey, word) -> word)
                        .groupByKey()
                        .count(Named.as("Counts"));
         wordCounts.toStream().to("word-count-output", Produced.with(Serdes.String(),Serdes.Long()));

        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.start();

        System.out.println(streams);

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    }

    private static String getEnvOrDefault(String environmentVariable, String defaultValue) {
        String value = System.getenv(environmentVariable);
        return (value != null) ? value : defaultValue;
    }

}