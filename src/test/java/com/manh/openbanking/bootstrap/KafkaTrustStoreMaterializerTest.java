package com.manh.openbanking.bootstrap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaTrustStoreMaterializerTest {
    private static final String LOCATION_PROPERTY = "KAFKA_TRUSTSTORE_LOCATION";

    @AfterEach
    void clearLocationProperty() {
        System.clearProperty(LOCATION_PROPERTY);
    }

    @Test
    void materializesClasspathTrustStoreAsAFile() {
        System.clearProperty(LOCATION_PROPERTY);

        KafkaTrustStoreMaterializer.configure();

        String location = System.getProperty(LOCATION_PROPERTY);
        assertThat(location).startsWith("file:");
        assertThat(Files.isRegularFile(Path.of(URI.create(location)))).isTrue();
    }

    @Test
    void preservesExplicitSystemProperty() {
        System.setProperty(LOCATION_PROPERTY, "file:/external/aiven-truststore.p12");

        KafkaTrustStoreMaterializer.configure();

        assertThat(System.getProperty(LOCATION_PROPERTY)).isEqualTo("file:/external/aiven-truststore.p12");
    }
}
