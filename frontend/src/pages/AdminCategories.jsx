import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import client from "../api/client";

const emptyForm = {
  name: "",
  description: "",
};

export default function AdminCategories() {
  const [categories, setCategories] = useState([]);

  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);

  const [form, setForm] = useState(emptyForm);

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState(null);

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // =========================
  // LOAD CATEGORIES
  // =========================

  const loadCategories = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await client.get("/categories");

      setCategories(response.data.data || []);
    } catch (err) {
      console.error(err);
      setError("Failed to load categories.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCategories();
  }, []);

  // =========================
  // FORM
  // =========================

  const handleChange = (e) => {
    const { name, value } = e.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  const openAddForm = () => {
    setEditingId(null);
    setForm(emptyForm);
    setError("");
    setSuccess("");
    setShowForm(true);
  };

  const openEditForm = (category) => {
    setEditingId(category.id);

    setForm({
      name: category.name || "",
      description: category.description || "",
    });

    setError("");
    setSuccess("");
    setShowForm(true);

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  };

  const closeForm = () => {
    setShowForm(false);
    setEditingId(null);
    setForm(emptyForm);
  };

  // =========================
  // CREATE / UPDATE
  // =========================

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      setSaving(true);
      setError("");
      setSuccess("");

      const payload = {
        name: form.name.trim(),
        description: form.description.trim(),
      };

      if (editingId) {
        await client.put(
          `/categories/${editingId}`,
          payload
        );

        setSuccess("Category updated successfully.");
      } else {
        await client.post(
          "/categories",
          payload
        );

        setSuccess("Category added successfully.");
      }

      setForm(emptyForm);
      setEditingId(null);
      setShowForm(false);

      await loadCategories();
    } catch (err) {
      console.error(err);

      setError(
        err.response?.data?.message ||
          `Failed to ${
            editingId ? "update" : "create"
          } category.`
      );
    } finally {
      setSaving(false);
    }
  };

  // =========================
  // DELETE
  // =========================

  const handleDelete = async (category) => {
    const confirmed = window.confirm(
      `Are you sure you want to delete "${category.name}"?`
    );

    if (!confirmed) {
      return;
    }

    try {
      setDeletingId(category.id);
      setError("");
      setSuccess("");

      await client.delete(
        `/categories/${category.id}`
      );

      setSuccess(
        `"${category.name}" deleted successfully.`
      );

      await loadCategories();
    } catch (err) {
      console.error(err);

      setError(
        err.response?.data?.message ||
          "Failed to delete category."
      );
    } finally {
      setDeletingId(null);
    }
  };

  // =========================
  // LOADING
  // =========================

  if (loading) {
    return (
      <main className="container">
        <h2>Loading categories...</h2>
      </main>
    );
  }

  // =========================
  // UI
  // =========================

  return (
    <main
      className="container"
      style={{ padding: "40px 0" }}
    >
      {/* HEADER */}

      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: "30px",
          gap: "20px",
        }}
      >
        <div>
          <h1>Manage Categories</h1>

          <p>
            Add, edit and remove product categories.
          </p>
        </div>

        <div>
          <Link
            to="/admin"
            className="btn"
            style={{ marginRight: "10px" }}
          >
            Back to Admin
          </Link>

          <button
            className="btn btn-primary"
            onClick={openAddForm}
          >
            + Add Category
          </button>
        </div>
      </div>

      {/* SUCCESS */}

      {success && (
        <div
          style={{
            padding: "12px 16px",
            marginBottom: "20px",
            borderRadius: "8px",
            background: "#e7f7ed",
            color: "#18733c",
          }}
        >
          {success}
        </div>
      )}

      {/* ERROR */}

      {error && (
        <div
          style={{
            padding: "12px 16px",
            marginBottom: "20px",
            borderRadius: "8px",
            background: "#ffe5e5",
            color: "#b00020",
          }}
        >
          {error}
        </div>
      )}

      {/* FORM */}

      {showForm && (
        <form
          onSubmit={handleSubmit}
          style={{
            border: "1px solid #ddd",
            borderRadius: "12px",
            padding: "25px",
            marginBottom: "30px",
          }}
        >
          <h2>
            {editingId
              ? "Edit Category"
              : "Add Category"}
          </h2>

          <div style={{ marginBottom: "15px" }}>
            <label>Category Name</label>

            <input
              type="text"
              name="name"
              value={form.name}
              onChange={handleChange}
              required
              maxLength={100}
              style={{
                width: "100%",
                padding: "10px",
                marginTop: "5px",
              }}
            />
          </div>

          <div style={{ marginBottom: "20px" }}>
            <label>Description</label>

            <textarea
              name="description"
              value={form.description}
              onChange={handleChange}
              rows="4"
              style={{
                width: "100%",
                padding: "10px",
                marginTop: "5px",
              }}
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            disabled={saving}
          >
            {saving
              ? "Saving..."
              : editingId
              ? "Update Category"
              : "Add Category"}
          </button>

          <button
            type="button"
            className="btn"
            onClick={closeForm}
            style={{ marginLeft: "10px" }}
          >
            Cancel
          </button>
        </form>
      )}

      {/* CATEGORY LIST */}

      {categories.length === 0 ? (
        <p>No categories found.</p>
      ) : (
        <div
          style={{
            display: "grid",
            gap: "15px",
          }}
        >
          {categories.map((category) => (
            <div
              key={category.id}
              style={{
                border: "1px solid #ddd",
                borderRadius: "10px",
                padding: "20px",
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                gap: "20px",
              }}
            >
              <div>
                <h3>{category.name}</h3>

                <p>
                  {category.description ||
                    "No description"}
                </p>
              </div>

              <div
                style={{
                  display: "flex",
                  gap: "10px",
                  flexShrink: 0,
                }}
              >
                <button
                  className="btn btn-primary"
                  onClick={() =>
                    openEditForm(category)
                  }
                >
                  Edit
                </button>

                <button
                  className="btn"
                  onClick={() =>
                    handleDelete(category)
                  }
                  disabled={
                    deletingId === category.id
                  }
                >
                  {deletingId === category.id
                    ? "Deleting..."
                    : "Delete"}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </main>
  );
}