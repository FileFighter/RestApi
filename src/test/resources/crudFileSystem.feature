Feature: FileSystem CRUD
  As a user i want to create, read, update and delete FileSystemItems.

  Background:
    Given database is empty
    And user 1234 exists
    And accessToken with value "900000" exists for user 1234

  Scenario: Get FileSystemItem works
    Given fileSystemItem with the fileSystemId 1234 exists, was created by user with userId 1234 and has the name "dummyFile.pdf"
    When user requests fileSystemInfo with fileSystemId 1234 and accessTokenValue "900000"
    Then response status code is 200
    And response contains key "fileSystemId" and value "1234"
    And response contains key "name" and value "dummyFile.pdf"

  Scenario: Get FileSystemItem does not work, because it doesn't exist
    When user requests fileSystemInfo with fileSystemId 1234 and accessTokenValue "900000"
    Then response status code is 400
    And response contains key "status" and value "Bad Request"
    And response contains key "message" and value "FileSystemItem could not be found or you are not allowed to view it. FileSystemId was 1234"

  Scenario: Get FileSystemItem does not work, because user is not allowed
    Given fileSystemItem with the fileSystemId 1234 exists, was created by user with userId 9999999 and has the name "dummyFile.pdf"
    When user requests fileSystemInfo with fileSystemId 1234 and accessTokenValue "900000"
    Then response status code is 400
    And response contains key "status" and value "Bad Request"
    And response contains key "message" and value "FileSystemItem could not be found or you are not allowed to view it. FileSystemId was 1234"
