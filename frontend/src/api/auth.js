import client, { unwrap } from "./client";

export function register({ fullName, email, password }) {
  return client.post("/auth/register", { fullName, email, password }).then(unwrap);
}

export function login({ email, password }) {
  return client.post("/auth/login", { email, password }).then(unwrap);
}
