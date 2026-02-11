package moment.notification.domain;

import java.util.Map;

public record SourceData(Map<String, Object> data) {

    public static SourceData of(Map<String, Object> data) {
        return new SourceData(data);
    }

    public static SourceData empty() {
        return new SourceData(Map.of());
    }

    public Object get(String key) {
        return data.get(key);
    }

    public Long getLong(String key) {
        Object value = data.get(key);
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
