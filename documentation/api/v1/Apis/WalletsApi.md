# WalletsApi

All URIs are relative to */api/v1*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**walletIdentifierCredentialsPost**](WalletsApi.md#walletIdentifierCredentialsPost) | **POST** /wallet/{identifier}/credentials | Store Verifiable Credential |
| [**walletIdentifierGet**](WalletsApi.md#walletIdentifierGet) | **GET** /wallet/{identifier} | Retrieve wallet by identifier |
| [**walletPost**](WalletsApi.md#walletPost) | **POST** /wallet | Create wallet |
| [**walletsGet**](WalletsApi.md#walletsGet) | **GET** /wallets | List of wallets |


<a name="walletIdentifierCredentialsPost"></a>
# **walletIdentifierCredentialsPost**
> SuccessResponse walletIdentifierCredentialsPost(identifier, IssuedVerifiableCredentialRequestDto)

Store Verifiable Credential

    Permission: **update_wallets** OR **update_wallet** (The BPN of wallet to extract credentials from must equal BPN of caller)  Store a verifiable credential in the wallet of the given identifier

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **identifier** | **String**|  | [default to null] |
| **IssuedVerifiableCredentialRequestDto** | [**IssuedVerifiableCredentialRequestDto**](../Models/IssuedVerifiableCredentialRequestDto.md)| The verifiable credential to be stored | [optional] |

### Return type

[**SuccessResponse**](../Models/SuccessResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

<a name="walletIdentifierGet"></a>
# **walletIdentifierGet**
> WalletDto walletIdentifierGet(identifier, withCredentials)

Retrieve wallet by identifier

    Permission: **view_wallets** OR **view_wallet** (The BPN of Wallet to retrieve must equal the BPN of caller)  Retrieve single wallet by identifier, with or without its credentials

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **identifier** | **String**|  | [default to null] |
| **withCredentials** | **Boolean**|  | [default to null] |

### Return type

[**WalletDto**](../Models/WalletDto.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="walletPost"></a>
# **walletPost**
> WalletDto walletPost(WalletCreateDto)

Create wallet

    Permission: **add_wallets**  Create a wallet and store it 

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **WalletCreateDto** | [**WalletCreateDto**](../Models/WalletCreateDto.md)| wallet to create | |

### Return type

[**WalletDto**](../Models/WalletDto.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

<a name="walletsGet"></a>
# **walletsGet**
> List walletsGet()

List of wallets

    Permission: **view_wallets**  Retrieve list of registered wallets

### Parameters
This endpoint does not need any parameter.

### Return type

[**List**](../Models/WalletDto.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

