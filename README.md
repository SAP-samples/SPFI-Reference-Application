# SPFI V2 Reference Application

A Spring Boot reference application that demonstrates how to implement the **SPFI V2 REST API contract** — the standard interface between a Unified Provisioning operator and a service provider for tenant lifecycle management and SSO configuration.

---

## Table of Contents

1. [What is the SPFI V2 Contract?](#what-is-the-spfi-v2-contract)
2. [Architecture Overview](#architecture-overview)
3. [API Endpoints](#api-endpoints)
4. [Data Model](#data-model)
5. [Tenant Lifecycle Flows](#tenant-lifecycle-flows)
    - [Provisioning (Slow Path)](#provisioning-slow-path)
    - [Update Flow](#update-flow)
    - [Deletion Flow](#deletion-flow)
    - [Lifecycle State Change (Block / Unblock)](#lifecycle-state-change-block--unblock)
6. [Tenant State Machine](#tenant-state-machine)
7. [Security: mTLS](#security-mtls)
    - [Spring Security Configuration](#spring-security-configuration)
8. [Running the Application](#running-the-application)
9. [Testing with curl](#testing-with-curl)
10. [Recommendations](#recommendations)
11. [Troubleshooting](#troubleshooting)
12. [References](#references)

---

## What is the SPFI V2 Contract?

**SPFI** stands for *Service Provider Fulfillment Interface*. Version 2 defines the REST API that any service provider must expose so that the Unified Provisioning platform (the *operator*) can manage the full lifecycle of a tenant on that provider's system.

Key characteristics of the contract:

- **Transport security**: All calls are protected by mutual TLS (mTLS). The operator presents a client certificate; the provider's server validates it.
- **Asynchronous by design**: Long-running operations (provisioning, update, deletion, SSO activation) return `202 Accepted` with a `Location` header pointing to a polling endpoint. Short operations may return `201 Created` or `200 OK` immediately.
- **Polling via `Retry-After`**: Every `202` response carries a `Retry-After` header telling the operator how many seconds to wait before polling the status endpoint again.
- **Optimistic concurrency for updates**: The `GET /tenants/{id}` response includes an `ETag` header. `PUT /tenants/{id}` requires the caller to pass that ETag in an `If-Match` header. A mismatch returns `409 Conflict`.
- **Idempotent activation**: A second `POST /v2/tenants` for the same `sapId` returns `409 Conflict` with the existing tenant's status URL, so the operator can resume polling instead of creating a duplicate.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                  Unified Provisioning Platform                  │
│                                                                 │
│  ┌────────────────────────┐    ┌──────────────────────────────┐ │
│  │   SPFITrigger Resource │    │  FulfillmentData / HostApp   │ │
│  │   (Kubernetes CRD)     │    │  (Source of provisioning     │ │
│  │                        │    │   intent & customer data)    │ │
│  └───────────┬────────────┘    └──────────────┬───────────────┘ │
│              │                                │                 │
│              └────────────┬───────────────────┘                 │
│                           │                                     │
│              ┌────────────▼────────────┐                        │
│              │   Tenant Operator V2    │                        │
│              │   (Go controller)       │                        │
│              │                         │                        │
│              │  - Watches SPFITrigger  │                        │
│              │  - Builds tenant payload│                        │
│              │  - Calls SPFI V2 API    │                        │
│              │    over mTLS            │                        │
│              └────────────┬────────────┘                        │
└───────────────────────────┼─────────────────────────────────────┘
                            │  HTTPS + mTLS
                            │  (client cert from PKI service)
                            │
            ┌───────────────▼───────────────────────┐
            │         SPFI V2 Reference App          │
            │         (Service Provider side)        │
            │                                        │
            │  POST   /v2/tenants                    │
            │  GET    /v2/tenants                    │
            │  GET    /v2/tenants/{id}               │
            │  PUT    /v2/tenants/{id}               │
            │  DELETE /v2/tenants/{id}               │
            │  GET    /v2/tenants/{id}/status        │
            │  POST   /v2/tenants/{id}/status        │
            │                                        │
            │                                        │
            └───────────────────────────────────────┘
```

---

## API Endpoints

### Tenant Lifecycle Endpoints

| Method   | Path                            | Description                                   |
|----------|---------------------------------|-----------------------------------------------|
| `POST`   | `/v2/tenants`                   | Activate (provision) a new tenant             |
| `GET`    | `/v2/tenants`                   | List all tenants                              |
| `GET`    | `/v2/tenants/{tenantId}`        | Get a specific tenant (returns ETag header)   |
| `PUT`    | `/v2/tenants/{tenantId}`        | Update a tenant (requires If-Match header)    |
| `DELETE` | `/v2/tenants/{tenantId}`        | Delete (deprovision) a tenant                 |
| `GET`    | `/v2/tenants/{tenantId}/status` | Poll the current status of an operation       |
| `POST`   | `/v2/tenants/{tenantId}/status` | Trigger a lifecycle state change (block/unblock) |

---

## Data Model

### Tenant Object

```json
{
  "id": "<uuid>",
  "sapId": "<CRM tenant ID>",
  "globalTenantId": "<global tenant ID>",
  "businessType": "production",
  "operationType": "standard",
  "customer": {
    "id": "<customer ID>",
    "name": "<customer name>",
    "erpId": "<ERP ID>",
    "contact": {
      "id": "<contact ID>",
      "name": "<contact name>",
      "email": "<email>",
      "phone": "<phone>"
    }
  },
  "location": {
    "geography": {
      "region": "<region>",
      "continent": "<continent>",
      "country": "<country>",
      "city": "<city>"
    },
    "infrastructure": {
      "platform": "<platform>",
      "region": "<region>",
      "zone": "<zone>",
      "dataCenter": "<data center>",
      "landscape": "<landscape>",
      "environment": "<environment>",
      "host": "<host>",
      "internalIdentifier": "<internal ID>"
    }
  },
  "contract": {
    "start": "<ISO date>",
    "end": "<ISO date>"
  },
  "products": [
    { "productId": "<SKU ID>", "quota": 100, "unit": "users" }
  ],
  "initialUsers": [
    { "id": "<user ID>", "email": "<email>", "firstName": "<first>", "lastName": "<last>" }
  ],
  "endpoints": {
    "applicationUrl": "https://app.example.com",
    "configurationUrl": "https://app.example.com/config"
  },
  "additionalProperties": {
    "fromManager": { "key": "value" },
    "fromProvider": { "key": "value" }
  },
  "hostTenantSpecification": {
    "group": "<api group>",
    "version": "<version>",
    "type": "<type>",
    "spec": "<JSON string>"
  },
  "mixinSpecifications": [],
  "status": {
    "state": "Active",
    "lastModified": "<ISO timestamp>",
    "details": {}
  }
}
```

> **Required fields on POST**: `sapId`, `customer` (with `name`, `email`), `location`, at least one `initialUser`.

### Status Object

```json
{
  "state": "In Activation",
  "lastModified": "2024-01-15T10:30:00Z",
  "details": {
    "message": "Tenant provisioning is in progress"
  }
}
```

### State Change Request (POST `/status`)

```json
{ "state": "active" }
```

or

```json
{ "state": "blocked" }
```

---

## Tenant Lifecycle Flows

![img.png](img.png)

### Provisioning (Slow Path)

The typical path for provisioning a new tenant. The provider starts the work asynchronously and the operator polls for completion.

```
Operator                                    Service Provider
   │                                               │
   │  POST /v2/tenants                             │
   │  Body: { sapId, customer, location, ... }     │
   │──────────────────────────────────────────────►│
   │                                               │ Persists tenant
   │                                               │ State → "In Activation"
   │  202 Accepted                                 │
   │  Location: /v2/tenants/{id}/status            │
   │  Retry-After: 20                              │
   │◄──────────────────────────────────────────────│
   │                                               │
   │  (waits Retry-After seconds)                  │
   │                                               │
   │  GET /v2/tenants/{id}/status                  │
   │──────────────────────────────────────────────►│
   │                                               │
   │  200 OK  { state: "In Activation" }           │
   │  Retry-After: 20                              │
   │◄──────────────────────────────────────────────│
   │                                               │ (work completes)
   │  (waits Retry-After seconds)                  │
   │                                               │
   │  GET /v2/tenants/{id}/status                  │
   │──────────────────────────────────────────────►│
   │                                               │
   │  200 OK  { state: "Active" }                  │
   │◄──────────────────────────────────────────────│
   │                                               │
   │  GET /v2/tenants/{id}                         │
   │──────────────────────────────────────────────►│
   │                                               │
   │  200 OK  { tenant object }                    │
   │  ETag: "abc123"                               │
   │◄──────────────────────────────────────────────│
```

**Duplicate activation (idempotency):**

```
Operator                                    Service Provider
   │                                               │
   │  POST /v2/tenants  (same sapId again)         │
   │──────────────────────────────────────────────►│
   │                                               │
   │  409 Conflict                                 │
   │  Location: /v2/tenants/{existingId}/status    │
   │  Body: { errorCode: "409-01", ... }           │
   │◄──────────────────────────────────────────────│
   │                                               │
   │  (operator resumes polling existing location) │
```

### Update Flow

Tenant updates use optimistic concurrency. The operator must supply the ETag from the most recent GET.

```
Operator                                    Service Provider
   │                                               │
   │  GET /v2/tenants/{id}                         │
   │──────────────────────────────────────────────►│
   │  200 OK + ETag: "abc123"                      │
   │◄──────────────────────────────────────────────│
   │                                               │
   │  PUT /v2/tenants/{id}                         │
   │  If-Match: "abc123"                           │
   │  Body: { updated tenant fields }              │
   │──────────────────────────────────────────────►│
   │                                               │ Validates ETag
   │                                               │ State → "In Update"
   │  202 Accepted                                 │
   │  Location: /v2/tenants/{id}/status            │
   │◄──────────────────────────────────────────────│
   │                                               │
   │  (polls /status until state = "Active")       │
```

**ETag mismatch (concurrent modification):**

```
   │  PUT /v2/tenants/{id}                         │
   │  If-Match: "stale-etag"                       │
   │──────────────────────────────────────────────►│
   │                                               │
   │  409 Conflict                                 │
   │  Body: { errorCode: "409-03", "Entity Tag Mismatch" }
   │◄──────────────────────────────────────────────│
```

> **Note**: Update is only allowed when the tenant is in `Active` state.

### Deletion Flow

```
Operator                                    Service Provider
   │                                               │
   │  DELETE /v2/tenants/{id}                      │
   │──────────────────────────────────────────────►│
   │                                               │ Validates not in-progress
   │                                               │ State → "In Deletion"
   │  202 Accepted                                 │
   │  Location: /v2/tenants/{id}/status            │
   │◄──────────────────────────────────────────────│
   │                                               │
   │  (polls /status)                              │
   │                                               │ (deletion completes)
   │  GET /v2/tenants/{id}/status                  │
   │──────────────────────────────────────────────►│
   │                                               │
   │  404 Not Found  (tenant no longer exists)     │
   │◄──────────────────────────────────────────────│
```

> **Note**: Deletion is rejected if the tenant is currently in any in-progress state (`In Activation`, `In Update`, `In Deletion`, `In Blocking`).

### Lifecycle State Change (Block / Unblock)

```
Operator                                    Service Provider
   │                                               │
   │  POST /v2/tenants/{id}/status                 │
   │  Body: { "state": "blocked" }                 │
   │──────────────────────────────────────────────►│
   │                                               │ Validates transition
   │                                               │ State → "In Blocking"
   │  202 Accepted                                 │
   │  Retry-After: 20                              │
   │◄──────────────────────────────────────────────│
   │                                               │ (blocking completes)
   │  GET /v2/tenants/{id}/status                  │
   │──────────────────────────────────────────────►│
   │  200 OK  { state: "Blocked" }                 │
   │◄──────────────────────────────────────────────│
```

**Allowed state transitions via POST `/status`:**

| Current State          | Allowed Target States |
|------------------------|-----------------------|
| `Active`               | `blocked`             |
| `Blocked`              | `active`              |
| `In Recoverable Error` | `active`, `blocked`   |
| Any in-progress state  | *(not allowed)*       |


---

## Tenant State Machine

```
                        POST /v2/tenants
                              │
                              ▼
                       ┌─────────────┐
                       │In Activation│
                       └──────┬──────┘
                    ┌─────────┴──────────┐
                    │                    │
                    ▼                    ▼
               ┌────────┐        ┌─────────────┐
               │ Active │        │ Final Error │
               └───┬────┘        └─────────────┘
         ┌─────────┼────────────┐
         │         │            │
         ▼         ▼            ▼
  ┌──────────┐ ┌──────────┐ ┌──────────────────────┐
  │In Update │ │In Blocking│ │In Recoverable Error  │
  └────┬─────┘ └────┬──────┘ └──────────┬───────────┘
       │            │                   │
       ▼            ▼                   │
  ┌────────┐   ┌─────────┐             │
  │ Active │   │ Blocked │◄────────────┘
  └────────┘   └────┬────┘  (via POST /status)
                    │
                    ▼
             ┌────────────┐
             │ In Deletion│
             └─────┬──────┘
                   │
                   ▼
              (tenant gone)


  ┌─────────────────────────────────────────────┐
  │  In-progress states — deletion and update   │
  │  are rejected while in any of these:        │
  │  In Activation, In Update, In Deletion,     │
  │  In Blocking                                │
  └─────────────────────────────────────────────┘
```

---

## Security: mTLS

All API calls are protected with mutual TLS. The operator presents a client certificate issued by the SAP PKI service; the application server validates this certificate against a configured truststore.


```

### Spring Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/health/**").permitAll()
                .anyRequest().authenticated()
            )
            .x509(x509 -> x509
                .subjectPrincipalRegex("CN=(.*?)(?:,|$)")
                .userDetailsService(userDetailsService())
            );
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> new User(username, "",
            AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
    }
}
```

## Running the Application

**Local (development profile):**

```shell
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Cloud Foundry:**

The `manifest.yaml` configures the CF deployment:

```yaml
env:
  SPRING_PROFILES_ACTIVE: "cf"
  HEADER_RETRY_AFTER_SECONDS: "20"
```

The `HEADER_RETRY_AFTER_SECONDS` variable controls the value returned in `Retry-After` headers (default: `120` seconds if not set).

---

## Testing with curl

```shell
# List tenants
curl https://<host>/v2/tenants \
  --cert certs/client-certificate_chain.pem \
  --key certs/private-key.pem \
  --cacert certs/ca.crt

# Activate a tenant
curl -X POST https://<host>/v2/tenants \
  -H "Content-Type: application/json" \
  --cert certs/client-certificate_chain.pem \
  --key certs/private-key.pem \
  --cacert certs/ca.crt \
  -d '{
    "sapId": "T-12345",
    "customer": {
      "id": "C-001",
      "name": "Acme Corp",
      "contact": { "name": "Jane", "email": "jane@acme.com", "phone": "+1234" }
    },
    "businessType": "production",
    "location": {
      "geography": { "region": "eu", "country": "DE" },
      "infrastructure": { "dataCenter": "eu10" }
    },
    "initialUsers": [{ "id": "U-001", "email": "admin@acme.com" }],
    "sso": [{
      "identityProvider": {
        "authProtocol": "SAML2",
        "metadataURL": "https://idp.acme.com/metadata"
      }
    }]
  }'

# Poll status
curl https://<host>/v2/tenants/{tenantId}/status \
  --cert certs/client-certificate_chain.pem \
  --key certs/private-key.pem \
  --cacert certs/ca.crt
```

---

## Recommendations

### API Implementation

- **Always return `Retry-After` on `202` responses.** The operator uses this value to schedule its next poll. A missing or very short value can cause tight polling loops that overload the provider.
- **Implement ETag correctly.** Generate the ETag from a hash of the tenant's mutable fields or a version counter. Never return a static or empty ETag — it breaks concurrent update detection.
- **Implement idempotency for `POST /v2/tenants`.** Check for an existing tenant with the same `sapId` before creating a new one. Return `409` with the existing tenant's status URL rather than creating a duplicate.
- **Validate state transitions strictly.** Reject `DELETE` and `PUT` requests when the tenant is in an in-progress state (`In Activation`, `In Update`, `In Deletion`, `In Blocking`). Returning a clear error message helps the operator handle retries correctly.
- **Propagate meaningful status details.** The `status.details` field is surfaced to platform users when provisioning fails. Include actionable information rather than internal stack traces.
- **Use `Final Error` state only for non-recoverable failures.** The operator stops retrying on `Final Error`. Use `In Recoverable Error` or `In Self-Recoverable Error` for transient issues.
- **Generate tenant IDs as GUIDs (UUID v4).** The `id` assigned to a tenant on creation should be a randomly generated UUID (e.g. `550e8400-e29b-41d4-a716-446655440000`). This ensures global uniqueness across tenants and systems, avoids predictable or sequential IDs that could be guessed, and aligns with the format expected by the operator when storing and referencing the tenant.

### Operations

- **Configure `HEADER_RETRY_AFTER_SECONDS` based on your actual provisioning time.** Setting it too low increases polling load; too high delays the operator from detecting completion. A value of 20–60 seconds is typical for most operations.
- **Implement health endpoints** (`/health/liveness`, `/health/readiness`) and permit them without mTLS so that the platform's load balancer can route traffic correctly.
- **Design the status endpoint to be stateless and fast.** It will be called frequently during provisioning. Avoid database writes or heavy computation in `GET /status` handlers.
- **Persist tenant state durably.** The reference application uses a file-based store for demonstration. Production implementations should use a reliable database so that state survives restarts.
- **Test the full async flow end-to-end.** Unit tests verify logic, but integration tests that exercise the complete slow-path provisioning loop (including polling) catch timing and state-machine bugs that unit tests miss.

---

## Troubleshooting

### Certificate Validation Failures

- Verify the certificate chain is complete (intermediate CAs included).
- Check certificate expiration: `openssl x509 -in cert.pem -noout -dates`
- Ensure the server's hostname matches the CN or SAN in the server certificate.
- The CN should be validated against the response of `/certInfo` Endpoint from the Operator.

### Connection Refused

- Verify the server is running on the expected port (default: `8443` for HTTPS).
- Check that `server.ssl.enabled=true` is set in the active profile.
- Confirm firewall or security group rules allow inbound HTTPS traffic.

### 409 Conflict on Activation

- A tenant with the same `sapId` already exists. Follow the `Location` header in the response to resume polling the existing tenant's status URL.

### 409 Conflict on Update (ETag Mismatch)

- Re-fetch the tenant with `GET /v2/tenants/{id}` to obtain a fresh ETag, then retry the `PUT`.

### 403 / 401 on Every Request

- Verify the client certificate is signed by a CA that is in the server's truststore.
- Check that `server.ssl.client-auth=need` is configured.
- Confirm the certificate validation filter is not rejecting the CN of the operator's certificate.

---

## References
- [Swagger Documentation](https://github.com/SAP-samples/SPFI-Reference-Application/blob/main/SPFI%20V2%20Reference%20Application%20Swagger.pdf)
- [Securing Spring Boot Applications with SSL](https://spring.io/blog/2023/06/07/securing-spring-boot-applications-with-ssl)
