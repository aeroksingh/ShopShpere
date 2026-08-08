import { useState } from "react";
import { productPhotoUrl } from "../utils/image";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";

export default function ProductCard({ product }) {
  const { isAuthenticated } = useAuth();
  const { addItem } = useCart();
  const [qty, setQty] = useState(1);
  const [adding, setAdding] = useState(false);
  const [justAdded, setJustAdded] = useState(false);

  const lowStock = product.stockQuantity > 0 && product.stockQuantity <= 5;
  const outOfStock = product.stockQuantity === 0;

  async function handleAdd() {
    setAdding(true);
    try {
      await addItem(product.id, qty);
      setJustAdded(true);
      setTimeout(() => setJustAdded(false), 1500);
    } finally {
      setAdding(false);
    }
  }

  return (
    <div className="product-card">
      <div className="product-photo-wrap">
        <img className="product-photo" src={productPhotoUrl(product)} alt={product.name} loading="lazy" />
        <div className="price-tag">${Number(product.price).toFixed(2)}</div>
      </div>

      <div className="product-body">
        <div className="product-category">{product.categoryName}</div>
        <div className="product-name">{product.name}</div>
        <div className={`product-stock${outOfStock ? " low" : lowStock ? " low" : ""}`}>
          {outOfStock ? "Out of stock" : lowStock ? `Only ${product.stockQuantity} left` : `${product.stockQuantity} in stock`}
        </div>

        {isAuthenticated && !outOfStock && (
          <div className="product-actions">
            <input
              type="number"
              min="1"
              max={product.stockQuantity}
              value={qty}
              className="qty-input"
              onChange={(e) => setQty(Math.max(1, Math.min(product.stockQuantity, Number(e.target.value))))}
            />
            <button className="btn btn-primary btn-sm" onClick={handleAdd} disabled={adding} style={{ flex: 1 }}>
              {justAdded ? "Added" : adding ? "Adding…" : "Add to cart"}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
