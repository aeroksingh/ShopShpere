import client, { unwrap } from "./client";

export function getProducts({ search, categoryId, page = 0, size = 20 } = {}) {
  return client
    .get("/products", { params: { search: search || undefined, categoryId: categoryId || undefined, page, size } })
    .then(unwrap);
}

export function getProduct(id) {
  return client.get(`/products/${id}`).then(unwrap);
}

export function getCategories() {
  return client.get("/categories").then(unwrap);
}
