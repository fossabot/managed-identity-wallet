# VerifiableCredentialsApi

All URIs are relative to */api/v1*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**credentialsGet**](VerifiableCredentialsApi.md#credentialsGet) | **GET** /credentials | Query Verifiable Credentials |
| [**credentialsIssuerMembershipPost**](VerifiableCredentialsApi.md#credentialsIssuerMembershipPost) | **POST** /credentials/issuer/membership | Issue a Membership Verifiable Credential with base wallet issuer |
| [**credentialsIssuerPost**](VerifiableCredentialsApi.md#credentialsIssuerPost) | **POST** /credentials/issuer | Issue a Verifiable Credential with base wallet issuer |
| [**credentialsPost**](VerifiableCredentialsApi.md#credentialsPost) | **POST** /credentials | Issue Verifiable Credential |
| [**credentialsValidationPost**](VerifiableCredentialsApi.md#credentialsValidationPost) | **POST** /credentials/validation | Validate Verifiable Credential |


<a name="credentialsGet"></a>
# **credentialsGet**
> List credentialsGet(holderIdentifier, credentialId, issuerIdentifier, type)

Query Verifiable Credentials

    Permission: **view_wallets** OR **view_wallet** (The BPN of holderIdentifier must equal BPN of caller)  Search verifiable credentials with filter criteria

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **holderIdentifier** | **String**|  | [optional] [default to null] |
| **credentialId** | **String**|  | [optional] [default to null] |
| **issuerIdentifier** | **String**|  | [optional] [default to null] |
| **type** | [**List**](../Models/String.md)|  | [optional] [default to null] |

### Return type

[**List**](../Models/VerifiableCredentialDto.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="credentialsIssuerMembershipPost"></a>
# **credentialsIssuerMembershipPost**
> VerifiableCredentialDto credentialsIssuerMembershipPost(CreateMembershipVCDto)

Issue a Membership Verifiable Credential with base wallet issuer

    Permission: **update_wallets** OR **update_wallet** (The BPN of base wallet must equal BPN of caller)  Issue a verifiable credential by base wallet

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **CreateMembershipVCDto** | [**CreateMembershipVCDto**](../Models/CreateMembershipVCDto.md)| The bpn of the holders wallet | |

### Return type

[**VerifiableCredentialDto**](../Models/VerifiableCredentialDto.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

<a name="credentialsIssuerPost"></a>
# **credentialsIssuerPost**
> VerifiableCredentialDto credentialsIssuerPost(VerifiableCredentialRequestWithoutIssuerDto)

Issue a Verifiable Credential with base wallet issuer

    Permission: **update_wallets** OR **update_wallet** (The BPN of base wallet must equal BPN of caller)  Issue a verifiable credential by base wallet

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **VerifiableCredentialRequestWithoutIssuerDto** | [**VerifiableCredentialRequestWithoutIssuerDto**](../Models/VerifiableCredentialRequestWithoutIssuerDto.md)| The verifiable credential input | [optional] |

### Return type

[**VerifiableCredentialDto**](../Models/VerifiableCredentialDto.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

<a name="credentialsPost"></a>
# **credentialsPost**
> VerifiableCredentialDto credentialsPost(VerifiableCredentialRequestDto)

Issue Verifiable Credential

    Permission: **update_wallets** OR **update_wallet** (The BPN of the issuer of the Verifiable Credential must equal BPN of caller)  Issue a verifiable credential with a given issuer DID

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **VerifiableCredentialRequestDto** | [**VerifiableCredentialRequestDto**](../Models/VerifiableCredentialRequestDto.md)| The verifiable credential input data | |

### Return type

[**VerifiableCredentialDto**](../Models/VerifiableCredentialDto.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

<a name="credentialsValidationPost"></a>
# **credentialsValidationPost**
> VerifyResponse credentialsValidationPost(withDateValidation, withRevocationValidation, VerifiableCredentialDto)

Validate Verifiable Credential

    Permission: **view_wallets** OR **view_wallet**  Validate Verifiable Credentials

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **withDateValidation** | **Boolean**|  | [optional] [default to false] |
| **withRevocationValidation** | **Boolean**|  | [optional] [default to true] |
| **VerifiableCredentialDto** | [**VerifiableCredentialDto**](../Models/VerifiableCredentialDto.md)| The verifiable credential to validate | [optional] |

### Return type

[**VerifyResponse**](../Models/VerifyResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

