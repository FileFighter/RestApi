Feature: View Folder
  As a user
  I want to see the content of folders and navigate in them, so they can see and interact with their uploaded and shared files.

Background:
  Given database is empty
  And user 1234 exists
  And user 1234 has access token "900000"
  And the folder with id 42 and path "bla" exists
  And the file with id 72 and path "bla/wow.txt" exists


Scenario: Successful interaction
  Given user 1234 has permission to view the folder with id 42
  And user 1234 has permission to view the file with id 72
  When user with token "900000" wants to see the content of folder with path "bla"
  Then response status code is 200
  And the response contains the file with id 72 and name "wow.txt"


Scenario: Folder does not exist
  Given user 1234 has permission to view the folder with id 42
  When user with token "900000" wants to see the content of folder with path "bla/fasel"
  Then response status code is 400
  And response message contains "Folder does not exist, or you are not allowed to see the folder."


Scenario: insufficient authorization
  Given user 9877 exists
  And user 9877 has access token "2345678"
  When user with token "2345678" wants to see the content of folder with path "bla"
  Then response status code is 400
  And response message contains "Folder does not exist, or you are not allowed to see the folder."

Scenario: shared file
  Given the folder with id 43 and path "bla" exists
  And the file with id 73 and path "bla/wow.txt" exists
  And user 1234 is owner of 42
  And user 1234 is owner of 72
  And user 1234 has permission of "view" for "folder" with id 43
  And user 1234 has permission of "view" for "file" with id 73
  When user with token "900000" wants to see the content of folder with path "bla"
  Then response status code is 200
  And the response contains the file with id 72 and name "wow.txt"
  And the response contains the file with id 73 and name "wow.txt"
  And in the response the file with id 73 has true for the property public
  #  "public": true ? or how is this defined?
  # shouldnt it be "shared": true?


Scenario: empty directory
  Given the folder with id 44 and path "empty" exists
  And user 1234 has permission to view the folder with id 44
  When user with token "900000" wants to see the content of folder with path "emtpy"
  Then response status code is 200
  And the response contains an empty list for files and folders