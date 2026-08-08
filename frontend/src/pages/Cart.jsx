import { Link, useNavigate } from "react-router-dom";
import { useCart } from "../context/CartContext";
import { productPhotoUrl } from "../utils/image";
import { Loader, EmptyState } from "../components/Loader";
export default function Cart() {
  const { cart, loading, updateItem, removeItem } = useCart();
  const navigate = useNavigate();

  if (loading && !cart) return <div className="container page"><Loader label="Loading your cart…" /></div>;

  const items = cart?.items || [];

  return (
    <div className="container page">
      <div className="page-header">
        <h1 className="page-title">Your cart</h1>
        <p className="page-subtitle">{items.length} item{items.length !== 1 ? "s" : ""}</p>
      </div>

      {items.length === 0 ? (
        <EmptyState
          title="Your cart is empty"
          subtitle="Add something from the catalog to get started."
          action={<Link to="/products" className="btn btn-primary" style={{ marginTop: 16 }}>Browse products</Link>}
        />
      ) : (
        <div className="grid-2">
          <div className="receipt" style={{ fontFamily: "var(--font-body)" }}>
            {items.map((item) => (
              <div className="cart-line" key={item.cartItemId}>
                <img
                  className="cart-line-photo"
                  src={productPhotoUrl({ id: item.productId })}
                  alt={item.productName}
                />
                <div className="cart-line-info">
                  <div className="cart-line-name">{item.productName}</div>
                  <div className="cart-line-price">${Number(item.unitPrice).toFixed(2)} each</div>
                </div>
                <div className="cart-line-controls">
                  <div className="stepper">
                    <button onClick={() => updateItem(item.cartItemId, Math.max(1, item.quantity - 1))}>–</button>
                    <span>{item.quantity}</span>
                    <button onClick={() => updateItem(item.cartItemId, item.quantity + 1)}>+</button>
                  </div>
                  <button className="btn btn-danger-ghost btn-sm" onClick={() => removeItem(item.cartItemId)}>
                    Remove
                  </button>
                </div>
              </div>
            ))}
          </div>

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
            <button
              className="btn btn-primary btn-block"
              style={{ marginTop: 18 }}
              onClick={() => navigate("/checkout")}
            >
              Proceed to checkout
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
