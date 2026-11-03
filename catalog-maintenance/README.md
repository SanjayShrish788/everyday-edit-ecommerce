# Everyday Edit catalog maintenance

The catalog is stored in the local PostgreSQL `sanjay` database. Product image bytes are stored as PostgreSQL large objects by the existing Spring API, not as remote image URLs or browser-only data.

- `catalog-before.json`: original API snapshot, including original image bytes.
- `catalog-after.json`: updated product metadata.
- `update_catalog.py`: repeatable update via http://localhost:8081/api. Matches existing product IDs or names to avoid duplicate additions.
- `images/`: downloaded replacement assets.

Prices are curated INR storefront prices, not live retailer quotations. Existing stock quantities are preserved; new catalog entries start with 30 units.

Image references: DummyJSON product catalog (https://dummyjson.com/products), LEGO product imagery (https://lego.bricksmegastore.com/products/lego-disney-up-house), and Colgate twin pack imagery (https://southstardrug.com.ph/products/colgate-triple-action-buy-2-at-p159-toothpaste-175-g). Original phone and jeans images are retained; the phone name now correctly identifies the pictured Pro model.

Verified through a separate PostgreSQL connection: 13 committed product rows with nonempty stored image data. All 13 image endpoints returned successfully. No backend source or schema changes were made.
