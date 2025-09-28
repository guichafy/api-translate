package sample_bedrock.translate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseRequest;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;


@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {
    "aws.bedrock.region=us-east-1",
    "aws.bedrock.model-id=anthropic.claude-3-sonnet-20240229-v1:0"
})
@DisplayName("TranslationService Tests")
class TranslationServiceTest {

    @Mock
    private BedrockRuntimeClient bedrockClient;

    @InjectMocks
    private TranslationService translationService;

    @BeforeEach
    void setUp() {
        // Configurar propriedades usando ReflectionTestUtils
        ReflectionTestUtils.setField(translationService, "awsRegion", "us-east-1");
        ReflectionTestUtils.setField(translationService, "modelId", "anthropic.claude-3-sonnet-20240229-v1:0");
        ReflectionTestUtils.setField(translationService, "bedrockClient", bedrockClient);
    }

    @Nested
    @DisplayName("translateTerms - Cenários de Sucesso")
    class TranslateTermsSuccessTests {

        @Test
        @DisplayName("Deve traduzir termos com sucesso")
        void shouldTranslateTermsSuccessfully() {
            // Given
            String originLocale = "pt-BR";
            String destinationLocale = "en-US";
            List<String> terms = Arrays.asList("casa", "carro", "computador");
            
            String expectedTranslation = "house\ncar\ncomputer";
            
            ConverseResponse mockResponse = createMockConverseResponse(expectedTranslation, "request-123");
            when(bedrockClient.converse(any(ConverseRequest.class))).thenReturn(mockResponse);

            // When
            List<String> result = translationService.translateTerms(originLocale, destinationLocale, terms);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result).containsExactly("house", "car", "computer");
            
            verify(bedrockClient).converse(any(ConverseRequest.class));
        }

        @Test
        @DisplayName("Deve traduzir um único termo")
        void shouldTranslateSingleTerm() {
            // Given
            String originLocale = "en-US";
            String destinationLocale = "pt-BR";
            List<String> terms = Collections.singletonList("hello");
            
            String expectedTranslation = "olá";
            
            ConverseResponse mockResponse = createMockConverseResponse(expectedTranslation, "request-456");
            when(bedrockClient.converse(any(ConverseRequest.class))).thenReturn(mockResponse);

            // When
            List<String> result = translationService.translateTerms(originLocale, destinationLocale, terms);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result).containsExactly("olá");
        }

        @Test
        @DisplayName("Deve lidar com traduções que contêm linhas em branco")
        void shouldHandleTranslationsWithBlankLines() {
            // Given
            String originLocale = "pt-BR";
            String destinationLocale = "en-US";
            List<String> terms = Arrays.asList("bom", "dia");
            
            String expectedTranslation = "good\n\nday\n";
            
            ConverseResponse mockResponse = createMockConverseResponse(expectedTranslation, "request-789");
            when(bedrockClient.converse(any(ConverseRequest.class))).thenReturn(mockResponse);

            // When
            List<String> result = translationService.translateTerms(originLocale, destinationLocale, terms);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly("good", "day");
        }

        @Test
        @DisplayName("Deve funcionar sem request ID no response metadata")
        void shouldWorkWithoutRequestId() {
            // Given
            String originLocale = "pt-BR";
            String destinationLocale = "en-US";
            List<String> terms = Arrays.asList("teste");
            
            String expectedTranslation = "test";
            
            ConverseResponse mockResponse = createMockConverseResponse(expectedTranslation, null);
            when(bedrockClient.converse(any(ConverseRequest.class))).thenReturn(mockResponse);

            // When
            List<String> result = translationService.translateTerms(originLocale, destinationLocale, terms);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result).containsExactly("test");
        }
    }

    @Nested
    @DisplayName("translateTerms - Cenários de Erro")
    class TranslateTermsErrorTests {

        @Test
        @DisplayName("Deve lançar exceção quando BedrockClient falha")
        void shouldThrowExceptionWhenBedrockClientFails() {
            // Given
            String originLocale = "pt-BR";
            String destinationLocale = "en-US";
            List<String> terms = Arrays.asList("casa", "carro");
            
            when(bedrockClient.converse(any(ConverseRequest.class)))
                .thenThrow(new RuntimeException("AWS Bedrock error"));

            // When & Then
            assertThatThrownBy(() -> translationService.translateTerms(originLocale, destinationLocale, terms))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha na tradução: AWS Bedrock error")
                .hasCauseInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando resposta é inválida - sem output")
        void shouldThrowExceptionWhenResponseHasNoOutput() {
            // Given
            String originLocale = "pt-BR";
            String destinationLocale = "en-US";
            List<String> terms = Arrays.asList("teste");
            
            ConverseResponse mockResponse = ConverseResponse.builder().build();
            when(bedrockClient.converse(any(ConverseRequest.class))).thenReturn(mockResponse);

            // When & Then
            assertThatThrownBy(() -> translationService.translateTerms(originLocale, destinationLocale, terms))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha na tradução: Resposta inválida do modelo de IA");
        }

        @Test
        @DisplayName("Deve lançar exceção quando resposta é inválida - sem message")
        void shouldThrowExceptionWhenResponseHasNoMessage() {
            // Given
            String originLocale = "pt-BR";
            String destinationLocale = "en-US";
            List<String> terms = Arrays.asList("teste");
            
            ConverseResponse mockResponse = ConverseResponse.builder()
                .output(builder -> builder.build())
                .build();
            when(bedrockClient.converse(any(ConverseRequest.class))).thenReturn(mockResponse);

            // When & Then
            assertThatThrownBy(() -> translationService.translateTerms(originLocale, destinationLocale, terms))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha na tradução: Resposta inválida do modelo de IA");
        }

        @Test
        @DisplayName("Deve lançar exceção quando resposta é inválida - sem content blocks")
        void shouldThrowExceptionWhenResponseHasNoContentBlocks() {
            // Given
            String originLocale = "pt-BR";
            String destinationLocale = "en-US";
            List<String> terms = Arrays.asList("teste");
            
            ConverseResponse mockResponse = ConverseResponse.builder()
                .output(outputBuilder -> outputBuilder
                    .message(messageBuilder -> messageBuilder
                        .content(Collections.emptyList())
                        .build())
                    .build())
                .build();
            when(bedrockClient.converse(any(ConverseRequest.class))).thenReturn(mockResponse);

            // When & Then
            assertThatThrownBy(() -> translationService.translateTerms(originLocale, destinationLocale, terms))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha na tradução: Resposta inválida do modelo de IA");
        }

        @Test
        @DisplayName("Deve lançar exceção quando content block não tem texto")
        void shouldThrowExceptionWhenContentBlockHasNoText() {
            // Given
            String originLocale = "pt-BR";
            String destinationLocale = "en-US";
            List<String> terms = Arrays.asList("teste");
            
            // Criar um ContentBlock vazio (sem texto)
            ContentBlock emptyContentBlock = ContentBlock.builder().build();
            
            ConverseResponse mockResponse = ConverseResponse.builder()
                .output(outputBuilder -> outputBuilder
                    .message(messageBuilder -> messageBuilder
                        .content(Collections.singletonList(emptyContentBlock))
                        .build())
                    .build())
                .build();
            when(bedrockClient.converse(any(ConverseRequest.class))).thenReturn(mockResponse);

            // When & Then
            assertThatThrownBy(() -> translationService.translateTerms(originLocale, destinationLocale, terms))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha na tradução: Resposta inválida do modelo de IA");
        }
    }

    @Nested
    @DisplayName("Testes de Inicialização do BedrockClient")
    class BedrockClientInitializationTests {
        
        @Test
        @DisplayName("Deve usar BedrockClient existente quando disponível")
        void shouldUseExistingBedrockClientWhenAvailable() {
            // Given
            String originLocale = "pt-BR";
            String destinationLocale = "en-US";
            List<String> terms = Arrays.asList("teste");
            
            ConverseResponse mockResponse = createMockConverseResponse("test", null);
            when(bedrockClient.converse(any(ConverseRequest.class))).thenReturn(mockResponse);

            // When
            List<String> result = translationService.translateTerms(originLocale, destinationLocale, terms);

            // Then
            assertThat(result).isNotNull();
            verify(bedrockClient).converse(any(ConverseRequest.class));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Deve lidar com lista vazia de termos")
        void shouldHandleEmptyTermsList() {
            // Given
            String originLocale = "pt-BR";
            String destinationLocale = "en-US";
            List<String> terms = Collections.emptyList();
            
            String expectedTranslation = "";
            
            ConverseResponse mockResponse = createMockConverseResponse(expectedTranslation, "request-empty");
            when(bedrockClient.converse(any(ConverseRequest.class))).thenReturn(mockResponse);

            // When
            List<String> result = translationService.translateTerms(originLocale, destinationLocale, terms);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Deve lidar com número diferente de traduções")
        void shouldHandleDifferentNumberOfTranslations() {
            // Given
            String originLocale = "pt-BR";
            String destinationLocale = "en-US";
            List<String> terms = Arrays.asList("casa", "carro", "computador");
            
            // Resposta com apenas 2 traduções em vez de 3
            String expectedTranslation = "house\ncar";
            
            ConverseResponse mockResponse = createMockConverseResponse(expectedTranslation, "request-mismatch");
            when(bedrockClient.converse(any(ConverseRequest.class))).thenReturn(mockResponse);

            // When
            List<String> result = translationService.translateTerms(originLocale, destinationLocale, terms);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly("house", "car");
        }

        @Test
        @DisplayName("Deve lidar com termos que contêm caracteres especiais")
        void shouldHandleTermsWithSpecialCharacters() {
            // Given
            String originLocale = "pt-BR";
            String destinationLocale = "en-US";
            List<String> terms = Arrays.asList("coração", "ação", "não");
            
            String expectedTranslation = "heart\naction\nno";
            
            ConverseResponse mockResponse = createMockConverseResponse(expectedTranslation, "request-special");
            when(bedrockClient.converse(any(ConverseRequest.class))).thenReturn(mockResponse);

            // When
            List<String> result = translationService.translateTerms(originLocale, destinationLocale, terms);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result).containsExactly("heart", "action", "no");
        }
    }

    // Método auxiliar para criar ConverseResponse usando builders
    private ConverseResponse createMockConverseResponse(String translatedText, String requestId) {
        ContentBlock contentBlock = ContentBlock.fromText(translatedText);
        
        // Usar apenas builders - não fazer mock de classes finais
        ConverseResponse.Builder responseBuilder = ConverseResponse.builder()
            .output(outputBuilder -> outputBuilder
                .message(messageBuilder -> messageBuilder
                    .content(Collections.singletonList(contentBlock))
                    .build())
                .build());
        
        // Para requestId, vamos simular através do comportamento esperado nos testes
        // em vez de tentar mockar responseMetadata
        return responseBuilder.build();
    }
}