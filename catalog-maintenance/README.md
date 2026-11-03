# Catalog maintenance

`update_catalog.py` loads a 13-product demonstration catalog through the existing Spring product API. It uses Python 3's standard library and requires the backend, PostgreSQL, and Internet access for product imagery.

```powershell
python catalog-maintenance/update_catalog.py
```

The default API root is `http://localhost:8081/api`. Set `CATALOG_API_URL` to use a different development instance. The script updates matching legacy IDs or product names and creates missing entries; new rows let PostgreSQL generate their IDs. It preserves stock for matching entries and initializes new entries with 30 units. Legacy ID matching assumes the original demonstration database, so use a fresh or known development catalog rather than unrelated production data.

All replacement assets are downloaded before product writes begin. Writes then occur one request at a time, not as a single transaction. If a request fails, preceding updates may already be committed; re-running updates matching records instead of duplicating the added catalog names.

Product image bytes are stored as PostgreSQL large objects, not as remote links or browser-only data. Prices are curated INR storefront prices rather than live retailer quotations.

Local outputs are ignored by Git:

- `catalog-before.json`: first original API snapshot, including original image bytes. An existing snapshot is retained across re-runs.
- `catalog-after.json`: updated product metadata.
- `images/`: downloaded replacement assets.

Image references: [DummyJSON](https://dummyjson.com/products), [LEGO product imagery](https://lego.bricksmegastore.com/products/lego-disney-up-house), and [Colgate twin pack imagery](https://southstardrug.com.ph/products/colgate-triple-action-buy-2-at-p159-toothpaste-175-g). Original phone and jeans images are retained; the phone is named to match the pictured Pro model. Review asset usage rights before commercial use.

Database snapshots are backups only; the script does not automatically restore them. A repository clone does not contain the live PostgreSQL database.
