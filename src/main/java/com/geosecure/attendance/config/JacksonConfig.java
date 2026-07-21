package com.geosecure.attendance.config;

import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * BUG FIX (6-hour timestamp discrepancy between Faculty/Student/Admin views):
 *
 * Every timestamp column in this project (scanned_at, expires_at, created_at,
 * last_login, ...) is stored as a naive {@link LocalDateTime}, and the
 * database/JPA layer is already pinned to UTC
 * (spring.jpa.properties.hibernate.jdbc.time_zone=UTC), so the values coming
 * out of the database are correctly UTC instants.
 *
 * The problem was purely in serialization: Jackson's default handling of
 * LocalDateTime ignores `spring.jackson.time-zone` (that setting only
 * affects zone-aware types like Instant/Date) and writes a plain,
 * zone-less string such as "2026-07-21T09:15:00". A browser then does
 * `new Date("2026-07-21T09:15:00")`, and per the ECMAScript spec a
 * date-time string with no "Z"/offset is parsed as *local* time in
 * whatever timezone the browser/device is in — not UTC. So the same
 * database value was silently reinterpreted differently depending on
 * which client (and which client's timezone) rendered it, which is what
 * produced the multi-hour drift between the QR scan time on the student's
 * scanner and the value shown on faculty/admin screens.
 *
 * Fix: force a single, explicit, timezone-aware wire format everywhere.
 * Every LocalDateTime is (de)serialized as an ISO-8601 instant with a
 * trailing "Z" (e.g. "2026-07-21T09:15:00Z"), treating the naive value as
 * UTC — consistent with how it's written by the DB layer. Every client
 * (faculty, student, admin) now receives and must parse the exact same,
 * unambiguous format.
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter UTC_INSTANT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer utcLocalDateTimeCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(LocalDateTime.class, new StdSerializer<LocalDateTime>(LocalDateTime.class) {
                @Override
                public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                    gen.writeString(value.format(UTC_INSTANT_FORMAT));
                }
            });
            module.addDeserializer(LocalDateTime.class, new StdDeserializer<LocalDateTime>(LocalDateTime.class) {
                @Override
                public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    String text = p.getText().trim();
                    // Accept both our own "...Z" format and a plain offset-less
                    // string (treated as already being UTC), so existing
                    // clients / test fixtures that omit the suffix still work.
                    if (text.endsWith("Z")) {
                        return java.time.Instant.parse(text).atZone(ZoneOffset.UTC).toLocalDateTime();
                    }
                    return LocalDateTime.parse(text);
                }
            });
            builder.modules(module);
        };
    }
}
