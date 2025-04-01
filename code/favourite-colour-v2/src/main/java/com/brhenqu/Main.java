package com.brhenqu;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
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
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, getEnvOrDefault("APPLICATION_ID_CONFIG","favourite-colour-app-v2"));
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, getEnvOrDefault("BOOTSTRAP_SERVERS_CONFIG","localhost:9092"));
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, getEnvOrDefault("AUTO_OFFSET_RESET_CONFIG","earliest"));
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        // we disable the cache to demonstrate all the "steps" involved in the transformation - not recommended in prod
        config.put(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, getEnvOrDefault("STATESTORE_CACHE_MAX_BYTES_CONFIG","0"));

        StreamsBuilder builder = new StreamsBuilder();

        // Step 1: Create topic of users keys to colours
        KStream<String, String> textLines = builder.stream("favourite-colour-input");

        KStream<String, String> usersAndColours = textLines
                // 1 - ensure that comma is here as we will split on it
                .filter((key, value) -> value.contains(","))
                // 2 - select a key that will be the user id(lowercase for safety)
                .selectKey((key, value) -> value.split(",")[0].toLowerCase())
                // 3 - we get the colour from the value(lowercase for safety)
                .mapValues(value -> value.split(",")[1].toLowerCase())
                // 4 - we filter undesired colours(could be a data sanitization step)
                .filter((user, color) -> Arrays.asList("green", "red", "blue").contains(color));

        usersAndColours.to("user-keys-and-colours");

        // step 2 - we read that topic as a KTable so that updates are read correctly
        KTable<String, String> usersAndColoursTable = builder.table("user-keys-and-colours");

        // step 3 - we count the occurrences of colours
        KTable<String, Long> favouriteColours = usersAndColoursTable
                // 5 - we group by colour within the KTable
                .groupBy((user, colour) -> new KeyValue<>(colour, colour))
                .count(Named.as("CountByColours"));

        // 6 - output the results to a kafka topic - with serializers
        favouriteColours.toStream().to("favourite-colour-output", Produced.with(Serdes.String(), Serdes.Long()));

        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        // only for dev :)
        streams.cleanUp();
        streams.start();

        // print the topology
        System.out.println(streams.toString());

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    }

    private static String getEnvOrDefault(String environmentVariable, String defaultValue) {
        String value = System.getenv(environmentVariable);
        return (value != null) ? value : defaultValue;
    }
}