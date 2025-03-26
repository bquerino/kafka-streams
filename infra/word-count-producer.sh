#!/bin/bash

# Kafka REST Proxy URL
KAFKA_REST_URL="http://localhost:8082"

# Topic to which the messages will be produced
TOPIC_NAME="word-count-input"

# Produce endpoint
PRODUCE_URL="${KAFKA_REST_URL}/topics/${TOPIC_NAME}"

# Headers for producing plain text as JSON
HEADERS=(
  "Content-Type: application/vnd.kafka.json.v2+json"
)

# Payload with the messages to be sent
PAYLOAD_DATA=$(cat <<EOF
{
  "records": [
    { "value": "hello kafka streams" },
    { "value": "kafka streams is working" },
    { "value": "i am kafka learner" }
  ]
}
EOF
)

# Make the POST request to produce the messages
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${PRODUCE_URL}" \
  -H "${HEADERS[0]}" \
  -d "${PAYLOAD_DATA}"
)

# Check the response
if [ "$RESPONSE" -eq 200 ]; then
  echo "Messages successfully produced to topic '${TOPIC_NAME}'!"
else
  echo "Error producing messages. Response code: $RESPONSE"
fi
