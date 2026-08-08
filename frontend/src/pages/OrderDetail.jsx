import { useEffect, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";

import { getOrder } from "../api/orders";
import { Loader } from "../components/Loader";

const STATUS_CLASS = {
    PENDING: "badge-pending",
    CONFIRMED: "badge-confirmed",
    SHIPPED: "badge-shipped",
    DELIVERED: "badge-delivered",
    CANCELLED: "badge-cancelled",
};

const PAYMENT_STATUS_CLASS = {
    PENDING: "badge-pending",
    SUCCESS: "badge-confirmed",
    FAILED: "badge-cancelled",
};

export default function OrderDetail() {
    const { id } = useParams();
    const location = useLocation();

    const [order, setOrder] = useState(null);
    const [loading, setLoading] = useState(true);

    const justPlaced = location.state?.justPlaced;
    const paymentSuccess = location.state?.paymentSuccess;

    useEffect(() => {
        setLoading(true);

        getOrder(id)
            .then(setOrder)
            .finally(() => setLoading(false));
    }, [id]);

    if (loading) {
        return <Loader />;
    }

    if (!order) {
        return (
            <div className="container page">
                <div className="alert alert-error">
                    Order not found.
                </div>

                <Link
                    to="/orders"
                    className="btn btn-ghost"
                    style={{ marginTop: 20 }}
                >
                    ← Back to orders
                </Link>
            </div>
        );
    }

    return (
        <div
            className="container page"
            style={{ maxWidth: 560 }}
        >
            {/* SUCCESS MESSAGE */}
            {(justPlaced || paymentSuccess) && (
                <div className="alert alert-success">
                    {paymentSuccess
                        ? "Payment successful! Your order has been confirmed."
                        : "Order placed! We've got it — thanks for shopping with us."}
                </div>
            )}

            {/* PAGE HEADER */}
            <div className="page-header">
                <h1 className="page-title">
                    Order #{order.orderId}
                </h1>

                <p className="page-subtitle">
                    Placed{" "}
                    {new Date(order.createdAt).toLocaleString(
                        undefined,
                        {
                            dateStyle: "medium",
                            timeStyle: "short",
                        }
                    )}
                </p>
            </div>

            {/* RECEIPT */}
            <div className="receipt">

                {/* RECEIPT HEADER */}
                <div
                    style={{
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "center",
                        gap: 10,
                        marginBottom: 4,
                        flexWrap: "wrap",
                    }}
                >
                    <div
                        className="receipt-header"
                        style={{ marginBottom: 0 }}
                    >
                        Receipt
                    </div>

                    <div
                        style={{
                            display: "flex",
                            gap: 8,
                            flexWrap: "wrap",
                        }}
                    >
                        {/* ORDER STATUS */}
                        <span
                            className={`badge ${
                                STATUS_CLASS[order.status] ||
                                "badge-pending"
                            }`}
                        >
                            {order.status}
                        </span>

                        {/* PAYMENT STATUS */}
                        <span
                            className={`badge ${
                                PAYMENT_STATUS_CLASS[
                                    order.paymentStatus
                                ] || "badge-pending"
                            }`}
                        >
                            Payment:{" "}
                            {order.paymentStatus || "PENDING"}
                        </span>
                    </div>
                </div>

                {/* SHIPPING ADDRESS */}
                <div
                    style={{
                        fontSize: 12,
                        color: "var(--ink-faint)",
                        marginBottom: 14,
                    }}
                >
                    Shipping to: {order.shippingAddress}
                </div>

                {/* ORDER ITEMS */}
                {order.items.map((item, index) => (
                    <div
                        className="receipt-row"
                        key={item.cartItemId || index}
                    >
                        <span>
                            <span className="name">
                                {item.productName}
                            </span>

                            <div className="meta">
                                {item.quantity} × $
                                {Number(
                                    item.priceAtPurchase
                                ).toFixed(2)}
                            </div>
                        </span>

                        <span>
                            $
                            {Number(item.subtotal).toFixed(2)}
                        </span>
                    </div>
                ))}

                {/* TOTAL */}
                <div className="receipt-total">
                    <span>Total</span>

                    <span>
                        $
                        {Number(
                            order.totalAmount
                        ).toFixed(2)}
                    </span>
                </div>
            </div>

            {/* RECEIPT TEAR */}
            <div className="receipt-tear" />

            {/* BACK BUTTON */}
            <Link
                to="/orders"
                className="btn btn-ghost"
                style={{ marginTop: 28 }}
            >
                ← Back to orders
            </Link>
        </div>
    );
}