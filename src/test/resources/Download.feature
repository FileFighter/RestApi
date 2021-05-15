Feature: Download Files
  As a user
  I want to be able to download files and whole directories

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
    When the user with token "Nasir" wants to download the files with Ids [72]
    Then response status code is 200
    And the response contains a entity with the path "Bergfried.avi" that has key "name" with value "Bergfried.avi"
    And the response contains a entity with the path "Bergfried.avi" that has key "fsItemId" with value "72"
    And the response contains a entity with the path "Bergfried.avi" that has key "mimeType" with value "video/avi"

  Scenario: Successful interaction, download of folder in personal folder
    When the user with token "Nasir" wants to download the files with Ids [42]
    Then response status code is 200
    And the response contains a entity with the path "Bergfried.avi" that has key "name" with value "Bergfried.avi"
    And the response contains a entity with the path "Bergfried.avi" that has key "fsItemId" with value "72"
    And the response contains a entity with the path "Bergfried.avi" that has key "mimeType" with value "video/avi"
    # this will be the name of the zip archive (could also return gebäude.zip)
    And the response has a header "X-FF-NAME" set with the value "gebäude"

  Scenario: Successful interaction, download of two files
    Given fileSystemItem with the fileSystemId 73 exists, has owner with userId 420 and name "Torhaus.avi"
    # does this step update the content?
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 73
    When the user with token "Nasir" wants to download the files with Ids [72,73]
    Then response status code is 200
    And the response contains a entity with the path "Bergfried.avi" that has key "name" with value "Bergfried.avi"
    And the response contains a entity with the path "Torhaus.avi" that has key "name" with value "Torhaus.avi"
    And the response has a header "X-FF-NAME" set with the value "gebäude"
