package moment.notification.infrastructure.expo;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class ExpoPushApiClient {

    private static final String EXPO_PUSH_SEND_URL = "https://exp.host/--/api/v2/push/send";
    private static final String EXPO_PUSH_RECEIPTS_URL = "https://exp.host/--/api/v2/push/getReceipts";
    private static final int MAX_RETRIES = 3;

    private final RestClient restClient;

    public ExpoPushApiClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public List<ExpoPushTicketResponse> send(List<ExpoPushMessage> messages) {
        if (messages.isEmpty()) {
            return List.of();
        }

        return executeWithRetry(() -> {
            ExpoPushSendResponse response = restClient.post()
                    .uri(EXPO_PUSH_SEND_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(messages)
                    .retrieve()
                    .body(ExpoPushSendResponse.class);

            if (response == null || response.data() == null) {
                return List.of();
            }
            return response.data();
        });
    }

    public Map<String, ExpoPushReceipt> getReceipts(List<String> ticketIds) {
        if (ticketIds.isEmpty()) {
            return Map.of();
        }

        return executeWithRetry(() -> {
            Map<String, Object> requestBody = Map.of("ids", ticketIds);

            ExpoPushReceiptResponse response = restClient.post()
                    .uri(EXPO_PUSH_RECEIPTS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(ExpoPushReceiptResponse.class);

            if (response == null || response.data() == null) {
                return Map.of();
            }
            return response.data();
        });
    }

    private <T> T executeWithRetry(RetryableOperation<T> operation) {
        int attempt = 0;
        long delayMs = 1000;

        while (true) {
            try {
                return operation.execute();
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 429) {
                    attempt++;
                    if (attempt >= MAX_RETRIES) {
                        throw new ExpoPushApiException("Expo Push API rate limited after " + MAX_RETRIES + " retries", e);
                    }
                    String retryAfter = e.getResponseHeaders() != null
                            ? e.getResponseHeaders().getFirst("Retry-After")
                            : null;
                    long waitMs = (retryAfter != null) ? Long.parseLong(retryAfter) * 1000 : delayMs;
                    sleep(waitMs);
                    delayMs *= 2;
                    continue;
                }
                throw new ExpoPushApiException("Expo Push API client error: " + e.getStatusCode(), e);
            } catch (HttpServerErrorException e) {
                attempt++;
                if (attempt >= MAX_RETRIES) {
                    throw new ExpoPushApiException("Expo Push API server error after " + MAX_RETRIES + " retries", e);
                }
                sleep(delayMs);
                delayMs *= 2;
            } catch (RestClientException e) {
                attempt++;
                if (attempt >= MAX_RETRIES) {
                    throw new ExpoPushApiException("Expo Push API network error after " + MAX_RETRIES + " retries", e);
                }
                sleep(delayMs);
                delayMs *= 2;
            }
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExpoPushApiException("Retry interrupted", e);
        }
    }

    @FunctionalInterface
    private interface RetryableOperation<T> {
        T execute();
    }

    private record ExpoPushSendResponse(List<ExpoPushTicketResponse> data) {
    }

    private record ExpoPushReceiptResponse(Map<String, ExpoPushReceipt> data) {
    }
}
