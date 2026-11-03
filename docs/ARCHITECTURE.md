# Architecture

## Frontend

`main.jsx` mounts one `AppProvider`. `App.jsx` defines routes, and the provider shares catalog records and shopping bag state with navigation and product screens. The storefront applies search, normalized category matching, and sorting in the browser; sorting copies the product list rather than mutating context state.

`src/axios.jsx` exports one API root derived from `VITE_API_BASE_URL`, with a localhost default. Both raw Axios callers and direct image URLs use that root. Vite resolves environment values at startup or build time.

The shopping bag is stored in browser localStorage. It does not reserve database stock, and captured prices/stock can become stale after catalog edits. Catalog entries and product image bytes are persisted separately by the backend.

## Backend

Controllers translate HTTP requests and status codes. Services call Spring Data repositories, and JPA maps products, orders, and line items to PostgreSQL. Request and response DTOs keep order HTTP payloads separate from bidirectional entity relationships.

Product forms use multipart requests with a JSON `product` part and an `imageFile` part. An update without a new image preserves the existing image. The route ID determines the target record, regardless of an ID supplied in the JSON body.

Images use the entity's `@Lob` field and PostgreSQL large objects. Image metadata and bytes are stored in the database; `spring.datasource.hikari.auto-commit=false` supports large-object reads. The image endpoint streams bytes with the saved MIME type.

## Orders and inventory

`OrderService.placeOrder` validates positive quantities, loads products, checks stock, decrements inventory, and saves the order within a transaction. An exception rolls back that transaction. Each line saves its total at checkout, so later catalog price edits do not change that saved amount.

A transaction alone does not prevent two simultaneous requests from reading the same stock. There is currently no explicit optimistic or pessimistic inventory locking. Deleting an order cascades to its line items but does not restock products.

The cart loads current catalog prices and stock while retaining persisted bag IDs and quantities. Checkout collects customer name and email, then posts one `OrderRequest` with product IDs and quantities to `/orders/place`. The backend assigns the date and reference. A successful response clears the bag and opens `/orders`; a failed request preserves the bag and displays an error. The Orders page renders saved `OrderResponse` fields and line totals. It is currently store-wide history rather than authenticated customer-specific history. No payment is collected.

## Configuration and data lifecycle

The tracked backend properties contain environment placeholders and development defaults. A Git-ignored `application-local.properties` can supply local credentials. The frontend has a Git-ignored `.env` option and a tracked example.

The catalog script writes through the same HTTP API as product forms. Its downloaded images and before/after snapshots remain local and are ignored by Git. PostgreSQL owns durable data; repository pushes do not upload your local database.
