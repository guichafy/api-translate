package sample_bedrock.translate.bdd;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import io.cucumber.spring.CucumberContextConfiguration;
import sample_bedrock.translate.service.TranslationService;

@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CucumberSpringContextConfiguration {

    @MockitoBean
    protected TranslationService translationService;
}
