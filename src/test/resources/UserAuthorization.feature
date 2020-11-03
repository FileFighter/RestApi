#Feature: User Authorization
#  As a user
#  I want to be able to log in with username and password, as well as verify my identity
#  when using the endpoints.
#
#Background:
#  Given database is empty
#  And user with id 1234 exists and has username "user", password "secure_password" and refreshToken "token"
#
#Scenario: Successful login with username and password.
#  When user requests login with username "user" and password "secure_password"
#  Then response contains key "refreshToken" and value "token"
#  And response status code is 200
#  And response contains the user with id 1234
#
#Scenario: Failed login with username and password.
#  When user requests login with username "user" and password "wrong_password"
#  Then response contains key "message" and value "User not authenticated."
#  And response contains key "status" and value "denied"
#  And response status code is 401
#
#Scenario: Successful creation of new accessToken with refreshToken.
#  When user requests accessToken with refreshToken "token" and userId 1234
#  Then response contains key "userId" and value "1234"
#  And response contains valid accessToken for user 1234
#  And response status code is 200
#
#Scenario: Successful request of existing accessToken with refreshToken.
#  Given accessToken with value "token" exists for user 1234
#  When user requests accessToken with refreshToken "token" and userId 1234
#  Then response contains key "userId" and value "1234"
#  And response contains valid accessToken for user 1234
#  And response status code is 200
#
#  Scenario: Failed retrieval of accessToken with wrong refreshToken.
#  When user requests accessToken with refreshToken "not_the_token" and userId 1234
#  Then response contains key "message" and value "User not authenticated."
#  And response contains key "status" and value "denied"
#  And response status code is 401
#
#Scenario: Successful UserInfo request with valid accessToken.
#  Given accessToken with value "accessToken" exists for user 1234
#  When user requests userInfo with accessToken "accessToken" and userId 1234
#  Then response contains the user with id 1234
#  And response status code is 200
#
#Scenario: Failed UserInfo request with invalid accessToken.
#  When user requests userInfo with accessToken "notTheAccessToken" and userId 1234
#  Then response contains key "message" and value "User not authenticated."
#  And response contains key "status" and value "denied"
#  And response status code is 401