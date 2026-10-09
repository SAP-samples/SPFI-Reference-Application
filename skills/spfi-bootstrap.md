# SPFI Bootstrap

You are an expert software engineer. Your job is to generate a **complete, runnable reference application** that implements the SPFI REST API contract for tenant lifecycle management.

The generated application must be **functionally equivalent** to the Java reference implementation described below, written in the language/framework the partner requests.

---

## How to Start

**IMPORTANT: Do NOT assume or default to any language. Always check for an explicit argument first.**

If the user provided a language argument (e.g. `/spfi-bootstrap Python`), use that language.
If no language argument was provided, you MUST stop and ask before generating anything:

> **"Which programming language would you like to use?"**
>
> Options:
> 1. Python
> 2. Node.js / JavaScript
> 3. TypeScript
> 4. Go
> 5. Java
> 6. C#
> 7. Ruby
> 8. PHP
> 9. Other (please specify)

Do not generate any code until the user has explicitly confirmed the language.

Then confirm the framework you will use before generating:
- **Python** → FastAPI
- **Node.js / JavaScript** → Express
- **TypeScript** → Express + TypeScript
- **Go** → Gin
- **Java** → Spring Boot (do NOT use Lombok — see Java notes below)
- **C#** → ASP.NET Core
- **Ruby** → Sinatra
- **PHP** → Slim
- **Other** → pick the most idiomatic REST framework for that language

---

## What to Generate

Generate ALL of the following files. Do not skip any. Generate each file completely — no placeholders, no `TODO: implement`, no truncation.

### Required files

1. **Main application entry point** — starts the HTTP server
2. **Router / controller** — all 7 tenant endpoints wired up
3. **Service layer** — business logic, state machine, async worker
4. **Storage layer** — file-based JSON storage (with custom impl comments)
5. **Data models** — all tenant-related types/classes/structs
6. **Security config** — two profiles: `local` (no mTLS) and `default` (mTLS required)
7. **`../README.md`** — setup, run instructions, Swagger link, env vars
8. **`GETTING_STARTED.md`** — step-by-step walkthrough with curl commands
9. **`.env.example`** — all environment variables with defaults and comments
10. **`postman_collection.json`** — importable Postman collection with all 7 endpoints pre-filled
11. **Dependency manifest** — `requirements.txt`, `package.json`, `go.mod`, `../pom.xml`, etc.
12. **Unit tests** — service layer + router layer, idiomatic test framework for the language, ≥80% coverage

---

## SPFI API Contract

Implement **exactly** these 7 endpoints under base path `/v2`:

### Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/v2/tenants` | Activate (provision) a new tenant |
| `GET` | `/v2/tenants` | List all tenants |
| `GET` | `/v2/tenants/{tenantId}` | Get tenant by ID |
| `PUT` | `/v2/tenants/{tenantId}` | Update a tenant |
| `DELETE` | `/v2/tenants/{tenantId}` | Delete (deprovision) a tenant |
| `GET` | `/v2/tenants/{tenantId}/status` | Poll tenant operation status |
| `POST` | `/v2/tenants/{tenantId}/status` | Trigger lifecycle state change (block/unblock) |
| `GET` | `/health` | Health check (no auth required) |

---

### POST /v2/tenants — Activate Tenant

**Request body (JSON):**
```json
{
  "sapId": "T-12345",
  "globalTenantId": "GT-001",
  "businessType": "production",
  "operationType": "standard",
  "customer": {
    "id": "C-001",
    "name": "Acme Corp",
    "erpId": "ERP-001",
    "contact": {
      "id": "CONT-001",
      "name": "Jane Doe",
      "email": "jane@acme.com",
      "phone": "+49123456789"
    }
  },
  "location": {
    "geography": {
      "region": "eu",
      "continent": "Europe",
      "country": "DE",
      "city": "Walldorf"
    },
    "infrastructure": {
      "platform": "cf",
      "region": "eu10",
      "zone": "az1",
      "dataCenter": "eu10",
      "landscape": "prod",
      "environment": "prod",
      "host": "cf.eu10.hana.ondemand.com",
      "internalIdentifier": "cf-eu10"
    }
  },
  "contract": {
    "validFrom": "2024-01-01",
    "validTo": "2025-01-01"
  },
  "products": [
    { "productId": "PRODUCT-001", "quota": 100, "unit": "users" }
  ],
  "initialUsers": [
    { "id": "U-001", "email": "admin@acme.com", "name": "Admin", "firstName": "Ad", "lastName": "Min" }
  ],
  "application": {
    "endpoints": {
      "applicationUrl": "https://app.example.com",
      "configurationUrl": "https://app.example.com/config"
    }
  },
  "additionalProperties": {
    "fromManager": { "key": "value" },
    "fromProvider": {}
  },
  "hostTenantSpecification": {
    "group": "api.example.com",
    "version": "v1",
    "type": "TenantSpec",
    "spec": "{}"
  },
  "mixinSpecifications": []
}
```

**Required fields:** `sapId`, `customer.name`, `location`

**Slow path response (202):**
```
HTTP/1.1 202 Accepted
Location: /v2/tenants/{newUUID}/status
Retry-After: 120
Content-Type: application/json

{ "id": "{newUUID}" }
```

**Fast path response (201):** Return immediately if provisioning completes synchronously:
```
HTTP/1.1 201 Created
Location: /v2/tenants/{id}
Content-Type: application/json

{ <full tenant object> }
```

**Duplicate sapId (409):**
```
HTTP/1.1 409 Conflict
Location: /v2/tenants/{existingId}/status
Content-Type: application/json

{
  "code": "409-01",
  "message": "The given resource already exists.",
  "target": "Tenant",
  "details": [{ "code": "900", "message": "Tenant with sapId T-12345 already exists." }]
}
```

---

### GET /v2/tenants — List Tenants

**Response (200):**
```json
[
  { <tenant object>, <tenant object>, ... }
]
```
Return `Retry-After` header with value from `HEADER_RETRY_AFTER_SECONDS`.

---

### GET /v2/tenants/{tenantId} — Get Tenant

**Response (200):**
```
HTTP/1.1 200 OK
ETag: "{md5-of-tenant-json}"
Cache-Control: no-store
Content-Type: application/json

{ <full tenant object> }
```

**Not found (404):**
```json
{
  "code": "404-01",
  "message": "The requested resource does not exist.",
  "target": "Tenant",
  "details": []
}
```

---

### PUT /v2/tenants/{tenantId} — Update Tenant

**Required header:** `If-Match: "{etag}"`

**Validation:**
- Tenant must be in `Active` state — else 409-02
- ETag must match current — else 409-03
- Tenant must not be in any in-progress state — else 409-02

**Slow path response (202):**
```
HTTP/1.1 202 Accepted
Location: /v2/tenants/{id}/status
Retry-After: 120
Content-Type: application/json

{ "id": "{tenantId}" }
```

**ETag mismatch (409):**
```json
{
  "code": "409-03",
  "message": "Entity Tag Mismatch.",
  "target": "Tenant",
  "details": []
}
```

**Wrong state (409):**
```json
{
  "code": "409-02",
  "message": "The resource is currently being processed.",
  "target": "Tenant",
  "details": []
}
```

---

### DELETE /v2/tenants/{tenantId} — Delete Tenant

**Validation:** Reject if tenant is in any in-progress state (`In Activation`, `In Update`, `In Deletion`, `In Blocking`) with 409-02.
Also reject if in `In Self-Recoverable Error` with 409-02.

**Slow path response (202):**
```
HTTP/1.1 202 Accepted
Location: /v2/tenants/{id}/status
Retry-After: 120
```

**No response body.** Do NOT return `null` or any JSON body. Use `Response` (empty body), not `JSONResponse(content=None)` — the latter serializes to the literal string `null`, which causes Swagger UI and some HTTP clients to hang.

Python/FastAPI example:
```python
from fastapi.responses import Response
return Response(status_code=202, headers=headers)
```

**In-progress conflict (409):**
```json
{
  "code": "409-02",
  "message": "The resource is currently being processed.",
  "target": "Tenant",
  "details": []
}
```

---

### GET /v2/tenants/{tenantId}/status — Poll Status

**Response (200) — while in progress:**
```
HTTP/1.1 200 OK
Retry-After: 120
Content-Type: application/json

{
  "state": "In Activation",
  "lastModified": "2024-01-15T10:30:00Z",
  "details": { "code": "in-activation", "message": "Tenant provisioning is in progress." }
}
```

**Response (200) — terminal state (Active, Blocked, Final Error):**
```
HTTP/1.1 200 OK
Content-Type: application/json

{
  "state": "Active",
  "lastModified": "2024-01-15T10:35:00Z",
  "details": {
    "code": "active",
    "message": "The resource is fully active and functional."
  }
}
```
No `Retry-After` header when in terminal state.

**`details.code` rule:** Always set to the **kebab-case** form of the current state. Complete mapping:

| `state` (Title Case — wire format) | `details.code` (kebab-case) |
|-------------------------------------|-----------------------------|
| `In Activation` | `in-activation` |
| `Active` | `active` |
| `In Update` | `in-update` |
| `In Blocking` | `in-blocking` |
| `Blocked` | `blocked` |
| `In Deletion` | `in-deletion` |
| `In Self-Recoverable Error` | `in-self-recoverable-error` |
| `In Recoverable Error` | `in-recoverable-error` |
| `Final Error` | `final-error` |

`details.message` is a human-readable description of that state.

**After deletion completes:** Return `404 Not Found`.

---

### POST /v2/tenants/{tenantId}/status — Change State

**Request body:**
```json
{ "state": "blocked" }
```
or
```json
{ "state": "active" }
```

**Allowed transitions:**

| Current State | Allowed Target |
|---------------|---------------|
| `Active` | `blocked` |
| `Blocked` | `active` |
| `In Recoverable Error` | `active`, `blocked` |
| Any in-progress state | *(rejected)* |

**Response (202) — async:**
```
HTTP/1.1 202 Accepted
Location: /v2/tenants/{id}/status
Retry-After: 120
Content-Type: application/json

{ "id": "{tenantId}" }
```

**Response (200) — sync:**
```
HTTP/1.1 200 OK
Content-Type: application/json

{ "id": "{tenantId}" }
```

**Invalid transition (409):**
```json
{
  "code": "409-02",
  "message": "The resource is currently being processed.",
  "target": "Tenant",
  "details": []
}
```

---

### GET /health — Health Check

```
HTTP/1.1 200 OK
Content-Type: application/json

{ "status": "UP" }
```

No authentication required on this endpoint.

---

## Tenant Object Schema

```json
{
  "id": "<uuid-v4>",
  "sapId": "<string>",
  "globalTenantId": "<string>",
  "businessType": "<string>",
  "operationType": "<string>",
  "customer": {
    "id": "<string>",
    "name": "<string>",
    "erpId": "<string>",
    "contact": {
      "id": "<string>",
      "name": "<string>",
      "email": "<string>",
      "phone": "<string>"
    }
  },
  "location": {
    "geography": {
      "region": "<string>",
      "continent": "<string>",
      "country": "<string>",
      "city": "<string>"
    },
    "infrastructure": {
      "platform": "<string>",
      "region": "<string>",
      "zone": "<string>",
      "dataCenter": "<string>",
      "landscape": "<string>",
      "environment": "<string>",
      "host": "<string>",
      "internalIdentifier": "<string>"
    }
  },
  "contract": {
    "validFrom": "<ISO date>",
    "validTo": "<ISO date>"
  },
  "products": [
    { "productId": "<string>", "quota": 0, "unit": "<string>" }
  ],
  "initialUsers": [
    { "id": "<string>", "email": "<string>", "name": "<string>", "firstName": "<string>", "lastName": "<string>" }
  ],
  "application": {
    "endpoints": {
      "applicationUrl": "<string>",
      "configurationUrl": "<string>"
    },
    "additionalProperties": {}
  },
  "additionalProperties": {
    "fromManager": {},
    "fromProvider": {}
  },
  "hostTenantSpecification": {
    "group": "<string>",
    "version": "<string>",
    "type": "<string>",
    "spec": "<string>"
  },
  "mixinSpecifications": [],
  "status": {
    "state": "<state>",
    "lastModified": "<ISO timestamp>",
    "details": {
      "code": "<string>",
      "message": "<string>"
    }
  }
}
```

---

## Tenant State Machine

```
POST /v2/tenants
       │
       ▼
 [In Activation]
       │
  ┌────┴────┐
  ▼         ▼
[Active]  [Final Error]   ← stop retrying
  │
  ├──► [In Update]    ──► [Active]
  ├──► [In Blocking]  ──► [Blocked]
  └──► [In Recoverable Error]  ──► [Active] or [Blocked] via POST /status

[Blocked]
  └──► [In Deletion] ──► tenant removed (404)

[Active]
  └──► [In Deletion] ──► tenant removed (404)
```

**In-progress states** (reject DELETE and PUT while in these):
- `In Activation`
- `In Update`
- `In Deletion`
- `In Blocking`

---

## Async Worker Pattern

The application MUST simulate async behavior using a background worker thread:

1. On `POST /v2/tenants`: save tenant with state `In Activation`, return 202, background worker transitions to `Active` after a configurable delay (`WORKER_DELAY_SECONDS`, default: `5`)
2. On `PUT /v2/tenants/{id}`: set state to `In Update`, return 202, worker transitions back to `Active`
3. On `DELETE /v2/tenants/{id}`: set state to `In Deletion`, return 202, worker deletes tenant after delay
4. On `POST /v2/tenants/{id}/status` (block/unblock): set state to `In Blocking`, return 202, worker transitions to `Blocked` (for block request) or `Active` (for unblock request)

Worker runs in a background thread/goroutine/async task — NOT blocking the request thread.

**Java / Spring Boot — do NOT use `@Async` for worker methods called from within the same bean.**

Spring's `@Async` works via AOP proxy. When a method calls another `@Async` method on `this`, the proxy is bypassed and the method runs synchronously on the request thread — defeating the async intent entirely.

Use `CompletableFuture.runAsync(...)` with an explicit executor instead:

```java
private final Executor workerExecutor = Executors.newCachedThreadPool();

public Tenant activateTenant(TenantRequest request) {
    // ... save tenant in IN_ACTIVATION state ...
    CompletableFuture.runAsync(() -> {
        sleep(workerDelaySeconds);
        // transition to ACTIVE
    }, workerExecutor);
    return tenant;
}
```

Note: `Executors.newVirtualThreadPerTaskExecutor()` requires Java 21+. Use `newCachedThreadPool()` for Java 17 compatibility.

**Java / Spring Boot — do NOT use Lombok.**

Maven annotation processing for Lombok is unreliable across build environments (missing `-proc:full`, IDE vs CLI differences, Spring Boot parent POM quirks). Use plain Java getters/setters and hand-written builders instead. The generated code is more portable and requires no annotation processor configuration.

---

## File Storage Pattern

Use **JSON file storage** — read/write a single JSON file as the tenant database.

Default file path: `./data/tenants.json` (configurable via `STORAGE_FILE_PATH` env var).

```
data/
  tenants.json   ← array of tenant objects
```

**Place this exact comment at EVERY point where storage is read or written:**

```
# Custom implementation can go here
```

(Use the appropriate comment syntax for the language: `//`, `#`, `/* */`, etc.)

Example (Python):
```python
def get_all_tenants(self):
    # Custom implementation can go here
    with open(self.file_path, 'r') as f:
        return json.load(f)
```

Place this comment in **all** of these locations:
- Read all tenants
- Read single tenant by ID
- Write/save tenant
- Delete tenant
- Any lock/transaction around storage

**Critical: avoid reentrant lock deadlock**

Use a private `_read_all()` internal helper (no lock) called only from within already-locked methods. Never call the public `get_all_tenants()` (which acquires the lock) from inside `save_tenant` or `delete_tenant` — that causes a deadlock because `threading.Lock` is not reentrant.

Correct pattern (Python example — apply equivalent pattern in all languages):
```python
import threading, json

_lock = threading.Lock()

def _read_all() -> list:
    # Custom implementation can go here
    with open(_file_path, "r") as f:
        return json.load(f)

def get_all_tenants() -> list:
    # Custom implementation can go here
    with _lock:
        return _read_all()          # call internal helper, not get_all_tenants()

def save_tenant(tenant: dict) -> None:
    # Custom implementation can go here
    with _lock:
        tenants = _read_all()       # NOT get_all_tenants() — lock already held
        tenants = [t for t in tenants if t["id"] != tenant["id"]]
        tenants.append(tenant)
        with open(_file_path, "w") as f:
            json.dump(tenants, f)

def delete_tenant(tenant_id: str) -> None:
    # Custom implementation can go here
    with _lock:
        tenants = _read_all()       # NOT get_all_tenants() — lock already held
        tenants = [t for t in tenants if t["id"] != tenant_id]
        with open(_file_path, "w") as f:
            json.dump(tenants, f)
```

---

## ETag Generation

Generate ETag as MD5 hash of the tenant's JSON representation (excluding the `status` field).

```
ETag = md5(json_serialize(tenant_without_status))
```

Store and compare ETags on every PUT request.

---

## Security Profiles

### Local profile (development — no mTLS)

Activated by env var `PROFILE=local` or equivalent flag.

- Plain HTTP on port `8080` (configurable via `PORT`)
- No client certificate required
- All endpoints accessible without auth
- Log a warning at startup: `"WARNING: Running in local mode — mTLS is disabled. Do not use in production."`

### Default profile (production — mTLS)

- HTTPS on port `8443` (configurable via `PORT`)
- Require client certificate (`client-auth: required`)
- Validate client cert against CA in truststore (`MTLS_CA_CERT_PATH` env var)
- Health endpoint `/health` exempt from mTLS
- On startup, fetch the certInfo endpoint (see below) and cache the result
- On every request (except `/health`), validate the incoming client cert headers against the cached certInfo

Implement mTLS using the standard TLS library for the chosen language. Do not require any external mTLS proxy — the app enforces it at the HTTP server level.

### Certificate Identity Validation (default profile only)

In production, TLS is terminated upstream (Istio/ingress). The operator forwards client certificate fields as request headers:

| Header | Description |
|--------|-------------|
| `X-Ssl-Client-Subject-Cn` | Subject CN of the client cert |
| `X-Ssl-Client-Issuer-Dn` | Issuer DN of the client cert |
| `X-Ssl-Client-Notbefore` | Cert validity start (ISO 8601) |
| `X-Ssl-Client-Notafter` | Cert validity end (ISO 8601) |

SAP provides a `/certInfo` endpoint per environment that returns the expected operator certificate info:

```json
{
  "subject": "CN=...,OU=...,O=SAP SE,L=...,C=..",
  "issuer": "CN=...,OU=...,O=SAP SE,L=...,C=..",
  "notBefore": "2026-06-12T13:33:48Z",
  "notAfter": "2027-06-12T14:33:48Z",
  "isExpired": false,
  "daysToExpiry": 247
}
```

The URL is provided by SAP per environment and set via `CERT_INFO_URL` env var.

**Startup behaviour:**
- Fetch `CERT_INFO_URL` at startup and cache the response
- Refresh the cache once every 24 hours (background thread/task)
- If the fetch fails at startup: log a warning and continue — do NOT block app startup
- If the cached value is unavailable (first fetch failed and no cached value): reject all requests with 403 and log a warning

**Per-request validation (middleware, runs before all endpoints except `/health`):**
1. If `PROFILE=local` — skip validation entirely
2. Read `X-Ssl-Client-Subject-Cn` and `X-Ssl-Client-Issuer-Dn` from request headers
3. Compare against cached certInfo `subject` and `issuer` — must match exactly (string equality)
4. Check cached certInfo `isExpired` — if `true`, reject with 403
5. If any check fails: return 403 Forbidden with log message; do not call the endpoint handler

Python/FastAPI example structure:
```python
import threading, time, requests as http

_cert_info_cache: dict | None = None
_cert_info_lock = threading.Lock()

def _fetch_cert_info():
    url = os.getenv("CERT_INFO_URL", "")
    if not url:
        return
    try:
        resp = http.get(url, timeout=10)
        resp.raise_for_status()
        with _cert_info_lock:
            global _cert_info_cache
            _cert_info_cache = resp.json()
        print(f"[certInfo] Cached cert info from {url}")
    except Exception as e:
        print(f"[certInfo] WARNING: Failed to fetch cert info: {e}")

def _start_cert_info_refresh():
    _fetch_cert_info()  # initial fetch
    def _loop():
        while True:
            time.sleep(86400)  # 24 hours
            _fetch_cert_info()
    t = threading.Thread(target=_loop, daemon=True)
    t.start()

# Call _start_cert_info_refresh() at app startup (lifespan or __main__)

# In middleware:
def _validate_cert(request) -> bool:
    if os.getenv("PROFILE", "default") == "local":
        return True
    with _cert_info_lock:
        info = _cert_info_cache
    if info is None:
        print("[certInfo] WARNING: No cached cert info — rejecting request")
        return False
    if info.get("isExpired"):
        print("[certInfo] WARNING: Operator cert is expired")
        return False
    subject_header = request.headers.get("X-Ssl-Client-Subject-Cn", "")
    issuer_header = request.headers.get("X-Ssl-Client-Issuer-Dn", "")
    expected_subject = info.get("subject", "")
    expected_issuer = info.get("issuer", "")
    # Extract CN from full subject DN for header comparison if needed
    if subject_header not in expected_subject:
        print(f"[certInfo] Subject mismatch: got '{subject_header}', expected in '{expected_subject}'")
        return False
    if issuer_header not in expected_issuer:
        print(f"[certInfo] Issuer mismatch: got '{issuer_header}', expected in '{expected_issuer}'")
        return False
    return True
```

Note: `X-Ssl-Client-Subject-Cn` contains only the CN value (e.g. `my-operator`), while certInfo `subject` is a full DN (e.g. `CN=my-operator,OU=...,O=SAP SE,...`). Use a `CN in full_dn` substring check, not strict equality on the full string.

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PROFILE` | `default` | `local` disables mTLS; `default` enforces mTLS |
| `PORT` | `8080` (local) / `8443` (default) | HTTP/HTTPS port |
| `HEADER_RETRY_AFTER_SECONDS` | `120` | Value for `Retry-After` response header |
| `WORKER_DELAY_SECONDS` | `5` | Seconds before background worker transitions state |
| `STORAGE_FILE_PATH` | `./data/tenants.json` | Path to JSON storage file |
| `MTLS_CA_CERT_PATH` | `` | Path to CA cert PEM file (required in default profile) |
| `MTLS_SERVER_CERT_PATH` | `` | Path to server cert PEM (required in default profile) |
| `MTLS_SERVER_KEY_PATH` | `` | Path to server key PEM (required in default profile) |
| `CERT_INFO_URL` | `` | SAP-provided certInfo endpoint URL (required in default profile) |
| `LOG_LEVEL` | `info` | Logging level: `debug`, `info`, `warn`, `error` |

---

## Request/Response Logging

Add a middleware/interceptor that logs every request and response:

```
[2024-01-15 10:30:00] POST /v2/tenants → 202 Accepted (12ms)
[2024-01-15 10:30:05] GET /v2/tenants/abc-123/status → 200 OK (3ms)
```

Include: timestamp, method, path, response status, duration.

**Critical (FastAPI/Python): use `BaseHTTPMiddleware`, not `@app.middleware("http")`**

`@app.middleware("http")` deadlocks on POST/PUT requests with a body in uvicorn — the ASGI receive channel blocks waiting for the body to be consumed but `call_next` never drains it. This causes all POST/PUT requests to hang indefinitely.

Correct pattern for FastAPI/Python:
```python
from starlette.middleware.base import BaseHTTPMiddleware
import time

class LoggingMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request, call_next):
        start = time.monotonic()
        response = await call_next(request)
        elapsed_ms = int((time.monotonic() - start) * 1000)
        print(f"[{datetime.utcnow().isoformat()}] {request.method} {request.url.path} → {response.status_code} ({elapsed_ms}ms)")
        return response

app.add_middleware(LoggingMiddleware)
```

For other frameworks use their idiomatic middleware — this deadlock is specific to FastAPI/uvicorn's `@app.middleware("http")` decorator.

---

## Swagger / OpenAPI Documentation

Generate an OpenAPI 3.0 spec and serve interactive Swagger UI:

- Swagger UI available at: `GET /swagger` or `/docs` (use whatever is idiomatic for the framework)
- OpenAPI JSON/YAML available at: `GET /openapi.json` or `/openapi.yaml`
- The spec must document all 7 tenant endpoints + health check
- Include request/response schemas, status codes, and header descriptions

Include the Swagger URL in README.md.

---

## Postman Collection

Generate `postman_collection.json` with:

- Collection name: `SPFI Reference Application`
- A folder `Tenants` with requests for all 7 endpoints
- Pre-filled example request bodies
- Environment variable `{{baseUrl}}` (default: `http://localhost:8080`)
- For `PUT /v2/tenants/{{tenantId}}`: include `If-Match` header with `{{etag}}`
- For `POST /v2/tenants/{id}/status`: two example requests (block and unblock)

### Auto-extraction scripts (required)

Every request that produces a value needed by a subsequent request MUST include a Postman `test` script to extract and set collection variables automatically. Partners must never need to copy-paste IDs or headers manually.

**POST /v2/tenants** — extract `tenantId`:
```javascript
var json = pm.response.json();
if (json.id) {
    pm.collectionVariables.set("tenantId", json.id);
    console.log("tenantId set to: " + json.id);
}
```

**GET /v2/tenants/{{tenantId}}** — extract `ETag` header:
```javascript
var etag = pm.response.headers.get("ETag");
if (etag) {
    pm.collectionVariables.set("etag", etag);
    console.log("etag set to: " + etag);
}
```

**GET /v2/tenants/{{tenantId}}/status** — extract current state (useful for polling):
```javascript
var json = pm.response.json();
if (json.state) {
    pm.collectionVariables.set("tenantState", json.state);
    console.log("tenantState set to: " + json.state);
}
```

Define these as initial collection variables (empty string defaults) so Postman does not show warnings:

| Variable | Initial value | Description |
|----------|--------------|-------------|
| `baseUrl` | `http://localhost:8080` | App base URL |
| `tenantId` | `` | Auto-set by POST /v2/tenants |
| `etag` | `` | Auto-set by GET /v2/tenants/{{tenantId}} |
| `tenantState` | `` | Auto-set by GET status |

---

## README.md Requirements

Include:
1. What this app is (SPFI reference implementation)
2. Prerequisites
3. Setup steps
4. How to run locally (local profile, no mTLS)
5. How to run with mTLS (default profile)
6. Environment variables table
7. **Swagger UI link**: `http://localhost:8080/swagger` (or framework equivalent)
8. How to import the Postman collection
9. mTLS certificate generation instructions (self-signed, for testing)
10. Brief description of the state machine

---

## GETTING_STARTED.md Requirements

Step-by-step walkthrough using curl against local (no-mTLS) mode:

```
Step 1: Start the app
Step 2: Check health
Step 3: Create a tenant → capture tenantId from response
Step 4: Poll status until Active
Step 5: Get tenant (capture ETag)
Step 6: Update tenant (use ETag in If-Match)
Step 7: Poll status until Active
Step 8: Block the tenant
Step 9: Poll status until Blocked
Step 10: Unblock the tenant
Step 11: Delete the tenant
Step 12: Poll status until 404
```

Each step shows the exact curl command with expected response.

---

## No Sensitive Information

Do NOT include:
- Any SAP-internal URLs, hostnames, or IP addresses
- Any credentials, API keys, passwords, or tokens
- Any SAP-internal certificate CNs or subject details
- Any internal system IDs or account IDs
- Any references to SAP internal tooling (Artifactory, GitHub Tools, internal Jira, etc.)

The generated application is for external partners. It must be self-contained and run without any SAP-internal dependencies.

---

## Unit Tests

Generate a complete test suite alongside the application. Target **≥80% line coverage** across the service and router layers.

### Test framework per language

| Language | Test framework | HTTP test tool | Coverage tool |
|----------|---------------|---------------|---------------|
| Python | pytest | httpx + TestClient (FastAPI) | pytest-cov |
| Node.js / JavaScript | Jest | supertest | Jest built-in |
| TypeScript | Jest + ts-jest | supertest | Jest built-in |
| Go | `testing` (stdlib) | `net/http/httptest` | `go test -cover` |
| Java | JUnit 5 | MockMvc (Spring) | JaCoCo |

**Java 21+ — Mockito compatibility fix required.**

Mockito's default inline mock-maker fails on Java 21+ without byte-buddy agent setup. Add both of these to the project:

`src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`:
```
mock-maker-subclass
```

In `../pom.xml` under `<properties>`:
```xml
<argLine>-XX:+EnableDynamicAgentLoading -Xshare:off</argLine>
```
| C# | xUnit | `WebApplicationFactory` | Coverlet |
| Ruby | RSpec | rack-test | SimpleCov |
| PHP | PHPUnit | — | PHPUnit built-in |

Add test dependencies to the dependency manifest.

### Test folder structure

Mirror the application's source folder structure under a `tests/` directory. Each source file has a corresponding test file at the same relative path:

```
# Python example
app/
  main.py
  router.py
  service.py
  storage.py
tests/
  test_main.py
  test_router.py
  test_service.py
  test_storage.py

# Go example
service.go        → service_test.go   (same package, Go convention)
router.go         → router_test.go

# Java example
src/main/java/com/example/
  TenantService.java
  TenantController.java
src/test/java/com/example/
  TenantServiceTest.java
  TenantControllerTest.java
```

Apply the same mirroring principle for all other languages.

### Service layer tests — what to cover

**Worker delay in tests:** Set `workerDelaySeconds` to at least `1` (not `0`) in test setup (`@BeforeEach` / `setUp()`). A delay of `0` causes the background worker to complete before assertions run, producing flaky failures on fast machines. 1000ms is enough to keep synchronous vs async state transitions deterministic.

**State machine transitions:**
- POST creates tenant in `In Activation`, worker transitions to `Active`
- PUT on `Active` tenant moves to `In Update`, worker moves back to `Active`
- DELETE on `Active` or `Blocked` tenant moves to `In Deletion`, worker deletes
- POST /status `blocked` on `Active` moves to `In Blocking`, worker moves to `Blocked`
- POST /status `active` on `Blocked` moves to `In Blocking`, worker moves to `Active`
- POST /status `active` or `blocked` on `In Recoverable Error` is allowed
- Reject DELETE when in any in-progress state (409-02)
- Reject DELETE when in `In Self-Recoverable Error` (409-02)
- Reject PUT when not in `Active` state (409-02)
- Reject POST /status when in any in-progress state (409-02)

**ETag:**
- ETag generated correctly from tenant JSON (excluding status field)
- PUT with correct ETag succeeds
- PUT with wrong ETag returns 409-03

**Duplicate detection:**
- POST with duplicate `sapId` returns 409-01

### Router layer tests — what to cover

For each endpoint, test at minimum:

| Endpoint | Happy path | Error cases |
|----------|-----------|-------------|
| POST /v2/tenants | 202 + Location + Retry-After | 409-01 (duplicate sapId) |
| GET /v2/tenants | 200 + array body | — |
| GET /v2/tenants/{id} | 200 + ETag + Cache-Control: no-store | 404 |
| PUT /v2/tenants/{id} | 202 + Location + Retry-After | 409-02 (wrong state), 409-03 (ETag mismatch), missing If-Match header |
| DELETE /v2/tenants/{id} | 202 + empty body (not null) | 409-02 (in-progress state) |
| GET /v2/tenants/{id}/status | 200 + Retry-After when in-progress, no Retry-After when terminal | 404 |
| POST /v2/tenants/{id}/status | 202 + Location + Retry-After | 409-02 (invalid transition) |
| GET /health | 200 `{"status":"UP"}` | — |

### Running tests

Include the test run command in `../README.md`:

| Language | Command |
|----------|---------|
| Python | `pytest --cov=. --cov-report=term-missing` |
| Node.js / TS | `npm test -- --coverage` |
| Go | `go test ./... -cover` |
| Java | `mvn test` (JaCoCo report in `target/site/jacoco`) |
| C# | `dotnet test --collect:"XPlat Code Coverage"` |

---

## Smoke Test

After generating all files, run an automatic smoke test before declaring done. Use `PROFILE=local` (no mTLS, no cert validation).

### Steps

1. **Install dependencies** using the appropriate command for the language.

2. **Start the app in the background** with `PROFILE=local`. Use the language-idiomatic way to start a background process and capture its PID so you can kill it afterwards.

3. **Wait for the app to be ready** — poll `GET /health` every second, up to 30 seconds. If not ready after 30 seconds, print an error and stop.

4. **POST a tenant** using the sample payload below. Capture the `tenantId` from the response body.

5. **Poll GET /v2/tenants/{tenantId}/status** every 2 seconds until `state` is `Active`, up to 30 seconds. If not `Active` after 30 seconds, print an error.

6. **GET /v2/tenants/{tenantId}** — verify 200 and that `ETag` header is present.

7. **Kill the background app process.**

8. **Report results** — print a clear summary:
   ```
   ✓ Health check passed
   ✓ Tenant created: {tenantId}
   ✓ Tenant reached Active state
   ✓ GET tenant returned ETag
   Smoke test PASSED
   ```
   Or if any step fails:
   ```
   ✗ [step name] failed: [reason]
   Smoke test FAILED — fix the issue before declaring done
   ```

### Sample payload for POST /v2/tenants

```json
{
  "sapId": "SMOKE-TEST-001",
  "globalTenantId": "GT-SMOKE-001",
  "businessType": "production",
  "operationType": "standard",
  "customer": {
    "id": "C-001",
    "name": "Smoke Test Corp",
    "erpId": "ERP-001",
    "contact": {
      "id": "CONT-001",
      "name": "Jane Doe",
      "email": "jane@smoketest.com",
      "phone": "+49123456789"
    }
  },
  "location": {
    "geography": {
      "region": "eu",
      "continent": "Europe",
      "country": "DE",
      "city": "Walldorf"
    },
    "infrastructure": {
      "platform": "cf",
      "region": "eu10",
      "zone": "az1",
      "dataCenter": "eu10",
      "landscape": "prod",
      "environment": "prod",
      "host": "cf.eu10.hana.ondemand.com",
      "internalIdentifier": "cf-eu10"
    }
  },
  "contract": {
    "validFrom": "2024-01-01",
    "validTo": "2025-01-01"
  },
  "products": [
    { "productId": "PRODUCT-001", "quota": 100, "unit": "users" }
  ],
  "initialUsers": [
    { "id": "U-001", "email": "admin@smoketest.com", "name": "Admin", "firstName": "Ad", "lastName": "Min" }
  ],
  "application": {
    "endpoints": {
      "applicationUrl": "https://app.example.com",
      "configurationUrl": "https://app.example.com/config"
    }
  },
  "additionalProperties": {
    "fromManager": { "key": "value" },
    "fromProvider": {}
  }
}
```

### Language-specific notes

- **Python**: `pip install -r requirements.txt`, start with `PROFILE=local python main.py &` (or `uvicorn`), use `requests` or `httpx` for the HTTP calls in a small inline script
- **Node.js / TS**: `npm install`, start with `PROFILE=local node index.js &`, use `fetch` or `axios`
- **Go**: `go mod tidy`, start with `PROFILE=local go run . &`, use `net/http` for calls
- **Java**: `mvn package -q`, start with `java -jar target/*.jar --spring.profiles.active=local &`
- Always clean up the background process (kill PID) whether the test passes or fails — use a try/finally equivalent

---

## Final Checklist Before Finishing

Before declaring done, verify:

- [ ] All 7 tenant endpoints implemented + health check
- [ ] Async worker runs in background thread (not blocking requests)
- [ ] File storage has `# Custom implementation can go here` comments at every read/write
- [ ] ETag generated and validated on PUT
- [ ] All required response headers present: `Location`, `Retry-After`, `ETag`, `Cache-Control: no-store`
- [ ] All error responses use the standard shape: `code`, `message`, `target`, `details[]`
- [ ] State machine transitions enforced (reject wrong-state operations with 409)
- [ ] Duplicate sapId returns 409-01 with Location header
- [ ] Local profile: plain HTTP, no auth, startup warning logged
- [ ] Default profile: HTTPS + mTLS at server level, /health exempt
- [ ] Default profile: certInfo fetched at startup (warn+continue on failure), cached 24h, refreshed in background
- [ ] Default profile: per-request middleware validates X-Ssl-Client-Subject-Cn, X-Ssl-Client-Issuer-Dn, isExpired against cached certInfo; /health exempt; local profile skips entirely
- [ ] Swagger UI served and linked in README
- [ ] Postman collection generated
- [ ] `.env.example` generated
- [ ] `../README.md` complete with Swagger link
- [ ] `GETTING_STARTED.md` with curl walkthrough
- [ ] DELETE returns 202 with empty body — use `Response`, not `JSONResponse(content=None)` (serializes to `null`, hangs Swagger UI)
- [ ] Unit tests generated for service + router layers, mirroring source folder structure
- [ ] Test dependencies added to dependency manifest
- [ ] Test run command documented in README.md
- [ ] Smoke test run automatically after generation: health check, tenant create, status poll to Active, GET with ETag — all passed
- [ ] No SAP-internal sensitive information anywhere
