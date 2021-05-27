Feature: Search Files and Directories
  As a user
  I want to be able to search for files or folders by their name.

  Background:
    Given database is empty
    And runtime user exists
    And user with userId 1234 exists and has username "Richard", password "badPassword"
    And user with userId 420 exists and has username "Nasir", password "AlsoBadPassword"
    And accessToken with value "Richard" exists for user 1234
    And accessToken with value "Nasir" exists for user 420
    And user with userId 1234 has HomeFolder with Id 1234
    And user with userId 420 has HomeFolder with Id 420
    And fileSystemItem with the fileSystemId 42 exists, has owner with userId 1234 has the path "/bla" and name "bla"
    And fileSystemItem with the fileSystemId 72 exists, has owner with userId 1234 and name "wow.txt"
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 72

  Scenario: Successful interaction, found file in personal folder
    When user with token "Richard" searches for "wow"
    Then response status code is 200
    And the response contains a entity with the path "/richard/bla/wow.txt" that has key "name" with value "wow.txt"

  Scenario: Successful interaction, searching for personal root folder
    When user with token "Richard" searches for "Richard"
    Then response status code is 200
    And the response contains a entity with the path "/richard" that has key "name" with value "Richard"

  Scenario: Successful interaction, found file in personal folder, ignore case
    When user with token "Richard" searches for "WOW.TXT"
    Then response status code is 200
    And the response contains a entity with the path "/richard/bla/wow.txt" that has key "name" with value "wow.txt"

  Scenario: Successful interaction, found folder in personal folder
    When user with token "Richard" searches for "bla"
    Then response status code is 200
    And the response contains a entity with the path "/richard/bla" that has key "fileSystemId" with value "42"

  Scenario: Successful interaction, found files in personal folder by half the name
    Given fileSystemItem with the fileSystemId 73 exists, has owner with userId 1234 and name "SuchAlongFileName.txt"
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 73
    And fileSystemItem with the fileSystemId 74 exists, has owner with userId 1234 and name "AnotherlongFileName.xml"
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 74
    When user with token "Richard" searches for "FileName"
    Then response status code is 200
    And the response contains a entity with the path "/richard/bla/suchalongfilename.txt" that has key "fileSystemId" with value "73"
    And the response contains a entity with the path "/richard/bla/anotherlongfilename.xml" that has key "fileSystemId" with value "74"

  Scenario: nothing found
    When user with token "Richard" searches for "ugabuga"
    Then response status code is 200
    And the response contains an empty list for files and folders

  Scenario: no permissions
    When user with token "Nasir" searches for "wow"
    Then response status code is 200
    And the response contains an empty list for files and folders

  Scenario: Successful interaction, found file in shared folder
    Given user with the userId 420 is allowed to VIEW the fileSystemItem with the fileSystemId 42
    And user with the userId 420 is allowed to VIEW the fileSystemItem with the fileSystemId 72
    When user with token "Nasir" searches for "wow"
    Then response status code is 200
    And the response contains a entity with the path "/richard/bla/wow.txt" that has key "name" with value "wow.txt"

  Scenario: Successful interaction, found folder in personal folder
    Given user with the userId 420 is allowed to VIEW the fileSystemItem with the fileSystemId 42
    And user with the userId 420 is allowed to VIEW the fileSystemItem with the fileSystemId 72
    When user with token "Richard" searches for "bla"
    Then response status code is 200
    And the response contains a entity with the path "/richard/bla" that has key "fileSystemId" with value "42"


  Scenario Outline: Search for <filename> with <search>
    Given fileSystemItem with the fileSystemId 73 exists, has owner with userId 1234 and name "<filename>"
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 73
    When user with token "Richard" searches for "<search>"
    Then response status code is 200
    And the response contains the file with fileSystemId 73 and name "<filename>"

    Examples:
      | filename                      | search                        |
      | HansWurst                     | st                            |
      | Esel.txt                      | .txt                          |
      | HinterhältigesWiesel.wav      | Hältiges                      |
      | HilfeHilfe                    | hilfe                         |
      | DuDummeSau                    | du                            |
      | NichtDasNagetier              | Tier                          |
      | DuBauer                       | auer                          |
      | DasIstWahreMacht              | d                             |
      | DIesESMALWerdeICHEuchBesiegen | DiesesMalWerdeIchEuchBesiegen |
      | filename                      | filename                      |
