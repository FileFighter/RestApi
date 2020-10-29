Feature: View Folder
  As a user
  I want to see the content of folders and navigate in them, so they can see and interact with their uploaded and shared files.

Background:
  Given database is empty
  And user 1234 exists
  And user 1234 has access token "900000"
  And the folder with id "42" and path "bla" exists
  And the file with id "72" and path "bla/wow.txt" exists


Scenario: Successful interaction
  Given user 1234 has permission to view the folder with id "42"
  And user 1234 has permission to view the file with id "72"
  When user with token "900000" wants to see the content of folder with path bla
  Then response status code is "200"
  And the reponse contains the file with id "72" and path "bla/wow.txt"


Scenario: Folder does not exist
  Given user 1234 has permission to view the folder with id "42"
  When user with token "900000" wants to see the content of folder with path bla/fasel
  Then response status code is "404"
  And response message cotains "folder with path bla/fasel does not exist"


Scenario: insufficient authorization
  Given user 9877 exists
  And user 9877 has access token "2345678"
  When user with token "2345678" wants to see the content of folder with path bla
  Then response status code is "404"
  And response message cotains "folder with path bla does not exist"