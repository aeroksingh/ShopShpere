import axios from "axios";

// Points at the Spring Boot backend from Phase 1. Override with a .env file
// (REACT_APP_API_URL=...) if the backend runs somewhere other than localhost:8080.
const API_URL = process.env.REACT_APP_API_URL || "http://localhost:8080/api";

const client = axios.create({
  baseURL: API_URL,
  headers: { "Content-Type": "application/json" },
});

// Attach the JWT to every outgoing request, if we have one.
client.interceptors.request.use((config) => {
  const token = localStorage.getItem("shopsphere_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// If the backend ever says "your token is invalid/expired" (401), clear it
// so the app doesn't keep sending a dead token on every request.
client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("shopsphere_token");
      localStorage.removeItem("shopsphere_user");
    }
    return Promise.reject(error);
  }
);

// Every backend response is wrapped as { success, message, data }.
// This helper unwraps it so the rest of the app just deals with plain data.
export function unwrap(response) {
  return response.data.data;
}

// Extracts a human-readable message from our GlobalExceptionHandler's error shape.
export function extractErrorMessage(error) {
  const data = error?.response?.data;
  if (!data) return "Something went wrong. Please try again.";
  if (data.fieldErrors) {
    return Object.values(data.fieldErrors)[0] || data.message;
  }
  return data.message || "Something went wrong. Please try again.";
}

export default client;
