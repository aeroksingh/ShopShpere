import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getMyOrders } from "../api/orders";
import { Loader, EmptyState } from "../components/Loader";

const STATUS_CLASS = {
  PENDING: "badge-pending",
  CONFIRMED: "badge-confirmed",
  SHIPPED: "badge-shipped",
  DELIVERED: "badge-delivered",
  CANCELLED: "badge-cancelled",
};

export default function Orders() {
  const [orders, setOrders] = useState(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    getMyOrders({ page }).then(setOrders).finally(() => setLoading(false));
  }, [page]);

  return (
    <div className="container page">
      <div className="page-header">
        <h1 className="page-title">Your orders</h1>
        <p className="page-subtitle">Order history and status.</p>
      </div>

      {loading && <Loader label="Loading orders…" />}

      {!loading && orders?.content?.length === 0 && (
        <EmptyState
          title="No orders yet"
          subtitle="Once you check out, your orders will show up here."
          action={<Link to="/products" className="btn btn-primary" style={{ marginTop: 16 }}>Browse products</Link>}
        />
      )}

      {!loading && orders?.content?.map((order) => (
        <Link to={`/orders/${order.orderId}`} key={order.orderId} className="order-card" style={{ display: "block" }}>
          <div className="order-card-top">
            <span className="order-card-id">Order #{order.orderId}</span>
            <span className={`badge ${STATUS_CLASS[order.status] || "badge-pending"}`}>{order.status}</span>
          </div>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end" }}>
            <span className="order-card-date">{new Date(order.createdAt).toLocaleDateString(undefined, { year: "numeric", month: "short", day: "numeric" })}</span>
            <span className="order-card-total">${Number(order.totalAmount).toFixed(2)}</span>
          </div>
        </Link>
      ))}

      {!loading && orders?.totalPages > 1 && (
        <div style={{ display: "flex", justifyContent: "center", gap: 10, marginTop: 24 }}>
          <button className="btn btn-ghost btn-sm" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>Previous</button>
          <span style={{ alignSelf: "center", fontFamily: "var(--font-mono)", fontSize: 13, color: "var(--ink-soft)" }}>
            Page {page + 1} of {orders.totalPages}
          </span>
          <button className="btn btn-ghost btn-sm" disabled={page + 1 >= orders.totalPages} onClick={() => setPage((p) => p + 1)}>Next</button>
        </div>
      )}
    </div>
  );
}
