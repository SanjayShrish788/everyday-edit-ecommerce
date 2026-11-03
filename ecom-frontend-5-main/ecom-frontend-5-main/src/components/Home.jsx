import { API_BASE_URL } from "../axios";
import { useContext, useState } from "react";
import { Link } from "react-router-dom";
import {
  FiArrowRight,
  FiArrowUpRight,
  FiCheck,
  FiPackage,
  FiShield,
  FiTruck,
  FiPlus,
  FiSliders,
} from "react-icons/fi";
import AppContext from "../Context/Context";

/** Normalizes legacy singular and lowercase category names for filtering. */

const normalizeCategory = (value) =>
  ({ phone: "mobile", toy: "toys", headphones: "headphone" })[
    String(value).toLowerCase()
  ] || String(value).toLowerCase();
const categories = [
  "All finds",
  "Laptop",
  "Headphone",
  "Mobile",
  "Electronics",
  "Toys",
  "Fashion",
  "Personal Care",
];
/** Renders API-backed products with browser-side search, category filters, and sorting. */
export default function Home({ searchQuery = "" }) {
  const { data, isError, addToCart, refreshData } = useContext(AppContext);
  const [category, setCategory] = useState("All finds");
  const [sort, setSort] = useState("featured");
  const [added, setAdded] = useState(null);
  // Copy before sorting so the shared catalog order remains unchanged.
  const products = data
    .filter(
      (p) =>
        (category === "All finds" ||
          normalizeCategory(p.category) === normalizeCategory(category)) &&
        `${p.name} ${p.brand} ${p.category}`
          .toLowerCase()
          .includes(searchQuery.toLowerCase()),
    )
    .slice()
    .sort((a, b) =>
      sort === "low"
        ? a.price - b.price
        : sort === "high"
          ? b.price - a.price
          : 0,
    );
  return (
    <main className="storefront">
      <section className="shop-hero">
        <div className="hero-copy">
          <div className="eyebrow">
            <span /> THE EVERYDAY EDIT / 2026
          </div>
          <h1>
            Good things.
            <br />
            Better <em>everydays.</em>
          </h1>
          <p>
            Thoughtful tech, fresh essentials, and little things
            <br className="desktop-break" /> that make a big difference. Find
            your kind of good.
          </p>
          <a className="shop-cta" href="#collection">
            Explore the collection <FiArrowUpRight />
          </a>
          <div className="hero-footnote">
            <span className="mini-stars">✳ ✳ ✳</span> Carefully picked. Ready
            for your everyday.
          </div>
        </div>
        <div className="hero-photo">
          <img
            src="https://images.unsplash.com/photo-1546435770-a3e426bf472b?auto=format&fit=crop&w=1200&q=85"
            alt="Over-ear headphones with a minimal, sculptural design"
          />
          <span className="photo-tag">LESS NOISE. MORE YOU.</span>
          <div className="photo-caption">
            <div>
              <small>YOUR DAILY SOUNDTRACK</small>
              <strong>Find your focus.</strong>
            </div>
            <button
              aria-label="Shop headphones"
              onClick={() => {
                setCategory("Headphone");
                document
                  .getElementById("collection")
                  .scrollIntoView({ behavior: "smooth" });
              }}
            >
              <FiArrowUpRight />
            </button>
          </div>
          <span className="image-index">01 — THE EVERYDAY ESSENTIALS</span>
        </div>
      </section>
      <section className="benefit-strip" aria-label="Shopping benefits">
        <div>
          <FiPackage />
          <span>
            Thoughtfully selected<small>Good finds, all in one place</small>
          </span>
        </div>
        <div>
          <FiShield />
          <span>
            Shop with confidence<small>Clear details. No guesswork.</small>
          </span>
        </div>
        <div>
          <FiTruck />
          <span>
            From our store to your door
            <small>Your next favorite is closer</small>
          </span>
        </div>
        <div className="strip-note">
          A better kind
          <br />
          of everyday. <span>✳</span>
        </div>
      </section>
      <section className="collection" id="collection">
        <div className="collection-heading">
          <div>
            <div className="eyebrow">THE GOOD STUFF</div>
            <h2>
              Your next favorite is here<span>.</span>
            </h2>
          </div>
          <span className="collection-intro">
            A little of everything.
            <br />
            Something just for you.
          </span>
        </div>
        <div className="collection-tools">
          <div className="category-pills" aria-label="Filter by category">
            {categories.map((c) => (
              <button
                key={c}
                className={category === c ? "selected" : ""}
                onClick={() => setCategory(c)}
              >
                {c === "All finds" && <span>✳ </span>}
                {c}
              </button>
            ))}
          </div>
          <label className="sort-control">
            <FiSliders />
            <select
              aria-label="Sort products"
              value={sort}
              onChange={(e) => setSort(e.target.value)}
            >
              <option value="featured">Featured</option>
              <option value="low">Price: low to high</option>
              <option value="high">Price: high to low</option>
            </select>
          </label>
        </div>
        <div className="result-count">
          {products.length} {products.length === 1 ? "find" : "finds"}
          {searchQuery && ` for “${searchQuery}”`}
        </div>
        {isError ? (
          <div className="store-empty">
            <FiPackage />
            <h3>Our shelves are taking a moment.</h3>
            <p>We couldn't load the collection. Please try again.</p>
            <button onClick={refreshData}>
              Try again <FiArrowRight />
            </button>
          </div>
        ) : products.length === 0 ? (
          <div className="store-empty">
            <FiSearchFallback />
            <h3>
              {searchQuery || category !== "All finds"
                ? "No finds just yet."
                : "Good things are on their way."}
            </h3>
            <p>
              {searchQuery || category !== "All finds"
                ? "Try another search or explore a different category."
                : "Our collection is waiting for its first arrival."}
            </p>
            {category !== "All finds" && (
              <button onClick={() => setCategory("All finds")}>
                See all finds <FiArrowRight />
              </button>
            )}
          </div>
        ) : (
          <div className="product-grid">
            {products.map((p, i) => (
              <article className="find-card" key={p.id}>
                <Link
                  className={`find-image tone-${i % 4}`}
                  to={`/product/${p.id}`}
                >
                  <span className="product-label">
                    {p.productAvailable
                      ? p.category || "THE GOOD STUFF"
                      : "OUT OF STOCK"}
                  </span>
                  <span className="product-placeholder" aria-hidden="true">
                    <FiPackage />
                  </span>
                  <img
                    src={`${API_BASE_URL}/product/${p.id}/image`}
                    alt={p.name}
                    loading="lazy"
                    onError={(e) => {
                      e.currentTarget.style.display = "none";
                    }}
                  />
                  <span className="product-open">
                    <FiArrowUpRight />
                  </span>
                </Link>
                <div className="find-details">
                  <small>{p.brand}</small>
                  <Link to={`/product/${p.id}`}>
                    <h3>{p.name}</h3>
                  </Link>
                  <div className="find-bottom">
                    <strong>
                      {new Intl.NumberFormat("en-IN", {
                        style: "currency",
                        currency: "INR",
                        maximumFractionDigits: 0,
                      }).format(p.price)}
                    </strong>
                    <button
                      disabled={!p.productAvailable}
                      onClick={() => {
                        addToCart(p);
                        setAdded(p.id);
                        setTimeout(() => setAdded(null), 1800);
                      }}
                      aria-label={`Add ${p.name} to bag`}
                    >
                      {added === p.id ? (
                        <>
                          <FiCheck /> Added
                        </>
                      ) : (
                        <>
                          <FiPlus /> Add to bag
                        </>
                      )}
                    </button>
                  </div>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
      <section className="closing-note">
        <span>✳</span>
        <h2>Life’s in the little upgrades.</h2>
        <p>Make room for something good.</p>
        <a href="#collection">
          Find your everyday essential <FiArrowUpRight />
        </a>
      </section>
      <footer className="store-footer">
        <Link className="footer-wordmark" to="/">
          Everyday Edit
        </Link>
        <span>Good finds. Every day.</span>
        <small>© {new Date().getFullYear()} Everyday Edit</small>
        <Link to="/add_product">
          Manage products <FiArrowUpRight />
        </Link>
      </footer>
      <div className="cart-announcement" role="status">
        {added !== null ? "Added to your bag. Good choice!" : ""}
      </div>
    </main>
  );
}
function FiSearchFallback() {
  return <FiPackage />;
}
