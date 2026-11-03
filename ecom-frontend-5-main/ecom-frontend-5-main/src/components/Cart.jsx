import { useContext, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  FiArrowRight,
  FiMinus,
  FiPlus,
  FiShoppingBag,
  FiTrash2,
} from "react-icons/fi";
import API, { API_BASE_URL } from "../axios";
import AppContext from "../Context/Context";
import CheckoutPopup from "./CheckoutPopup";

const money = (value) =>
  new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" }).format(
    value,
  );

/** Uses current catalog prices and submits one OrderRequest instead of editing products. */
export default function Cart() {
  const { cart, removeFromCart, clearCart, setCartQuantity, refreshData } =
    useContext(AppContext);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [showModal, setShowModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [checkoutError, setCheckoutError] = useState("");
  const navigate = useNavigate();

  /** Refresh stock and price from the API; localStorage only supplies IDs and quantities. */
  useEffect(() => {
    let active = true;
    API.get("/products")
      .then(({ data }) => {
        if (active) setProducts(data);
      })
      .catch(() => {
        if (active)
          setLoadError(
            "We couldn’t load current prices and stock. Refresh to try again.",
          );
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  const items = cart.map((line) => {
    const product = products.find((p) => p.id === line.id);
    return { ...line, ...product, quantity: line.quantity, missing: !product };
  });
  const invalid = items.some(
    (p) => p.missing || !p.productAvailable || p.stockQuantity < p.quantity,
  );
  const total = items.reduce((sum, p) => sum + Number(p.price) * p.quantity, 0);

  /** Matches OrderRequest: customerName, email, and productId/quantity pairs only. */
  const handleCheckout = async ({ customerName, email }) => {
    if (submitting) return;
    setSubmitting(true);
    setCheckoutError("");
    try {
      const { data: order } = await API.post("/orders/place", {
        customerName: customerName.trim(),
        email: email.trim(),
        items: items.map((p) => ({ productId: p.id, quantity: p.quantity })),
      });
      // Only clear the bag after the transaction succeeds; keep it intact on any failure.
      clearCart();
      refreshData();
      navigate("/orders", { state: { placedOrderId: order.orderId } });
    } catch (error) {
      const body = error.response?.data;
      setCheckoutError(
        typeof body === "string"
          ? body
          : body?.message ||
              "Your order could not be placed. Check available stock and try again. Your bag has been kept.",
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="commerce-page">
      <div className="eyebrow">YOUR EVERYDAY FINDS</div>
      <div className="commerce-heading">
        <h1>
          Shopping bag<span>.</span>
        </h1>
        <Link to="/">
          Continue shopping <FiArrowRight />
        </Link>
      </div>
      {loading ? (
        <p role="status">Loading your bag…</p>
      ) : loadError ? (
        <div className="commerce-message" role="alert">
          {loadError}
        </div>
      ) : !items.length ? (
        <div className="store-empty">
          <FiShoppingBag />
          <h3>Your next favorite is waiting.</h3>
          <p>Add a little everyday good to your bag.</p>
          <Link className="shop-cta" to="/">
            Explore the collection <FiArrowRight />
          </Link>
        </div>
      ) : (
        <div className="bag-layout">
          <div className="bag-lines">
            {items.map((item) => (
              <article className="bag-line" key={item.id}>
                <img
                  src={`${API_BASE_URL}/product/${item.id}/image`}
                  alt={item.name}
                />
                <div className="bag-product">
                  <small>{item.brand}</small>
                  <Link to={`/product/${item.id}`}>
                    <h2>{item.name}</h2>
                  </Link>
                  <span>{money(item.price)} each</span>
                  {(item.missing ||
                    !item.productAvailable ||
                    item.stockQuantity < item.quantity) && (
                    <p className="commerce-error">
                      {item.missing
                        ? "This product is no longer available. Remove it to continue."
                        : `Only ${item.stockQuantity} available. Adjust your bag to continue.`}
                    </p>
                  )}
                </div>
                <div className="bag-quantity">
                  <button
                    aria-label={`Decrease ${item.name}`}
                    disabled={item.quantity <= 1 || submitting}
                    onClick={() => setCartQuantity(item.id, item.quantity - 1)}
                  >
                    <FiMinus />
                  </button>
                  <span>{item.quantity}</span>
                  <button
                    aria-label={`Increase ${item.name}`}
                    disabled={
                      item.missing ||
                      item.quantity >= item.stockQuantity ||
                      submitting
                    }
                    onClick={() => setCartQuantity(item.id, item.quantity + 1)}
                  >
                    <FiPlus />
                  </button>
                </div>
                <strong>{money(item.price * item.quantity)}</strong>
                <button
                  className="bag-remove"
                  aria-label={`Remove ${item.name}`}
                  disabled={submitting}
                  onClick={() => removeFromCart(item.id)}
                >
                  <FiTrash2 />
                </button>
              </article>
            ))}
          </div>
          <aside className="bag-summary">
            <div className="eyebrow">THE GOOD STUFF, TOGETHER</div>
            <h2>Order summary</h2>
            <div>
              <span>Items</span>
              <span>{items.reduce((sum, p) => sum + p.quantity, 0)}</span>
            </div>
            <div className="bag-total">
              <span>Total</span>
              <strong>{money(total)}</strong>
            </div>
            <p>
              Name and email are collected at checkout. Your order date and
              reference are assigned when the order is saved.
            </p>
            <button
              className="shop-cta"
              disabled={invalid || submitting}
              onClick={() => {
                setCheckoutError("");
                setShowModal(true);
              }}
            >
              Checkout <FiArrowRight />
            </button>
            <small>No payment is collected in this demo.</small>
          </aside>
        </div>
      )}
      <CheckoutPopup
        show={showModal}
        handleClose={() => {
          if (!submitting) setShowModal(false);
        }}
        cartItems={items}
        totalPrice={total}
        handleCheckout={handleCheckout}
        submitting={submitting}
        error={checkoutError}
      />
    </main>
  );
}
