Feature: Upload Files
  As a user
  I want to be able to upload files and also folders with files in them

  Background:
    Given database is empty
    And user 1234 exists
    And accessToken with value "9000000" exists for user 123
    And fileSystemItem with the fileSystemId 42 exists, was created by user with userId 420 has the path "/bla" and name "bla"
    And fileSystemItem with the fileSystemId 72 exists, was created by user with userId 420 and has the name "wow.txt"
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 72

  Scenario: Successful interaction, upload of file
    Given user with the userId 1234 is allowed to EDIT the fileSystemItem with the fileSystemId 42
    When the user with token "9000000" wants to upload a file with the name "eggs.food", path "eggs.food", mimeType "text/html" and size 123 to the folder with the id 42
    Then response status code is 201
    And response contains key "type" and value "TEXT"
    And response contains key "mimeType" and value "text/html"
    And response contains key "fileSystemId" and value of at least 0
    And response contains key "lastUpdated" and value of at least 1617264805
    And response contains key "name" and value "eggs.food"
    When user with token "9000000" wants to see the content of folder with path "/bla"
    Then the response contains the file with name "eggs.food"
    Then the response contains the folder with fileSystemId 72 and name "wow.txt"

  Scenario: Successful interaction, upload of file in folder
    Given user with the userId 1234 is allowed to EDIT the fileSystemItem with the fileSystemId 42
    When the user with token "9000000" wants to upload a file with the name "NaDasIstJaGesternNichtSoGutGelaufen.mp3", path "kiSounds/NaDasIstJaGesternNichtSoGutGelaufen.mp3", mimeType "audio/mpeg" and size 99989 to the folder with the id 42
    Then response status code is 201
    And response contains key "type" and value "AUDIO"
    And response contains key "mimeType" and value "audio/mpeg"
    And response contains key "fileSystemId" and value of at least 0
    And response contains key "lastUpdated" and value of at least 1617264805
    And response contains key "name" and value "NaDasIstJaGesternNichtSoGutGelaufen.mp3"
    When user with token "9000000" wants to see the content of folder with path "/bla/kiSounds"
    Then the response contains the file with name "NaDasIstJaGesternNichtSoGutGelaufen.mp3"
    Then the response contains the folder with fileSystemId 72 and name "wow.txt"

  Scenario: Folder does not exist
    Given user with the userId 1234 is allowed to EDIT the fileSystemItem with the fileSystemId 42
    When the user with token "9000000" wants to upload a file with the name "NaDasIstJaGesternNichtSoGutGelaufen.mp3", path "kiSounds/NaDasIstJaGesternNichtSoGutGelaufen.mp3", mimeType "audio/mpeg" and size 99989 to the folder with the id 422
    Then response status code is 400
    And response contains key "message" and value "Folder does not exist, or you are not allowed to see the folder."

  Scenario: Is not a folder
    Given user with the userId 1234 is allowed to EDIT the fileSystemItem with the fileSystemId 42
    When the user with token "9000000" wants to upload a file with the name "NaDasIstJaGesternNichtSoGutGelaufen.mp3", path "kiSounds/NaDasIstJaGesternNichtSoGutGelaufen.mp3", mimeType "audio/mpeg" and size 99989 to the folder with the id 72
    Then response status code is 400
    And response contains key "message" and value "The specified rootItemId was a file. The specified rootItemId was a file."

  Scenario: insufficient authorization (can't view)
    When the user with token "9000000" wants to upload a file with the name "IchLassEuchHängen.mp3", path "kiSounds/IchLassEuchHängen.mp3", mimeType "audio/mpeg" and size 666 to the folder with the id 42
    Then response status code is 400
    And response contains key "message" and value "Folder does not exist, or you are not allowed to see the folder."

  Scenario: insufficient authorization (can't edit)
    Given user with the userId 1234 is allowed to VIEW the fileSystemItem with the fileSystemId 42
    Then response status code is 400
    And response contains key "message" and value "Insufficient permissions."
