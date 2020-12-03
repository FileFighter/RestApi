Feature: SystemHealth
  As a user
  I want to be able to get status information about the state of the application.

Background:
  Given database is empty

Scenario: SystemHealth is requested without users in db
  When the systemHealth endpoint is requested
  And the user waits for 2 second(s)
  Then response contains key "userCount" and value "0"
  And response contains key "uptimeInSeconds" and value of at least 1
  And response status code is 200

Scenario: SystemHealth is requested with users in db
  Given user 1234 exists
  And user 3214 exists
  And the user waits for 2 second(s)
  When the systemHealth endpoint is requested
  Then response contains key "userCount" and value "2"
  And response contains key "uptimeInSeconds" and value of at least 1
  And response status code is 200

Scenario: SystemHealth is Unstable
  Given accessToken with value "token" exists for user 1234
  And user with userId 1234 exists and has username "user", password "pw"
  And user with userId 1234 exists and has username "user", password "pw"
  When user with accessToken "token" searches user with search-value "user"
  And response status code is 500
  And the systemHealth endpoint is requested
  Then response contains key "dataIntegrity" and value "UNSTABLE"
  And response status code is 200


