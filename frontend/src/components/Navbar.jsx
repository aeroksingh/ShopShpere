import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const { itemCount } = useCart();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/products");
  }

  return (
    <header className="navbar">
      <div className="container navbar-inner">
        <NavLink to="/products" className="brand">
          Shop<span className="brand-dot">Sphere</span>
        </NavLink>

        <nav className="nav-links">
          <NavLink to="/products" className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}>
            Products
          </NavLink>

          {isAuthenticated && (
  <>
    <NavLink
      to="/cart"
      className={({ isActive }) =>
        `nav-link${isActive ? " active" : ""}`
      }
    >
      Cart
      {itemCount > 0 && (
        <span className="cart-badge">{itemCount}</span>
      )}
    </NavLink>

    <NavLink
      to="/orders"
      className={({ isActive }) =>
        `nav-link${isActive ? " active" : ""}`
      }
    >
      Orders
    </NavLink>

    {user?.role === "ROLE_ADMIN" && (
      <NavLink
        to="/admin"
        className={({ isActive }) =>
          `nav-link${isActive ? " active" : ""}`
        }
      >
        Admin
      </NavLink>
    )}
  </>
)}

          {isAuthenticated ? (
            <button className="btn btn-ghost btn-sm" onClick={handleLogout}>
              Log out{user?.fullName ? ` (${user.fullName.split(" ")[0]})` : ""}
            </button>
          ) : (
            <>
              <NavLink to="/login" className="nav-link">Log in</NavLink>
              <NavLink to="/register" className="btn btn-primary btn-sm">Sign up</NavLink>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
