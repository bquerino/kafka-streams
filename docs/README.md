# Kafka Streams Application Terminology

```mermaid
flowchart TB
%% Classes to color the Source and Sink stream processors
    classDef source fill:#FFFFB3,stroke:#000,stroke-width:1px,color:#000
    classDef sink fill:#C5FDB5,stroke:#000,stroke-width:1px,color:#000

%% Declaration of the nodes (Stream Processors)
    A([Source Stream Processor]):::source
    B([Source Stream Processor]):::source
    C(Stream Processor 1)
    D(Stream Processor 2)
    E(Stream Processor 3)
    F([Sink Stream Processor]):::sink

%% Flow of the immutable streams
    A -->|stream| C
    B -->|stream| C
    C -->|stream| D
    D -->|stream| E
    E -->|stream| F
    D -->|stream| F

```
- A `stream` is a sequence of immutable data records, that fully ordered, can be replayed, and is fault tolerant.
- A `stream processor` is a node in the processor topology (graph). It transforms incoming streams, record by record, and may create a new stream from it.
- A `source processor` is a special processor that takes its data directly from a Kafka Topic. It has no predecessors in a topology, and don't transform the data.
- A `sink processor` is a processor that does not have children, it sends the stream data directly to a Kafka topic.

## Topology

In this section, we will see examples of `high level dsl` and the `low level processor api`.

### **High Level DSL**

#TODO

### **Low Level Processor API**

#TODO

---

## Internal Topics

- Running a Kafka Streams may eventually create internal intermediary topics.

### Types

- **Repartitioning topics**: in case you start transforming the key of your stream, a repartitioning will happen at some processor.
- **Changelog topics**: in case you perform aggregations, Kafka Streams will save compacted data in these topics

### Observations

- Are managed by Kafka Streams
- Are used by Kafka Streams to save / restore state and repartition data
- Are prefixed by application.id parameter
- Should ever be deleted, altered or published to. **They are internal**

> If you list your topics probably will see something like `${application.id}-KSTREAM-AGGREGATE-STATE-STORE-${number}-repartition` and the same name ending with `changelog`.

---

## KStreams and KTables

### **KStream**

KStream treats each message as an event (“change log”). All occurrences of (Alice, 1) and (Alice, 2) are in the stream.

```mermaid
sequenceDiagram
    participant P as Producer
    participant T as KafkaTopic
    participant KS as KStream

    P->>T: (Alice, 1)
    T->>KS: (Alice, 1)
    note right of KS: Primeiro evento<br/>no stream

    P->>T: (Alice, 2)
    T->>KS: (Alice, 2)
    note right of KS: Segundo evento<br/>no stream

```

### **KTable**

KTable maintains a current state per key. When it reaches (Alice, 2), it overwrites (Alice, 1). In the end, only (Alice, 2) exists for the key “Alice”. And when it receives a null value, it deletes the record, as in the case of (Bob, null).

```mermaid
sequenceDiagram
    participant P as Producer
    participant T as KafkaTopic
    participant KT as KTable

    P->>T: (Alice, 1)
    T->>KT: (Alice, 1)
    note right of KT: KTable faz<br/>Alice -> 1

    P->>T: (Alice, 2)
    T->>KT: (Alice, 2)
    note right of KT: KTable faz upsert<br/>Alice -> 2

    P->>T: (Bob, 10)
    T->>KT: (Bob, 10)
    note right of KT: KTable faz<br/>Bob -> 10

    P->>T: (Bob, null) - tombstone
    T->>KT: (Bob, null)
    note right of KT: KTable remove<br/>Bob

```

---