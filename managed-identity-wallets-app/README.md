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

[« Up](..)

## Overview

The Managed Identity Wallets Application is a Spring Boot project encompassing services, controllers, events, health
checks, and additional functionalities. It also incorporates features related to Self-Sovereign Identity (SSI).

### Invoker Components

The system comprises four distinct component types, each activated under specific conditions to potentially alter the
application's state:

- API
- Event Listener
- Cron Jobs
- Health Indicator

### Business Components

These components have the ability to invoke factories, generating essential objects to execute their designated tasks.
Utilizing these objects, they can access required services to fulfill their functions.

### Persistence Components

Services initiate interactions with repositories to persist newly created or updated application states.


<img src="../images/appDesign.png" alt="design.png" style="max-width: 800px">

## Directory Structure

| Directory        | Description                                                                                | Further Documentation                                                | 
|------------------|--------------------------------------------------------------------------------------------|----------------------------------------------------------------------|
| `/annotations`   | The annotations directory contains all custom annotations used throughout the application. |                                                                      |
| `/api`           | The api directory contains all REST API controllers.                                       | [Application API Documentation](./documentation/api)                 |
| `/command`       | The command directory contains all command classes.                                        |                                                                      |
| `/config`        | The config directory contains all configuration classes.                                   |                                                                      |
| `/cron`          | The cron directory contains all cron job classes.                                          | [Application Cron Documentation](./documentation/cron)               |
| `/event`         | The event directory contains all event classes.                                            | [Application Events Documentation](./documentation/events)           |
| `/eventListener` | The eventListener directory contains all event listener classes.                           | [Application Events Documentation](./documentation/events)           |
| `/exception`     | The exception directory contains all exception classes.                                    |                                                                      |
| `/factory`       | The factory directory contains all factory classes.                                        |                                                                      |
| `/health`        | The health directory contains all health indicator classes.                                |                                                                      |
| `/models`        | The models directory contains all model classes.                                           |                                                                      |
| `/repository`    | The repository directory contains all repository classes.                                  | [Application Persistence Documentation](./documentation/persistence) |
| `/service`       | The service directory contains all service classes.                                        |                                                                      |
| `/util`          | The util directory contains all utility classes.                                           |                                                                      |
