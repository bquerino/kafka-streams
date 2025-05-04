package com.brhenqu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import static com.brhenqu.TransactionsProducer.getEnvOrDefault;

public class Main {

    public static void main(String[] args) {
        // 🔁 Latch para manter a aplicação viva
        CountDownLatch latch = new CountDownLatch(1);

        // 🔄 Inicia o producer de forma síncrona em uma thread separada
        Thread producerThread = new Thread(() -> {
            System.out.println("🚀 Starting transaction producer...");
            TransactionsProducer.produce();
        });
        producerThread.start();

        // Kafka Streams config
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG,
                getEnvOrDefault("APPLICATION_ID_CONFIG","bank-balance-app"));
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
                getEnvOrDefault("BOOTSTRAP_SERVERS_CONFIG","localhost:9092"));
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                getEnvOrDefault("AUTO_OFFSET_RESET_CONFIG","earliest"));
        config.put(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG,
                getEnvOrDefault("CACHE_MAX_BYTES_BUFFERING_CONFIG","0"));

        config.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        // serde JSON
        final JsonNodeSerde jsonNodeSerde = new JsonNodeSerde();

        // Set default value serde to JsonNode serde for state stores
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonNodeSerde.class);

        // definição do stream
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> bankTransactions = builder
                .stream("bank-transactions", Consumed.with(Serdes.String(), jsonNodeSerde));

        ObjectNode initialBalance = JsonNodeFactory.instance.objectNode();
        initialBalance.put("count", 0);
        initialBalance.put("balance", 0);
        initialBalance.put("time", Instant.ofEpochMilli(0L).toString());

        KTable<String, JsonNode> bankBalance = bankTransactions
                .groupByKey()
                .aggregate(
                        () -> initialBalance,
                        (key, transaction, balance) -> newBalance(transaction, balance)
                );

        bankBalance.toStream().to("bank-balance-exaclty-once", Produced.with(Serdes.String(), jsonNodeSerde));

        try (KafkaStreams streams = new KafkaStreams(builder.build(), config)) {

            // Shutdown Hook para encerramento gracioso
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("🛑 Shutting down Kafka Streams and producer...");
                streams.close();
                TransactionsProducer.stop(); // você criará esse método
                latch.countDown(); // libera o main
            }));

            // Inicia o Kafka Streams
            try {
                streams.start();
                latch.await(); // bloqueia aqui até shutdown
            } catch (Throwable e) {
                System.err.println("❌ Fatal error: " + e.getMessage());
                System.exit(1);
            }
        }

        System.exit(0);
    }

    private static JsonNode newBalance(JsonNode transaction, JsonNode balance) {
        ObjectNode newBalance = JsonNodeFactory.instance.objectNode();
        newBalance.put("count", balance.get("count").asInt() + 1);
        newBalance.put("balance", balance.get("balance").asInt() + transaction.get("amount").asInt());

        long balanceEpoch = Instant.parse(balance.get("time").asText()).toEpochMilli();
        long transactionEpoch = Instant.parse(transaction.get("time").asText()).toEpochMilli();
        newBalance.put("time", Instant.ofEpochMilli(Math.max(balanceEpoch, transactionEpoch)).toString());

        return newBalance;
    }
}
