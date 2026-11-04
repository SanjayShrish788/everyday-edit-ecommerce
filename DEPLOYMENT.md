# Everyday Edit deployment

Frontend: https://everyday-edit-tan.vercel.app

Backend health: https://everyday-edit-api.onrender.com/actuator/health

Vercel Hobby serves the Vite build from `ecom-frontend-5-main/ecom-frontend-5-main`, Node 24. `VITE_API_BASE_URL` is the public HTTPS backend URL including `/api`. Credentials must never be placed in VITE variables. SPA fallback is configured in vercel.json.

Render Free runs `Spring-ECOM/Dockerfile` with Java 21, Singapore, non-root runtime, PORT binding and bounded JVM/pool/thread resources. Automatic deployment is off. render.yaml is reference configuration, not a linked Blueprint. Runtime variables: DB_URL, DB_USERNAME, DB_PASSWORD, FRONTEND_ORIGIN, ADMIN_USERNAME, ADMIN_PASSWORD, SPRING_PROFILES_ACTIVE=prod. PostgreSQL verifies server certificates/hostname using the Java trust store.

Neon uses the existing separate Singapore E-commerce project. Flyway initializes its previously empty schema; Hibernate validates, clean/baseline are disabled. Never run tests or enable automatic schema reset against production. Product images remain in PostgreSQL large objects; transactions and auto-commit=false support their reads.

Customers create accounts and sign in through the account form; credentials remain in memory and refresh clears sign-in. Checkout and order history require authentication. Customer history is filtered by the authenticated username, independent of supplied customer name/email. Admin credentials are separate Render environment variables; registered customers cannot obtain the admin role. Username `store_admin` is reserved. Admin can manage products and view store-wide orders. To retrieve the generated admin password securely, open the Render service Environment page and reveal ADMIN_PASSWORD yourself; never paste it into chat or Git.

Orders lock inventory rows during checkout, prices come from stored products, and catalog changes require admin authorization. Only the corresponding Vercel origin is allowed. Writes require X-Requested-With: XMLHttpRequest to prevent browser-ambient Basic credentials from authorizing cross-site form requests. HTTPS is required in deployment.

Remaining limits: demo checkout collects no payment; no email verification/password recovery/MFA or dedicated abuse limiter. Uploads are bounded but comprehensive file-content validation remains future work. Dependency advisories remain in the Vite 5/Router 6 toolchain; no development server or SSR runs in production. Existing CSS references two missing legacy decorative assets. Free services can sleep and cold starts are slow. No paid services enabled. Pushes do not automatically publish; run checks then deploy manually.
