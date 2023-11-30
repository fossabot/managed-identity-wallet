# Documentation for Verifiable Credentials API

<a name="documentation-for-api-endpoints"></a>
## Documentation for API Endpoints

All URIs are relative to */api/v2*

| Class | Method | HTTP request | Description |
|------------ | ------------- | ------------- | -------------|
| *AdministratorApi* | [**adminCreateVerifiableCredential**](Apis/AdministratorApi.md#admincreateverifiablecredential) | **POST** /admin/verifiable-credentials | Create Verifiable Credential |
*AdministratorApi* | [**adminCreateWallet**](Apis/AdministratorApi.md#admincreatewallet) | **POST** /admin/wallets | Create Wallet for User |
*AdministratorApi* | [**adminDeleteVerifiableCredentialById**](Apis/AdministratorApi.md#admindeleteverifiablecredentialbyid) | **DELETE** /admin/verifiable-credentials/{verifiableCredentialId} | Delete Verifiable Credential |
*AdministratorApi* | [**adminDeleteWalletById**](Apis/AdministratorApi.md#admindeletewalletbyid) | **DELETE** /admin/wallets/{walletId} | Delete Wallet for User |
*AdministratorApi* | [**adminGetVerifiableCredentialById**](Apis/AdministratorApi.md#admingetverifiablecredentialbyid) | **GET** /admin/verifiable-credentials/{verifiableCredentialId} | Get Verifiable Credentials by Id |
*AdministratorApi* | [**adminGetVerifiableCredentials**](Apis/AdministratorApi.md#admingetverifiablecredentials) | **GET** /admin/verifiable-credentials | Get Verifiable Credentials |
*AdministratorApi* | [**adminGetWalletById**](Apis/AdministratorApi.md#admingetwalletbyid) | **GET** /admin/wallets/{walletId} | Get Wallet for User |
*AdministratorApi* | [**adminGetWallets**](Apis/AdministratorApi.md#admingetwallets) | **GET** /admin/wallets | Get All Wallets |
*AdministratorApi* | [**adminUpdateWallet**](Apis/AdministratorApi.md#adminupdatewallet) | **PUT** /admin/wallets | Update Wallet for User |
| *UserApi* | [**userCreateVerifiableCredential**](Apis/UserApi.md#usercreateverifiablecredential) | **POST** /verifiable-credentials | Create Verifiable Credential |
*UserApi* | [**userDeleteVerifiableCredentialById**](Apis/UserApi.md#userdeleteverifiablecredentialbyid) | **DELETE** /verifiable-credentials/{verifiableCredentialId} | Delete Verifiable Credential |
*UserApi* | [**userGetIssuedVerifiableCredentials**](Apis/UserApi.md#usergetissuedverifiablecredentials) | **GET** /signed-verifiable-credentials | Get Issued Verifiable Credentials |
*UserApi* | [**userGetVerifiableCredentialById**](Apis/UserApi.md#usergetverifiablecredentialbyid) | **GET** /verifiable-credentials/{verifiableCredentialId} | Get Verifiable Credential by ID |
*UserApi* | [**userGetVerifiableCredentials**](Apis/UserApi.md#usergetverifiablecredentials) | **GET** /verifiable-credentials | Get Verifiable Credentials |
*UserApi* | [**userGetWallet**](Apis/UserApi.md#usergetwallet) | **GET** /wallet | Get Wallet |
*UserApi* | [**userIssuedVerifiableCredential**](Apis/UserApi.md#userissuedverifiablecredential) | **POST** /signed-verifiable-credentials | Issue Verifiable Credential |
*UserApi* | [**userIssuedVerifiablePresentation**](Apis/UserApi.md#userissuedverifiablepresentation) | **POST** /signed-verifiable-presentations | Issue Verifiable Presentation |
*UserApi* | [**userIssuedVerifiablePresentationJwt**](Apis/UserApi.md#userissuedverifiablepresentationjwt) | **POST** /signed-verifiable-presentations/jwt | Issue Verifiable Presentation as JWT |
*UserApi* | [**verifiableCredentialsValidationPost**](Apis/UserApi.md#verifiablecredentialsvalidationpost) | **POST** /verifiable-credentials-validation | Validate Verifiable Credential |
*UserApi* | [**verifiablePresentationJwtValidationPost**](Apis/UserApi.md#verifiablepresentationjwtvalidationpost) | **POST** /verifiable-presentation-jwt-validation | Validate Verifiable JWT Presentation |
*UserApi* | [**verifiablePresentationValidationPost**](Apis/UserApi.md#verifiablepresentationvalidationpost) | **POST** /verifiable-presentation-validation | Validate Verifiable Presentation |


<a name="documentation-for-models"></a>
## Documentation for Models

 - [CreateWalletRequestPayload](./Models/CreateWalletRequestPayload.md)
 - [CreateWalletResponsePayload](./Models/CreateWalletResponsePayload.md)
 - [IssueVerifiableCredentialRequestPayload](./Models/IssueVerifiableCredentialRequestPayload.md)
 - [IssueVerifiablePresentationJwtRequestPayload](./Models/IssueVerifiablePresentationJwtRequestPayload.md)
 - [IssueVerifiablePresentationJwtResponsePayload](./Models/IssueVerifiablePresentationJwtResponsePayload.md)
 - [IssueVerifiablePresentationRequestPayload](./Models/IssueVerifiablePresentationRequestPayload.md)
 - [ListWalletsResponsePayload](./Models/ListWalletsResponsePayload.md)
 - [ListWalletsResponsePayload_allOf](./Models/ListWalletsResponsePayload_allOf.md)
 - [Page](./Models/Page.md)
 - [UpdateWalletRequestPayload](./Models/UpdateWalletRequestPayload.md)
 - [UpdateWalletResponsePayload](./Models/UpdateWalletResponsePayload.md)
 - [ValidateVerifiableCredentialRequestPayload](./Models/ValidateVerifiableCredentialRequestPayload.md)
 - [ValidateVerifiableCredentialResponsePayload](./Models/ValidateVerifiableCredentialResponsePayload.md)
 - [ValidateVerifiablePresentationJwtRequestPayload](./Models/ValidateVerifiablePresentationJwtRequestPayload.md)
 - [ValidateVerifiablePresentationJwtResponsePayload](./Models/ValidateVerifiablePresentationJwtResponsePayload.md)
 - [ValidateVerifiablePresentationRequestPayload](./Models/ValidateVerifiablePresentationRequestPayload.md)
 - [ValidateVerifiablePresentationResponsePayload](./Models/ValidateVerifiablePresentationResponsePayload.md)
 - [VerifiableCredentialListResponsePayload](./Models/VerifiableCredentialListResponsePayload.md)
 - [VerifiableCredentialValidationResponsePayload](./Models/VerifiableCredentialValidationResponsePayload.md)
 - [VerifiableCredentialValidationResult](./Models/VerifiableCredentialValidationResult.md)
 - [VerifiableCredentialValidationResult_violations_inner](./Models/VerifiableCredentialValidationResult_violations_inner.md)
 - [VerifiablePresentationJwtValidationResult](./Models/VerifiablePresentationJwtValidationResult.md)
 - [VerifiablePresentationValidationResponsePayload](./Models/VerifiablePresentationValidationResponsePayload.md)
 - [VerifiablePresentationValidationResult](./Models/VerifiablePresentationValidationResult.md)
 - [Wallet](./Models/Wallet.md)
 - [WalletKey](./Models/WalletKey.md)
 - [WalletResponsePayload](./Models/WalletResponsePayload.md)


<a name="documentation-for-authorization"></a>
## Documentation for Authorization

<a name="OAuth2"></a>
### OAuth2

- **Type**: OAuth
- **Flow**: accessCode
- **Authorization URL**: https://example.com/oauth/authorize
- **Scopes**: 
  - read: Grants read access
  - write: Grants write access
  - admin: Grants access to admin operations

