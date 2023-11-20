
<a name="readme-top"></a>

<!-- Caption -->

<br />
<div align="center">
  <a href="https://eclipse-tractusx.github.io/img/logo_tractus-x.svg">
    <img src="https://eclipse-tractusx.github.io/img/logo_tractus-x.svg" alt="Logo" width="80" height="80">
  </a>

<h3 align="center">Operations Documentation</h3>
<h4 align="center">Tractus-X Managed Identity Wallets</h4>

</div>

[« Up](../README.md)


## Manual Keycloak Configuration

Within the development setup the Keycloak instance is initially prepared with the
values in `./dev-assets/docker-environment/keycloak`. The realm could also be
manually added and configured at http://localhost:8080 via the "Add realm"
button. It can be for example named `localkeycloak`. Also add an additional client,
e.g. named `miw_private_client` with *valid redirect url* set to
`http://localhost:8080/*`. The roles

* add_wallets
* view_wallets
* update_wallets
* delete_wallets
* view_wallet
* update_wallet
* manage_app

Roles can be added under *Clients > miw_private_client > Roles* and then
assigned to the client using *Clients > miw_private_client > Client Scopes*
*> Service Account Roles > Client Roles > miw_private_client*.

The available scopes/roles are:

1. Role `add_wallets` to create a new wallet
2. Role `view_wallets`:

    * to get a list of all wallets
    * to retrieve one wallet by its identifier
    * to validate a Verifiable Credential
    * to validate a Verifiable Presentation
    * to get all stored Verifiable Credentials
3. Role `update_wallets` for the following actions:

    * to store Verifiable Credential
    * to issue a Verifiable Credential
    * to issue a Verifiable Presentation
4. Role `update_wallet`:

    * to remove a Verifiable Credential
    * to store a Verifiable Credential
    * to issue a Verifiable Credential
    * to issue a Verifiable Presentation
5. Role `view_wallet` requires the BPN of Caller and it can be used:

    * to get the Wallet of the related BPN
    * to get stored Verifiable Credentials of the related BPN
    * to validate any Verifiable Credential
    * to validate any Verifiable Presentation
6. Role `manage_app` used to change the log level of the application at runtime. Check Logging in the application section for more
   details

Overview by Endpoint

| Artefact                                  | CRUD   | HTTP Verb/ Request | Endpoint                              | Roles                                        | Constraints                                                |
|-------------------------------------------|--------|--------------------|---------------------------------------|----------------------------------------------|------------------------------------------------------------|
| **Wallets**                               | Read   | GET                | /api/wallets                          | **view_wallets**                             |                                                            |
| **Wallets**                               | Create | POST               | /api/wallets                          | **add_wallets**                              | **1 BPN : 1 WALLET**(PER ONE [1] BPN ONLY ONE [1] WALLET!) |
| **Wallets**                               | Create | POST               | /api/wallets/{identifier}/credentials | **update_wallets** <br />OR**update_wallet** |                                                            |
| **Wallets**                               | Read   | GET                | /api/wallets/{identifier}             | **view_wallets** OR<br />**view_wallet**     |                                                            |
| **Verifiable Presentations - Generation** | Create | POST               | /api/presentation                     | **update_wallets** OR<br />**update_wallet** |                                                            |
| **Verifiable Presentations - Validation** | Create | POST               | /api/presentations/validation         | **view_wallets** OR<br />**view_wallet**     |                                                            |
| **Verifiable Credential - Holder**        | Read   | GET                | /api/credentials                      | **view_wallets** OR<br />**view_wallet**     |                                                            |
| **Verifiable Credential - Holder**        | Create | POST               | /api/credentials                      | **update_wallet** OR<br />**update_wallet**  |                                                            |
| **Verifiable Credential - Holder**        | Delete | DELETE             | /api/credentials                      | **update_wallet**                            |                                                            |
| **Verfiable Credential - Validation**     | Create | POST               | /api/credentials/validation           | **view_wallets** OR<br />**view_wallet**     |                                                            |
| **Verfiable Credential - Issuer**         | Read   | GET                | /api/credentials/issuer               | **view_wallets**                             |                                                            |
| **Verfiable Credential - Issuer**         | Create | POST               | /api/credentials/issuer               | **update_wallets**                           |                                                            |
| **Verfiable Credential - Issuer**         | Create | POST               | /api/credentials/issuer/membership    | **update_wallets**                           |                                                            |
| **Verfiable Credential - Issuer**         | Create | POST               | /api/credentials/issuer/framework     | **update_wallets**                           |                                                            |
| **Verfiable Credential - Issuer**         | Create | POST               | /api/credentials/issuer/distmantler   | **update_wallets**                           |                                                            |
| **DIDDocument**                           | Read   | GET                | /{bpn}/did.json                       | N/A                                          |                                                            |
| **DIDDocument**                           | Read   | GET                | /api/didDocuments/{identifier}        | N/A                                          |                                                            |



Additionally, a Token mapper can be created under *Clients* &gt;
*ManagedIdentityWallets* &gt; *Mappers* &gt; *create* with the following
configuration (using as an example `BPNL000000001`):

| Key                                | Value           |
|------------------------------------|-----------------|
| Name                               | StaticBPN       |
| Mapper Type                        | Hardcoded claim |
| Token Claim Name                   | BPN             |
| Claim value                        | BPNL000000001   |
| Claim JSON Type                    | String          |
| Add to ID token                    | OFF             |
| Add to access token                | ON              |
| Add to userinfo                    | OFF             |
| includeInAccessTokenResponse.label | ON              |

If you receive an error message that the client secret is not valid, please go into
keycloak admin and within *Clients > Credentials* recreate the secret.

# Logging in application

Log level in application can be set using environment variable ``APP_LOG_LEVEL``. Possible values
are ``OFF, ERROR, WARN, INFO, DEBUG, TRACE`` and default value set to ``INFO``

### Change log level at runtime using Spring actuator

We can use ``/actuator/loggers`` API endpoint of actuator for log related things. This end point can be accessible with
role ``manage_app``. We can add this role to authority wallet client using keycloak as below:

![manage_app.png](docs%2Fmanage_app.png)


1. API to get current log settings

```bash
curl --location 'http://localhost:8090/actuator/loggers' \
--header 'Authorization: Bearer access_token'
```

2. Change log level at runtime

```bash
curl --location 'http://localhost:8090/actuator/loggers/{java package name}' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer access_token' \
--data '{"configuredLevel":"INFO"}'
```
i.e.
```bash
curl --location 'http://localhost:8090/actuator/loggers/org.eclipse.tractusx.managedidentitywallets' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer access_token' \
--data '{"configuredLevel":"INFO"}'
```