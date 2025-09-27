package sample_bedrock.translate.config.logging;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;

import ch.qos.logback.classic.spi.ILoggingEvent;
import net.logstash.logback.composite.AbstractFieldJsonProvider;

/**
 * Writes HTTP request/response details collected from MDC under a single JSON object.
 */
public class HttpMdcJsonProvider extends AbstractFieldJsonProvider<ILoggingEvent> {

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
        Map<String, String> mdc = event.getMDCPropertyMap();
        if (mdc == null || mdc.isEmpty()) {
            return;
        }

        Map<String, String> http = new LinkedHashMap<>();
        putIfNotBlank(http, "method", mdc.get("http.method"));
        putIfNotBlank(http, "path", mdc.get("http.path"));
        putIfNotBlank(http, "status", mdc.get("http.status"));
        putIfNotBlank(http, "clientIp", mdc.get("http.clientIp"));
        putIfNotBlank(http, "userAgent", mdc.get("http.userAgent"));

        if (http.isEmpty()) {
            return;
        }

        generator.writeFieldName(getFieldName());
        generator.writeStartObject();
        for (Map.Entry<String, String> entry : http.entrySet()) {
            generator.writeStringField(entry.getKey(), entry.getValue());
        }
        generator.writeEndObject();
    }

    private void putIfNotBlank(Map<String, String> target, String key, String value) {
        if (value != null && !value.isBlank()) {
            target.put(key, value);
        }
    }
}
