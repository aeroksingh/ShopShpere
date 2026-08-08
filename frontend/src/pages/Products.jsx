import { useEffect, useState } from "react";
import { getProducts, getCategories } from "../api/products";
import ProductCard from "../components/ProductCard";
import { Loader, EmptyState } from "../components/Loader";

export default function Products() {
  const [products, setProducts] = useState(null);
  const [categories, setCategories] = useState([]);
  const [search, setSearch] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getCategories().then(setCategories).catch(() => setCategories([]));
  }, []);

  useEffect(() => {
    setLoading(true);
    const timeout = setTimeout(() => {
      getProducts({ search, categoryId: categoryId || undefined, page })
        .then(setProducts)
        .finally(() => setLoading(false));
    }, 250); // small debounce so typing doesn't fire a request per keystroke

    return () => clearTimeout(timeout);
  }, [search, categoryId, page]);

  return (
    <div className="container page">
      <div className="page-header">
        <h1 className="page-title">All products</h1>
        <p className="page-subtitle">Browse the full catalog.</p>
      </div>

      <div className="toolbar">
        <input
          className="search-input"
          placeholder="Search products…"
          value={search}
          onChange={(e) => { setSearch(e.target.value); setPage(0); }}
        />
        <select
          className="field"
          style={{ padding: "10px 12px", border: "1px solid var(--line-strong)", borderRadius: "3px" }}
          value={categoryId}
          onChange={(e) => { setCategoryId(e.target.value); setPage(0); }}
        >
          <option value="">All categories</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>
      </div>

      {loading && <Loader label="Loading products…" />}

      {!loading && products?.content?.length === 0 && (
        <EmptyState title="No products found" subtitle="Try a different search or category." />
      )}

      {!loading && products?.content?.length > 0 && (
        <>
          <div className="product-grid">
            {products.content.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>

          {products.totalPages > 1 && (
            <div style={{ display: "flex", justifyContent: "center", gap: 10, marginTop: 32 }}>
              <button className="btn btn-ghost btn-sm" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
                Previous
              </button>
              <span style={{ alignSelf: "center", fontFamily: "var(--font-mono)", fontSize: 13, color: "var(--ink-soft)" }}>
                Page {page + 1} of {products.totalPages}
              </span>
              <button
                className="btn btn-ghost btn-sm"
                disabled={page + 1 >= products.totalPages}
                onClick={() => setPage((p) => p + 1)}
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
