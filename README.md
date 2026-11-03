# Everyday Edit

[![Build and test](https://github.com/SanjayShrish788/everyday-edit-ecommerce/actions/workflows/ci.yml/badge.svg)](https://github.com/SanjayShrish788/everyday-edit-ecommerce/actions/workflows/ci.yml)

A full-stack ecommerce storefront built with **React, Spring Boot, and PostgreSQL**. Browse a curated catalog, search and filter products, manage product details and images, and keep a shopping bag between visits.

## Features

- Responsive storefront with category filters, text search, price sorting, and INR pricing.
- Product detail, create, edit, and delete screens.
- Multipart image uploads stored permanently in PostgreSQL, rather than browser storage or remote image links.
- A shopping bag persisted in localStorage with quantity controls and checkout that collects customer name and email.
- Saved order history with server-assigned order references, dates, status, item quantities, and INR totals.
- Backend order APIs with stock validation, transactional inventory updates, and saved line totals.
- Repeatable catalog maintenance script for a 13-product demonstration collection.
- GitHub Actions jobs for the frontend production build and backend verification against PostgreSQL.

## Stack

| Layer | Technology |
| --- | --- |
| Frontend | React 18, Vite 5, React Router, Axios, Bootstrap, React Icons |
| Backend | Java 21, Spring Boot 4.1.1, Spring Data JPA, Hibernate, Lombok |
| Database | PostgreSQL; local verification used version 18 |
| Catalog tooling | Python 3 standard library |
| Automation | GitHub Actions, npm, Maven Wrapper |

## Project layout

```text
Spring-ECOM/                                  Spring Boot application
  src/main/java/com/sanjay/Spring_ECOM/
    controllers/                              HTTP endpoints
    service/                                  Catalog and order operations
    repo/                                     Spring Data repositories
    model/                                    Entities and request/response DTOs
  src/main/resources/application.properties   Environment-based configuration
ecom-frontend-5-main/ecom-frontend-5-main/      React application
  src/components/                            Routed pages and shared UI
  src/Context/                               Catalog and shopping bag state
  src/axios.jsx                              Shared API URL and Axios client
catalog-maintenance/                         Catalog importer and usage notes
docs/                                        Architecture and API reference
.github/workflows/ci.yml                      Build and verification jobs
```

## Run locally

Prerequisites: Java 21, Node.js 20 with npm, PostgreSQL, and Python 3 if you want to import the example catalog. Maven is supplied through the wrapper.

### 1. Configure PostgreSQL

Create an empty database named `sanjay` using your local PostgreSQL administrator account:

```sql
CREATE DATABASE sanjay;
```

From the repository root, copy the configuration template:

```powershell
Copy-Item Spring-ECOM/application-local.properties.example Spring-ECOM/application-local.properties
```

Edit `Spring-ECOM/application-local.properties` with your database credentials. This file is ignored by Git. Alternatively, set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in the backend process environment. Environment values used in the main properties file can be overridden by the imported local properties file, so remove matching local entries when switching to environment-only configuration.

### 2. Start the backend

```powershell
cd Spring-ECOM
.\mvnw.cmd spring-boot:run
```

On macOS/Linux, use `sh ./mvnw spring-boot:run`. Ensure `JAVA_HOME` points to a JDK 21 installation. The default API is **http://localhost:8081/api**; `SERVER_PORT` overrides the backend port.

### 3. Start the frontend in a second terminal

```powershell
cd ecom-frontend-5-main/ecom-frontend-5-main
npm ci
npm run dev
```

Open **http://localhost:5173**. To change the API address, copy `.env.example` to `.env` in the frontend directory, edit `VITE_API_BASE_URL`, and restart Vite. Image requests and form submissions use the same setting.

### 4. Populate the demonstration catalog (optional)

With the backend running, return to the repository root:

```powershell
python catalog-maintenance/update_catalog.py
```

The script downloads product images, then creates or updates entries through the existing multipart API. It writes to the configured database and can update existing prices and descriptions. Use it against a development catalog. See [catalog maintenance](catalog-maintenance/README.md) for matching, backups, image sources, and configuration.

A fresh clone does not include your local database or its snapshots. Imported products and uploaded images survive application restarts as long as the PostgreSQL database is retained.

## Validation

```powershell
# Frontend production build
cd ecom-frontend-5-main/ecom-frontend-5-main
npm run build

# Backend test and package (from the backend directory)
cd ../../Spring-ECOM
.\mvnw.cmd verify
```

The current backend test starts the Spring application context and requires a reachable PostgreSQL database. CI provides a separate disposable database; it does not use your local catalog. The existing ESLint configuration is available through `npm run lint`, but lint is not currently a CI gate.

## API and architecture

See the [API reference](docs/API.md) and [architecture notes](docs/ARCHITECTURE.md) for endpoint contracts, data persistence, and checkout behavior.

## Current scope

This is a local development and portfolio project. It does not implement authentication, payments, shipping, or production deployment. Product-management endpoints are currently open and CORS is unrestricted.

Checkout posts one `OrderRequest` to `/api/orders/place`. The backend saves customer details and line items, assigns the order date and reference, and updates inventory in its existing transaction. The bag is cleared only after success, and `/orders` displays saved `OrderResponse` records. The API currently exposes store-wide order history without authentication. Concurrent stock updates are not protected with inventory locks.

Demo prices are curated INR prices, not live retailer quotations. Product imagery comes from external catalogs and retained sample assets; review image usage rights before publishing a commercial storefront. Local database snapshots, credentials, installed dependencies, and build output are excluded from Git.
