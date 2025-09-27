package sample_bedrock.translate.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Response de erro da API")
public record ErrorResponse(
    @Schema(description = "Timestamp do erro", example = "2024-01-15T10:30:00")
    LocalDateTime timestamp,
    
    @Schema(description = "Código de status HTTP", example = "400")
    int status,
    
    @Schema(description = "Tipo do erro", example = "Bad Request")
    String error,
    
    @Schema(description = "Mensagem de erro", example = "Dados de entrada inválidos")
    String message,
    
    @Schema(description = "Caminho da requisição", example = "/api/v1/translate")
    String path
) {
    
    // Construtor de conveniência que define o timestamp automaticamente
    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path);
    }
}