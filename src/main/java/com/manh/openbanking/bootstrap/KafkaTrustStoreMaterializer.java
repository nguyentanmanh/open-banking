package com.manh.openbanking.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

final class KafkaTrustStoreMaterializer {
    private static final String LOCATION_PROPERTY = "KAFKA_TRUSTSTORE_LOCATION";
    private static final String CLASSPATH_LOCATION = "/certs/aiven-truststore.p12";

    private KafkaTrustStoreMaterializer() {
    }

    static void configure() {
        if (hasExternalTrustStoreLocation()) {
            return;
        }

        try (InputStream trustStore = KafkaTrustStoreMaterializer.class.getResourceAsStream(CLASSPATH_LOCATION)) {
            if (trustStore == null) {
                throw new IllegalStateException("Kafka truststore resource is missing: " + CLASSPATH_LOCATION);
            }

            Path materializedTrustStore = Files.createTempFile("open-banking-kafka-truststore-", ".p12");
            Files.copy(trustStore, materializedTrustStore, StandardCopyOption.REPLACE_EXISTING);
            materializedTrustStore.toFile().deleteOnExit();
            System.setProperty(LOCATION_PROPERTY, materializedTrustStore.toUri().toString());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to materialize the Kafka truststore", exception);
        }
    }

    private static boolean hasExternalTrustStoreLocation() {
        String environmentValue = System.getenv(LOCATION_PROPERTY);
        String systemValue = System.getProperty(LOCATION_PROPERTY);
        return environmentValue != null && !environmentValue.isBlank()
            || systemValue != null && !systemValue.isBlank();
    }
}
