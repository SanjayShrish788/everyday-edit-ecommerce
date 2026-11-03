import { useContext, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  FiArrowUpRight,
  FiSearch,
  FiShoppingBag,
  FiPlus,
} from "react-icons/fi";
import AppContext from "../Context/Context";

/** Renders navigation and search with the shared bag item count. */

export default function Navbar({ onSearch }) {
  const { cart } = useContext(AppContext);
  const [query, setQuery] = useState("");
  const navigate = useNavigate();
  return (
    <header className="store-header">
      <div className="announcement">
        A little upgrade. A lot more everyday.{" "}
        <span>
          Discover your next favorite <FiArrowUpRight />
        </span>
      </div>
      <nav className="store-nav" aria-label="Main navigation">
        <Link className="wordmark" to="/">
          Everyday Edit<small>GOOD FINDS. EVERY DAY.</small>
        </Link>
        <div className="nav-links">
          <Link to="/">Shop all</Link>
          <a href="/#collection">
            The collection <FiArrowUpRight />
          </a>
        </div>
        <form
          className="store-search"
          onSubmit={(e) => {
            e.preventDefault();
            navigate("/");
            document
              .getElementById("collection")
              ?.scrollIntoView({ behavior: "smooth" });
          }}
        >
          <FiSearch />
          <input
            aria-label="Search products"
            placeholder="Find something good…"
            value={query}
            onChange={(e) => {
              setQuery(e.target.value);
              onSearch(e.target.value);
              if (location.pathname !== "/") navigate("/");
            }}
          />
          <kbd>⌕</kbd>
        </form>
        <Link className="add-link" to="/add_product">
          <FiPlus /> Add product
        </Link>
        <Link
          className="bag-link"
          to="/cart"
          aria-label={`Shopping bag, ${cart.reduce((sum, p) => sum + p.quantity, 0)} items`}
        >
          <FiShoppingBag />
          <span>Bag</span>
          <b>{cart.reduce((sum, p) => sum + p.quantity, 0)}</b>
        </Link>
      </nav>
    </header>
  );
}
