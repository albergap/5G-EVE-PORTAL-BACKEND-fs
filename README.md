# Interface
## Main
Tables of 5G_EVE - TFG.pdf
- Path/upload: File *multipart/form-data* `file`; request parameters `dzuuid` and `List<site>`
- Path/download/{fileUuid}: Table 29

## Tests
- GET /: list of all uploaded files downloadables and form for upload
- GET /download_file/{fileUuid}: download file
- GET /download/{fileUuid}: download file in Base64
- POST /upload: upload file *multipart/form-data* `file`, `dzuuid` and `List<site>`


# Configurations
## Config.java
- Directory for uploaded files
- Parent path of endpoint

## src/main/resources/application.properties
- Port
- Max file size for uploads
- Max file size for downloads