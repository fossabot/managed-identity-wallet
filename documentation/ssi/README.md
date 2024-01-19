<a name="readme-top"></a>

<!-- Caption -->

<br />
<div align="center">
  <a href="https://eclipse-tractusx.github.io/img/logo_tractus-x.svg">
    <img src="https://eclipse-tractusx.github.io/img/logo_tractus-x.svg" alt="Logo" width="80" height="80">
  </a>

<h3 align="center">Self-Sovereign Identity Introduction</h3>
<h4 align="center">Tractus-X Managed Identity Wallets</h4>

</div>

[« Up](../../README.md)

## Introduction to Self-Sovereign Identity (SSI)

In the rapidly evolving landscape of digital interactions and data exchange, the concept of Self-Sovereign Identity (
SSI) has emerged as a groundbreaking paradigm shift. SSI represents a departure from traditional identity management
systems, placing individuals at the center of control over their own identity information in the digital realm.

Unlike conventional identity models, where centralized entities like governments, corporations, or service providers
store and manage user information, SSI empowers individuals with the authority to own, control, and share their personal
data on their terms. This revolutionary approach is rooted in principles of privacy, security, and user autonomy, aiming
to address the inherent challenges and vulnerabilities associated with centralized identity systems.

### Verifiable Credentials

SSI Verifiable Credentials are a cornerstone of Self-Sovereign Identity (SSI), offering a transformative solution to
traditional identity verification. These credentials, often stored on decentralized ledgers like blockchain, enable
individuals to own and control their digital identity attributes. By leveraging cryptographic proofs, verifiable
credentials allow for secure and tamper-proof verification without the need for centralized authorities. This
breakthrough in identity management fosters privacy, interoperability, and user autonomy, revolutionizing how
individuals share and authenticate their personal information in the digital realm.

<details>
    <summary>Example</summary>
    <pre>
    {
        "@context": [
            "https://www.w3.org/2018/credentials/v1",
            "https://www.w3.org/2018/credentials/examples/v1"
        ],
        "id": "http://example.edu/credentials/58473",
        "type": ["VerifiableCredential", "AlumniCredential"],
        "credentialSubject": {
            "id": "did:example:ebfeb1f712ebc6f1c276e12ec21",
            "image": "https://example.edu/images/58473",
            "alumniOf": {
                "id": "did:example:c276e12ec21ebfeb1f712ebc6f1",
                "name": [{
                    "value": "Example University",
                    "lang": "en"
                    }, {
                    "value": "Exemple d'Université",
                    "lang": "fr"
                }]
            }
        },
        "proof": {
        }
    }
    </pre>
</details>

### Verifiable Presentations

SSI Verifiable Presentations are a pivotal aspect of Self-Sovereign Identity (SSI), offering a dynamic way for
individuals to share and prove their identity attributes. Built on the principles of decentralized identity, these
presentations allow users to selectively disclose verifiable credentials, securely attesting to their identity without
revealing unnecessary details.

<details>
    <summary>Example</summary>
    <pre>
    {
        "@context": [
            "https://www.w3.org/2018/credentials/v1",
            "https://www.w3.org/2018/credentials/examples/v1"
        ],
        "type": "VerifiablePresentation",
        "verifiableCredential": [
            {
                "@context": [
                    "https://www.w3.org/2018/credentials/v1",
                    "https://www.w3.org/2018/credentials/examples/v1"
                ],
                "id": "http://example.edu/credentials/1872",
                "type": [
                    "VerifiableCredential",
                    "AlumniCredential"
                ],
                "issuer": "https://example.edu/issuers/565049",
                "issuanceDate": "2010-01-01T19:23:24Z",
                "credentialSubject": {
                    "id": "did:example:ebfeb1f712ebc6f1c276e12ec21",
                    "alumniOf": {
                        "id": "did:example:c276e12ec21ebfeb1f712ebc6f1",
                        "name": [
                            {
                                "value": "Example University",
                                "lang": "en"
                            },
                            {
                                "value": "Exemple d'Université",
                                "lang": "fr"
                            }
                        ]
                    }
                },
                "proof": {
                    "type": "RsaSignature2018",
                    "created": "2017-06-18T21:19:10Z",
                    "proofPurpose": "assertionMethod",
                    "verificationMethod": "https://example.edu/issuers/565049#key-1",
                    "jws": "..."
                }
            }
        ],
        "proof": {
            "type": "RsaSignature2018",
            "created": "2018-09-14T21:19:10Z",
            "proofPurpose": "authentication",
            "verificationMethod": "did:example:ebfeb1f712ebc6f1c276e12ec21#keys-1",
            "challenge": "1f44d55f-f161-4938-a659-f8026467f126",
            "domain": "4jt78h47fh47",
            "jws": "..."
        }
    }
    </pre>
</details>

### Decentralized Identifiers (DID)

In the realm of Self-Sovereign Identity (SSI), Decentralized Identifiers (DIDs) play a pivotal role in reshaping digital
identity management. DIDs are unique, persistent identifiers created on decentralized networks, providing a secure
foundation for user-controlled identity interactions. Complementing DIDs are DID Documents, which contain essential
information such as public keys, authentication methods, and service endpoints associated with the DID. Importantly,
DIDs can be resolved to their corresponding DID Documents, allowing for dynamic retrieval of key identity information.
This dynamic duo, grounded in decentralization and cryptographic security, empowers individuals to independently own,
control, and selectively share their identities across diverse platforms.

<details>
    <summary>Example</summary>
    <table>
        <tr>
            <td>Decentralized Identifier (DID)</td>
            <td><strong>did:example:123456789abcdefghi</strong></td>
        </tr>
        <tr>
            <td>DID document</td>
            <td>
                <pre>
                {
                    "@context": [
                        "https://www.w3.org/ns/did/v1",
                        "https://w3id.org/security/suites/ed25519-2020/v1"
                    ],
                    "id": "did:example:123456789abcdefghi",
                    "verificationMethod": [
                        {
                            "id": "did:example:123456789abcdefghi#key-1",
                            "type": "Ed25519VerificationKey2020",
                            "controller": "did:example:123456789abcdefghi",
                            "publicKeyMultibase": "zH3C2AVvLMv6gmMNam3uVAjZpfkcJCwDwnZn6z3wXmqPV"
                        }
                    ],
                    "authentication": [
                        "#key-1"
                    ]
                }
                </pre>
            </td>
        </tr>
    </table>
</details>

### SSI Roles

Overview of Roles in SSI:

1. **Issuer:**
    - The Issuer is a key participant in the Self-Sovereign Identity (SSI) ecosystem responsible for creating and
      issuing verifiable credentials. These credentials represent specific identity attributes of an individual, such as
      a driver's license or a university degree. The Issuer cryptographically signs these credentials, establishing
      trust and authenticity. Examples of issuers could include government entities, educational institutions, or
      organizations verifying specific attributes.

2. **Holder:**
    - The Holder is the individual or entity that possesses and controls the verifiable credentials issued to them.
      Unlike traditional identity systems, where data is stored centrally, in SSI, the Holder maintains their
      credentials in a decentralized manner. This decentralized control empowers individuals to decide when, where, and
      with whom they share their identity attributes. The Holder actively manages their digital identity on their terms,
      enhancing privacy and control.

3. **Verifier:**
    - The Verifier is an entity or service seeking to authenticate the identity of an individual. Verifiers use
      cryptographic methods to confirm the validity of presented verifiable credentials without the need for direct
      access to a centralized database. This process allows for secure and privacy-preserving identity verification.
      Verifiers could be service providers, employers, or any entity requiring proof of specific attributes without the
      need to store personal data centrally.

In the SSI framework, these three roles collaborate to establish a decentralized, secure, and user-centric approach to
identity management, providing a flexible and trust-based system for digital interactions.

## Verifiable Credentials for Data Spaces

The MIW is not only about managing self sovereign identities, it is also about data spaces. A data space typically
refers to a virtual or conceptual environment where data is organized, stored, and managed. It is a framework that
allows for the structured representation, storage, and retrieval of information. The concept of a data space is often
associated with the idea of creating a unified, accessible, and coherent space for handling diverse types of data.

Tracxtus-X Managed Identity Wallets (MIW) are designed to support the use of Verifiable Credentials (VC) in the context
of data spaces. So this repository introduces a set of Verifiable Credentials that may be used to enforce access control
within a data space.

Access control through Verifiable Credentials could be implemented as follows:

- All members within a data space place trust in one or more Verifiable Credential Issuers. This trust relationship can
  vary, accommodating scenarios where a single issuer is responsible for all Verifiable Credentials or where different
  issuers handle specific types of Verifiable Credentials, depending on the use case.
- The Issuers verteilen distribute these Verifiable Credentials to the participants (Holders) within the data space as
  required.
- A participant in the data space securely stores these Verifiable Credentials in their digital wallet.
- When two participants within the data space intend to share data, they initiate the process by exchanging Verifiable
  Credentials. This exchange serves the purpose of verifying whether both participants belong to the same data space and
  possess the necessary access rights before proceeding with any data sharing activities.

### Membership Verifiable Credential

A Membership Verifiable Credential in the context of data spaces refers to a type of verifiable credential that attests
to an individual or entity's membership status within a specific data space or community. This credential provides
cryptographic proof of the entity's association with the data space.

<details>
    <summary>Example</summary>
    <pre>
    {
        "issuanceDate": "2024-01-19T08:00:17Z",
        "credentialSubject": [
            {
                "holderIdentifier": "BPN12345",
                "startTime": "2024-01-19T08:00:17.748160281Z",
                "memberOf": "Tractus-X",
                "id": "did:web:managed-identity-wallets.foo:BPN12345",
                "type": "MembershipCredential",
                "status": "Active"
            }
        ],
        "id": "did:web:managed-identity-wallets.foo:BPNL0000000ISSUER#1b6813e3-14f3-462c-afce-9a5c3d75e83f",
        "proof": {
            "proofPurpose": "assertionMethod",
            "verificationMethod": "did:web:managed-identity-wallets.foo:BPNL0000000ISSUER#049f920c-e702-4e36-9b01-540423788a90",
            "type": "JsonWebSignature2020",
            "created": "2024-01-19T08:00:17Z",
            "jws": "..."
        },
        "type": [
            "VerifiableCredential",
            "MembershipCredential"
        ],
        "@context": [
            "https://www.w3.org/2018/credentials/v1",
            "https://localhost/your-context.json",
            "https://w3id.org/security/suites/jws-2020/v1"
        ],
        "issuer": "did:web:managed-identity-wallets.foo:BPNL0000000ISSUER",
        "expirationDate": "2024-06-30T00:00:00Z"
    }
    </pre>
</details>

### Business Partner Number Verifiable Credential

A Business Partner Number (BPN) Verifiable Credential serves the purpose of linking a participant to a specific Business
Partner Number within a given data space, forming an integral part of the Verifiable Credential Subject. Each Business
Partner Number is distinctly unique within the confines of the data space.

<details>
    <summary>Example</summary>
    <pre>
    {
        "credentialSubject": [
            {
                "contractTemplate": "https://public.catena-x.org/contracts/",
                "holderIdentifier": "BPN12345",
                "id": "did:web:managed-identity-wallets.foo:BPN12345",
                "items": [
                    "BpnCredential"
                ],
                "type": "SummaryCredential"
            }
        ],
        "issuanceDate": "2023-07-18T09:33:11Z",
        "id": "did:web:managed-identity-wallets.foo:BPNL0000000ISSUER#340fc333-18b3-436b-abdb-461e8d0d4084",
        "proof": {
            "created": "2023-07-18T09:33:11Z",
            "jws": "...",
            "proofPurpose": "proofPurpose",
            "type": "JsonWebSignature2020",
            "verificationMethod": "did:web:managed-identity-wallets.foo:BPNL0000000ISSUER#"
        },
        "type": [
            "VerifiableCredential",
            "SummaryCredential"
        ],
        "@context": [
            "https://www.w3.org/2018/credentials/v1",
            "https://catenax-ng.github.io/product-core-schemas/SummaryVC.json",
            "https://w3id.org/security/suites/jws-2020/v1"
        ],
        "issuer": "did:web:managed-identity-wallets.foo:BPNL0000000ISSUER",
        "expirationDate": "2023-10-01T00:00:00Z"
    }
    </pre>
</details>

### Dismantler Verifiable Credential

A Verifiable Credential can extend its utility by associating the participant with a particular role within the data
space. In this instance, the Verifiable Credential Subject provides evidence that the participant holds the role of a
dismantler. Furthermore, the Verifiable Credential Subject elaborates on the specific capabilities and qualifications of
the participant in their capacity as a dismantler.

<details>
    <summary>Example</summary>
    <pre>
    {
        "credentialSubject": [
            {
                "bpn": "BPN12345",
                "id": "did:web:managed-identity-wallets.foo:BPN12345",
                "type": "DismantlerCredential",
                "activityType": "vehicleDismantle",
                "allowedVehicleBrands": "Alfa Romeo, Mercedes-Benz"
            }
        ],
        "issuanceDate": "2023-07-13T12:35:00Z",
        "id": "did:web:managed-identity-wallets.foo:BPNL0000000ISSUER#845ee4fd-4743-48d4-9b84-c09f29c49b80",
        "proof": {
            "created": "2023-07-13T12:35:00Z",
            "jws": "...",
            "proofPurpose": "proofPurpose",
            "type": "JsonWebSignature2020",
            "verificationMethod": "did:web:managed-identity-wallets.foo:BPNL0000000ISSUER#"
        },
        "type": [
            "VerifiableCredential",
            "DismantlerCredent"proof":ial"
        ],
        "@context": [
            "https://www.w3.org/2018/credentials/v1",
            "https://localhost/your-context.json",
            "https://w3id.org/security/suites/jws-2020/v1"
        ],
        "issuer": "did:web:managed-identity-wallets.foo:BPNL0000000ISSUER",
        "expirationDate": "2023-09-30T22:00:00Z"
    }
    </pre>
</details>

> Proposed Verifiable Credentials schemas for data spaces a further discussed in
> the [schemas documentation](../schemas/README.md).
