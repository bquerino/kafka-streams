# **Kafka Streams Study Repository**

This repository will show how Kafka Streams API works.

## Getting started

### Understanding this repository

```scss
├── code
│   └── ... (Java projects for Kafka Streams)
├── docs
│   └── ... (Documentation on Kafka Streams theory and applicability)
├── infra
│   └── ... (Infrastructure scripts, Docker Compose, message-producing scripts, etc.)
└── LICENSE.md
└── README.md
```

### Setup Infra

- Go into the `/infra` folder.
- Run `docker-compose up -d` to start Kafka, and any additional services.
- Verify that containers are running correctly.

### Kafka configs

- For any additional config or something that you need to see in your kafka topics use the [Control Center](http://localhost:9021/).

### Build and Run Java Projects

- Navigate to each project in `/code`.
- Use `mvn clean install` to build.
- Run the application to start processing streams.

### Check Docs

- Review the `/docs/README.md` for a deeper understanding of Kafka Streams and guidelines.

### Produce and Consume

- Use the scripts in `/infra` to create the needed topics and produce test messages.
- Inspect the Java Kafka Streams application to confirm the messages are being processed correctly.

> **Optional**: If you have IntelliJ Ultimate Edition you can configure Kafka plugin to send and receive messages to your topics.

---
## References

This repo was created based on study of the following references:

- [Apache Kafka Series - Kafka Streams for Data Processing](https://www.udemy.com/course/kafka-streams)
- [Baeldung - Introduction to KafkaStreams in Java](https://www.baeldung.com/java-kafka-streams)
- [Mastering Kafka Streams and ksqlDB](https://learning.oreilly.com/library/view/mastering-kafka-streams/9781492062486/ch07.html)
- [Kafka Streams Developer Guide for Confluent Platform](https://docs.confluent.io/platform/current/streams/developer-guide/overview.html)

