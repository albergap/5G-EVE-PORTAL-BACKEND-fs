# Interface
## Main
Tables of 5G_EVE - TFG.pdf
- Path/upload: Table 30, only dzuuid and List site
- Path/download/{fileUuid}: Table 29

## Tests
- GET /: list of all uploaded files and form for upload
- POST /: RequestParam"file" and "dzuuid"


# Configurations
## Config.java
- Directory for uploaded files
- Parent path of endpoint

## src/main/resources/application.properties
- Port
- Max file size for uploads
- Max file size for downloads