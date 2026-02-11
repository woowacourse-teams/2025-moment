package moment.notification.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Map;
import moment.notification.domain.SourceData;

@Converter
public class SourceDataConverter implements AttributeConverter<SourceData, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(SourceData sourceData) {
        if (sourceData == null || sourceData.data() == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(sourceData.data());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("source_data JSON 변환 실패", e);
        }
    }

    @Override
    public SourceData convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) {
            return SourceData.empty();
        }
        try {
            Map<String, Object> data = objectMapper.readValue(json,
                    new TypeReference<Map<String, Object>>() {});
            return SourceData.of(data);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("source_data JSON 파싱 실패", e);
        }
    }
}
