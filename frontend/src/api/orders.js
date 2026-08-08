import client, { unwrap } from "./client";

export function checkout(shippingAddress) {
  return client.post("/orders/checkout", { shippingAddress }).then(unwrap);
}

export function getMyOrders({ page = 0, size = 10 } = {}) {
  return client.get("/orders", { params: { page, size } }).then(unwrap);
}

export function getOrder(id) {
  return client.get(`/orders/${id}`).then(unwrap);
}
