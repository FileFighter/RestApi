#Feature: View Folder
#  As a user
#  I want to see the content of folders and navigate in them, so they can see and interact with their uploaded and shared files.
#
#Background:
#  Given database is empty
#  And user 1234 exists
#  And user 1234 has access token "900000"
#  And "folder" exists with id 42 and path "bla"
#  And "file" exists with id 72 and path "bla/wow.txt"
#
#
#Scenario: Successful interaction
#  Given user 1234 has permission of "view" for "folder" with id 42
#  And user 1234 has permission of "view" for "file" with id 72
#  When user with token "900000" wants to see the content of folder with path "bla"
#  Then response status code is 200
#  And the response contains the file with id 72 and name "wow.txt"
#
#
#Scenario: Folder does not exist
#  Given user 1234 has permission of "view" for "folder" with id 42
#  When user with token "900000" wants to see the content of folder with path "bla/fasel"
#  Then response status code is 400
#  And response contains key "message" and value "Folder does not exist, or you are not allowed to see the folder."
#
#
#Scenario: insufficient authorization
#  Given user 9877 exists
#  And user 9877 has access token "2345678"
#  When user with token "2345678" wants to see the content of folder with path "bla"
#  Then response status code is 400
#  And response contains key "message" and value "Folder does not exist, or you are not allowed to see the folder."
#
#
#Scenario: shared file
#  Given "folder" exists with id 43 and path "bla"
#  And "file" exists with id 73 and path "bla/wow.txt"
#  And user 1234 is owner of file or folder with id 42
#  And user 1234 is owner of file or folder with id 72
#  And user 1234 has permission of "view" for "folder" with id 43
#  And user 1234 has permission of "view" for "file" with id 73
#  When user with token "900000" wants to see the content of folder with path "bla"
#  Then response status code is 200
#  And the response contains the file with id 72 and name "wow.txt"
#  And the response contains the file with id 73 and name "wow.txt"
#
#Scenario: empty directory
#  Given "folder" exists with id 44 and path "empty"
#  And user 1234 has permission of "view" for "folder" with id 44
#  When user with token "900000" wants to see the content of folder with path "empty"
#  Then response status code is 200
#  And the response contains an empty list for files and folders