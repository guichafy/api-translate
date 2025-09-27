package sample_bedrock.translate.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:" + serverPort);
        server.setDescription("Servidor de Desenvolvimento");

        Contact contact = new Contact();
        contact.setEmail("dev@translate.com");
        contact.setName("Translation API Team");

        License license = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Translation API")
                .version("1.0.0")
                .contact(contact)
                .description("API para tradução de termos utilizando AWS Bedrock")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}