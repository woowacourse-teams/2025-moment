package moment.notification.infrastructure.expo;

import java.util.Map;

public record ExpoPushReceipt(String status, String message, Map<String, Object> details) {

    public boolean isOk() {
        return "ok".equals(status);
    }

    public boolean isDeviceNotRegistered() {
        return details != null && "DeviceNotRegistered".equals(details.get("error"));
    }

    public boolean isMessageRateExceeded() {
        return details != null && "MessageRateExceeded".equals(details.get("error"));
    }
}
