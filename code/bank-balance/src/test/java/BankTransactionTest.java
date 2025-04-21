import com.brhenqu.Main;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BankTransactionTest {

    @Test
    public void newRandomTransaction() throws JsonProcessingException {
        ProducerRecord<String, String> record = Main.newRandomTransaction("john");
        String key = record.key();
        String value = record.value();

        assertEquals("john", key);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(value);
        assertEquals("john", jsonNode.get("Name").asText());
        assertTrue(jsonNode.get("amount").asInt() < 100, "Amount should be less than 100");

    }
}
