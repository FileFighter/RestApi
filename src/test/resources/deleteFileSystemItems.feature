Feature: FileSystem Delete
  As a user i want to delete FileSystemItems.

  Background:
    Given database is empty
    And user with userId 1234 exists and has username "Richard", password "badPassword"
    And user with userId 420 exists and has username "Nasir", password "AlsoBadPassword"
    And accessToken with value "900000" exists for user 1234
    And accessToken with value "222222" exists for user 420
    And fileSystemItem with the fileSystemId 42 exists, was created by user with userId 1234 has the path "/bla" and name "bla"
    # todo add owner? (1234 in this case)
    And fileSystemItem with the fileSystemId 72 exists, was created by user with userId 1234 and has the name "wow.txt"
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 72

  Scenario: File Deletion
    When user with token "900000" wants to see the content of folder with path "/Richard/bla"
    Then response status code is 200
    And the response contains the file with fileSystemId 72 and name "wow.txt"
    When user with token "900000" wants to delete the fileSystemItem with the fileSystemId 72
    Then response status code is 200
    #And response contains key "message" and value "Successfully deleted all requested FileSystemItems."
    When user with token "900000" wants to see the content of folder with path "/Richard/bla"
    Then the response contains an empty list for files and folders
    And response status code is 200

  Scenario: Folder and content Deletion
    When user with token "900000" wants to delete the fileSystemItem with the fileSystemId 42
    Then response status code is 200
    When user with token "900000" wants to see the content of folder with path "/Richard/bla"
    Then response status code is 400
    And response contains key "message" and value "Folder does not exist, or you are not allowed to see the folder."

    # todo add owners
  Scenario: recursion
    Given fileSystemItem with the fileSystemId 0 exists, was created by user with userId 1234 has the path "/foo" and name "foo"
    And fileSystemItem with the fileSystemId 0 is a folder and contains the fileSystemId 1
    And fileSystemItem with the fileSystemId 1 exists, was created by user with userId 1234 has the path "/foo/bar" and name "bar"
    And fileSystemItem with the fileSystemId 1 is a folder and contains the fileSystemId 2
    And fileSystemItem with the fileSystemId 2 exists, was created by user with userId 1234 and has the name "git.exe"
    When user with token "900000" wants to delete the fileSystemItem with the fileSystemId 0
    Then response status code is 200
    When user with token "900000" wants to see the content of folder with path "/Richard/foo/bar"
    Then response status code is 400
    And response contains key "message" and value "Folder does not exist, or you are not allowed to see the folder."
    When user with token "900000" wants to see the content of folder with path "/Richard/foo"
    Then response status code is 400
    And response contains key "message" and value "Folder does not exist, or you are not allowed to see the folder."


  Scenario: insufficient authorization
    Given user with the userId 420 is allowed to VIEW the fileSystemItem with the fileSystemId 42
    When user with token "222222" wants to delete the fileSystemItem with the fileSystemId 42
    Then response status code is 400
    And response contains key "message" and value "FileSystemEntity could not be deleted. FileSystemId was 42"

  Scenario: insufficient permission
    Given user 9877 exists
    And accessToken with value "2345678" exists for user 9877
    When user with token "2345678" wants to delete the fileSystemItem with the fileSystemId 42
    Then response status code is 400
    And response contains key "message" and value "FileSystemEntity could not be deleted. FileSystemId was 42"

  Scenario: Folder does not exist
    When user with token "900000" wants to delete the fileSystemItem with the fileSystemId 42432567
    Then response status code is 400
    And response contains key "message" and value "FileSystemEntity could not be deleted. FileSystemId was 42432567"

    # todo
  Scenario: Folder was created by runtime user.
    Given database is empty
    # If this fails check the runtime user id.
    And fileSystemItem with the fileSystemId 0 exists, was created by user with userId 0 has the path "/" and name "HOME_kevin"
    And user with userId 123123123 exists and has username "kevin", password "securePassword123"
    And user with the userId 123123123 is allowed to VIEW the fileSystemItem with the fileSystemId 0
    And user with the userId 123123123 is allowed to EDIT the fileSystemItem with the fileSystemId 0
    And accessToken with value "token" exists for user 123123123
    When user with token "token" wants to delete the fileSystemItem with the fileSystemId 0
    Then response status code is 400
    And response contains key "message" and value "FileSystemEntity could not be deleted. FileSystemId was 0"

    # todo
  Scenario: File was created by runtime user.
    Given database is empty
    And user with userId 123123123 exists and has username "kevin", password "securePassword123"
    And user with the userId 123123123 is allowed to VIEW the fileSystemItem with the fileSystemId 2
    And user with the userId 123123123 is allowed to EDIT the fileSystemItem with the fileSystemId 2
    And accessToken with value "token" exists for user 123123123
    And fileSystemItem with the fileSystemId 0 exists, was created by user with userId 123123123 has the path "/foo" and name "foo"
    And fileSystemItem with the fileSystemId 0 is a folder and contains the fileSystemId 1
    And fileSystemItem with the fileSystemId 1 exists, was created by user with userId 123123123 has the path "/foo/bar" and name "bar"
    And fileSystemItem with the fileSystemId 1 is a folder and contains the fileSystemId 2
    And fileSystemItem with the fileSystemId 2 exists, was created by user with userId 0 and has the name "veryImportantDocumentDon'tDeleteMePls.exe"
    When user with token "token" wants to delete the fileSystemItem with the fileSystemId 0
    Then response status code is 200
    And response contains key "message" and value "Not everything got deleted, because you are not allowed to edit some files."
