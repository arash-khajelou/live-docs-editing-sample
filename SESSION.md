# Services needed for the project

## Authentication and User management
- register
- login
- logout
- profile management

## Permission and sharing
- share a document
- list shared documents
- update sharing permissions

## Document management

Manage the sharing/permissions for each document

- create document
- list documents
- get document
- commit document
- delete document

## Real-time communication (Web socket)

- edit session start
- edit session end
- connect
- document change submit
- document change broadcast


DTO for each user

- cursor position
    - line number
    - column number


## Attachments and media

- upload
- list attachments
- delete attachment


## Front-end

global event listener

up, down, left, right
any other keys


## Simulator

Doc
- cursor[]
- body

- pressKey(keyId, cursorId)
- validate(keyId, cursorId)
- 