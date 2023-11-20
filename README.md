<a name="readme-top"></a>

<!-- Shields -->
[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]



<!-- Caption -->

<br />
<div align="center">
  <a href="https://eclipse-tractusx.github.io/img/logo_tractus-x.svg">
    <img src="https://eclipse-tractusx.github.io/img/logo_tractus-x.svg" alt="Logo" width="80" height="80">
  </a>

<h3 align="center">Tractus-X Managed Identity Wallets</h3>

  <p align="center">
    This open source project has emerged with a goal to foster collaborative development and innovation in the area of Self-Sovereign Identity.
    <br />
        <a href="https://github.com/eclipse-tractusx/SSI-agent-lib/tree/main/cx-ssi-lib/docs"><strong>Explore the docs »</strong></a>
        <br />
    <br />
    <a href="https://github.com/eclipse-tractusx/SSI-agent-lib/issues">Report Bug</a>
    ·
    <a href="https://github.com/eclipse-tractusx/SSI-agent-lib/issues">Request Feature</a>
  </p>
</div>

## About the Project

The Managed Identity Wallets (MIW) service implements the Self-Sovereign-Identity (SSI) using did:web

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Documentation

| Topic                    | Description                                                                                                                                     | Link                                                            |
|--------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------|
| API Documentation        | The API documentation provides information about the REST API of the MIW service.                                                               | [API Documentation](/documentation/api/README.md)               |
| Developer Documentation  | The developer documentation provides information about the architecture, the design and the implementation of the MIW service.                  | [Developer Documentation](/documentation/development/README.md) |
| Operations Documentation | The operations documentation provides information about the deployment and the operation of the MIW service.                                    | [Operations Documentation](/documentation/operations/README.md) |
| SSI Documentation        | The SSI documentation offers a broad overview of the SSI concept and provides specific details about its implementation within the MIW service. | [SSI Documentation](/documentation/ssi/README.md)               |

## Project Structure

| Directory                           | Description                                                                               | Further Documentation                                        |
|-------------------------------------|-------------------------------------------------------------------------------------------|--------------------------------------------------------------|
| `/dev-assets`                       | The dev-assets directory contains all development assets.                                 |                                                              |
| `/charts`                           | The charts directory contains all Helm charts.                                            |                                                              |
| `/documentation`                    | The documentation directory contains all documentation files.                             |                                                              |
| `/images`                           | The images directory contains all images used in the documentation.                       |                                                              |
| `/managed-identity-wallets-api-v1`  | OpenAPI generated projects. Contains the API v1 controller definition of the MIW service. | [API Documentation](/documentation/api/README.md)            |
| `/managed-identity-wallets-api-v2`  | OpenAPI generated projects. Contains the API v2 controller definition of the MIW service. | [API Documentation](/documentation/api/README.md)            |
| `/managed-identity-wallets-api-app` | Managed Identity Wallets application.                                                     | [App Documentation](/managed-identity-wallets-app/README.md) |

## Reference of external lib

1. https://github.com/eclipse-tractusx/managed-identity-wallet
2. https://www.testcontainers.org/modules/databases/postgres/
3. https://github.com/dasniko/testcontainers-keycloak

<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- CONTRIBUTING -->

## Contributing

We are thrilled to have you here and excited about your interest in contributing to our project.
Your contributions play a vital role in making our project successful and we truly appreciate your
support.

To ensure a smooth and enjoyable experience for everyone involved, we have put together this guide
to help you understand how you can contribute effectively. Please take a moment to read through
the [CONTRIBUTING.md](CONTRIBUTING.md) before you start contributing.

Please ensure that you adhere to the project's coding style, write unit tests for your changes if
applicable, and provide clear documentation for any new features or changes.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- LICENSE -->

# License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for more
information.

```
Apache License
Version 2.0, January 2004
http://www.apache.org/licenses/
```

You can freely use, modify, and distribute this project under the terms of the Apache License 2.0.
<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- CONTACT -->

# Contact

If you have any questions, suggestions, or feedback regarding this project, please feel free to
reach out to us. You can contact our team at:

| Name          | Link                                                               |
|---------------|--------------------------------------------------------------------|
| Organization  | https://eclipse-tractusx.github.io                                 |
| Issue Tracker | https://github.com/eclipse-tractusx/managed-identity-wallet/issues |

We value your input and appreciate your interest in contributing to the project. Don't hesitate to
contact us if you need any assistance or want to get involved.

<p align="right">(<a href="#readme-top">back to top</a>)</p>


[contributors-shield]: https://img.shields.io/github/contributors/eclipse-tractusx/managed-identity-wallet.svg?style=for-the-badge

[contributors-url]: https://github.com/eclipse-tractusx/managed-identity-wallet/graphs/contributors

[forks-shield]: https://img.shields.io/github/forks/eclipse-tractusx/managed-identity-wallet.svg?style=for-the-badge

[forks-url]: https://github.com/eclipse-tractusx/managed-identity-wallet/network/members

[stars-shield]: https://img.shields.io/github/stars/eclipse-tractusx/managed-identity-wallet.svg?style=for-the-badge

[stars-url]: https://github.com/eclipse-tractusx/managed-identity-wallet/stargazers

[issues-shield]: https://img.shields.io/github/issues/eclipse-tractusx/managed-identity-wallet.svg?style=for-the-badge

[issues-url]: https://github.com/eclipse-tractusx/managed-identity-wallet/issues

[license-shield]: https://img.shields.io/github/license/eclipse-tractusx/managed-identity-wallet.svg?style=for-the-badge

[license-url]: https://github.com/eclipse-tractusx/managed-identity-wallet/blob/master/LICENSE.txt
