# UserApi

All URIs are relative to */api/v2*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**userCreateVerifiableCredential**](UserApi.md#userCreateVerifiableCredential) | **POST** /verifiable-credentials | Create Verifiable Credential |
| [**userDeleteVerifiableCredentialById**](UserApi.md#userDeleteVerifiableCredentialById) | **DELETE** /verifiable-credentials/{verifiableCredentialId} | Delete Verifiable Credential |
| [**userGetIssuedVerifiableCredentials**](UserApi.md#userGetIssuedVerifiableCredentials) | **GET** /signed-verifiable-credentials | Get Issued Verifiable Credentials |
| [**userGetVerifiableCredentialById**](UserApi.md#userGetVerifiableCredentialById) | **GET** /verifiable-credentials/{verifiableCredentialId} | Get Verifiable Credential by ID |
| [**userGetVerifiableCredentials**](UserApi.md#userGetVerifiableCredentials) | **GET** /verifiable-credentials | Get Verifiable Credentials |
| [**userGetWallet**](UserApi.md#userGetWallet) | **GET** /wallet | Get Wallet |
| [**userIssuedVerifiableCredential**](UserApi.md#userIssuedVerifiableCredential) | **POST** /signed-verifiable-credentials | Issue Verifiable Credential |
| [**userIssuedVerifiablePresentation**](UserApi.md#userIssuedVerifiablePresentation) | **POST** /signed-verifiable-presentations | Issue Verifiable Presentation |
| [**userIssuedVerifiablePresentationJwt**](UserApi.md#userIssuedVerifiablePresentationJwt) | **POST** /signed-verifiable-presentations/jwt | Issue Verifiable Presentation as JWT |
| [**verifiableCredentialsValidationPost**](UserApi.md#verifiableCredentialsValidationPost) | **POST** /verifiable-credentials-validation | Validate Verifiable Credential |
| [**verifiablePresentationJwtValidationPost**](UserApi.md#verifiablePresentationJwtValidationPost) | **POST** /verifiable-presentation-jwt-validation | Validate Verifiable JWT Presentation |
| [**verifiablePresentationValidationPost**](UserApi.md#verifiablePresentationValidationPost) | **POST** /verifiable-presentation-validation | Validate Verifiable Presentation |


<a name="userCreateVerifiableCredential"></a>
# **userCreateVerifiableCredential**
> Map userCreateVerifiableCredential(request\_body)

Create Verifiable Credential

    Stores the Verifiable Credential in the wallet.

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **request\_body** | [**Map**](../Models/AnyType.md)|  | |

### Return type

[**Map**](../Models/AnyType.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

<a name="userDeleteVerifiableCredentialById"></a>
# **userDeleteVerifiableCredentialById**
> userDeleteVerifiableCredentialById(verifiableCredentialId)

Delete Verifiable Credential

    Removes Verifiable Credential from the wallet by ID.

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **verifiableCredentialId** | **String**| The ID of the verifiable credential. | [default to null] |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: Not defined

<a name="userGetIssuedVerifiableCredentials"></a>
# **userGetIssuedVerifiableCredentials**
> VerifiableCredentialListResponsePayload userGetIssuedVerifiableCredentials(page, per\_page, type)

Get Issued Verifiable Credentials

    Retrieves a list of issued Verifiable Credentials. These Verifiable Credentials may or may not be stored in the wallet.

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **page** | **Integer**| The page to return | [optional] [default to null] |
| **per\_page** | **Integer**| The maximum number of items to return per page | [optional] [default to null] |
| **type** | **String**| Verifiable Credential Type | [optional] [default to null] |

### Return type

[**VerifiableCredentialListResponsePayload**](../Models/VerifiableCredentialListResponsePayload.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="userGetVerifiableCredentialById"></a>
# **userGetVerifiableCredentialById**
> Map userGetVerifiableCredentialById(verifiableCredentialId)

Get Verifiable Credential by ID

    Retrieves a verifiable credential from the wallet by ID.

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **verifiableCredentialId** | **String**| The ID of the verifiable credential. | [default to null] |

### Return type

[**Map**](../Models/AnyType.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="userGetVerifiableCredentials"></a>
# **userGetVerifiableCredentials**
> VerifiableCredentialListResponsePayload userGetVerifiableCredentials(page, per\_page, type, issuer)

Get Verifiable Credentials

    Retrieves a list of Verifiable Credentials from the wallet.

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **page** | **Integer**| The page to return | [optional] [default to null] |
| **per\_page** | **Integer**| The maximum number of items to return per page | [optional] [default to null] |
| **type** | **String**| Verifiable Credential Type | [optional] [default to null] |
| **issuer** | **String**| Verifiable Credential Issuer | [optional] [default to null] |

### Return type

[**VerifiableCredentialListResponsePayload**](../Models/VerifiableCredentialListResponsePayload.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="userGetWallet"></a>
# **userGetWallet**
> WalletResponsePayload userGetWallet()

Get Wallet

    Retrieves wallet information.

### Parameters
This endpoint does not need any parameter.

### Return type

[**WalletResponsePayload**](../Models/WalletResponsePayload.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="userIssuedVerifiableCredential"></a>
# **userIssuedVerifiableCredential**
> Map userIssuedVerifiableCredential(IssueVerifiableCredentialRequestPayload)

Issue Verifiable Credential

    Issues a new Verifiable Credential.

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **IssueVerifiableCredentialRequestPayload** | [**IssueVerifiableCredentialRequestPayload**](../Models/IssueVerifiableCredentialRequestPayload.md)|  | [optional] |

### Return type

[**Map**](../Models/AnyType.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

<a name="userIssuedVerifiablePresentation"></a>
# **userIssuedVerifiablePresentation**
> Map userIssuedVerifiablePresentation(IssueVerifiablePresentationRequestPayload)

Issue Verifiable Presentation

    Issues a new Verifiable Presentation.

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **IssueVerifiablePresentationRequestPayload** | [**IssueVerifiablePresentationRequestPayload**](../Models/IssueVerifiablePresentationRequestPayload.md)|  | [optional] |

### Return type

[**Map**](../Models/AnyType.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

<a name="userIssuedVerifiablePresentationJwt"></a>
# **userIssuedVerifiablePresentationJwt**
> IssueVerifiablePresentationJwtResponsePayload userIssuedVerifiablePresentationJwt(IssueVerifiablePresentationJwtRequestPayload)

Issue Verifiable Presentation as JWT

    Issues a new Verifiable Presentation as JWT.

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **IssueVerifiablePresentationJwtRequestPayload** | [**IssueVerifiablePresentationJwtRequestPayload**](../Models/IssueVerifiablePresentationJwtRequestPayload.md)|  | [optional] |

### Return type

[**IssueVerifiablePresentationJwtResponsePayload**](../Models/IssueVerifiablePresentationJwtResponsePayload.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

<a name="verifiableCredentialsValidationPost"></a>
# **verifiableCredentialsValidationPost**
> ValidateVerifiableCredentialResponsePayload verifiableCredentialsValidationPost(ValidateVerifiableCredentialRequestPayload)

Validate Verifiable Credential

    Validates a Verifiable Credential

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **ValidateVerifiableCredentialRequestPayload** | [**ValidateVerifiableCredentialRequestPayload**](../Models/ValidateVerifiableCredentialRequestPayload.md)|  | [optional] |

### Return type

[**ValidateVerifiableCredentialResponsePayload**](../Models/ValidateVerifiableCredentialResponsePayload.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

<a name="verifiablePresentationJwtValidationPost"></a>
# **verifiablePresentationJwtValidationPost**
> ValidateVerifiablePresentationJwtResponsePayload verifiablePresentationJwtValidationPost(ValidateVerifiablePresentationJwtRequestPayload)

Validate Verifiable JWT Presentation

    Validates a Verifiable JWT Presentation

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **ValidateVerifiablePresentationJwtRequestPayload** | [**ValidateVerifiablePresentationJwtRequestPayload**](../Models/ValidateVerifiablePresentationJwtRequestPayload.md)|  | [optional] |

### Return type

[**ValidateVerifiablePresentationJwtResponsePayload**](../Models/ValidateVerifiablePresentationJwtResponsePayload.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

<a name="verifiablePresentationValidationPost"></a>
# **verifiablePresentationValidationPost**
> ValidateVerifiablePresentationResponsePayload verifiablePresentationValidationPost(ValidateVerifiablePresentationRequestPayload)

Validate Verifiable Presentation

    Validates a Verifiable Presentation

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **ValidateVerifiablePresentationRequestPayload** | [**ValidateVerifiablePresentationRequestPayload**](../Models/ValidateVerifiablePresentationRequestPayload.md)|  | [optional] |

### Return type

[**ValidateVerifiablePresentationResponsePayload**](../Models/ValidateVerifiablePresentationResponsePayload.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

