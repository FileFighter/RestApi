Feature: SystemHealth
  As a user
  I want to be able to get status information about the state of the application.

Background:
  Given database is empty

Scenario: SystemHealth is requested without users in db
  When the systemHealth endpoint is requested
  Then response contains key "userCount" and value "0"
  And response contains key "uptimeInSeconds" and value of at least 1

Scenario: SystemHealth is requested with users in db
  Given user 1234 exists
  And user 3214 exists
  And the user waits for 1 second(s)
  When the systemHealth endpoint is requested
  Then response contains key "userCount" and value "2"
  And response contains key "uptimeInSeconds" and value of at least 1

