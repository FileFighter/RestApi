Feature: Upload Files
  As a user
  I want to be able to upload files and also folders with files in them

  Background:
    Given database is empty
    And runtime user exists
    And user with userId 1234 exists and has username "Richard", password "badPassword"
    And user with userId 420 exists and has username "Nasir", password "AlsoBadPassword"
    And accessToken with value "9000000" exists for user 1234
    And accessToken with value "420" exists for user 420
    And user with userId 1234 has HomeFolder with Id 1234
    And user with userId 420 has HomeFolder with Id 420
    And fileSystemItem with the fileSystemId 420 is a folder and contains the fileSystemId 42
    And fileSystemItem with the fileSystemId 42 exists, has owner with userId 1234 has the path "/gebäude" and name "gebäude"
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 72
    And fileSystemItem with the fileSystemId 72 exists, has owner with userId 1234 and name "Bergfried.avi"

  Scenario: Successful interaction, upload of file in personal folder
    When the user with token "9000000" wants to upload a file with the name "unterkünfte.avi", path "unterkünfte.avi", mimeType "text/html" and size 123 to the folder with the id 42
    Then response status code is 201
    When user with token "9000000" wants to see the content of folder with path "/Richard/gebäude"
    Then the response contains the file with name "unterkünfte.avi"
    Then the response contains the file with fileSystemId 72 and name "Bergfried.avi"

  Scenario: Successful interaction, upload of file in personal folder update parent info
    When the user with token "9000000" wants to upload a file with the name "unterkünfte.avi", path "unterkünfte.avi", mimeType "text/html" and size 123 to the folder with the id 42
    When user with token "9000000" wants to get the info of fileSystemItem with the fileSystemId 42
    Then response status code is 200
    And response contains a valid timestamp at key "lastUpdated".
    And response contains the user with userId 1234 at key "lastUpdatedBy"

  Scenario: Successful interaction, upload of file in shared folder
    Given user with the userId 420 is allowed to EDIT the fileSystemItem with the fileSystemId 42
    And user with the userId 420 is allowed to VIEW the fileSystemItem with the fileSystemId 42
    Given user with the userId 420 is allowed to EDIT the fileSystemItem with the fileSystemId 72
    And user with the userId 420 is allowed to VIEW the fileSystemItem with the fileSystemId 72
    When the user with token "420" wants to upload a file with the name "eggs.food", path "eggs.food", mimeType "text/html" and size 123 to the folder with the id 42
    Then response status code is 201
    And the response contains the item with path "/richard/gebäude/eggs.food" and name "eggs.food" and mimeType "text/html" and type "TEXT" and size 123
    When user with token "420" wants to see the content of folder with path "/Richard/gebäude"
    Then the response contains the file with name "eggs.food"
    Then the response contains the file with fileSystemId 72 and name "Bergfried.avi"
    When user with token "420" wants to get the info of fileSystemItem with the fileSystemId 42
    Then response status code is 200
    And response contains a valid timestamp at key "lastUpdated".
    And response contains the user with userId 420 at key "lastUpdatedBy"

  Scenario: Successful interaction, upload of file in shared folder update parent info
    Given user with the userId 420 is allowed to EDIT the fileSystemItem with the fileSystemId 42
    Given user with the userId 420 is allowed to VIEW the fileSystemItem with the fileSystemId 42
    When the user with token "420" wants to upload a file with the name "eggs.food", path "eggs.food", mimeType "text/html" and size 123 to the folder with the id 42
    When user with token "420" wants to get the info of fileSystemItem with the fileSystemId 42
    Then response status code is 200
    And response contains a valid timestamp at key "lastUpdated".
    And response contains the user with userId 420 at key "lastUpdatedBy"

  Scenario: Successful interaction, upload of file in folder
    When the user with token "9000000" wants to upload a file with the name "NaDasIstJaGesternNichtSoGutGelaufen.mp3", path "kiSounds/NaDasIstJaGesternNichtSoGutGelaufen.mp3", mimeType "audio/mpeg" and size 99989 to the folder with the id 1234
    Then response status code is 201
    And the response contains the item with path "/richard/kisounds/nadasistjagesternnichtsogutgelaufen.mp3" and name "NaDasIstJaGesternNichtSoGutGelaufen.mp3" and mimeType "audio/mpeg" and type "AUDIO" and size 99989
    When user with token "9000000" wants to see the content of folder with path "/Richard"
    Then the response contains the folder with name "kiSounds"
    When user with token "9000000" wants to see the content of folder with path "/Richard/kiSounds"
    Then the response contains the file with name "NaDasIstJaGesternNichtSoGutGelaufen.mp3"

  Scenario: Folder does not exist
    When the user with token "9000000" wants to upload a file with the name "NaDasIstJaGesternNichtSoGutGelaufen.mp3", path "kiSounds/NaDasIstJaGesternNichtSoGutGelaufen.mp3", mimeType "audio/mpeg" and size 99989 to the folder with the id 422
    Then response status code is 400
    And response contains key "message" and value "FileSystemEntity could not be uploaded."

  Scenario: Is not a folder
    When the user with token "9000000" wants to upload a file with the name "NaDasIstJaGesternNichtSoGutGelaufen.mp3", path "kiSounds/NaDasIstJaGesternNichtSoGutGelaufen.mp3", mimeType "audio/mpeg" and size 99989 to the folder with the id 72
    Then response status code is 400
    And response contains key "message" and value "FileSystemEntity could not be uploaded. Tried uploading to a file. Upload to a folder instead."

  Scenario: insufficient authorization (can't view)
    When the user with token "420" wants to upload a file with the name "IchLassEuchHängen.mp3", path "kiSounds/IchLassEuchHängen.mp3", mimeType "audio/mpeg" and size 666 to the folder with the id 42
    Then response status code is 400
    And response contains key "message" and value "FileSystemEntity could not be uploaded."

  Scenario: insufficient authorization (can't edit)
    Given user with the userId 420 is allowed to VIEW the fileSystemItem with the fileSystemId 42
    When the user with token "420" wants to upload a file with the name "IchLassEuchHängen.mp3", path "kiSounds/IchLassEuchHängen.mp3", mimeType "audio/mpeg" and size 666 to the folder with the id 42
    Then response status code is 400
    And response contains key "message" and value "FileSystemEntity could not be uploaded."
