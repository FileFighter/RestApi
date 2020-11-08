Feature: User Authorization
  As a user
  I want to be able to log in with username and password, as well as verify my identity
  when using the endpoints.

Background:
  Given database is empty
  And user with id 1234 exists and has username "user", password "secure_password" and refreshToken "token"

Scenario: Successful login with username and password.
  When user requests login with username "user" and password "secure_password"
  Then response status code is 200
  And response contains refreshToken "token" and the user with id 1234

Scenario: Failed login with wrong username or password.
  When user requests login with username "user" and password "wrong_password"
  Then response contains key "message" and value "No User found with this username and password."
  And response contains key "status" and value "denied"
  And response status code is 404

Scenario: Successful creation of new accessToken with refreshToken.
  When user requests accessToken with refreshToken "token" and userId 1234
  Then response contains key "userId" and value "1234"
  And response contains valid accessToken for user 1234
  And response status code is 200

Scenario: Successful retrieval of existing accessToken with refreshToken.
  Given accessToken with value "6bb9cb4f-7b51-4c0a-8013-ed7a34e56282" exists for user 1234
  When user requests accessToken with refreshToken "token" and userId 1234
  Then response contains key "userId" and value "1234"
  And response contains valid accessToken for user 1234
  And response status code is 200

  # Better scenario description?
Scenario: Successful retrieval of overwritten accessToken with refreshToken
  Given accessToken with value "6bb9cb4f-7b51-4c0a-8013-ed7a34e56282" exists for user 1234 and is valid until 0
  When user requests accessToken with refreshToken "token" and userId 1234
  Then response contains key "userId" and value "1234"
  And response contains valid accessToken for user 1234 with a different value than "6bb9cb4f-7b51-4c0a-8013-ed7a34e56282"
  And response status code is 200

  Scenario: Failed retrieval of accessToken with wrong refreshToken.
  When user requests accessToken with refreshToken "not_the_token" and userId 1234
  Then response contains key "message" and value "Could not find user 1234"
  And response contains key "status" and value "denied"
  And response status code is 404

Scenario: Successful UserInfo request with valid accessToken.
  Given accessToken with value "6bb9cb4f-7b51-4c0a-8013-ed7a34e56282" exists for user 1234
  When user requests userInfo with accessToken "6bb9cb4f-7b51-4c0a-8013-ed7a34e56282" and userId 1234
  Then response contains the user with id 1234
  And response status code is 200

Scenario: Failed UserInfo request with invalid accessToken.
  When user requests userInfo with accessToken "tokenInWrongFormat" and userId 1234
  Then response contains key "message" and value "User with the id 1234 could not be authenticated."
  And response contains key "status" and value "denied"
  And response status code is 401