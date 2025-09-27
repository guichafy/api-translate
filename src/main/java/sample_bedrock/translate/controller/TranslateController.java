package sample_bedrock.translate.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import sample_bedrock.translate.dto.TranslateRequest;
import sample_bedrock.translate.dto.TranslateResponse;
import sample_bedrock.translate.exception.ErrorResponse;
import sample_bedrock.translate.service.TranslationService;

@RestController
@RequestMapping("/api/v1")
@Validated
@Tag(name = "Translation API", description = "API para tradução de termos usando AWS Bedrock")
public class TranslateController {

    private static final Logger logger = LoggerFactory.getLogger(TranslateController.class);

    @Autowired
    private TranslationService translationService;

    @PostMapping("/translate")
    @Operation(
        summary = "Traduzir termos",
        description = "Traduz uma lista de termos de um idioma para outro usando AWS Bedrock"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tradução realizada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TranslateResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados de entrada inválidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<TranslateResponse> translate(@Valid @RequestBody TranslateRequest request) {
        try {
            logger.info("Recebida requisição de tradução: {} -> {}, {} termos", 
                       request.getOriginLocale(), 
                       request.getDestinationLocale(), 
                       request.getTerms().size());

            List<String> translatedTerms = translationService.translateTerms(
                request.getOriginLocale(),
                request.getDestinationLocale(),
                request.getTerms()
            );

            TranslateResponse response = new TranslateResponse(translatedTerms);
            
            logger.info("Tradução concluída com sucesso para {} termos", translatedTerms.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erro durante a tradução: {}", e.getMessage(), e);
            throw e; // Será tratado pelo GlobalExceptionHandler
        }
    }
}