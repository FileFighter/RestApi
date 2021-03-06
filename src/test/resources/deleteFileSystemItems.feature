Feature: FileSystem Delete
  As a user i want to delete FileSystemItems.

  Background:
    Given database is empty
    And user 1234 exists
    And user 420 exists
    And accessToken with value "900000" exists for user 1234
    And fileSystemItem with the fileSystemId 42 exists, was created by user with userId 420 has the path "/bla" and name "bla"
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 72
    And fileSystemItem with the fileSystemId 72 exists, was created by user with userId 420 and has the name "wow.txt"

  Scenario: File Deletion
    Given user with the userId 1234 is allowed to EDIT the fileSystemItem with the fileSystemId 72
    And user with the userId 1234 is allowed to VIEW the fileSystemItem with the fileSystemId 72
    And user with the userId 1234 is allowed to VIEW the fileSystemItem with the fileSystemId 42
    When user with token "900000" wants to see the content of folder with path "/bla"
    Then response status code is 200
    And the response contains the file with fileSystemId 72 and name "wow.txt"
    When user with token "900000" wants to delete the fileSystemItem with the fileSystemId 72
    Then response status code is 200
    And response contains key "message" and value "Successfully deleted all requested FileSystemItems."
    When user with token "900000" wants to see the content of folder with path "/bla"
    And the response contains an empty list for files and folders
    And response status code is 200

  Scenario: Folder and content Deletion
    Given user with the userId 1234 is allowed to EDIT the fileSystemItem with the fileSystemId 42
    And user with the userId 1234 is allowed to VIEW the fileSystemItem with the fileSystemId 42
    And user with the userId 1234 is allowed to EDIT the fileSystemItem with the fileSystemId 72
    And user with the userId 1234 is allowed to VIEW the fileSystemItem with the fileSystemId 72
    When user with token "900000" wants to delete the fileSystemItem with the fileSystemId 42
    Then response status code is 200
    When user with token "900000" wants to see the content of folder with path "/bla"
    Then response status code is 400
    And response contains key "message" and value "Folder does not exist, or you are not allowed to see the folder."

  Scenario: Folder and content Deletion with remaining content
    Given user with the userId 1234 is allowed to VIEW the fileSystemItem with the fileSystemId 42
    And user with the userId 1234 is allowed to EDIT the fileSystemItem with the fileSystemId 42
    And user with the userId 1234 is allowed to VIEW the fileSystemItem with the fileSystemId 72
    And user with the userId 1234 is allowed to EDIT the fileSystemItem with the fileSystemId 72
    And fileSystemItem with the fileSystemId 1080 exists, was created by user with userId 420 and has the name "IwillStay.txt"
    And user with the userId 1234 is allowed to VIEW the fileSystemItem with the fileSystemId 1080
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 1080
    When user with token "900000" wants to delete the fileSystemItem with the fileSystemId 42
    Then response status code is 200
    And response contains key "message" and value "Not everything got deleted, because you are not allowed to edit some files."
    When user with token "900000" wants to see the content of folder with path "/bla"
    And the response does not contains the file with fileSystemId 72 and name "wow.txt"
    And the response contains the file with fileSystemId 1080 and name "IwillStay.txt"

  Scenario: Folder and content Deletion with remaining content (invisible)
    And accessToken with value "2000000" exists for user 420
    Given user with the userId 1234 is allowed to EDIT the fileSystemItem with the fileSystemId 42
    Given user with the userId 1234 is allowed to VIEW the fileSystemItem with the fileSystemId 42
    When user with token "900000" wants to delete the fileSystemItem with the fileSystemId 42
    Then response status code is 200
      # This sucks, because for the user everything got deleted. But the message doesnt say that because "NOT EVERYTHING GOT DELETED".
    And response contains key "message" and value "Not everything got deleted, because you are not allowed to edit some files."
    When user with token "900000" wants to see the content of folder with path "/bla"
    Then response status code is 400
    And response contains key "message" and value "Folder does not exist, or you are not allowed to see the folder."
    When user with token "2000000" wants to see the content of folder with path "/bla"
    Then response status code is 200
    And the response contains the file with fileSystemId 72 and name "wow.txt"

  Scenario: recursion
    Given fileSystemItem with the fileSystemId 0 exists, was created by user with userId 1234 has the path "/foo" and name "foo"
    And fileSystemItem with the fileSystemId 0 is a folder and contains the fileSystemId 1
    And fileSystemItem with the fileSystemId 1 exists, was created by user with userId 1234 has the path "/foo/bar" and name "bar"
    And fileSystemItem with the fileSystemId 1 is a folder and contains the fileSystemId 2
    And fileSystemItem with the fileSystemId 2 exists, was created by user with userId 1234 and has the name "git.exe"
    When user with token "900000" wants to delete the fileSystemItem with the fileSystemId 0
    Then response status code is 200
    And response contains key "message" and value "Successfully deleted all requested FileSystemItems."
    When user with token "900000" wants to see the content of folder with path "/foo/bar"
    Then response status code is 400
    And response contains key "message" and value "Folder does not exist, or you are not allowed to see the folder."
    When user with token "900000" wants to see the content of folder with path "/foo"
    Then response status code is 400
    And response contains key "message" and value "Folder does not exist, or you are not allowed to see the folder."

  Scenario: recursion with remaining file
    Given fileSystemItem with the fileSystemId 0 exists, was created by user with userId 1234 has the path "/foo" and name "foo"
    And fileSystemItem with the fileSystemId 0 is a folder and contains the fileSystemId 1
    And fileSystemItem with the fileSystemId 1 exists, was created by user with userId 1234 has the path "/foo/bar" and name "bar"
    And fileSystemItem with the fileSystemId 1 is a folder and contains the fileSystemId 2
    And fileSystemItem with the fileSystemId 1 is a folder and contains the fileSystemId 3
    And fileSystemItem with the fileSystemId 2 exists, was created by user with userId 1234 and has the name "git.exe"
    And fileSystemItem with the fileSystemId 3 exists, was created by user with userId 420 and has the name "subversion.exe"
    And user with the userId 1234 is allowed to VIEW the fileSystemItem with the fileSystemId 3
    When user with token "900000" wants to delete the fileSystemItem with the fileSystemId 0
    Then response status code is 200
    And response contains key "message" and value "Not everything got deleted, because you are not allowed to edit some files."
    When user with token "900000" wants to see the content of folder with path "/foo/bar"
    Then response status code is 200
    And the response contains the file with fileSystemId 3 and name "subversion.exe"
    When user with token "900000" wants to see the content of folder with path "/foo"
    Then response status code is 200
    And the response contains the folder with fileSystemId 1 and name "bar"
    And the response does not contains the file with fileSystemId 2 and name "git.exe"

  Scenario: insufficient authorization
    Given user with the userId 1234 is allowed to VIEW the fileSystemItem with the fileSystemId 42
    When user with token "900000" wants to delete the fileSystemItem with the fileSystemId 42
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