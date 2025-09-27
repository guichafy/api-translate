package sample_bedrock.translate.config.logging;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;

import ch.qos.logback.classic.spi.ILoggingEvent;
import net.logstash.logback.composite.AbstractFieldJsonProvider;

/**
 * Extracts AWS Bedrock related metadata from MDC and writes it under the "aws" field.
 */
public class AwsMdcJsonProvider extends AbstractFieldJsonProvider<ILoggingEvent> {

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
        Map<String, String> mdc = event.getMDCPropertyMap();
        if (mdc == null || mdc.isEmpty()) {
            return;
        }

        Map<String, String> aws = new LinkedHashMap<>();
        putIfNotBlank(aws, "bedrockModel", mdc.get("aws.bedrockModel"));
        putIfNotBlank(aws, "region", mdc.get("aws.region"));
        putIfNotBlank(aws, "bedrockRequestId", mdc.get("aws.bedrockRequestId"));

        if (aws.isEmpty()) {
            return;
        }

        generator.writeFieldName(getFieldName());
        generator.writeStartObject();
        for (Map.Entry<String, String> entry : aws.entrySet()) {
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
