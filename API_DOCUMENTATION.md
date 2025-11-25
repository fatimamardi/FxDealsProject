# API Documentation

## Base URL
```
http://localhost:8080/api/v1/deals
```

## Endpoints

### 1. Import Single Deal

**Endpoint:** `POST /api/v1/deals`

**Description:** Import a single FX deal into the data warehouse.

**Request Body:**
```json
{
  "dealUniqueId": "DEAL-2024-001",
  "fromCurrencyIsoCode": "USD",
  "toCurrencyIsoCode": "EUR",
  "dealTimestamp": "2024-01-15T10:30:00",
  "dealAmount": 1000000.50
}
```

**Field Descriptions:**
- `dealUniqueId` (required, string, max 100 chars): Unique identifier for the deal
- `fromCurrencyIsoCode` (required, string, 3 chars): Source currency ISO 4217 code (e.g., USD, EUR)
- `toCurrencyIsoCode` (required, string, 3 chars): Target currency ISO 4217 code
- `dealTimestamp` (required, datetime): When the deal occurred (ISO 8601 format)
- `dealAmount` (required, decimal): Amount in the ordering currency (max 4 decimal places)

**Success Response (201 Created):**
```json
{
  "id": 1,
  "dealUniqueId": "DEAL-2024-001",
  "fromCurrencyIsoCode": "USD",
  "toCurrencyIsoCode": "EUR",
  "dealTimestamp": "2024-01-15T10:30:00",
  "dealAmount": 1000000.50,
  "createdAt": "2024-01-15T12:00:00"
}
```

**Error Responses:**

- **400 Bad Request** - Validation error
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed: Invalid currency code"
}
```

- **409 Conflict** - Duplicate deal
```json
{
  "errorCode": "DUPLICATE_DEAL",
  "message": "Deal with unique ID DEAL-2024-001 already exists"
}
```

**Example using cURL:**
```bash
curl -X POST http://localhost:8080/api/v1/deals \
  -H "Content-Type: application/json" \
  -d '{
    "dealUniqueId": "DEAL-2024-001",
    "fromCurrencyIsoCode": "USD",
    "toCurrencyIsoCode": "EUR",
    "dealTimestamp": "2024-01-15T10:30:00",
    "dealAmount": 1000000.50
  }'
```

---

### 2. Import Bulk Deals

**Endpoint:** `POST /api/v1/deals/bulk`

**Description:** Import multiple FX deals in a single request. Each deal is processed independently - valid deals are saved even if others fail.

**Request Body:**
```json
{
  "deals": [
    {
      "dealUniqueId": "DEAL-2024-001",
      "fromCurrencyIsoCode": "USD",
      "toCurrencyIsoCode": "EUR",
      "dealTimestamp": "2024-01-15T10:30:00",
      "dealAmount": 1000000.50
    },
    {
      "dealUniqueId": "DEAL-2024-002",
      "fromCurrencyIsoCode": "GBP",
      "toCurrencyIsoCode": "JPY",
      "dealTimestamp": "2024-01-15T11:45:00",
      "dealAmount": 500000.25
    }
  ]
}
```

**Success Response (201 Created):**
```json
{
  "totalReceived": 2,
  "successfullyImported": 2,
  "skippedDuplicates": 0,
  "failed": 0,
  "errors": [],
  "importedDeals": [
    {
      "id": 1,
      "dealUniqueId": "DEAL-2024-001",
      ...
    },
    {
      "id": 2,
      "dealUniqueId": "DEAL-2024-002",
      ...
    }
  ]
}
```

**Partial Success Response (206 Partial Content):**
```json
{
  "totalReceived": 3,
  "successfullyImported": 2,
  "skippedDuplicates": 1,
  "failed": 0,
  "errors": [
    "Deal[1] (DEAL-2024-002): Deal with unique ID DEAL-2024-002 already exists"
  ],
  "importedDeals": [...]
}
```

**Example using cURL:**
```bash
curl -X POST http://localhost:8080/api/v1/deals/bulk \
  -H "Content-Type: application/json" \
  -d @sample-deals.json
```

---

### 3. Get All Deals

**Endpoint:** `GET /api/v1/deals`

**Description:** Retrieve all deals from the data warehouse.

**Success Response (200 OK):**
```json
[
  {
    "id": 1,
    "dealUniqueId": "DEAL-2024-001",
    "fromCurrencyIsoCode": "USD",
    "toCurrencyIsoCode": "EUR",
    "dealTimestamp": "2024-01-15T10:30:00",
    "dealAmount": 1000000.50,
    "createdAt": "2024-01-15T12:00:00"
  },
  {
    "id": 2,
    "dealUniqueId": "DEAL-2024-002",
    ...
  }
]
```

**Example using cURL:**
```bash
curl http://localhost:8080/api/v1/deals
```

---

### 4. Get Deal by Unique ID

**Endpoint:** `GET /api/v1/deals/{dealUniqueId}`

**Description:** Retrieve a specific deal by its unique identifier.

**Path Parameters:**
- `dealUniqueId` (string): The unique identifier of the deal

**Success Response (200 OK):**
```json
{
  "id": 1,
  "dealUniqueId": "DEAL-2024-001",
  "fromCurrencyIsoCode": "USD",
  "toCurrencyIsoCode": "EUR",
  "dealTimestamp": "2024-01-15T10:30:00",
  "dealAmount": 1000000.50,
  "createdAt": "2024-01-15T12:00:00"
}
```

**Error Response (404 Not Found):**
```json
{
  "errorCode": "NOT_FOUND",
  "message": "Deal not found"
}
```

**Example using cURL:**
```bash
curl http://localhost:8080/api/v1/deals/DEAL-2024-001
```

---

## Validation Rules

### Deal Unique ID
- Required
- Cannot be null or empty
- Maximum 100 characters
- No leading/trailing whitespace

### Currency ISO Codes
- Required
- Must be exactly 3 characters
- Must be uppercase letters (A-Z)
- From and To currencies must be different
- Valid ISO 4217 format (e.g., USD, EUR, GBP)

### Deal Timestamp
- Required
- Must be a valid ISO 8601 datetime
- Cannot be in the future
- Cannot be more than 10 years old

### Deal Amount
- Required
- Must be greater than 0
- Maximum 15 integer digits
- Maximum 4 decimal places
- Maximum value: 1,000,000,000,000 (1 trillion)

## Error Codes

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| `VALIDATION_ERROR` | 400 | Request validation failed |
| `INVALID_ARGUMENT` | 400 | Invalid argument provided |
| `DUPLICATE_DEAL` | 409 | Deal with the same unique ID already exists |
| `ILLEGAL_STATE` | 409 | Illegal state (e.g., duplicate detected) |
| `NOT_FOUND` | 404 | Resource not found |
| `INTERNAL_ERROR` | 500 | Internal server error |


See `sample-deals.json` for example deal data that can be imported using the bulk endpoint.

