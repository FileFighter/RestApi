Feature: Upload Files Preflight
  As a user
  I want to be able to upload files without getting errors, and being able to decide if i want to overwrite files and if i want to merge folders

  Background:
    Given database is empty
    And user 1234 exists
    And accessToken with value "9000000" exists for user 123
    And fileSystemItem with the fileSystemId 42 exists, was created by user with userId 420 has the path "/bla" and name "bla"
    And fileSystemItem with the fileSystemId 72 exists, was created by user with userId 420 and has the name "wow.txt"
    And fileSystemItem with the fileSystemId 42 is a folder and contains the fileSystemId 72

  Scenario: