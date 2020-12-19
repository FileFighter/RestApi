Feature: View Folder
  As a user
  I want to see the content of folders and navigate in them, so they can see and interact with their uploaded and shared files.

  Background:
    Given database is empty
    And user 1234 exists
    And accessToken with value "900000" exists for user 1234
    And fileSystemItem with the fileSystemId 42 exists and has the path "/bla"
    And fileSystemItem with the fileSystemId 72 exists and has the name "wow.txt"
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 72

  Scenario: Successful interaction
    Given user with the userId 1234 is allowed to VIEW the fileSystemItem with the fileSystemId 42
    And user with the userId 1234 is allowed to VIEW the fileSystemItem with the fileSystemId 72
    When user with token "900000" wants to see the content of folder with path "/bla"
    Then response status code is 200
    And the response contains the file with fileSystemId 72 and name "wow.txt"

  Scenario: Folder does not exist
    Given user with the userId 1234 is allowed to VIEW the fileSystemItem with the fileSystemId 42
    When user with token "900000" wants to see the content of folder with path "/bla/fasel"
    Then response status code is 400
    And response contains key "message" and value "Folder does not exist, or you are not allowed to see the folder."

  Scenario: insufficient authorization
    Given user 9877 exists
    And accessToken with value "2345678" exists for user 9877
    And response contains key "message" and value "Folder does not exist, or you are not allowed to see the folder."

  Scenario: shared folder
    Given user 4321 exists
    And accessToken with value "123321123" exists for user 4321
    And user with the userId 4321 is allowed to VIEW the fileSystemItem with the fileSystemId 73
    When user with token "123321123" wants to see the content of folder with path "/bla"
    Then response status code is 200
    And the response contains an empty list for files and folders

  Scenario: shared folder and file
    Given user 4321 exists
    And accessToken with value "123321123" exists for user 4321
    And user with the userId 4321 is allowed to VIEW the fileSystemItem with the fileSystemId 73
    And user with the userId 4321 is allowed to VIEW the fileSystemItem with the fileSystemId 42
    When user with token "123321123" wants to see the content of folder with path "/bla"
    Then response status code is 200
    And the response contains the file with fileSystemId 72 and name "wow.txt"

  Scenario: empty directory
    Given fileSystemItem with the fileSystemId 44 exists and has the path "/empty"
    And user with the userId 1234 is allowed to VIEW the fileSystemItem with the fileSystemId 44
    When user with token "900000" wants to see the content of folder with path "empty"
    Then response status code is 200
    And the response contains an empty list for files and folders