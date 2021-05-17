Feature: User Authorization
  As a user
  I want to be able to log in with username and password, as well as verify my identity
  when using the endpoints.

Background:
  Given database is empty
  And user with userId 1234 exists and has username "user", password "86C9C198F7DF1F0E6633E21A12BCA14730A27070BBCC742FEC8B2B14B44A0126" and refreshToken "token"

Scenario: Successful login with username and password.
  When user requests login with username "user" and password "86C9C198F7DF1F0E6633E21A12BCA14730A27070BBCC742FEC8B2B14B44A0126"
  Then response status code is 200
  And response contains refreshToken "token" and the user with userId 1234

  Scenario: Successful login with username in different Case and password.
    When user requests login with username "UsEr" and password "86C9C198F7DF1F0E6633E21A12BCA14730A27070BBCC742FEC8B2B14B44A0126"
    Then response status code is 200
    And response contains refreshToken "token" and the user with userId 1234

  Scenario: Successful login with username in different Case, whiteSpaces and password.
    When user requests login with username "U  s E r" and password "86C9C198F7DF1F0E6633E21A12BCA14730A27070BBCC742FEC8B2B14B44A0126"
    Then response status code is 200
    And response contains refreshToken "token" and the user with userId 1234

  Scenario: Failed login with wrong username or password.
  # the hash is different.
    When user requests login with username "user" and password "86C9C198F7DF1F0E6633E21A12BCA14730A27070BBCC742FEC8B2B14B44A0128"
    Then response contains key "message" and value "User could not be authenticated. No User found with this username and password."
    And response contains key "status" and value "Unauthorized"
    And response status code is 401

  Scenario: Successful creation of new accessToken with refreshToken.
    When user requests accessToken with refreshToken "token"
    Then response contains key "userId" and value "1234"
    And response contains valid accessToken for user 1234
    And response status code is 200

  Scenario: Successful retrieval of existing accessToken with refreshToken.
    Given accessToken with value "6bb9cb4f-7b51-4c0a-8013-ed7a34e56282" exists for user 1234
    When user requests accessToken with refreshToken "token"
  Then response contains key "userId" and value "1234"
  And response contains valid accessToken for user 1234
  And response status code is 200

  # Better scenario description?
Scenario: Successful retrieval of overwritten accessToken with refreshToken
  Given accessToken with value "6bb9cb4f-7b51-4c0a-8013-ed7a34e56282" exists for user 1234 and is valid until 0
  When user requests accessToken with refreshToken "token"
  Then response contains key "userId" and value "1234"
  And response contains valid accessToken for user 1234 with a different value than "6bb9cb4f-7b51-4c0a-8013-ed7a34e56282"
  And response status code is 200

  Scenario: Failed retrieval of accessToken with wrong refreshToken.
  When user requests accessToken with refreshToken "not_the_token"
  Then response contains key "message" and value "User could not be authenticated. No user found for this Refresh Token."
  And response contains key "status" and value "Unauthorized"
  And response status code is 401

Scenario: Successful UserInfo request with valid accessToken.
  Given accessToken with value "6bb9cb4f-7b51-4c0a-8013-ed7a34e56282" exists for user 1234
  When user requests userInfo with accessToken "6bb9cb4f-7b51-4c0a-8013-ed7a34e56282" and userId 1234
  Then response contains the user with userId 1234
  And response status code is 200

Scenario: Failed UserInfo request with invalid accessToken.
  When user requests userInfo with accessToken "tokenInWrongFormat" and userId 1234
  Then response contains key "message" and value "User could not be authenticated. AccessToken not found."
  And response contains key "status" and value "Unauthorized"
  And response status code is 401