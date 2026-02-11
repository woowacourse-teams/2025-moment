package moment.notification.infrastructure.expo;

import java.util.Map;

public record ExpoPushTicketResponse(String id, String status, String message,
                                     Map<String, Object> details) {
    public boolean isOk() {
        return "ok".equals(status);
    }

    public boolean isError() {
        return "error".equals(status);
    }
}
