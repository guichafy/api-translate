package sample_bedrock.translate.bdd.steps;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import sample_bedrock.translate.dto.TranslateRequest;
import sample_bedrock.translate.service.TranslationService;

public class TranslateControllerStepDefinitions {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final TranslationService translationService;
    private final WebApplicationContext webApplicationContext;

    private String originLocale;
    private String destinationLocale;
    private List<String> requestTerms;
    private List<String> mockedTranslations;
    private ResultActions response;

    @Autowired
    public TranslateControllerStepDefinitions(
            WebApplicationContext webApplicationContext,
            ObjectMapper objectMapper,
            TranslationService translationService) {
        this.webApplicationContext = webApplicationContext;
        this.objectMapper = objectMapper;
        this.translationService = translationService;
    }

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        originLocale = null;
        destinationLocale = null;
        requestTerms = new ArrayList<>();
        mockedTranslations = new ArrayList<>();
        response = null;
        reset(translationService);
    }

    @Given("a translation request from {string} to {string} with terms:")
    public void a_translation_request_with_terms(String origin, String destination, DataTable termsTable) {
        this.originLocale = origin;
        this.destinationLocale = destination;
        this.requestTerms = new ArrayList<>(termsTable.asList());
    }

    @Given("a translation request from {string} to {string} with no terms")
    public void a_translation_request_with_no_terms(String origin, String destination) {
        this.originLocale = origin;
        this.destinationLocale = destination;
        this.requestTerms = new ArrayList<>();
    }

    @Given("the translation service returns:")
    public void the_translation_service_returns(DataTable translatedTable) {
        this.mockedTranslations = new ArrayList<>(translatedTable.asList());
        when(translationService.translateTerms(originLocale, destinationLocale, requestTerms))
                .thenReturn(mockedTranslations);
    }

    @When("the client calls POST {string}")
    public void the_client_calls_post_translate(String path) throws Exception {
        TranslateRequest requestBody = new TranslateRequest(originLocale, destinationLocale, requestTerms);
        response = mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)));
    }

    @Then("the response status is {int}")
    public void the_response_status_is(int statusCode) throws Exception {
        response.andExpect(status().is(statusCode));
    }

    @Then("the response JSON contains translated terms:")
    public void the_response_json_contains_translated_terms(DataTable expectedTable) throws Exception {
        List<String> expectedTerms = expectedTable.asList();
        response.andExpect(jsonPath("$.terms_translated", hasSize(expectedTerms.size())));
        for (int index = 0; index < expectedTerms.size(); index++) {
            response.andExpect(jsonPath("$.terms_translated[" + index + "]").value(expectedTerms.get(index)));
        }
    }

    @Then("the response JSON has array size {int} for terms")
    public void the_response_json_has_array_size_for_terms(int expectedSize) throws Exception {
        response.andExpect(jsonPath("$.terms_translated", hasSize(expectedSize)));
    }

    @Then("the translation service is invoked with the request payload")
    public void the_translation_service_is_invoked_with_the_request_payload() {
        verify(translationService).translateTerms(originLocale, destinationLocale, requestTerms);
    }

    @Then("no translation service call is performed")
    public void no_translation_service_call_is_performed() {
        verifyNoInteractions(translationService);
    }

    @Then("the response JSON message contains {string}")
    public void the_response_json_message_contains(String expectedMessageFragment) throws Exception {
        response.andExpect(jsonPath("$.message", containsString(expectedMessageFragment)));
    }
}
