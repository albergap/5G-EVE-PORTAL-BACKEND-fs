# Interface
**Bearer token required**
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
- `FILES_DIR_LOCATION`: Directory for uploaded files
- `PATH`: Parent path of endpoint
- `KEYCLOAK_URL_PATTERN`: URL of keycloak
- `KEYCLOAK_ROLE`: role allowed

## src/main/resources/application.properties
- `server.port`: Port
- `spring.servlet.multipart`.max-file-size: Max file size for uploads
- `spring.servlet.multipart`.max-request-size: Max file size for downloads
- `keycloak.auth-server-url`: Keycloak URL
- `keycloak.realm`: Keycloak realm
- `keycloak.resource`: Keycloak resource