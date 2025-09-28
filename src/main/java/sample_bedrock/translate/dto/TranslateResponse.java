package sample_bedrock.translate.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response da tradução de termos")
public record TranslateResponse(
    @Schema(description = "Lista de termos traduzidos", example = "[\"Hi, Chafy\", \"How are you?\"]", required = true)
    @JsonProperty("terms_translated")
    List<String> termsTranslated
) {}