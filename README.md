# Interface
**Bearer token required**
## Main
Tables of 5G_EVE - TFG.pdf
- Path/upload: File *multipart/form-data* `file`; request parameters `dzuuid` and `List<site>`
- Path/download/{fileUuid}: Table 29


# Configurations
## Config.java
- `FILES_DIR_LOCATION`: directory for uploaded files
- `PARENT_PATH`: parent path of endpoint
- `PATH_DOWNLOAD`: path of download URL
- `PATH_UPLOAD`: path of upload URL

## src/main/resources/application.properties
- `server.port`: port
- `spring.servlet.multipart`.max-file-size: Max file size for uploads
- `spring.servlet.multipart`.max-request-size: Max file size for downloads
- `keycloak.auth-server-url`: Keycloak URL
- `keycloak.realm`: Keycloak realm
- `keycloak.resource`: Keycloak resource (client)
- `keycloak.credentials.secret`: secret of Keycloak resource (client)


# Keycloak objects
    Tutorial: https://medium.com/@ravthiru/rest-service-protected-using-keycloak-authorization-services-a6ad2d8ecb9f

> {2} means: 2, for download and upload

- Client (FS)
 - Access Type: confidential
 - Authorization Enabled: ON
 - Authorization Tab:
   - Resources: {2}
   - Authorization Scopes: {2}
   - Policies: {2}, for roles
   - Permissions: {2}
- Roles: {2}
- Users: {2}, with previous roles