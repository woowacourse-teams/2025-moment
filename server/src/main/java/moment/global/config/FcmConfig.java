package moment.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FcmConfig {

    @Value("${fcm.service-account-json}")
    private String serviceAccountJson;

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        if (serviceAccountJson.isEmpty()) {
            log.warn("FCM service account JSON is not configured. FirebaseMessaging bean will not be created.");
            return null;
        }

        try (InputStream serviceAccountStream = new FileInputStream(serviceAccountJson)) {
            FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                    .build();

            FirebaseApp firebaseApp = getFirebaseApp(firebaseOptions);
            return FirebaseMessaging.getInstance(firebaseApp);
        }
    }

    private FirebaseApp getFirebaseApp(FirebaseOptions options) {
        if (FirebaseApp.getApps().isEmpty()) {
            log.info("FirebaseApp initialized successfully.");
            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }
}
