Feature: Translation endpoint behaviour
  Validate that the translation controller maps requests correctly, returns proper HTTP status codes, and produces the expected JSON structure.

  Scenario: Tradução do inglês para o português
    Given a translation request from "en-US" to "pt-BR" with terms:
      | Hello |
      | How are you? |
    And the translation service returns:
      | Olá |
      | Como você está? |
    When the client calls POST "/api/v1/translate"
    Then the response status is 200
    And the response JSON contains translated terms:
      | Olá |
      | Como você está? |
    And the response JSON has array size 2 for terms
    And the translation service is invoked with the request payload

  Scenario: Tradução do português para o inglês
    Given a translation request from "pt-BR" to "en-US" with terms:
      | Bom dia |
      | Obrigado |
    And the translation service returns:
      | Good morning |
      | Thank you |
    When the client calls POST "/api/v1/translate"
    Then the response status is 200
    And the response JSON contains translated terms:
      | Good morning |
      | Thank you |
    And the response JSON has array size 2 for terms
    And the translation service is invoked with the request payload

  Scenario: Rejeitar requisição com lista de termos vazia
    Given a translation request from "en-US" to "pt-BR" with no terms
    When the client calls POST "/api/v1/translate"
    Then the response status is 400
    And the response JSON message contains "Lista de termos não pode estar vazia"
    And no translation service call is performed
