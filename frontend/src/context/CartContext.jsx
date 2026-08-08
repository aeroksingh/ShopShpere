import { createContext, useCallback, useContext, useEffect, useState } from "react";
import * as cartApi from "../api/cart";
import { useAuth } from "./AuthContext";

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const { isAuthenticated } = useAuth();
  const [cart, setCart] = useState(null); // { cartId, items: [], grandTotal }
  const [loading, setLoading] = useState(false);

  const refreshCart = useCallback(async () => {
    if (!isAuthenticated) {
      setCart(null);
      return;
    }
    setLoading(true);

    try {
    const data = await cartApi.getCart();
    setCart(data);
} catch (error) {
    console.error("Failed to load cart:", error);
    setCart(null);
} finally {
    setLoading(false);
}
  }, [isAuthenticated]);

  useEffect(() => {
    refreshCart();
  }, [refreshCart]);

  async function addItem(productId, quantity) {
    const data = await cartApi.addToCart(productId, quantity);
    setCart(data);
  }

  async function updateItem(cartItemId, quantity) {
    const data = await cartApi.updateCartItem(cartItemId, quantity);
    setCart(data);
  }

  async function removeItem(cartItemId) {
    const data = await cartApi.removeCartItem(cartItemId);
    setCart(data);
  }

  async function clear() {
    await cartApi.clearCart();
    setCart((prev) => (prev ? { ...prev, items: [], grandTotal: 0 } : prev));
  }

  const itemCount = cart?.items?.reduce((sum, i) => sum + i.quantity, 0) || 0;

  return (
    <CartContext.Provider value={{ cart, loading, itemCount, refreshCart, addItem, updateItem, removeItem, clear }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error("useCart must be used within a CartProvider");
  return ctx;
}
