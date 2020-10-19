# Interface
**Bearer token required**
## Main
Tables of 5G_EVE - TFG.pdf
- Path/upload: File *multipart/form-data* `file`; request parameters `dzuuid` and `List<site>`
  HttpStatus.FORBIDDEN:
  - if a ExperimentDeveloper tries to download a file not owned
  - if a SiteManager tries to download a file not managed
- Path/download/{fileUuid}/{owner}: Table 29
- Path/list: list files array(File[uuid, owner, array(sites)]) for user:
  if SiteManager -> files with managed site
  if ExperimentDeveloper -> files owned
- Path/deploy/{fileUuid}/{owner}/{site}: set deployment status DEPLOYED


# Keycloak
    Tutorial: https://medium.com/@ravthiru/rest-service-protected-using-keycloak-authorization-services-a6ad2d8ecb9f

- Client (FS)
 - Access Type: confidential
 - Authorization Enabled: ON
 - Authorization Tab:
   - Resources
   - Authorization Scopes
   - Policies
   - Permissions
- Roles
- Users, with previous roles

And fill [application.properties](## src/main/resources/application.properties)



# Database
PostgreSQL database and a role with permissions
- Fill [application.properties](## src/main/resources/application.properties)
- Execute `DB.sql`


# Configurations
## Config.java
- `FILES_DIR_LOCATION`: directory for uploaded files
- `PATH_DOWNLOAD`: path of download URL
- `PATH_UPLOAD`: path of upload URL
- `PATH_LIST`: path of list URL
- `PATH_DEPLOY`: path of deploy URL
- `PATH_RBAC_MANAGED_SITES`: URL of RBAC managed sites
- `ROLE_SITE_MANAGER`: manager role of Keycloak
- `ROLE_USER`: developer role of Keycloak

## src/main/resources/application.properties
- `server.port`: port
- `spring.servlet.multipart`.max-file-size: Max file size for uploads
- `spring.servlet.multipart`.max-request-size: Max file size for downloads

Keycloak:
- `keycloak.auth-server-url`: Keycloak URL
- `keycloak.realm`: Keycloak realm
- `keycloak.resource`: Keycloak resource (client)
- `keycloak.credentials.secret`: secret of Keycloak resource (client)

Database:
- `spring.datasource.url`: URL and DB name
- `spring.datasource.username`: DB user
- `spring.datasource.password`: DB password