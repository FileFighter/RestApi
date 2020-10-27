Feature: View Folder
  As a user
  I want to see the content of folders and navigate in them, so they can see and interact with their uploaded and shared files.

Scenario: Successful interaction
  Given credential are valid
  When the user 1234 requests to see the content of folder foo/bar
  And the folder foo/bar exists
  And the user 1234 has the permission to view folder foo/bar
  Then the user 1234 sees a list of files and folder that are in folder foo/bar

Scenario: Folder does not exist
  Given credential are valid
  When the user 1234 requests to see the content of of folder foo/bar/404
  And the folder foo/bar/404 does not exist
  Then the user 1234 sees a notifaction that the folder foo/bar/404 does not exist

Scenario: insufficient authorization
  Given credential are valid
  When the user 9870 requests to see the content of the folder foo/bar
  And the folder foo/bar exists
  And the user 9870 does not have the permission to view the folder foo/bar
  Then the user 9870 sees a notifaction that the folder foo/bar does not exists