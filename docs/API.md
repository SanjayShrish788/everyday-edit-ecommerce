# API reference

Default base URL: `http://localhost:8081/api`.

## Products

| Method | Path | Behavior |
| --- | --- | --- |
| GET | `/products` | List persisted products. |
| GET | `/product/{id}` | Return one product, or 404. |
| GET | `/product/{id}/image` | Return stored image bytes and MIME type, or 404. |
| POST | `/product` | Create a product from multipart parts; returns 201. |
| PUT | `/product/{id}` | Update metadata and optionally an image; unknown IDs return 404. |
| DELETE | `/product/{id}` | Delete a product; returns 200 or 404. |
| GET | `/products/search?keyword=...` | Case-insensitive name, description, brand, and category search. |

Creation requires a JSON `product` part and an `imageFile` part. Updates require `product`; `imageFile` is optional. The JSON metadata has this shape:

```json
{
  "name": "Everyday headphones",
  "brand": "Example",
  "description": "Over-ear headphones for everyday listening.",
  "price": 2499,
  "category": "Headphone",
  "releaseDate": "2026-01-01T00:00:00.000+00:00",
  "productAvailable": true,
  "stockQuantity": 20
}
```

Upload example (run with paths to your own JSON and image files):

```sh
curl -X POST http://localhost:8081/api/product \
  -F 'product=@product.json;type=application/json' \
  -F 'imageFile=@headphones.jpg;type=image/jpeg'
```

Images are saved in PostgreSQL. The frontend uses the image endpoint instead of embedding the serialized `imageData` field returned in product entities.

## Orders

| Method | Path | Behavior |
| --- | --- | --- |
| POST | `/orders/place` | Validate quantities and stock, then save an order; returns 201. |
| GET | `/orders` | List order response DTOs and their line totals. |
| DELETE | `/orders/{orderId}` | Delete using the public reference (such as `ORD1234ABCD`); returns 204 or 404. |

Example request body:

```json
{
  "customerName": "Example Customer",
  "email": "customer@example.com",
  "items": [
    { "productId": 1, "quantity": 2 }
  ]
}
```

Use an actual product ID with sufficient stock. The service calculates totals from persisted prices. Quantity or stock failures currently raise runtime exceptions; they do not have a dedicated validation-error response contract.

The frontend checkout calls this endpoint with customer name, email, and product quantities. The server assigns `orderId`, `orderDate`, and `status`; the Orders page shows these alongside `customerName`, `email`, and the returned item totals. Deleting an order removes history and line items; it does not restore stock.

## Reachability

`GET http://localhost:8081/hello` returns a greeting. This is a simple reachability check rather than a database health endpoint.
