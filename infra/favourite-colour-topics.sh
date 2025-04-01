#!/bin/bash

# Kafka REST Proxy configurations
KAFKA_REST_URL="http://localhost:8082"    # Kafka REST Proxy URL
PARTITIONS=3                              # Number of partitions
REPLICATION_FACTOR=1                      # Replication factor

# Topics names
TOPICS=("favourite-colour-input" "user-keys-and-colours" "favourite-colour-output")

# Base URL for listing clusters
CLUSTERS_URL="$KAFKA_REST_URL/v3/clusters"

# Get the cluster ID
CLUSTER_ID=$(curl -s "$CLUSTERS_URL" | jq -r '.data[0].cluster_id')

if [ -z "$CLUSTER_ID" ]; then
  echo "Error: Could not retrieve the cluster ID."
  exit 1
fi

echo "Cluster ID found: $CLUSTER_ID"

# Endpoint for creating topics
CREATE_TOPIC_URL="$KAFKA_REST_URL/v3/clusters/$CLUSTER_ID/topics"

# Request headers
HEADERS=(
  "Content-Type: application/json"
)

# Create each topic inside the TOPICS array
for TOPIC_NAME in "${TOPICS[@]}"; do

  CLEANUP_POLICY="delete"  # default policy

  if [[ "$TOPIC_NAME" == "user-keys-and-colours" || "$TOPIC_NAME" == "favourite-colour-output" ]]; then
    CLEANUP_POLICY="compact"
  fi

  # Request body in JSON
  PAYLOAD=$(cat <<EOF
{
  "topic_name": "$TOPIC_NAME",
  "partitions_count": $PARTITIONS,
  "replication_factor": $REPLICATION_FACTOR,
  "configs": [
    {"name": "cleanup.policy", "value": "$CLEANUP_POLICY"},
    {"name": "retention.ms", "value": "604800000"}
  ]
}
EOF
)

  # Perform the POST to create the topic
  RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$CREATE_TOPIC_URL" \
    -H "${HEADERS[0]}" \
    -d "$PAYLOAD")

  if [ "$RESPONSE" -eq 201 ]; then
    echo "Topic '$TOPIC_NAME' created successfully!"
  else
    echo "Error creating topic '$TOPIC_NAME'. Response code: $RESPONSE"
    # Additional log in case of error
    curl -X POST "$CREATE_TOPIC_URL" -H "${HEADERS[0]}" -d "$PAYLOAD"
  fi

done