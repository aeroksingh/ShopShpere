import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useCart } from "../context/CartContext";
import { checkout } from "../api/orders";
import { extractErrorMessage } from "../api/client";
import { EmptyState } from "../components/Loader";

export default function Checkout() {
  const { cart, refreshCart } = useCart();
  const navigate = useNavigate();
  const [shippingAddress, setShippingAddress] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const items = cart?.items || [];

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      const order = await checkout(shippingAddress);
      await refreshCart();

      navigate(`/payment/${order.orderId}`);
      } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  if (items.length === 0) {
    return (
      <div className="container page">
        <EmptyState
          title="Nothing to check out"
          subtitle="Your cart is empty."
          action={<Link to="/products" className="btn btn-primary" style={{ marginTop: 16 }}>Browse products</Link>}
        />
      </div>
    );
  }

  return (
    <div className="container page">
      <div className="page-header">
        <h1 className="page-title">Checkout</h1>
        <p className="page-subtitle">Confirm your shipping details.</p>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="grid-2">
        <form className="receipt" style={{ fontFamily: "var(--font-body)" }} onSubmit={handleSubmit}>
          <div className="field" style={{ marginBottom: 20 }}>
            <label htmlFor="shippingAddress">Shipping address</label>
            <input
              id="shippingAddress"
              required
              maxLength={300}
              placeholder="123 Main St, Springfield, IL"
              value={shippingAddress}
              onChange={(e) => setShippingAddress(e.target.value)}
            />
          </div>
          <button className="btn btn-primary btn-block" type="submit" disabled={submitting}>
            {submitting ? "Placing order…" : `Place order — $${Number(cart.grandTotal).toFixed(2)}`}
          </button>
        </form>

        <div className="receipt">
          <div className="receipt-header">Order summary</div>
          {items.map((item) => (
            <div className="receipt-row" key={item.cartItemId}>
              <span className="name">{item.productName} × {item.quantity}</span>
              <span>${Number(item.subtotal).toFixed(2)}</span>
            </div>
          ))}
          <div className="receipt-total">
            <span>Total</span>
            <span>${Number(cart.grandTotal).toFixed(2)}</span>
          </div>
        </div>
      </div>
    </div>
  );
}
