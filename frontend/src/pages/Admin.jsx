import { Link } from "react-router-dom";

export default function Admin() {
  return (
    <main className="container" style={{ padding: "40px 0" }}>
      <h1>ShopSphere Admin</h1>

      <p style={{ marginBottom: "30px" }}>
        Manage your store from here.
      </p>

      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
          gap: "20px",
        }}
      >
        <Link to="/admin/products" className="btn btn-primary">
          Manage Products
        </Link>

        <Link to="/admin/categories" className="btn btn-primary">
          Manage Categories
        </Link>
      </div>
    </main>
  );
}