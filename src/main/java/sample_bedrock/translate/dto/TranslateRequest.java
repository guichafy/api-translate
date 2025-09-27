package sample_bedrock.translate.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request para tradução de termos")
public class TranslateRequest {

    @NotBlank(message = "Origin locale é obrigatório")
    @Schema(description = "Idioma de origem", example = "pt-BR", required = true)
    @JsonProperty("origin_locale")
    private String originLocale;

    @NotBlank(message = "Destination locale é obrigatório")
    @Schema(description = "Idioma de destino", example = "en-US", required = true)
    @JsonProperty("destination_locale")
    private String destinationLocale;

    @NotNull(message = "Lista de termos não pode ser nula")
    @NotEmpty(message = "Lista de termos não pode estar vazia")
    @Size(max = 100, message = "Máximo de 100 termos por requisição")
    @Schema(description = "Lista de termos para traduzir", example = "[\"Olá Chafy\", \"Como você está?\"]", required = true)
    private List<String> terms;

    // Constructors
    public TranslateRequest() {}

    public TranslateRequest(String originLocale, String destinationLocale, List<String> terms) {
        this.originLocale = originLocale;
        this.destinationLocale = destinationLocale;
        this.terms = terms;
    }

    // Getters and Setters
    public String getOriginLocale() {
        return originLocale;
    }

    public void setOriginLocale(String originLocale) {
        this.originLocale = originLocale;
    }

    public String getDestinationLocale() {
        return destinationLocale;
    }

    public void setDestinationLocale(String destinationLocale) {
        this.destinationLocale = destinationLocale;
    }

    public List<String> getTerms() {
        return terms;
    }

    public void setTerms(List<String> terms) {
        this.terms = terms;
    }

    @Override
    public String toString() {
        return "TranslateRequest{" +
                "originLocale='" + originLocale + '\'' +
                ", destinationLocale='" + destinationLocale + '\'' +
                ", terms=" + terms +
                '}';
    }
}