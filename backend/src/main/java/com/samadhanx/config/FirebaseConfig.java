package com.samadhanx.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    private final ResourceLoader resourceLoader;

    @Value("${samadhanx.firebase.credentials-path:}")
    private String credentialsPath;

    @Value("${samadhanx.firebase.enabled:false}")
    private boolean firebaseEnabled;

    public FirebaseConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void initializeFirebase() {
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("FirebaseApp already initialized");
            return;
        }

        try {
            InputStream serviceAccountStream = null;

            // 1. Check explicit configuration path
            if (credentialsPath != null && !credentialsPath.isBlank()) {
                if (credentialsPath.startsWith("classpath:")) {
                    Resource resource = resourceLoader.getResource(credentialsPath);
                    if (resource.exists()) {
                        serviceAccountStream = resource.getInputStream();
                        log.info("Loading Firebase credentials from classpath: {}", credentialsPath);
                    }
                } else {
                    File file = new File(credentialsPath);
                    if (file.exists() && file.isFile()) {
                        serviceAccountStream = new FileInputStream(file);
                        log.info("Loading Firebase credentials from file: {}", credentialsPath);
                    }
                }
            }

            // 2. Check default resources location: firebase-service-account.json
            if (serviceAccountStream == null) {
                Resource defaultResource = resourceLoader.getResource("classpath:firebase-service-account.json");
                if (defaultResource.exists()) {
                    serviceAccountStream = defaultResource.getInputStream();
                    log.info("Loading Firebase credentials from default classpath: firebase-service-account.json");
                }
            }

            // 3. Check environment variable GOOGLE_APPLICATION_CREDENTIALS
            if (serviceAccountStream == null) {
                String envCredentials = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
                if (envCredentials != null && !envCredentials.isBlank()) {
                    File envFile = new File(envCredentials);
                    if (envFile.exists()) {
                        serviceAccountStream = new FileInputStream(envFile);
                        log.info("Loading Firebase credentials from GOOGLE_APPLICATION_CREDENTIALS: {}", envCredentials);
                    }
                }
            }

            if (serviceAccountStream != null) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK initialized successfully for real FCM push messaging.");
            } else {
                log.info("Firebase service account credentials not found. Running in Development mode (In-app notifications active).");
            }
        } catch (Exception e) {
            log.warn("Firebase Admin SDK initialization notice: {}. (Push notifications will fall back gracefully)", e.getMessage());
        }
    }
}
