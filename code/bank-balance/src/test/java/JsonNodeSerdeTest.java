import com.brhenqu.JsonNodeDeserializer;
import com.brhenqu.JsonNodeSerializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JsonNodeSerdeTest {

    @Test
    public void testJsonNodeSerializerAndDeserializer() throws Exception {
        // Create a test JsonNode
        ObjectNode testNode = JsonNodeFactory.instance.objectNode();
        testNode.put("Name", "John");
        testNode.put("amount", 123);
        
        // Format date to match the expected format
        LocalDateTime dateTime = LocalDateTime.parse("2025-07-19T05:24:52", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        testNode.put("time", dateTime.toString());

        // Create serializer and deserializer
        Serializer<JsonNode> serializer = new JsonNodeSerializer();
        Deserializer<JsonNode> deserializer = new JsonNodeDeserializer();

        // Serialize
        byte[] serialized = serializer.serialize("test-topic", testNode);
        assertNotNull(serialized, "Serialized data should not be null");

        // Deserialize
        JsonNode deserialized = deserializer.deserialize("test-topic", serialized);
        assertNotNull(deserialized, "Deserialized data should not be null");

        // Verify the deserialized data matches the original
        assertEquals("John", deserialized.get("Name").asText(), "Name should match");
        assertEquals(123, deserialized.get("amount").asInt(), "Amount should match");
        assertEquals(dateTime.toString(), deserialized.get("time").asText(), "Time should match");

        // Print the serialized JSON for debugging
        System.out.println("Serialized JSON: " + new String(serialized, java.nio.charset.StandardCharsets.UTF_8));
    }
}