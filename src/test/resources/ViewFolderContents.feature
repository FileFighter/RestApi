Feature: View Folder
  As a user
  I want to see the content of folders and navigate in them, so they can see and interact with their uploaded and shared files.

  Background:
    Given database is empty
    And runtime user exists
    And user with userId 1234 exists and has username "Richard", password "badPassword"
    And user with userId 420 exists and has username "Nasir", password "AlsoBadPassword"
    And accessToken with value "900000" exists for user 1234
    And accessToken with value "222222" exists for user 420
    And user with userId 1234 has HomeFolder with Id 1234
    And user with userId 420 has HomeFolder with Id 420
    And fileSystemItem with the fileSystemId 42 exists, has owner with userId 1234 has the path "/bla" and name "bla"
    And fileSystemItem with the fileSystemId 72 exists, has owner with userId 1234 and name "wow.txt"
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 72

  Scenario: Successful interaction
    When user with token "900000" wants to see the content of folder with path "/Richard/bla"
    Then response status code is 200
    And the response contains the file with fileSystemId 72 and name "wow.txt"

  Scenario: Successful interaction shared folder
    # the folder
    Given user with the userId 420 is allowed to VIEW the fileSystemItem with the fileSystemId 42
    # the file
    And user with the userId 420 is allowed to VIEW the fileSystemItem with the fileSystemId 72
    When user with token "222222" wants to see the content of folder with path "/Richard/bla"
    Then response status code is 200
    And the response contains the file with fileSystemId 72 and name "wow.txt"

  Scenario: Folder does not exist
    When user with token "900000" wants to see the content of folder with path "/Richard/bla/fasel"
    Then response status code is 400
    And response contains key "message" and value "Folder does not exist, or you are not allowed to see the folder."

  Scenario: insufficient authorization
    When user with token "222222" wants to see the content of folder with path "/Richard/bla"
    Then response status code is 400
    And response contains key "message" and value "Folder does not exist, or you are not allowed to see the folder."

  Scenario: shared folder (group)
    And user with userId 420 is in group with groupId 1
    And group with the groupId 1 is allowed to VIEW the fileSystemItem with the fileSystemId 42
    And group with the groupId 1 is allowed to VIEW the fileSystemItem with the fileSystemId 72
    When user with token "222222" wants to see the content of folder with path "/Richard/bla"
    Then response status code is 200
    And the response contains the file with fileSystemId 72 and name "wow.txt"

  Scenario: empty directory
    Given fileSystemItem with the fileSystemId 44 exists, has owner with userId 1234 has the path "/empty" and name "empty"
    And fileSystemItem with the fileSystemId 44 is a folder
    When user with token "900000" wants to see the content of folder with path "/Richard/empty"
    Then response status code is 200
    And the response contains an empty list for files and folders

  Scenario: root folder
    When user with token "900000" wants to see the content of folder with path "/"
    Then response status code is 200
    And the response contains the folder with name "Richard"

  Scenario: root folder shared
    Given user with the userId 420 is allowed to VIEW the fileSystemItem with the fileSystemId 1234
    When user with token "222222" wants to see the content of folder with path "/"
    Then response status code is 200
    And the response contains the folder with name "Richard"
    And the response contains the folder with name "Nasir"

  Scenario: nested shared folder
    Given fileSystemItem with the fileSystemId 4 exists, has owner with userId 1234 has the path "/pläne" and name "pläne"
    And fileSystemItem with the fileSystemId 5 exists, has owner with userId 1234 has the path "/pläne/städte" and name "städte"
    And fileSystemItem with the fileSystemId 12 exists, has owner with userId 1234 has the path "/pläne/städte/jerusalem" and name "jerusalem"
    And fileSystemItem with the fileSystemId 13 exists, has owner with userId 1234 and name "we_will_take.mp3"
    And fileSystemItem with the fileSystemId 12 is a folder and contains the fileSystemId 13
    And fileSystemItem with the fileSystemId 4 is a folder and contains the fileSystemId 5
    And fileSystemItem with the fileSystemId 5 is a folder and contains the fileSystemId 12
    And user with the userId 420 is allowed to VIEW the fileSystemItem with the fileSystemId 12
    And user with the userId 420 is allowed to VIEW the fileSystemItem with the fileSystemId 13
    # Use this is the crud permissions feature file.
    #When user with token "222222" wants to see the content of folder with path "/Richard"
    #Then response status code is 200
    #And the response does not contains the file with fileSystemId 42 and name "bla"
    #And the response contains the folder with fileSystemId 4 and name "pläne"
    #When user with token "222222" wants to see the content of folder with path "/Richard/pläne"
    #Then response status code is 200
    #And the response contains the folder with fileSystemId 5 and name "städte"
    #When user with token "222222" wants to see the content of folder with path "/Richard/pläne/städte"
    #Then response status code is 200
    #And the response contains the folder with fileSystemId 12 and name "jerusalem"
    When user with token "222222" wants to see the content of folder with path "/Richard/pläne/städte/jerusalem"
    Then response status code is 200
    And the response contains the file with fileSystemId 13 and name "we_will_take.mp3"
