import client, { unwrap } from "./client";

export function getCart() {
  return client.get("/cart").then(unwrap);
}

export function addToCart(productId, quantity) {
  return client.post("/cart/items", { productId, quantity }).then(unwrap);
}

export function updateCartItem(cartItemId, quantity) {
  return client.patch(`/cart/items/${cartItemId}`, null, { params: { quantity } }).then(unwrap);
}

export function removeCartItem(cartItemId) {
  return client.delete(`/cart/items/${cartItemId}`).then(unwrap);
}

export function clearCart() {
  return client.delete("/cart").then(unwrap);
}
