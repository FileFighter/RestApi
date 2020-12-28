Feature: FileSystem Delete
  As a user i want to delete FileSystemItems.

  Background:
    Given database is empty
    And user 1234 exists
    And accessToken with value "900000" exists for user 1234
    And fileSystemItem with the fileSystemId 42 exists, was created by user with userId 420 and has the path "/bla"
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 72
    And fileSystemItem with the fileSystemId 72 exists, was created by user with userId 420 and has the name "wow.txt"

  Scenario: File Deletion
    Given user with the userId 1234 is allowed to EDIT the fileSystemItem with the fileSystemId 72
    When user with token "900000" wants to see the content of folder with path "/bla"
    Then response status code is 200
    And the response contains the file with fileSystemId 72 and name "wow.txt"
    When user with token "900000" wants to delete the fileSystemItem with the fileSystemId 72
    Then response status code is 200
    When user with token "900000" wants to see the content of folder with path "/bla"
    And the response contains an empty list for files and folders

  Scenario: Folder and content Deletion
    Given user with the userId 1234 is allowed to EDIT the fileSystemItem with the fileSystemId 42
    And user with the userId 1234 is allowed to EDIT the fileSystemItem with the fileSystemId 72
    When user with token "900000" wants to delete the fileSystemItem with the fileSystemId 42
    Then response status code is 200
    When user with token "900000" wants to see the content of folder with path "/bla"
    Then response status code is 400
    And response contains key "message" and value "Folder does not exist, or you are not allowed to edit the folder."

  Scenario: Folder and content Deletion with remaining content
    Given user with the userId 1234 is allowed to EDIT the fileSystemItem with the fileSystemId 42
    And user with the userId 1234 is allowed to EDIT the fileSystemItem with the fileSystemId 72
    And fileSystemItem with the fileSystemId 1080 exists, was created by user with userId 420 and has the name "IwillStay.txt"
    And user with the userId 1234 is allowed to VIEW the fileSystemItem with the fileSystemId 1080
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 1080
    # this should addd the ID!!!!!!!! ^ or we make another step
    When user with token "900000" wants to delete the fileSystemItem with the fileSystemId 42
    Then response status code is 200
    And response contains key "message" and value "Not everything got deleted, because you are not allowed to edit some files."
    When user with token "900000" wants to see the content of folder with path "/bla"
    And the response not contains the file with fileSystemId 72 and name "wow.txt"
    And the response contains the file with fileSystemId 1080 and name "IwillStay.txt"

  Scenario: Folder and content Deletion with remaining content (invisible)
    And user 420 exists
    And accessToken with value "2000000" exists for user 1234
    Given user with the userId 1234 is allowed to EDIT the fileSystemItem with the fileSystemId 42
    When user with token "900000" wants to delete the fileSystemItem with the fileSystemId 42
    Then response status code is 200
    When user with token "900000" wants to see the content of folder with path "/bla"
    Then response status code is 400
    And response contains key "message" and value "Folder does not exist, or you are not allowed to edit the folder."
    When user with token "2000000" wants to see the content of folder with path "/bla"
    Then response status code is 200
    And the response contains the file with fileSystemId 72 and name "wow.txt"

  Scenario: recursion
    Given fileSystemItem with the fileSystemId 1 exists, was created by user with userId 1234 and has the path "/bla/fasel"
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 1
    And fileSystemItem with the fileSystemId 2 exists, was created by user with userId 1234 and has the name "git.exe"
    And fileSystemItem with the fileSystemId 1 is a folder and contains the fileSystemId 2
    When user with token "900000" wants to delete the fileSystemItem with the fileSystemId 42
    Then response status code is 200
    When user with token "900000" wants to see the content of folder with path "/bla/fasel"
    Then response status code is 400
    And response contains key "message" and value "Folder does not exist, or you are not allowed to edit the folder."
    When user with token "900000" wants to see the content of folder with path "/bla"
    Then response status code is 400
    And response contains key "message" and value "Folder does not exist, or you are not allowed to edit the folder."

  Scenario: recursion with remaining file
    Given fileSystemItem with the fileSystemId 1 exists, was created by user with userId 1234 and has the path "/bla/fasel"
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 1
    And fileSystemItem with the fileSystemId 2 exists, was created by user with userId 1234 and has the name "git.exe"
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 2
    And fileSystemItem with the fileSystemId 3 exists, was created by user with userId 420 and has the name "subversion.exe"
    And fileSystemItem with the fileSystemId 1 is a folder and contains the fileSystemId 3
    And user with the userId 1234 is allowed to VIEW the fileSystemItem with the fileSystemId 3
    When user with token "900000" wants to delete the fileSystemItem with the fileSystemId 42
    Then response status code is 200
    When user with token "900000" wants to see the content of folder with path "/bla/fasel"
    Then response status code is 200
    And the response contains the file with fileSystemId 3 and name "subversion.exe"
    When user with token "900000" wants to see the content of folder with path "/bla"
    Then response status code is 200
    And the response contains the folder with fileSystemId 1 and name "fasel"
    And the response not contains the file with fileSystemId 2 and name "git.exe"

  Scenario: insufficient authorization
    Given user with the userId 1234 is allowed to VIEW the fileSystemItem with the fileSystemId 42
    When user with token "900000" wants to delete the fileSystemItem with the fileSystemId 42
    Then response status code is 400
    And response contains key "message" and value "Folder does not exist, or you are not allowed to edit the folder."

  Scenario: insufficient permission
    Given user 9877 exists
    And accessToken with value "2345678" exists for user 9877
    When user with token "2345678" wants to delete the fileSystemItem with the fileSystemId 42
    Then response status code is 400
    And response contains key "message" and value "Folder does not exist, or you are not allowed to edit the folder."

  Scenario: Folder does not exist
    When user with token "900000" wants to delete the fileSystemItem with the fileSystemId 42432567
    Then response status code is 400
    And response contains key "message" and value "Folder does not exist, or you are not allowed to edit the folder."
