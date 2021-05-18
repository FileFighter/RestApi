Feature: Download Files
  As a user
  I want to be able to download files and whole directories or multiple files from the same directory

  Background:
    Given database is empty
    And runtime user exists
    And user with userId 1234 exists and has username "Richard", password "badPassword"
    And user with userId 420 exists and has username "Nasir", password "AlsoBadPassword"
    And accessToken with value "Richard" exists for user 1234
    And accessToken with value "Nasir" exists for user 420
    And user with userId 1234 has HomeFolder with Id 1234
    And user with userId 420 has HomeFolder with Id 420
    And fileSystemItem with the fileSystemId 420 is a folder and contains the fileSystemId 42
    And fileSystemItem with the fileSystemId 42 exists, has owner with userId 420 has the path "/gebäude" and name "gebäude"
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 72
    And fileSystemItem with the fileSystemId 72 exists, has owner with userId 420 and name "Bergfried.avi" and mimeType "video/avi"

  Scenario: Successful interaction, download of file in personal folder
    When the user with token "Nasir" wants to download the fileSystemItems with Ids [72]
    Then response status code is 200
    And the response contains a entity with the path "bergfried.avi" that has key "name" with value "Bergfried.avi"
    And the response contains a entity with the path "bergfried.avi" that has key "fileSystemId" with value "72"
    And the response contains a entity with the path "bergfried.avi" that has key "mimeType" with value "video/avi"

  Scenario: Successful interaction, download of file in personal folder, authorization with cookie
    When the user with a cookie-token "Nasir" wants to download the fileSystemItems with Ids [72]
    Then response status code is 200
    And the response contains a entity with the path "bergfried.avi" that has key "name" with value "Bergfried.avi"
    And the response contains a entity with the path "bergfried.avi" that has key "fileSystemId" with value "72"
    And the response contains a entity with the path "bergfried.avi" that has key "mimeType" with value "video/avi"

  Scenario: Successful interaction, download of folder in personal folder
    When the user with token "Nasir" wants to download the fileSystemItems with Ids [42]
    Then response status code is 200
    And the response contains a entity with the path "bergfried.avi" that has key "name" with value "Bergfried.avi"
    And the response contains a entity with the path "bergfried.avi" that has key "fileSystemId" with value "72"
    And the response contains a entity with the path "bergfried.avi" that has key "mimeType" with value "video/avi"
    # this will be the name of the zip archive (could also return gebäude.zip)
    And the response has a header "X-FF-NAME" set with the value "gebäude"

  Scenario: Successful interaction, download of two files
    Given fileSystemItem with the fileSystemId 73 exists, has owner with userId 420 and name "Torhaus.avi"
    # does this step update the content?
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 73
    When the user with token "Nasir" wants to download the fileSystemItems with Ids [72,73]
    Then response status code is 200
    And the response contains a entity with the path "bergfried.avi" that has key "name" with value "Bergfried.avi"
    And the response contains a entity with the path "torhaus.avi" that has key "name" with value "Torhaus.avi"
    And the response has a header "X-FF-NAME" set with the value "gebäude"

  Scenario: Successful interaction, download of file and folder
    Given fileSystemItem with the fileSystemId 43 exists, has owner with userId 420 has the path "/gebäude/wirtschaft" and name "wirtschaft"
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 43
    And fileSystemItem with the fileSystemId 43 is a folder and contains the fileSystemId 73
    And fileSystemItem with the fileSystemId 73 exists, has owner with userId 420 and name "Eisenmine.avi"
    When the user with token "Nasir" wants to download the fileSystemItems with Ids [43,72]
    Then response status code is 200
    And the response contains a entity with the path "bergfried.avi" that has key "name" with value "Bergfried.avi"
    And the response contains a entity with the path "wirtschaft/eisenmine.avi" that has key "name" with value "Eisenmine.avi"
    And the response has a header "X-FF-NAME" set with the value "gebäude"

  Scenario: Successful interaction, download of folder with nested content
    Given fileSystemItem with the fileSystemId 43 exists, has owner with userId 420 has the path "/gebäude/wirtschaft" and name "wirtschaft"
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 43
    And fileSystemItem with the fileSystemId 43 is a folder and contains the fileSystemId 73
    And fileSystemItem with the fileSystemId 73 exists, has owner with userId 420 and name "Eisenmine.avi"
    When the user with token "Nasir" wants to download the fileSystemItems with Ids [42]
    Then response status code is 200
    And the response contains a entity with the path "bergfried.avi" that has key "name" with value "Bergfried.avi"
    And the response contains a entity with the path "wirtschaft/eisenmine.avi" that has key "name" with value "Eisenmine.avi"
    And the response has a header "X-FF-NAME" set with the value "gebäude"

  Scenario: Successful interaction, download of shared file (user)
    Given user with the userId 1234 is allowed to VIEW the fileSystemItem with the fileSystemId 72
    When the user with token "Richard" wants to download the fileSystemItems with Ids [72]
    Then response status code is 200
    And the response contains a entity with the path "bergfried.avi" that has key "name" with value "Bergfried.avi"
    And the response contains a entity with the path "bergfried.avi" that has key "fileSystemId" with value "72"

  Scenario: Successful interaction, download of shared file (group)
    Given group with the groupId 1 is allowed to VIEW the fileSystemItem with the fileSystemId 72
    And user with userId 1234 is in group with groupId 1
    When the user with token "Richard" wants to download the fileSystemItems with Ids [72]
    Then response status code is 200
    And the response contains a entity with the path "bergfried.avi" that has key "name" with value "Bergfried.avi"
    And the response contains a entity with the path "bergfried.avi" that has key "fileSystemId" with value "72"

  Scenario: File does not exist
    When the user with token "Nasir" wants to download the fileSystemItems with Ids [666]
    Then response status code is 400
    And response contains key "message" and value "The FileSystemItem could not be downloaded. FileSystemEntity does not exist or you are not allowed to see the entity."

  Scenario: Folder is empty
    Given fileSystemItem with the fileSystemId 43 exists, has owner with userId 420 has the path "/gebäude/wirtschaft" and name "wirtschaft"
    And fileSystemItem with the fileSystemId 43 is a folder
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 43
    When the user with token "Nasir" wants to download the fileSystemItems with Ids [43]
    Then response status code is 200
    And the response contains an empty list for files and folders

  Scenario: No unique parent folder
    When the user with token "Nasir" wants to download the fileSystemItems with Ids [42,72]
    Then response status code is 400
    And response contains key "message" and value "The FileSystemItem could not be downloaded. FileSystemEntity need to have a common parent entity."

  Scenario: insufficient authorization
    When the user with token "Richard" wants to download the fileSystemItems with Ids [42]
    Then response status code is 400
    And response contains key "message" and value "The FileSystemItem could not be downloaded. FileSystemEntity does not exist or you are not allowed to see the entity."


