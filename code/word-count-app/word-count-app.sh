#!/bin/bash

docker run -d --name my-kafka-streams-app --network infra_kafka-net my-kafka-streams-app
