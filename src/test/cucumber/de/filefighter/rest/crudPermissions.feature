Feature: CRUD Permissions
  As a user and owner a file
  I want want to be able to give or revoke other users permissions to either see or see and edit certain files or folders, so they can work together on the same files

  Background:
    Given credential are valid
    And <path> exists
    # can you use <var> in background?

  #TODO: fix scenarios, in a way that is implementable. -> https://cucumber.io/docs/cucumber/cucumber-expressions/

Scenario Outline: Successful interaction for changing existing permission
  Given user 1234 is owner of <type> <path>
  And user 9877 was given permission of <old_permission> for <type> <path>
  When user 1234 wants to change permissions of <type> <path>
  Then list all users that have permissions for <type> <path>
  When user 1234 changes permissions of user 9877 to <new_permission>
  Then user 9877 has the following permissions for <type> <path> : <new_permission>
  Examples:
    | type    | path     | old_permission | new_permission |
    | file    | f/42.txt | edit           | none           |
    | folder  | f        | view           | edit           |
    | file    | cat.c    | edit           | view           |


Scenario Outline: Successful interaction adding new permission
  Given user 1234 is owner of <type> <path>
  And user 9877 exists
  # what does "was given" mean, -> does he have it?
  And user 9877 was given permission of none for  <type> <path>
  And user 9877 is not owner of <type> <path>
  # has no permissions
  And user 9877 was given permission of none for  <type> <path>
  When user 1234 wants to give permissions of <new_permission> for <type> <path> to user 9877
  # Difference between "has the following permissions" and "was given permission" ??
  Then user 9877 has the following permissions for <type> <path> : <new_permission>
  Examples:
    | type    | path     | new_permission |
    | file    | fasel.ts | edit           |
    | folder  | bla/blub | view           |




