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
    <br />
        <a href="https://github.com/eclipse-tractusx/managed-identity-wallet/tree/main/cx-ssi-lib/docs"><strong>Explore the docs »</strong></a>
        <br />
    <br />
    <a href="https://github.com/eclipse-tractusx/managed-identity-wallet/issues">Report Bug</a>
    ·
    <a href="https://github.com/eclipse-tractusx/managed-identity-wallet/issues">Request Feature</a>
  </p>
</div>

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
    </li>
    <li><a href="#documentation">Documentation</a></li>
    <li><a href="#project-structure">Project Structure</a></li>
    <li><a href="#reference-of-external-lib">Reference of external lib</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>

## About the Project

The Managed Identity Wallets (MIW) service implements the Self-Sovereign-Identity (SSI) using did:web

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Documentation

| Topic                      | Description                                                                                                                      | Link                                                      |
|----------------------------|----------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------|
| API Documentation          | Dive into the intricacies of the MIW service's REST API with detailed documentation.                                             | [API Documentation](/documentation/api)                   |
| Code Documentation         | Access specific details related to the documentation of the Managed Identity Wallets Application.                                | [Code Documentation](/managed-identity-wallets-app)       |
| Contribution Documentation | Learn how to navigate and contribute to this repository from a developer's perspective.                                          | [Contribution Documentation](/documentation/contribution) |
| Integration Documentation  | Understand the seamless integration process of the MIW service into various applications.                                        | [Integration Documentation](/documentation/integration)   |
| Operations Documentation   | Find comprehensive information on the deployment and operational aspects of the MIW service.                                     | [Operations Documentation](/documentation/operations)     |
| SSI Documentation          | Explore a detailed overview of the Self-Sovereign Identity (SSI) concept and its specific implementation within the MIW service. | [SSI Documentation](/documentation/ssi)                   |

If you find any gaps in our documentation, please don't hesitate to open a ticket. Let us know which topics you feel are
not adequately covered, and we'll address them promptly. Your feedback is valuable in ensuring comprehensive
documentation.

## Project Structure

| Directory                                   | Description                                                                                 | 
|---------------------------------------------|---------------------------------------------------------------------------------------------|
| `/dev-assets`                               | Housing all development assets.                                                             |
| `/charts`                                   | Storage for Helm charts related to the project.                                             |
| `/documentation`                            | Repository for all documentation files.                                                     |
| `/images`                                   | Collection of images utilized in the documentation.                                         |
| `/managed-identity-wallets-api-v1`          | OpenAPI-generated projects containing the API v1 controller definition for the MIW service. |
| `/managed-identity-wallets-api-v2`          | OpenAPI-generated projects containing the API v2 controller definition for the MIW service. |
| `/managed-identity-wallets-api-app`         | Application directory for the Managed Identity Wallets.                                     |
| `/managed-identity-wallets-okhttp3-v1`      | OpenAPI-generated projects with the API v1 Okhttp3 client for the MIW service.              |
| `/managed-identity-wallets-okhttp3-v2`      | OpenAPI-generated projects with the API v2 Okhttp3 client for the MIW service.              |
| `/managed-identity-wallets-resttemplate-v1` | OpenAPI-generated projects with the API v1 RestTemplate client for the MIW service.         |
| `/managed-identity-wallets-resttemplate-v2` | OpenAPI-generated projects with the API v2 RestTemplate client for the MIW service.         |

## Reference of external lib

Sometimes the utilized libraries provides valuable insights into the implementation. Here's a compilation of the key
libraries employed in this project:

1. https://spring.io/projects/spring-boot
2. https://github.com/querydsl/querydsl
3. https://github.com/OpenAPITools/openapi-generator
4. https://github.com/eclipse-tractusx/managed-identity-wallet
5. https://www.testcontainers.org/modules/databases/postgres/
6. https://github.com/dasniko/testcontainers-keycloak
7. https://github.com/mapstruct/mapstruct

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
