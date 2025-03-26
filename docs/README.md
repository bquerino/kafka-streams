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