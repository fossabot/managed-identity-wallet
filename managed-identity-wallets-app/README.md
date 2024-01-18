<a name="readme-top"></a>

<!-- Caption -->

<br />
<div align="center">
  <a href="https://eclipse-tractusx.github.io/img/logo_tractus-x.svg">
    <img src="https://eclipse-tractusx.github.io/img/logo_tractus-x.svg" alt="Logo" width="80" height="80">
  </a>

<h3 align="center">Application</h3>
<h4 align="center">Tractus-X Managed Identity Wallets</h4>

</div>

[« Up](../README.md)

## Overview

The Managed Identity Wallets Application is a Spring Boot project encompassing services, controllers, events, health
checks, and additional functionalities. It also incorporates features related to Self-Sovereign Identity (SSI).

The application is build in three layers (API, Business, Persistence) and is designed to be modular and extensible.

**The API Layer** is responsible for exposing the application's functionalities to the outside world. It is mostly
generated from a OpenAPI specification file. The project then implements the generated interfaces and maps the calls
into the business layer.

**The Business Layer** is responsible for implementing the application's functionalities. It is mostly composed of
services, events, and factories. All state changes are performed by services, which delegate the new state to the
persistence layer. Events are used to notify other components of state changes. Factories are used to create business
objects.

**The Persistence Layer** is responsible for persisting the application's state. At the time of writing the application
state is persisted in a relational database and a key vault. The persistence layer is composed of repositories featuring a
query model to retrieve data.

## Directory Structure

| Directory        | Description                                                                                | Further Documentation                                                          | 
|------------------|--------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| `/annotations`   | The annotations directory contains all custom annotations used throughout the application. |                                                                                |
| `/api`           | The api directory contains all REST API controllers.                                       | [Application API Documentation](./documentation/api/README.md)                 |
| `/command`       | The command directory contains all command classes.                                        |                                                                                |
| `/config`        | The config directory contains all configuration classes.                                   |                                                                                |
| `/event`         | The event directory contains all event classes.                                            | [Application Events Documentation](./documentation/events/README.md)           |
| `/eventListener` | The eventListener directory contains all event listener classes.                           | [Application Events Documentation](./documentation/events/README.md)           |
| `/exception`     | The exception directory contains all exception classes.                                    |                                                                                |
| `/factory`       | The factory directory contains all factory classes.                                        |                                                                                |
| `/health`        | The health directory contains all health indicator classes.                                |                                                                                |
| `/models`        | The models directory contains all model classes.                                           |                                                                                |
| `/repository`    | The repository directory contains all repository classes.                                  | [Application Persistence Documentation](./documentation/persistence/README.md) |
| `/service`       | The service directory contains all service classes.                                        |                                                                                |
| `/util`          | The util directory contains all utility classes.                                           |                                                                                |
