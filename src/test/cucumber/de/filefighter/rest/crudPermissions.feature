Feature: CRUD Permissions
  As a user and owner a file
  I want want to be able to give or revoke other users permissions to either see or see and edit certain files or folders, so they can work together on the same files


  Background:
    Given database is empty
    And user 1234 exists
    And user 9877 exists
    And user 1234 has access token "900000"
    And user 9877 has access token "2345678"


Scenario Outline: Successful interaction for changing existing permission
  Given "<type>" exists with id <id> and path "<path>"
  And user 1234 is owner of file or folder with id <id>
  And user 9877 has permission of "<old_permission>" for "<type>" with id <id>
  When user with token "900000" wants to change permissions of "<type>" with id <id> for user with id 9877 to "<new_permission>"
  Then response status code is <status_code>
  And user 9877 has permission of "<new_permission>" for "<type>" with id <id>
  Examples:
    | type    | id | path      | old_permission | new_permission | status_code  |
    | file    | 12 | bar.txt   | edit           | view           |     200      |
    | folder  | 11 | f         | edit           | view           |     200      |
    | folder  | 11 | f         | view           | edit           |     200      |
    | folder  | 11 | f         | view           | view           |     304      |
    | file    | 11 | f.txt     | edit           | edit           |     304      |


Scenario Outline: Successful interaction for removing existing permission
  Given "<type>" exists with id <id> and path "<path>"
  And user 1234 is owner of file or folder with id <id>
  And user 9877 has permission of "<old_permission>" for "<type>" with id <id>
  When user with token "900000" wants to remove permissions of "<type>" with id <id> for user 9877
  Then response status code is <status_code>
  And  user with id 9877 has no permission for "<type>" with id <id>
  Examples:
    | type    | id | path | old_permission  | status_code |
    | file    | 12 | fo.c | view            |         200 |
    | folder  | 11 | f    | view            |         200 |
    | file    | 10 | f.c  | edit            |         200 |
    | folder  | 10 | fc   | edit            |         200 |


Scenario: removing not existing permission
  Given "file" exists with id 111 and path "bla.txt"
  And user 1234 is owner of file or folder with id 111
  And user 9877 has no permission for "file" with id 111
  When user with token "900000" wants to remove permissions of "file" with id 111 for user 9877
  Then response status code is 400
  And response contains key "message" and value "Couldn't remove permission that does not exit."


Scenario Outline: Successful interaction adding new permission
  Given "<type>" exists with id <id> and path "<path>"
  And user 1234 is owner of file or folder with id <id>
  And user 9877 has no permission for "<type>" with id <id>
  When user with token "900000" wants to give "<new_permission>" permission for "<type>" with id <id> to user 9877
  Then response status code is 200
  And user 9877 has permission of "<new_permission>" for "<type>" with id <id>
  Examples:
    | type    | id | path | new_permission  |
    | file    | 12 | f.c  | edit            |
    | file    | 12 | f.c  | view            |
    | folder  | 21 | f    | edit            |
    | folder  | 22 | fd   | view            |


Scenario: User is not owner of file
  Given "file" exists with id 111 and path "bla.txt"
  And user 3131 exists
  And user 9877 is owner of file or folder with id 111
  When user with token "900000" wants to give "edit" permission for "file" with id 111 to user 3131
  Then response status code is 403
  And response contains key "message" and value "User with id 1234 is not owner of file with id 111."


Scenario: User does not exist
  Given "file" exists with id 111 and path "bla.txt"
  And user 1234 is owner of file or folder with id 111
  When user with token "900000" wants to give "edit" permission for "file" with id 111 to user 3131
  Then response status code is 404
  And response contains key "message" and value "User 3131 does not exist."


Scenario: File does not exist
  And user 1234 is owner of file or folder with id 111
  When user with token "900000" wants to give "edit" permission for "file" with id 111 to user 9877
  Then response status code is 404
  And response contains key "message" and value "No File with id 111 found."


Scenario: User is already owner
  Given "file" exists with id 111 and path "bla.txt"
  And user 1234 is owner of file or folder with id 111
  When user with token "900000" wants to give "edit" permission for "file" with id 111 to user 1234
  Then response status code is 405
  And response contains key "message" and value "User with id 1234 is already owner of file with id 111."