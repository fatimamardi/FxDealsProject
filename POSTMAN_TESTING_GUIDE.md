# Postman Testing Guide - Bloomberg FX Deals Data Warehouse

## Prerequisites

1. Application is running on **http://localhost:8080**
2. Postman is installed

---

## Step 1: Import a Single Deal

### Import Single Deal

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/v1/deals`
- **Headers:**
  - `Content-Type: application/json`
- **Body (raw JSON):**
```json
{
  "dealUniqueId": "DEAL-2024-001",
  "fromCurrencyIsoCode": "USD",
  "toCurrencyIsoCode": "EUR",
  "dealTimestamp": "2024-01-15T10:30:00",
  "dealAmount": 1000000.50
}
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "dealUniqueId": "DEAL-2024-001",
  "fromCurrencyIsoCode": "USD",
  "toCurrencyIsoCode": "EUR",
  "dealTimestamp": "2024-01-15T10:30:00",
  "dealAmount": 1000000.50,
  "createdAt": "2024-11-24T20:00:00"
}
```

**Test Cases:**

1. **Valid Deal** - Use the request above
2. **Duplicate Deal** - Send the same request again (should return 409 Conflict)
3. **Invalid Currency Code** - Change `fromCurrencyIsoCode` to "XX" (should return 400 Bad Request)
4. **Missing Field** - Remove `dealUniqueId` (should return 400 Bad Request)
5. **Future Timestamp** - Set `dealTimestamp` to a future date (should return 400 Bad Request)
6. **Same From/To Currency** - Set both currencies to "USD" (should return 400 Bad Request)
7. **Zero Amount** - Set `dealAmount` to 0 (should return 400 Bad Request)

---

## Step 2: Import Bulk Deals

### Import Multiple Deals

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/v1/deals/bulk`
- **Headers:**
  - `Content-Type: application/json`
- **Body (raw JSON):**
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
    },
    {
      "dealUniqueId": "DEAL-2024-003",
      "fromCurrencyIsoCode": "EUR",
      "toCurrencyIsoCode": "USD",
      "dealTimestamp": "2024-01-15T12:00:00",
      "dealAmount": 2500000.75
    }
  ]
}
```

**Expected Response (201 Created):**
```json
{
  "totalReceived": 3,
  "successfullyImported": 3,
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
    },
    {
      "id": 3,
      "dealUniqueId": "DEAL-2024-003",
      ...
    }
  ]
}
```

**Test Cases:**

1. **All Valid Deals** - Use the request above
2. **Mixed Valid/Invalid** - Include one invalid deal (should return 206 Partial Content)
3. **Duplicate in Batch** - Include same `dealUniqueId` twice (should fail the duplicate)
4. **Duplicate with Existing** - Try to import a deal that was already imported (should skip)

---

## Step 3: Retrieve Deals

### Get All Deals

**Request:**
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/v1/deals`

**Expected Response (200 OK):**
```json
[
  {
    "id": 1,
    "dealUniqueId": "DEAL-2024-001",
    "fromCurrencyIsoCode": "USD",
    "toCurrencyIsoCode": "EUR",
    "dealTimestamp": "2024-01-15T10:30:00",
    "dealAmount": 1000000.50,
    "createdAt": "2024-11-24T20:00:00"
  },
  {
    "id": 2,
    "dealUniqueId": "DEAL-2024-002",
    ...
  }
]
```

### Get Deal by Unique ID

**Request:**
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/v1/deals/DEAL-2024-001`

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "dealUniqueId": "DEAL-2024-001",
  "fromCurrencyIsoCode": "USD",
  "toCurrencyIsoCode": "EUR",
  "dealTimestamp": "2024-01-15T10:30:00",
  "dealAmount": 1000000.50,
  "createdAt": "2024-11-24T20:00:00"
}
```

**Test Case:**
- **Non-existent Deal** - Use `DEAL-999` (should return 404 Not Found)

---

## Complete Testing Workflow

### Recommended Testing Sequence:

1. **Import Single Deal** → Test basic import
2. **Try Duplicate** → Verify duplicate prevention
3. **Import Bulk** → Test bulk import
4. **Get All Deals** → Verify data persistence
5. **Get Deal by ID** → Test retrieval
6. **Test Validation** → Try invalid data

---

## Postman Collection Setup

### Create a Postman Collection:

1. **Create New Collection:** "Bloomberg FX Deals API"
2. **Add Environment Variables:**
   - `base_url`: `http://localhost:8080`
   - `api_base`: `{{base_url}}/api/v1/deals`

3. **Create Requests:**
   - Import Single: `POST {{api_base}}`
   - Import Bulk: `POST {{api_base}}/bulk`
   - Get All: `GET {{api_base}}`
   - Get by ID: `GET {{api_base}}/{{dealUniqueId}}`

---

## Sample Invalid Requests for Testing Validation

### Missing Required Field
```json
{
  "fromCurrencyIsoCode": "USD",
  "toCurrencyIsoCode": "EUR",
  "dealTimestamp": "2024-01-15T10:30:00",
  "dealAmount": 1000000.50
}
```
*Missing `dealUniqueId` - Should return 400*

### Invalid Currency Code
```json
{
  "dealUniqueId": "DEAL-999",
  "fromCurrencyIsoCode": "XXX",
  "toCurrencyIsoCode": "EUR",
  "dealTimestamp": "2024-01-15T10:30:00",
  "dealAmount": 1000000.50
}
```
*Invalid currency code - Should return 400*

### Future Timestamp
```json
{
  "dealUniqueId": "DEAL-999",
  "fromCurrencyIsoCode": "USD",
  "toCurrencyIsoCode": "EUR",
  "dealTimestamp": "2025-12-31T23:59:59",
  "dealAmount": 1000000.50
}
```
*Future timestamp - Should return 400*

### Same From/To Currency
```json
{
  "dealUniqueId": "DEAL-999",
  "fromCurrencyIsoCode": "USD",
  "toCurrencyIsoCode": "USD",
  "dealTimestamp": "2024-01-15T10:30:00",
  "dealAmount": 1000000.50
}
```
*Same currencies - Should return 400*

### Zero Amount
```json
{
  "dealUniqueId": "DEAL-999",
  "fromCurrencyIsoCode": "USD",
  "toCurrencyIsoCode": "EUR",
  "dealTimestamp": "2024-01-15T10:30:00",
  "dealAmount": 0
}
```
*Zero amount - Should return 400*

---

## Quick Test Script

You can also use `curl` commands in terminal:

```bash
# Import Single Deal
curl -X POST http://localhost:8080/api/v1/deals ^
  -H "Content-Type: application/json" ^
  -d "{\"dealUniqueId\":\"DEAL-001\",\"fromCurrencyIsoCode\":\"USD\",\"toCurrencyIsoCode\":\"EUR\",\"dealTimestamp\":\"2024-01-15T10:30:00\",\"dealAmount\":1000000.50}"

# Get All Deals
curl http://localhost:8080/api/v1/deals
```

---

## Expected Error Responses

### Validation Error (400 Bad Request)
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed: Invalid currency code"
}
```

### Duplicate Deal (409 Conflict)
```json
{
  "errorCode": "DUPLICATE_DEAL",
  "message": "Deal with unique ID DEAL-2024-001 already exists"
}
```

### Not Found (404)
*Empty response body*

---

## Tips

1. **Use Postman's Collection Runner** to test multiple requests in sequence
2. **Save responses** as examples for documentation
3. **Use variables** for dealUniqueId to test duplicates easily
4. **Check H2 Console** at http://localhost:8080/h2-console to see stored data
5. **View application logs** in IntelliJ console for detailed error messages

---

Happy Testing! 🚀

