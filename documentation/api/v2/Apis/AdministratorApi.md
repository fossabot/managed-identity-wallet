# AdministratorApi

All URIs are relative to */api/v2*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**adminCreateVerifiableCredential**](AdministratorApi.md#adminCreateVerifiableCredential) | **POST** /admin/verifiable-credentials | Create Verifiable Credential |
| [**adminCreateWallet**](AdministratorApi.md#adminCreateWallet) | **POST** /admin/wallets | Create Wallet for User |
| [**adminDeleteVerifiableCredentialById**](AdministratorApi.md#adminDeleteVerifiableCredentialById) | **DELETE** /admin/verifiable-credentials/{verifiableCredentialId} | Delete Verifiable Credential |
| [**adminDeleteWalletById**](AdministratorApi.md#adminDeleteWalletById) | **DELETE** /admin/wallets/{walletId} | Delete Wallet for User |
| [**adminGetVerifiableCredentialById**](AdministratorApi.md#adminGetVerifiableCredentialById) | **GET** /admin/verifiable-credentials/{verifiableCredentialId} | Get Verifiable Credentials by Id |
| [**adminGetVerifiableCredentials**](AdministratorApi.md#adminGetVerifiableCredentials) | **GET** /admin/verifiable-credentials | Get Verifiable Credentials |
| [**adminGetWalletById**](AdministratorApi.md#adminGetWalletById) | **GET** /admin/wallets/{walletId} | Get Wallet for User |
| [**adminGetWallets**](AdministratorApi.md#adminGetWallets) | **GET** /admin/wallets | Get All Wallets |
| [**adminUpdateWallet**](AdministratorApi.md#adminUpdateWallet) | **PUT** /admin/wallets | Update Wallet for User |


<a name="adminCreateVerifiableCredential"></a>
# **adminCreateVerifiableCredential**
> Map adminCreateVerifiableCredential(request\_body)

Create Verifiable Credential

    Creates a verifiable credential

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

<a name="adminCreateWallet"></a>
# **adminCreateWallet**
> CreateWalletResponsePayload adminCreateWallet(CreateWalletRequestPayload)

Create Wallet for User

    Creates a wallet for a user.

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **CreateWalletRequestPayload** | [**CreateWalletRequestPayload**](../Models/CreateWalletRequestPayload.md)|  | |

### Return type

[**CreateWalletResponsePayload**](../Models/CreateWalletResponsePayload.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

<a name="adminDeleteVerifiableCredentialById"></a>
# **adminDeleteVerifiableCredentialById**
> adminDeleteVerifiableCredentialById(verifiableCredentialId)

Delete Verifiable Credential

    Deletes a verifiable credential

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

<a name="adminDeleteWalletById"></a>
# **adminDeleteWalletById**
> adminDeleteWalletById(walletId)

Delete Wallet for User

    Deletes a wallet for by wallet ID.

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **walletId** | **String**| The ID of the wallet. | [default to null] |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: Not defined

<a name="adminGetVerifiableCredentialById"></a>
# **adminGetVerifiableCredentialById**
> Map adminGetVerifiableCredentialById(verifiableCredentialId)

Get Verifiable Credentials by Id

    Retrieves a list of verifiable credentials for a user by user ID.

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

<a name="adminGetVerifiableCredentials"></a>
# **adminGetVerifiableCredentials**
> VerifiableCredentialListResponsePayload adminGetVerifiableCredentials(page, per\_page, id, type, issuer, holder)

Get Verifiable Credentials

    Retrieves a list of verifiable credentials

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **page** | **Integer**| The page to return | [optional] [default to null] |
| **per\_page** | **Integer**| The maximum number of items to return per page | [optional] [default to null] |
| **id** | **String**| Verifiable Credential ID | [optional] [default to null] |
| **type** | **String**| Verifiable Credential Type | [optional] [default to null] |
| **issuer** | **String**| Verifiable Credential Issuer | [optional] [default to null] |
| **holder** | **String**| Verifiable Credential ID | [optional] [default to null] |

### Return type

[**VerifiableCredentialListResponsePayload**](../Models/VerifiableCredentialListResponsePayload.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="adminGetWalletById"></a>
# **adminGetWalletById**
> WalletResponsePayload adminGetWalletById(walletId)

Get Wallet for User

    Retrieves a wallet for a user by user ID.

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **walletId** | **String**| The ID of the wallet. | [default to null] |

### Return type

[**WalletResponsePayload**](../Models/WalletResponsePayload.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="adminGetWallets"></a>
# **adminGetWallets**
> ListWalletsResponsePayload adminGetWallets(page, per\_page)

Get All Wallets

    Retrieves a list of wallets.

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **page** | **Integer**| The page to return | [optional] [default to null] |
| **per\_page** | **Integer**| The maximum number of items to return per page | [optional] [default to null] |

### Return type

[**ListWalletsResponsePayload**](../Models/ListWalletsResponsePayload.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="adminUpdateWallet"></a>
# **adminUpdateWallet**
> UpdateWalletResponsePayload adminUpdateWallet(UpdateWalletRequestPayload)

Update Wallet for User

    Updates a wallet for a user by user ID.

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **UpdateWalletRequestPayload** | [**UpdateWalletRequestPayload**](../Models/UpdateWalletRequestPayload.md)|  | |

### Return type

[**UpdateWalletResponsePayload**](../Models/UpdateWalletResponsePayload.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

