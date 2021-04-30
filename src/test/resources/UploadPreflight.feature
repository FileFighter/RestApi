Feature: Upload Files Preflight
  As a user
  I want to be able to upload files without getting errors, and being able to decide if i want to overwrite files and if i want to merge folders

  Background:
    Given database is empty
    And runtime user exists
    And user with userId 1234 exists and has username "Richard", password "badPassword"
    And user with userId 420 exists and has username "Nasir", password "AlsoBadPassword"
    And accessToken with value "9000000" exists for user 1234
    And accessToken with value "420" exists for user 420
    And user with userId 1234 has HomeFolder with Id 1234
    And user with userId 420 has HomeFolder with Id 420
    And fileSystemItem with the fileSystemId 42 exists, has owner with userId 1234 has the path "/gebäude" and name "gebäude"
    And fileSystemItem with the fileSystemId 72 exists, has owner with userId 1234 and name "Bergfried.avi"
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 72

  Scenario: upload is possible, single file
    When the user with token "9000000" wants to do a preflight containing a file with the name "unterkünfte.avi", path "unterkünfte.avi", mimeType "text/html" and size 123 to the folder with the id 42
    Then response status code is 200
    Then the response contains a entity with the path "unterkünfte.avi" that has key "name" with value "unterkünfte.avi"
    And the response contains a entity with the path "unterkünfte.avi" that has the preflight response "FILE_CAN_BE_CREATED"
    And the response contains a entity with the path "unterkünfte.avi" that has key "isFile" with value "true"

  Scenario: upload is possible, file in folder
    When the user with token "9000000" wants to do a preflight containing a file with the name "unterkünfte.avi", path "armee/unterkünfte.avi", mimeType "text/html" and size 123 to the folder with the id 42
    Then response status code is 200
    And the response contains a entity with the path "armee" that has key "name" with value "armee"
    And the response contains a entity with the path "armee" that has the preflight response "FOLDER_CAN_BE_CREATED"
    And the response contains a entity with the path "armee" that has key "isFile" with value "false"
    And the response contains a entity with the path "armee/unterkünfte.avi" that has key "name" with value "unterkünfte.avi"
    And the response contains a entity with the path "armee/unterkünfte.avi" that has the preflight response "FILE_CAN_BE_CREATED"
    And the response contains a entity with the path "armee/unterkünfte.avi" that has key "isFile" with value "true"

  Scenario: name conflict folder and file
    When the user with token "9000000" wants to do a preflight containing a file with the name "Bergfried.avi", path "gebäude/Bergfried.avi", mimeType "text/html" and size 123 to the folder with the id 1234
    Then response status code is 200
    Then the response contains a entity with the path "gebäude" that has key "name" with value "gebäude"
    And the response contains a entity with the path "gebäude" that has the preflight response "FOLDER_CAN_BE_MERGED"
    And the response contains a entity with the path "gebäude" that has key "isFile" with value "false"
    And the response contains a entity with the path "gebäude/Bergfried.avi" that has key "name" with value "Bergfried.avi"
    And the response contains a entity with the path "gebäude/Bergfried.avi" that has the preflight response "FILE_CAN_BE_OVERWRITEN"
    And the response contains a entity with the path "gebäude/Bergfried.avi" that has key "isFile" with value "true"

  Scenario: invalid Name file
    When the user with token "9000000" wants to do a preflight containing a file with the name "D[{~______~j}]D", path "D[{~______~j}]D", mimeType "text/html" and size 123 to the folder with the id 1234
    Then response status code is 200
    And the response contains a entity with the path "D[{~______~j}]D" that has key "name" with value "D[{~______~j}]D"
    And the response contains a entity with the path "D[{~______~j}]D" that has the preflight response "NAME_WAS_NOT_VALID"
    And the response contains a entity with the path "D[{~______~j}]D" that has key "isFile" with value "true"

  Scenario: invalid Name folder
    When the user with token "9000000" wants to do a preflight containing a file with the name "file", path "~~dsa~~/file", mimeType "text/html" and size 123 to the folder with the id 1234
    Then response status code is 200
    And the response contains a entity with the path "~~dsa~~" that has key "name" with value "~~dsa~~"
    And the response contains a entity with the path "~~dsa~~" that has the preflight response "NAME_WAS_NOT_VALID"
    And the response contains a entity with the path "~~dsa~~" that has key "isFile" with value "false"
    And the response contains a entity with the path "~~dsa~~/file" that has key "name" with value "file"
    And the response contains a entity with the path "~~dsa~~/file" that has the preflight response "STATEMENT_CANNOT_BE_MADE"
    And the response contains a entity with the path "~~dsa~~/file" that has key "isFile" with value "true"

  Scenario: permission not sufficient, folder not viewable
    When the user with token "420" wants to do a preflight containing a file with the name "file", path "file", mimeType "text/html" and size 123 to the folder with the id 1234
    Then response status code is 400

  Scenario: permission not sufficient, folder does not exist
    When the user with token "420" wants to do a preflight containing a file with the name "file", path "file", mimeType "text/html" and size 123 to the folder with the id 32454645
    Then response status code is 400

  Scenario: permission not sufficient, folder viewable but not editable
    Given user with the userId 420 is allowed to VIEW the fileSystemItem with the fileSystemId 1234
    When the user with token "420" wants to do a preflight containing a file with the name "file", path "file", mimeType "text/html" and size 123 to the folder with the id 1234
    Then response status code is 400

  Scenario: permission not sufficient, folder editable, but subfolder not editable
    Given user with the userId 420 is allowed to EDIT the fileSystemItem with the fileSystemId 1234
    Given user with the userId 420 is allowed to VIEW the fileSystemItem with the fileSystemId 1234
    When the user with token "420" wants to do a preflight containing a file with the name "Bergfried.avi", path "gebäude/Bergfried.avi", mimeType "text/html" and size 123 to the folder with the id 1234
    Then response status code is 200
    And the response contains a entity with the path "gebäude" that has key "name" with value "gebäude"
    And the response contains a entity with the path "gebäude" that has the preflight response "FOLDER_CANT_BE_MERGED"
    And the response contains a entity with the path "gebäude" that has key "isFile" with value "false"
    And the response contains a entity with the path "gebäude/Bergfried.avi" that has key "name" with value "Bergfried.avi"
    And the response contains a entity with the path "gebäude/Bergfried.avi" that has the preflight response "STATEMENT_CANNOT_BE_MADE"
    And the response contains a entity with the path "gebäude/Bergfried.avi" that has key "isFile" with value "true"





