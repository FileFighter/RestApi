Feature: Create new folders
  As a user
  I want to be able to create new folders to be able to upload file to them

  Background:
    Given database is empty
    And runtime user exists
    And user with userId 1234 exists and has username "Richard", password "badPassword"
    And user with userId 420 exists and has username "Nasir", password "AlsoBadPassword"
    And accessToken with value "1234" exists for user 1234
    And accessToken with value "420" exists for user 420
    And user with userId 1234 has HomeFolder with Id 1234
    And user with userId 420 has HomeFolder with Id 420
    And fileSystemItem with the fileSystemId 420 is a folder and contains the fileSystemId 42
    And fileSystemItem with the fileSystemId 42 exists, has owner with userId 420 has the path "/gebäude" and name "gebäude"
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 72
    And fileSystemItem with the fileSystemId 72 exists, has owner with userId 420 and name "Bergfried.avi"

  Scenario: Successful interaction, creation of folder in personal folder
    When the user with token "420" wants to create a folder with name "thefolder" in the the folder with the id 42
    Then response status code is 201
    Then response contains key "path" and value "/nasir/gebäude/thefolder"
    Then response contains key "name" and value "thefolder"
    When user with token "420" wants to see the content of folder with path "/Nasir/gebäude"
    Then the response contains the folder with name "thefolder"

  Scenario: Successful interaction, creation of folder in home folder
    When the user with token "420" wants to create a folder with name "thefolder" in the the folder with the id 420
    Then response status code is 201
    Then response contains key "path" and value "/nasir/thefolder"
    Then response contains key "name" and value "thefolder"
    When user with token "420" wants to see the content of folder with path "/Nasir"
    Then the response contains the folder with name "thefolder"

  Scenario: Successful interaction, creation of folder in shared folder
    Given user with the userId 1234 is allowed to EDIT the fileSystemItem with the fileSystemId 42
    And user with the userId 1234 is allowed to VIEW the fileSystemItem with the fileSystemId 42
    When the user with token "1234" wants to create a folder with name "thefolder" in the the folder with the id 42
    Then response status code is 201
    Then response contains key "path" and value "/nasir/gebäude/thefolder"
    Then response contains key "name" and value "thefolder"
    When user with token "1234" wants to see the content of folder with path "/Nasir/gebäude"
    Then the response contains the folder with name "thefolder"

  Scenario: Folder does not exist
    When the user with token "420" wants to create a folder with name "thefolder" in the the folder with the id 422
    Then response status code is 400
    And response contains key "message" and value "FileSystemEntity could not be uploaded. Could not find parent entity or you are not allowed to see it."

  Scenario: Folder already exists
    When the user with token "420" wants to create a folder with name "gebäudE" in the the folder with the id 420
    Then response status code is 400
    And response contains key "message" and value "FileSystemEntity could not be uploaded. A Entity with the same name already exists in this directory."

  Scenario: File with same name already exists
    When the user with token "420" wants to create a folder with name "Bergfried.avi" in the the folder with the id 42
    Then response status code is 400
    And response contains key "message" and value "FileSystemEntity could not be uploaded. A Entity with the same name already exists in this directory."

  Scenario: insufficient authorization (can't view)
    When the user with token "1234" wants to create a folder with name "thefolder" in the the folder with the id 420
    Then response status code is 400
    And response contains key "message" and value "FileSystemEntity could not be uploaded. Could not find parent entity or you are not allowed to see it."

  Scenario: insufficient authorization (can't edit)
    Given user with the userId 1234 is allowed to VIEW the fileSystemItem with the fileSystemId 420
    When the user with token "1234" wants to create a folder with name "thefolder" in the the folder with the id 420
    Then response status code is 400
    And response contains key "message" and value "FileSystemEntity could not be uploaded. You dont have write permissions in that directory."
