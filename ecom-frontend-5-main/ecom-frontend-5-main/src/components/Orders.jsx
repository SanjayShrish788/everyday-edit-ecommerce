import { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import {
  FiArrowRight,
  FiCheckCircle,
  FiPackage,
  FiRefreshCw,
} from "react-icons/fi";
import API from "../axios";
const money = (value) =>
  new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" }).format(
    value,
  );
/** Displays persisted OrderResponse DTOs, including customer identity and server-assigned dates. */
export default function Orders() {
  const { state } = useLocation();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  /** Reloads database order history; the API currently returns store-wide orders. */
  const loadOrders = async () => {
    setLoading(true);
    setError("");
    try {
      const { data } = await API.get("/orders");
      setOrders(data.slice().reverse());
    } catch {
      setError("We couldn’t load order history. Please try again.");
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => {
    loadOrders();
  }, []);
  return (
    <main className="commerce-page">
      <div className="eyebrow">YOUR FINDS, ON RECORD</div>
      <div className="commerce-heading">
        <h1>
          Orders<span>.</span>
        </h1>
        <button
          className="orders-refresh"
          disabled={loading}
          onClick={loadOrders}
        >
          <FiRefreshCw /> Refresh
        </button>
      </div>
      {state?.placedOrderId && (
        <div className="order-success" role="status">
          <FiCheckCircle />
          <div>
            <strong>Your order is placed.</strong>
            <span>
              Reference {state.placedOrderId}. Your details and items have been
              saved.
            </span>
          </div>
        </div>
      )}
      <p className="orders-intro">
        Order references, customer details, and dates from the store’s saved
        order history.
      </p>
      {loading ? (
        <p role="status">Loading orders…</p>
      ) : error ? (
        <div className="commerce-message" role="alert">
          {error}
          <button onClick={loadOrders}>Try again</button>
        </div>
      ) : !orders.length ? (
        <div className="store-empty">
          <FiPackage />
          <h3>Your first good find starts here.</h3>
          <p>Place an order from your bag and it will appear here.</p>
          <Link className="shop-cta" to="/">
            Explore the collection <FiArrowRight />
          </Link>
        </div>
      ) : (
        <div className="orders-list">
          {orders.map((order) => (
            <article className="order-card" key={order.orderId}>
              <div className="order-card-heading">
                <div>
                  <small>ORDER REFERENCE</small>
                  <h2>{order.orderId}</h2>
                </div>
                <span className="order-status">{order.status}</span>
              </div>
              <dl className="order-meta">
                <div>
                  <dt>Customer</dt>
                  <dd>{order.customerName}</dd>
                </div>
                <div>
                  <dt>Email</dt>
                  <dd>{order.email}</dd>
                </div>
                <div>
                  <dt>Order date</dt>
                  <dd>
                    {order.orderDate
                      ? new Date(
                          `${order.orderDate}T00:00:00`,
                        ).toLocaleDateString("en-IN", {
                          day: "numeric",
                          month: "long",
                          year: "numeric",
                        })
                      : "Not recorded"}
                  </dd>
                </div>
              </dl>
              <div className="order-table-wrap">
                <table className="order-table">
                  <thead>
                    <tr>
                      <th>Item</th>
                      <th>Quantity</th>
                      <th>Line total</th>
                    </tr>
                  </thead>
                  <tbody>
                    {order.items.map((item, index) => (
                      <tr key={index}>
                        <td>{item.productName}</td>
                        <td>{item.quantity}</td>
                        <td>{money(item.totalPrice)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <div className="order-card-total">
                <span>Order total</span>
                <strong>
                  {money(
                    order.items.reduce(
                      (sum, item) => sum + Number(item.totalPrice),
                      0,
                    ),
                  )}
                </strong>
              </div>
            </article>
          ))}
        </div>
      )}
    </main>
  );
}
