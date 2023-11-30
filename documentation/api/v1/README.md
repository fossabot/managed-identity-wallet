# Documentation for Verifiable Credentials API

<a name="documentation-for-api-endpoints"></a>
## Documentation for API Endpoints

All URIs are relative to */api/v1*

| Class | Method | HTTP request | Description |
|------------ | ------------- | ------------- | -------------|
| *DIDDocumentApi* | [**didDocumentsIdentifierGet**](Apis/DIDDocumentApi.md#diddocumentsidentifierget) | **GET** /didDocuments/{identifier} | Resolve DID Document |
| *VerifiableCredentialsApi* | [**credentialsGet**](Apis/VerifiableCredentialsApi.md#credentialsget) | **GET** /credentials | Query Verifiable Credentials |
*VerifiableCredentialsApi* | [**credentialsIssuerMembershipPost**](Apis/VerifiableCredentialsApi.md#credentialsissuermembershippost) | **POST** /credentials/issuer/membership | Issue a Membership Verifiable Credential with base wallet issuer |
*VerifiableCredentialsApi* | [**credentialsIssuerPost**](Apis/VerifiableCredentialsApi.md#credentialsissuerpost) | **POST** /credentials/issuer | Issue a Verifiable Credential with base wallet issuer |
*VerifiableCredentialsApi* | [**credentialsPost**](Apis/VerifiableCredentialsApi.md#credentialspost) | **POST** /credentials | Issue Verifiable Credential |
*VerifiableCredentialsApi* | [**credentialsValidationPost**](Apis/VerifiableCredentialsApi.md#credentialsvalidationpost) | **POST** /credentials/validation | Validate Verifiable Credential |
| *VerifiablePresentationsApi* | [**presentationsPost**](Apis/VerifiablePresentationsApi.md#presentationspost) | **POST** /presentations | Create Verifiable Presentation |
*VerifiablePresentationsApi* | [**presentationsValidationPost**](Apis/VerifiablePresentationsApi.md#presentationsvalidationpost) | **POST** /presentations/validation | Validate Verifiable Presentation |
| *WalletsApi* | [**walletIdentifierCredentialsPost**](Apis/WalletsApi.md#walletidentifiercredentialspost) | **POST** /wallet/{identifier}/credentials | Store Verifiable Credential |
*WalletsApi* | [**walletIdentifierGet**](Apis/WalletsApi.md#walletidentifierget) | **GET** /wallet/{identifier} | Retrieve wallet by identifier |
*WalletsApi* | [**walletPost**](Apis/WalletsApi.md#walletpost) | **POST** /wallet | Create wallet |
*WalletsApi* | [**walletsGet**](Apis/WalletsApi.md#walletsget) | **GET** /wallets | List of wallets |


<a name="documentation-for-models"></a>
## Documentation for Models

 - [CreateMembershipVCDto](./Models/CreateMembershipVCDto.md)
 - [DidDocumentDto](./Models/DidDocumentDto.md)
 - [DidVerificationMethodDto](./Models/DidVerificationMethodDto.md)
 - [ExceptionResponse](./Models/ExceptionResponse.md)
 - [IssuedVerifiableCredentialRequestDto](./Models/IssuedVerifiableCredentialRequestDto.md)
 - [LdProofDto](./Models/LdProofDto.md)
 - [LocalDate](./Models/LocalDate.md)
 - [LocalDateTime](./Models/LocalDateTime.md)
 - [LocalTime](./Models/LocalTime.md)
 - [PublicKeyJwkDto](./Models/PublicKeyJwkDto.md)
 - [SuccessResponse](./Models/SuccessResponse.md)
 - [VerifiableCredentialDto](./Models/VerifiableCredentialDto.md)
 - [VerifiableCredentialRequestDto](./Models/VerifiableCredentialRequestDto.md)
 - [VerifiableCredentialRequestWithoutIssuerDto](./Models/VerifiableCredentialRequestWithoutIssuerDto.md)
 - [VerifiablePresentationDto](./Models/VerifiablePresentationDto.md)
 - [VerifiablePresentationRequestDto](./Models/VerifiablePresentationRequestDto.md)
 - [VerifyResponse](./Models/VerifyResponse.md)
 - [WalletCreateDto](./Models/WalletCreateDto.md)
 - [WalletDto](./Models/WalletDto.md)


<a name="documentation-for-authorization"></a>
## Documentation for Authorization

<a name="auth-token"></a>
### auth-token

- **Type**: HTTP basic authentication

