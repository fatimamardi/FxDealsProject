# Bloomberg FX Deals Data Warehouse - Project Explanation

## 📋 Table of Contents

1. [Project Overview](#project-overview)
2. [Business Requirements](#business-requirements)
3. [Technical Requirements](#technical-requirements)
4. [System Architecture](#system-architecture)
5. [Key Features Implementation](#key-features-implementation)
6. [Project Structure](#project-structure)
7. [How It Works](#how-it-works)
8. [Deliverables Checklist](#deliverables-checklist)

---

## 🎯 Project Overview

This project is a **Data Warehouse System** developed for **Bloomberg** to analyze Foreign Exchange (FX) deals. The system accepts deal details from external sources, validates them, and persists them into a database for analysis purposes.

### Context
- **Client**: Bloomberg (Financial Data & Analytics Company)
- **Purpose**: Data warehouse for FX deal analysis
- **Team**: Scrum/Agile Development Team
- **Type**: Enterprise-grade data ingestion system

---

## 📝 Business Requirements

### Customer Story
> "As a data analyst at Bloomberg, I need to import FX deal data from various sources into our data warehouse so that I can analyze trading patterns, currency flows, and market trends."

### Core Requirements

#### 1. **Accept Deal Details**
The system must accept the following fields for each FX deal:

| Field | Description | Format | Example |
|-------|-------------|--------|---------|
| **Deal Unique Id** | Unique identifier for the deal | String (max 100 chars) | "DEAL-2024-001" |
| **From Currency ISO Code** | Source currency (Ordering Currency) | 3-letter ISO 4217 code | "USD" |
| **To Currency ISO Code** | Target currency | 3-letter ISO 4217 code | "EUR" |
| **Deal Timestamp** | When the deal occurred | ISO 8601 datetime | "2024-01-15T10:30:00" |
| **Deal Amount** | Amount in ordering currency | Decimal (max 4 decimal places) | 1000000.50 |

#### 2. **Validate Row Structure**
The system must validate:
- ✅ **Missing Fields**: All required fields must be present
- ✅ **Type Format**: Data types must be correct (string, datetime, decimal)
- ✅ **Format Validation**: 
  - Currency codes must be 3 uppercase letters (ISO 4217)
  - Timestamps must be valid ISO 8601 format
  - Amounts must be positive numbers with max 4 decimal places
- ✅ **Business Rules**:
  - From and To currencies must be different
  - Timestamp cannot be in the future
  - Amount must be greater than 0

#### 3. **Prevent Duplicate Imports**
- ✅ System must not import the same request twice
- ✅ Duplicate detection based on **Deal Unique Id**
- ✅ Check duplicates both:
  - Within the same batch (if bulk import)
  - Against existing database records

#### 4. **No Rollback Policy**
- ✅ **Critical Requirement**: Every valid row imported must be saved in DB
- ✅ If one deal fails, others in the batch should still be saved
- ✅ Each deal is processed in its own transaction (REQUIRES_NEW propagation)
- ✅ Failed deals are logged but don't prevent successful imports

---

## 🛠 Technical Requirements

### 1. **Database**
- ✅ **Actual Database Required** (not in-memory)
- ✅ Options: PostgreSQL, MySQL, or MongoDB
- ✅ **Choice**: PostgreSQL 15 (selected for this project)
- ✅ Database schema with proper constraints and indexes

### 2. **Build Tool**
- ✅ **Maven or Gradle** project required
- ✅ **Choice**: Maven (selected for this project)
- ✅ Proper dependency management
- ✅ Build configuration for production

### 3. **Deployment**
- ✅ **Docker Compose** for easy deployment
- ✅ Includes:
  - Application container
  - Database container
  - Network configuration
  - Volume management
- ✅ **Sample file** included for testing

### 4. **Error/Exception Handling**
- ✅ Comprehensive exception handling
- ✅ Custom exception classes
- ✅ Global exception handler
- ✅ Meaningful error messages
- ✅ Proper HTTP status codes

### 5. **Logging**
- ✅ Proper logging throughout the application
- ✅ Different log levels (DEBUG, INFO, WARN, ERROR)
- ✅ Log file rotation
- ✅ Structured logging for monitoring

### 6. **Unit Testing**
- ✅ Comprehensive unit tests
- ✅ High code coverage (target: >80%)
- ✅ Tests for:
  - Service layer (business logic)
  - Controller layer (REST endpoints)
  - Validation logic
  - Repository layer

### 7. **Documentation**
- ✅ **Markdown (.md) files** required
- ✅ README.md with setup instructions
- ✅ API documentation
- ✅ Project explanation (this document)
- ✅ Code comments and JavaDoc

### 8. **GitHub Delivery**
- ✅ Code delivered over GitHub.com
- ✅ Proper .gitignore file
- ✅ Clean commit history
- ✅ Repository structure

### 9. **Makefile (Plus)**
- ✅ Makefile to streamline running the application
- ✅ Common commands:
  - Build
  - Test
  - Run
  - Docker operations
  - Coverage reports

---

## 🏗 System Architecture

### High-Level Architecture

```
┌─────────────────┐
│   Client/API    │
│   Consumer      │
└────────┬─────────┘
         │ HTTP/REST
         │
┌────────▼─────────────────────────────────────┐
│         REST API Layer                       │
│  ┌──────────────────────────────────────┐   │
│  │   FxDealController                   │   │
│  │   - POST /api/v1/deals               │   │
│  │   - POST /api/v1/deals/bulk          │   │
│  │   - GET  /api/v1/deals               │   │
│  │   - GET  /api/v1/deals/{id}         │   │
│  └──────────────────────────────────────┘   │
└────────┬─────────────────────────────────────┘
         │
┌────────▼─────────────────────────────────────┐
│         Service Layer                         │
│  ┌──────────────────────────────────────┐   │
│  │   FxDealService                      │   │
│  │   - importDeal()                     │   │
│  │   - importDealsBulk()                │   │
│  │   - Transaction Management            │   │
│  └──────────────────────────────────────┘   │
│  ┌──────────────────────────────────────┐   │
│  │   DealValidationService              │   │
│  │   - validateDeal()                  │   │
│  │   - Field validation                 │   │
│  │   - Business rule validation         │   │
│  └──────────────────────────────────────┘   │
└────────┬─────────────────────────────────────┘
         │
┌────────▼─────────────────────────────────────┐
│         Repository Layer                      │
│  ┌──────────────────────────────────────┐   │
│  │   FxDealRepository                   │   │
│  │   - JPA Repository                   │   │
│  │   - Custom queries                   │   │
│  │   - Duplicate checking               │   │
│  └──────────────────────────────────────┘   │
└────────┬─────────────────────────────────────┘
         │
┌────────▼─────────────────────────────────────┐
│         Database Layer                        │
│  ┌──────────────────────────────────────┐   │
│  │   PostgreSQL Database                │   │
│  │   - fx_deals table                   │   │
│  │   - Unique constraints               │   │
│  │   - Indexes                          │   │
│  └──────────────────────────────────────┘   │
└──────────────────────────────────────────────┘
```

### Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| **Language** | Java | 17 |
| **Framework** | Spring Boot | 3.2.0 |
| **Build Tool** | Maven | 3.9+ |
| **Database** | PostgreSQL | 15 |
| **ORM** | JPA/Hibernate | (via Spring Boot) |
| **Validation** | Bean Validation | (Jakarta Validation) |
| **Testing** | 