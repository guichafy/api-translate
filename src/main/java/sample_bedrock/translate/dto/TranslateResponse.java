package sample_bedrock.translate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response da tradução de termos")
public class TranslateResponse {

    @Schema(description = "Lista de termos traduzidos", example = "[\"Hi, Chafy\", \"How are you?\"]", required = true)
    @JsonProperty("terms_translated")
    private List<String> termsTranslated;

    // Constructors
    public TranslateResponse() {}

    public TranslateResponse(List<String> termsTranslated) {
        this.termsTranslated = termsTranslated;
    }

    // Getters and Setters
    public List<String> getTermsTranslated() {
        return termsTranslated;
    }

    public void setTermsTranslated(List<String> termsTranslated) {
        this.termsTranslated = termsTranslated;
    }

    @Override
    public String toString() {
        return "TranslateResponse{" +
                "termsTranslated=" + termsTranslated +
                '}';
    }
}