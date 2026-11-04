# Everyday Edit

**[Open the live application](https://everyday-edit-tan.vercel.app)** | [Backend health](https://everyday-edit-api.onrender.com/actuator/health)

A full-stack application built with React 18, Java 21, Spring Boot 4.1.1 and PostgreSQL.

## Features

Browse the repository's 13-product catalog with persisted images, search/filter/sort, and maintain a shopping bag. Customers register and sign in for checkout and private order history. A separate admin account manages products and store-wide orders. Inventory rows are locked during checkout; this demo collects no payment.

## Deployment

| Layer | Service | Configuration |
| --- | --- | --- |
| Frontend | Vercel Hobby | Vite, Node.js 24, SPA routing |
| Spring Boot API | Render Free | Singapore, Java 21 Docker |
| PostgreSQL | Neon Free | Separate database, AWS Singapore (`aws-ap-southeast-1`) |

Production source is on `deployment/production`. Deployment is manual: pushing a commit does not automatically release it. Free backends sleep after inactivity, so initial requests may take a few minutes. See [deployment instructions](DEPLOYMENT.md).

## Project layout

- `Spring-ECOM/`: Spring Boot API, Dockerfile, Flyway migration and tests.
- `ecom-frontend-5-main/ecom-frontend-5-main/`: React frontend and Vercel routing configuration.
- `render.yaml`: reproducible Render reference configuration; the live service is managed directly.

## Local setup

1. Install Java 21, Node.js 24 and PostgreSQL. Create an empty disposable development database.
2. Supply backend environment variables `DB_URL` (JDBC URL), `DB_USERNAME`, and `DB_PASSWORD`. The application reads these directly; keep secrets out of Git. `PORT` defaults to 8080.
3. Set `FRONTEND_ORIGIN` to the exact frontend origin. Local backend configuration accepts `http://localhost:3000`; use that origin/port for local frontend development, or deliberately adjust local CORS for another development port.
4. From `Spring-ECOM`, run `./mvnw spring-boot:run` (Windows: `.\mvnw.cmd spring-boot:run`). Flyway initializes an empty schema; Hibernate validates it. Existing schemas need a reviewed migration, not a reset or automatic baseline.
5. From `ecom-frontend-5-main/ecom-frontend-5-main`, run `npm ci`, then configure `VITE_API_BASE_URL` for the API. Development can use HTTP localhost; production builds require HTTPS.
6. Start the frontend with `npm start` for Joblane or `npm run dev -- --port 3000` for Everyday Edit.

Use `SPRING_PROFILES_ACTIVE=prod` only with production HTTPS origins and TLS database settings. Production verifies PostgreSQL server certificates and hostnames. Passwords use BCrypt and authenticated requests use HTTP Basic over HTTPS. Credentials remain in browser memory and refresh clears sign-in.

## Checks

Run Maven `verify` against a disposable PostgreSQL database, never production. Run `npm run build` with the appropriate public API variable configured. Joblane additionally has `npm test -- --watchAll=false`; Everyday Edit has no frontend automated test suite.

Deployment checks included builds, database health, live API reads/writes, authentication, CORS and browser rendering. Flyway clean and automatic baseline are disabled. No dedicated abuse limiter, password recovery, email verification or MFA is implemented. Remaining frontend toolchain dependency advisories and operational limitations are documented in [DEPLOYMENT.md](DEPLOYMENT.md).

## Store administration and catalog

Set `ADMIN_USERNAME` and `ADMIN_PASSWORD` in the backend environment, including local tests. The live admin username is `store_admin`; retrieve its password privately from the Render service Environment page. Never put admin credentials in frontend variables or repository files.

The live catalog contains the 13 products defined in `catalog-maintenance/update_catalog.py`. A Git clone does not contain database records. The original maintenance script predates admin authentication and must be adapted to provide authentication and `X-Requested-With: XMLHttpRequest` before running against this secured API. Do not run it blindly against production. Images are PostgreSQL large objects; read operations retain transaction support. Image rights and curated demo prices are described in [catalog notes](catalog-maintenance/README.md).
