package com.brhenqu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

public class TransactionsProducer {

    private static volatile boolean running = true; // flag de controle

    // ObjectMapper instance for JSON serialization
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setDateFormat(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));

    public static void produce() {
        Properties config = new Properties();
        config.put(ProducerConfig.CLIENT_ID_CONFIG, getEnvOrDefault("APPLICATION_ID_CONFIG", "bank-balance-app"));
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getEnvOrDefault("BOOTSTRAP_SERVERS_CONFIG", "localhost:9092"));
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // producer acks
        config.put(ProducerConfig.ACKS_CONFIG, "all"); // strongest producing guarantee
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        // leverage idempotent producer
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // ensure we don't send duplicates

        Producer<String, String> producer = new KafkaProducer<>(config);

        int i = 0;

        while (true){
            System.out.println("Producing batch: " + i);
            try {
                producer.send(newRandomTransaction("john"));
                producer.send(newRandomTransaction("bruno"));
                producer.send(newRandomTransaction("maria"));
                Thread.sleep(100);
                i += 1;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // boa prática
                System.out.println("🛑 Failed to produce batch: " + i + " due to: " + e.getMessage());
                break;
            }finally {
                producer.close();
                System.out.println("✅ Producer closed.");
            }
        }
        producer.close();
    }

    public static ProducerRecord<String, String> newRandomTransaction(String name){
        ObjectNode transaction = JsonNodeFactory.instance.objectNode();
        Integer amount = ThreadLocalRandom.current().nextInt(0, 100);
        Instant now = Instant.now();
        transaction.put("Name", name);
        transaction.put("amount", amount);
        transaction.put("time", now.toString());
        System.out.println("Producing transaction: " + transaction);
        return new ProducerRecord<>("bank-transactions", name, transaction.toString());

    }

    static String getEnvOrDefault(String environmentVariable, String defaultValue) {
        String value = System.getenv(environmentVariable);
        return (value != null) ? value : defaultValue;
    }

    // chamada pelo shutdown hook
    public static void stop() {
        System.out.println("🛑 Stopping producer...");
        running = false;
    }
}
