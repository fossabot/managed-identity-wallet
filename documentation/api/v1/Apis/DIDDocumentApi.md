# DIDDocumentApi

All URIs are relative to */api/v1*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**didDocumentsIdentifierGet**](DIDDocumentApi.md#didDocumentsIdentifierGet) | **GET** /didDocuments/{identifier} | Resolve DID Document |


<a name="didDocumentsIdentifierGet"></a>
# **didDocumentsIdentifierGet**
> DidDocumentDto didDocumentsIdentifierGet(identifier)

Resolve DID Document

    Resolve the DID document for a given DID or BPN

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **identifier** | **String**|  | [default to null] |

### Return type

[**DidDocumentDto**](../Models/DidDocumentDto.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

