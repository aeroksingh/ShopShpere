import { useEffect, useState } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import { getOrder } from "../api/orders";
import { processPayment } from "../api/payments";
import { extractErrorMessage } from "../api/client";
import { Loader } from "../components/Loader";

export default function Payment() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [order, setOrder] = useState(null);
    const [loading, setLoading] = useState(true);
    const [paying, setPaying] = useState(false);
    const [error, setError] = useState("");
    const [paymentMethod, setPaymentMethod] = useState("UPI");

    useEffect(() => {
        async function loadOrder() {
            try {
                setLoading(true);
                setError("");

                const data = await getOrder(id);
                setOrder(data);
            } catch (err) {
                console.error(err);
                setError(extractErrorMessage(err));
            } finally {
                setLoading(false);
            }
        }

        loadOrder();
    }, [id]);

    async function handlePayment() {
        try {
            setPaying(true);
            setError("");

            await processPayment(id, true);

            navigate(`/orders/${id}`, {
                state: {
                    paymentSuccess: true,
                },
            });
        } catch (err) {
            console.error(err);
            setError(extractErrorMessage(err));
        } finally {
            setPaying(false);
        }
    }

    if (loading) {
        return <Loader />;
    }

    if (!order) {
        return (
            <main className="container page">
                <div className="alert alert-error">
                    {error || "Order not found."}
                </div>

                <Link to="/orders" className="btn btn-ghost">
                    ← Back to orders
                </Link>
            </main>
        );
    }

    return (
        <main
            className="container page"
            style={{ maxWidth: 720 }}
        >
            <div className="page-header">
                <h1 className="page-title">
                    Complete your payment
                </h1>

                <p className="page-subtitle">
                    Order #{order.orderId}
                </p>
            </div>

            {error && (
                <div className="alert alert-error">
                    {error}
                </div>
            )}

            <div
                className="grid-2"
                style={{ alignItems: "start" }}
            >
                {/* PAYMENT */}
                <div className="receipt">
                    <div className="receipt-header">
                        Payment method
                    </div>

                    <div
                        style={{
                            display: "grid",
                            gap: 12,
                            marginTop: 20,
                        }}
                    >
                        <label
                            style={{
                                border: "1px solid #ddd",
                                borderRadius: 8,
                                padding: 14,
                                cursor: "pointer",
                            }}
                        >
                            <input
                                type="radio"
                                name="paymentMethod"
                                value="UPI"
                                checked={paymentMethod === "UPI"}
                                onChange={(e) =>
                                    setPaymentMethod(e.target.value)
                                }
                            />

                            <span style={{ marginLeft: 10 }}>
                                UPI
                            </span>
                        </label>

                        <label
                            style={{
                                border: "1px solid #ddd",
                                borderRadius: 8,
                                padding: 14,
                                cursor: "pointer",
                            }}
                        >
                            <input
                                type="radio"
                                name="paymentMethod"
                                value="CARD"
                                checked={paymentMethod === "CARD"}
                                onChange={(e) =>
                                    setPaymentMethod(e.target.value)
                                }
                            />

                            <span style={{ marginLeft: 10 }}>
                                Credit / Debit Card
                            </span>
                        </label>

                        <label
                            style={{
                                border: "1px solid #ddd",
                                borderRadius: 8,
                                padding: 14,
                                cursor: "pointer",
                            }}
                        >
                            <input
                                type="radio"
                                name="paymentMethod"
                                value="NET_BANKING"
                                checked={
                                    paymentMethod === "NET_BANKING"
                                }
                                onChange={(e) =>
                                    setPaymentMethod(e.target.value)
                                }
                            />

                            <span style={{ marginLeft: 10 }}>
                                Net Banking
                            </span>
                        </label>
                    </div>

                    <div
                        style={{
                            marginTop: 24,
                            padding: 14,
                            background: "#fff8e8",
                            borderRadius: 8,
                            fontSize: 13,
                            color: "#795548",
                        }}
                    >
                        <strong>Demo Payment</strong>
                        <br />
                        This is a simulated payment for the
                        ShopSphere project. No real money will be
                        charged.
                    </div>

                    <button
                        className="btn btn-primary btn-block"
                        style={{ marginTop: 20 }}
                        onClick={handlePayment}
                        disabled={paying}
                    >
                        {paying
                            ? "Processing payment..."
                            : `Pay ₹${Number(
                                  order.totalAmount
                              ).toFixed(2)}`}
                    </button>
                    <button
                    className="btn btn-ghost btn-block"
                    style={{ marginTop: 10 }}
                    onClick={() => handlePayment(false)}
                    disabled={paying}
                >
                    Simulate Failed Payment
                </button>
                </div>

                {/* ORDER SUMMARY */}
                <div className="receipt">
                    <div className="receipt-header">
                        Order summary
                    </div>

                    {order.items?.map((item, index) => (
                        <div
                            className="receipt-row"
                            key={index}
                        >
                            <span>
                                <span className="name">
                                    {item.productName}
                                </span>

                                <div className="meta">
                                    {item.quantity} × ₹
                                    {Number(
                                        item.priceAtPurchase
                                    ).toFixed(2)}
                                </div>
                            </span>

                            <span>
                                ₹
                                {Number(
                                    item.subtotal
                                ).toFixed(2)}
                            </span>
                        </div>
                    ))}

                    <div className="receipt-total">
                        <span>Total</span>

                        <span>
                            ₹
                            {Number(
                                order.totalAmount
                            ).toFixed(2)}
                        </span>
                    </div>
                </div>
            </div>
        </main>
    );
}