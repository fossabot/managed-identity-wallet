<a name="readme-top"></a>

<!-- Caption -->

<br />
<div align="center">
  <a href="https://eclipse-tractusx.github.io/img/logo_tractus-x.svg">
    <img src="https://eclipse-tractusx.github.io/img/logo_tractus-x.svg" alt="Logo" width="80" height="80">
  </a>

<h3 align="center">Application API Documentation</h3>
<h4 align="center">Tractus-X Managed Identity Wallets</h4>

</div>

[« Up](../../README.md)

## Summary

This chapter contains the documentation of the API of the Tractus-X Managed Identity Wallets.

It is intended for Managed-Identity-Wallets developers/contributors. For a more API documentation from a users' point of
view please refer to the [API Documentation](../../../documentation/api/README.md) page.

## Generators

This repository heavily relies on the [OpenAPI Generator](https://openapi-generator.tech/) project. The following
generators are used:

| Generator                                                                 | Project                                                                                                               | Description                                                                                             | 
|---------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| [spring](https://openapi-generator.tech/docs/generators/spring)           | [managed-identity-wallets-api-v1](../../../managed-identity-wallets-api-v1/README.md)                                 | Generates Spring API v1.                                                                                |
| [spring](https://openapi-generator.tech/docs/generators/spring)           | [managed-identity-wallets-api-v2](../../../managed-identity-wallets-api-v1/README.md)                                 | Generates Spring API v2.                                                                                |
| [java](https://openapi-generator.tech/docs/generators/java)               | [managed-identity-wallets-client-okhttp3-v1](../../../managed-identity-wallets-client-okhttp3-v1/README.md)           | Generates an OkHttp3 Client to access the API v1.                                                       |
| [java](https://openapi-generator.tech/docs/generators/java)               | [managed-identity-wallets-client-okhttp3-v2](../../../managed-identity-wallets-client-okhttp3-v2/README.md)           | Generates an OkHttp3 Client to access the API v2.                                                       |
| [java](https://openapi-generator.tech/docs/generators/java)               | [managed-identity-wallets-client-resttemplate-v1](../../../managed-identity-wallets-client-resttemplate-v1/README.md) | Generates an Spring RestTemplate Client to access the API v1.                                           |
| [java](https://openapi-generator.tech/docs/generators/java)               | [managed-identity-wallets-client-resttemplate-v2](../../../managed-identity-wallets-client-resttemplate-v2/README.md) | Generates an Spring RestTemplate Client to access the API v2.                                           |
| [markdown(beta)](https://openapi-generator.tech/docs/generators/markdown) | [managed-identity-wallets-client-markdown-v1](../../../managed-identity-wallets-markdown-v1/README.md)                | Generates an Markdown to document the API v1. The documentation is written into `documentation/api/v1/` | 
| [markdown(beta)](https://openapi-generator.tech/docs/generators/markdown) | [managed-identity-wallets-client-markdown-v2](../../../managed-identity-wallets-markdown-v2/README.md)                | Generates an Markdown to document the API v2. The documentation is written into `documentation/api/v2/` |

## API v1

> Deprecated. Not generated.

As the API v1 was developed before the introduction of the API generators, the API v1 code
does not use the generated code. This prevents the risk of introducing breaking changes, as the code might be slightly
different from the generated OpenAPI spec code.

Additionally, the `/api/v1` directory contains miscellaneous deprecated classes which are used by the v1 controllers.
All these v1 classes remained as untouched as possible to prevent breaking changes, too.

## API v2
