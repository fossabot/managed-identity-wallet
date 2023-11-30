# VerifiablePresentationsApi

All URIs are relative to */api/v1*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**presentationsPost**](VerifiablePresentationsApi.md#presentationsPost) | **POST** /presentations | Create Verifiable Presentation |
| [**presentationsValidationPost**](VerifiablePresentationsApi.md#presentationsValidationPost) | **POST** /presentations/validation | Validate Verifiable Presentation |


<a name="presentationsPost"></a>
# **presentationsPost**
> VerifiablePresentationDto presentationsPost(VerifiablePresentationRequestDto, asJwt, withCredentialsDateValidation, withCredentialsValidation, withRevocationValidation)

Create Verifiable Presentation

    Permission: **update_wallets** OR **update_wallet** (The BPN of the issuer of the Verifiable Presentation must equal to BPN of caller)  Create a verifiable presentation from a list of verifiable credentials, signed by the holder

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **VerifiablePresentationRequestDto** | [**VerifiablePresentationRequestDto**](../Models/VerifiablePresentationRequestDto.md)| The verifiable presentation input data | |
| **asJwt** | **Boolean**|  | [optional] [default to true] |
| **withCredentialsDateValidation** | **Boolean**|  | [optional] [default to true] |
| **withCredentialsValidation** | **Boolean**|  | [optional] [default to true] |
| **withRevocationValidation** | **Boolean**|  | [optional] [default to true] |

### Return type

[**VerifiablePresentationDto**](../Models/VerifiablePresentationDto.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

<a name="presentationsValidationPost"></a>
# **presentationsValidationPost**
> VerifyResponse presentationsValidationPost(VerifiablePresentationDto, withDateValidation, withRevocationValidation)

Validate Verifiable Presentation

    Permission: **view_wallets** OR **view_wallet**  Validate Verifiable Presentation with all included credentials

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **VerifiablePresentationDto** | [**VerifiablePresentationDto**](../Models/VerifiablePresentationDto.md)| The verifiable presentation to validate | |
| **withDateValidation** | **Boolean**|  | [optional] [default to false] |
| **withRevocationValidation** | **Boolean**|  | [optional] [default to true] |

### Return type

[**VerifyResponse**](../Models/VerifyResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

