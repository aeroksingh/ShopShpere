# ShopSphere Frontend

React (Create React App) storefront for the ShopSphere Spring Boot backend —
login/register, product catalog with search & category filtering, cart,
checkout, and order history.

## Design

- **Palette**: warm off-white background, signal orange accent, warm charcoal ink
- **Type**: Fraunces (display) + Inter (body) + JetBrains Mono (prices, order numbers)
- **Signature details**: die-cut price-tag corner on product cards; cart/checkout/order
  totals styled like a printed receipt (dashed line-items, torn paper edge)
- Product photos are royalty-free stock photography (Picsum, Unsplash-licensed),
  seeded by product id so each product consistently shows the same photo

## Setup

```bash
npm install
cp .env.example .env   # adjust REACT_APP_API_URL if your backend isn't on localhost:8080
npm start
```

Runs at `http://localhost:3000`. Make sure the Spring Boot backend (Phase 1) is running
at `http://localhost:8080` first — the backend's `SecurityConfig` already has CORS
configured to accept requests from any origin in dev, so no extra setup is needed there.

## What's implemented

- **Auth**: register / login, JWT stored in `localStorage`, attached to every API call
  via an axios request interceptor (`src/api/client.js`)
- **Products**: paginated grid, debounced search, category filter (all public — no login required to browse)
- **Cart**: add/update/remove items, live totals (protected route — requires login)
- **Checkout**: shipping address form → calls `/api/orders/checkout` → redirects to the new order
- **Orders**: paginated order history + a detail/receipt view per order

## Structure

```
src/
├── api/          # axios client + one file per backend resource
├── context/      # AuthContext, CartContext (React Context, no external state library)
├── components/   # Navbar, ProductCard, PrivateRoute, Loader/EmptyState
├── pages/        # one component per route
└── index.css     # design tokens + all component styles (no CSS framework)
```

## Notes

- If the backend rejects a request with 401 (expired/invalid token), the axios
  interceptor clears the stored token automatically — the next protected action
  will redirect to `/login`.
- There's no "my token expired, please log back in" toast yet — a reasonable
  next step if you want to polish this further.
