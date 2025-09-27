package sample_bedrock.translate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.document.Document;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class TranslationService {

    private static final Logger logger = LoggerFactory.getLogger(TranslationService.class);

    @Value("${aws.bedrock.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.bedrock.model-id:anthropic.claude-3-sonnet-20240229-v1:0}")
    private String modelId;

    private BedrockRuntimeClient bedrockClient;

    private BedrockRuntimeClient getBedrockClient() {
        if (bedrockClient == null) {
            bedrockClient = BedrockRuntimeClient.builder()
                    .region(Region.of(awsRegion))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        }
        return bedrockClient;
    }

    public List<String> translateTerms(String originLocale, String destinationLocale, List<String> terms) {
        try {
            logger.info("Iniciando tradução de {} termos de {} para {}", terms.size(), originLocale, destinationLocale);

            String systemPrompt = buildSystemPrompt(originLocale, destinationLocale);
            String userMessage = buildUserMessage(terms);

            // Criar mensagens para a Converse API
            List<Message> messages = new ArrayList<>();
            messages.add(Message.builder()
                    .role(ConversationRole.USER)
                    .content(ContentBlock.fromText(userMessage))
                    .build());

            // Configurar parâmetros de inferência
            InferenceConfiguration inferenceConfig = InferenceConfiguration.builder()
                    .maxTokens(4000)
                    .temperature(0.1f)
                    .topP(0.9f)
                    .build();

            // Criar request para Converse API
            ConverseRequest converseRequest = ConverseRequest.builder()
                    .modelId(modelId)
                    .messages(messages)
                    .system(SystemContentBlock.fromText(systemPrompt))
                    .inferenceConfig(inferenceConfig)
                    .build();

            // Executar a tradução
            ConverseResponse response = getBedrockClient().converse(converseRequest);

            // Extrair e processar a resposta
            String translatedContent = extractTranslatedContent(response);
            List<String> translatedTerms = parseTranslatedTerms(translatedContent, terms.size());

            logger.info("Tradução concluída com sucesso para {} termos", translatedTerms.size());
            return translatedTerms;

        } catch (Exception e) {
            logger.error("Erro durante a tradução: {}", e.getMessage(), e);
            throw new RuntimeException("Falha na tradução: " + e.getMessage(), e);
        }
    }

    private String buildSystemPrompt(String originLocale, String destinationLocale) {
        return String.format(
                "Você é um tradutor profissional especializado em tradução precisa e contextual. " +
                "Sua tarefa é traduzir termos do idioma '%s' para o idioma '%s'. " +
                "Regras importantes: " +
                "1. Mantenha o contexto e o tom original " +
                "2. Para nomes próprios, mantenha-os inalterados a menos que tenham uma tradução estabelecida " +
                "3. Retorne APENAS as traduções, uma por linha, na mesma ordem dos termos originais " +
                "4. Não adicione explicações, numeração ou formatação extra " +
                "5. Se um termo não puder ser traduzido, mantenha o termo original",
                originLocale, destinationLocale
        );
    }

    private String buildUserMessage(List<String> terms) {
        StringBuilder message = new StringBuilder("Traduza os seguintes termos:\n\n");
        for (String term : terms) {
            message.append(term).append("\n");
        }
        return message.toString();
    }

    private String extractTranslatedContent(ConverseResponse response) {
        if (response.output() != null && response.output().message() != null) {
            List<ContentBlock> contentBlocks = response.output().message().content();
            if (!contentBlocks.isEmpty()) {
                ContentBlock firstBlock = contentBlocks.get(0);
                if (firstBlock.text() != null) {
                    return firstBlock.text().trim();
                }
            }
        }
        throw new RuntimeException("Resposta inválida do modelo de IA");
    }

    private List<String> parseTranslatedTerms(String translatedContent, int expectedCount) {
        String[] lines = translatedContent.split("\n");
        List<String> translatedTerms = new ArrayList<>();

        for (String line : lines) {
            String trimmedLine = line.trim();
            if (!trimmedLine.isEmpty()) {
                translatedTerms.add(trimmedLine);
            }
        }

        // Validar se o número de traduções corresponde ao esperado
        if (translatedTerms.size() != expectedCount) {
            logger.warn("Número de traduções ({}) não corresponde ao esperado ({})", 
                       translatedTerms.size(), expectedCount);
        }

        return translatedTerms;
    }
}