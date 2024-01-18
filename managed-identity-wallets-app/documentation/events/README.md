<a name="readme-top"></a>

<!-- Caption -->

<br />
<div align="center">
  <a href="https://eclipse-tractusx.github.io/img/logo_tractus-x.svg">
    <img src="https://eclipse-tractusx.github.io/img/logo_tractus-x.svg" alt="Logo" width="80" height="80">
  </a>

<h3 align="center">Application Events Documentation</h3>
<h4 align="center">Tractus-X Managed Identity Wallets</h4>

</div>

[« Up](../../README.md)

# Managed Identity Wallets Application - Developer Documentation

## Overview

The Managed Identity Wallets application employs a set of events and corresponding event listeners to manage various
operations related to wallets and verifiable credentials. This document provides detailed information about each event,
its purpose, and the associated event listeners.

### Transaction Lifecycle

- Events ending with 'ing': Invoked before the new state is persisted. Transaction is interrupt able (e.g. by throwing
  an exception).
- Events ending with 'ed': Called after the transaction, once the new state is finalized/persisted.

## Events

### VerifiableCredentialCreatedEvent

- **Description**: Triggered after a verifiable credential has been successfully created.
- **Transaction Lifecycle**: After transaction is finalized.

### VerifiableCredentialCreatingEvent

- **Description**: Triggered when a new verifiable credential is being created.
- **Transaction Lifecycle**: Before Transaction is finalized.

### VerifiableCredentialDeletedEvent

- **Description**: Triggered after a verifiable credential has been successfully deleted.
- **Transaction Lifecycle**: After transaction is finalized.

### VerifiableCredentialDeletingEvent

- **Description**: Triggered when a verifiable credential is about to be deleted.
- **Transaction Lifecycle**: Before Transaction is finalized.

### VerifiableCredentialRemovedFromWalletEvent

- **Description**: Triggered after a verifiable credential has been removed from a wallet.
- **Transaction Lifecycle**: After transaction is finalized.

### VerifiableCredentialRemovingFromWalletEvent

- **Description**: Triggered when a verifiable credential is about to be removed from a wallet.
- **Transaction Lifecycle**: Before Transaction is finalized.

### VerifiableCredentialStoredInWalletEvent

- **Description**: Triggered after a verifiable credential has been successfully stored in a wallet.
- **Transaction Lifecycle**: After transaction is finalized.

### VerifiableCredentialStoringInWalletEvent

- **Description**: Triggered when a verifiable credential is about to be stored in a wallet.
- **Transaction Lifecycle**: Before Transaction is finalized.

### WalletCreatedEvent

- **Description**: Triggered after a new wallet has been successfully created.
- **Transaction Lifecycle**: After transaction is finalized.

### WalletCreatingEvent

- **Description**: Triggered when a new wallet is being created.
- **Transaction Lifecycle**: Before Transaction is finalized.concluded

### WalletDeletedEvent

- **Description**: Triggered after a wallet has been successfully deleted.
- **Transaction Lifecycle**: After transaction is finalized.

### WalletDeletingEvent

- **Description**: Triggered when a wallet is about to be deleted.
- **Transaction Lifecycle**: Before Transaction is finalized.

### WalletUpdatedEvent

- **Description**: Triggered after a wallet has been successfully updated.
- **Transaction Lifecycle**: After transaction is finalized.

### WalletUpdatingEvent

- **Description**: Triggered when a wallet is about to be updated.
- **Transaction Lifecycle**: Before Transaction is finalized.

## Event Listeners

### ApplicationStartedEventListener

- **Description**: Responds to the application start event.
- **Usage**:
    - Creates the initial authority wallet.
    - Registers embedded resources (offline JSON LD schemas)

### VerifiableCredentialStoringInWalletEventListener

- **Description**: Responds to the `VerifiableCredentialStoringInWalletEvent`. Updates the summary verifiable
  credential (if necessary) in the same transaction.
- **Usage**:
    - Updates the summary verifiable credential in the same transaction (if required).

### WalletCreatingEventListener

- **Description**: Responds to the `WalletCreatingEvent`.
- **Usage**:
    - Generates the initial keys for the new wallet (if not present).
    - Issues a BusinessPartnerNumber Verifiable credential for new wallets.

For more detailed information about each event and event listener, please refer to the source code.


