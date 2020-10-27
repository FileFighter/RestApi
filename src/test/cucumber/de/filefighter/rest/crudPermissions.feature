Feature: CRUD Permissions
  The owner of a file want to be able to give or revoke other users permissions to either see or see and edit certain files or folders, so they can work together on the same files.

Scenario Outline: Successful interaction for changing existing permission
  Given credential are valid
  And user 1234 is owner of <type> <path>
  And user 9877 was given permisssion of  <old_permission> for <type> <path>
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
    Given credential are valid
    And user 1234 is owner of <type> <path>
    And user 9877 was given permisssion of none for  <type> <path>
    When user 1234 wants to give permissions of <new_permission> for <type> <path> for user 9877
    And user 9877 exists
    And user 9877 is not owner of <type> <path>
    And user 9877 was given permisssion of none for  <type> <path>
    Then user 9877 has the following permissions for <type> <path> : <new_permission>
  Examples:
    | type    | path     | new_permission |
    | file    | fasel.ts | edit           |
    | folder  | bla/blub | view           |




