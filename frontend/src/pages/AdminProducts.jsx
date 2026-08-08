    import { useEffect, useState } from "react";
    import { Link } from "react-router-dom";
    import client from "../api/client";

    const emptyForm = {
    name: "",
    description: "",
    price: "",
    stockQuantity: "",
    categoryId: "",
    imageUrl: "",
    };

    export default function AdminProducts() {
    const [products, setProducts] = useState([]);
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
    // LOAD PRODUCTS + CATEGORIES
    // =========================

    const loadData = async () => {
        try {
        setLoading(true);
        setError("");

        const [productsResponse, categoriesResponse] =
            await Promise.all([
            client.get("/products"),
            client.get("/categories"),
            ]);

        // Products API returns a paginated response
        setProducts(
            productsResponse.data?.data?.content || []
        );

        // Categories API returns an array
        setCategories(
            categoriesResponse.data?.data || []
        );
        } catch (err) {
        console.error("Failed to load data:", err);

        setError(
            err.response?.data?.message ||
            "Failed to load products or categories."
        );
        } finally {
        setLoading(false);
        }
    };

    useEffect(() => {
        loadData();
    }, []);

    // =========================
    // FORM HANDLING
    // =========================

    const handleChange = (e) => {
        const { name, value } = e.target;

        setForm((previous) => ({
        ...previous,
        [name]: value,
        }));
    };

    // =========================
    // OPEN ADD FORM
    // =========================

    const openAddForm = () => {
        setEditingId(null);
        setForm(emptyForm);

        setError("");
        setSuccess("");

        setShowForm(true);

        window.scrollTo({
        top: 0,
        behavior: "smooth",
        });
    };

    // =========================
    // OPEN EDIT FORM
    // =========================

    const openEditForm = (product) => {
        setEditingId(product.id);

        setForm({
        name: product.name || "",
        description: product.description || "",
        price: product.price || "",
        stockQuantity: product.stockQuantity || "",
        categoryId: product.categoryId || "",
        imageUrl: product.imageUrl || "",
        });

        setError("");
        setSuccess("");

        setShowForm(true);

        window.scrollTo({
        top: 0,
        behavior: "smooth",
        });
    };

    // =========================
    // CLOSE FORM
    // =========================

    const closeForm = () => {
        setShowForm(false);
        setEditingId(null);
        setForm(emptyForm);
    };

    // =========================
    // CREATE / UPDATE PRODUCT
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
            price: Number(form.price),
            stockQuantity: Number(form.stockQuantity),
            categoryId: Number(form.categoryId),
            imageUrl: form.imageUrl.trim(),
        };

        console.log("Sending product:", payload);

        if (editingId) {
            await client.put(
            `/products/${editingId}`,
            payload
            );

            setSuccess(
            "Product updated successfully."
            );
        } else {
            await client.post(
            "/products",
            payload
            );

            setSuccess(
            "Product added successfully."
            );
        }

        setForm(emptyForm);
        setEditingId(null);
        setShowForm(false);

        await loadData();
        } catch (err) {
        console.error(
            "Product save failed:",
            err
        );

        setError(
            err.response?.data?.message ||
            `Failed to ${
                editingId ? "update" : "create"
            } product.`
        );
        } finally {
        setSaving(false);
        }
    };

    // =========================
    // DELETE PRODUCT
    // =========================

    const handleDelete = async (product) => {
        const confirmed = window.confirm(
        `Are you sure you want to delete "${product.name}"?`
        );

        if (!confirmed) {
        return;
        }

        try {
        setDeletingId(product.id);
        setError("");
        setSuccess("");

        await client.delete(
            `/products/${product.id}`
        );

        setSuccess(
            `"${product.name}" deleted successfully.`
        );

        await loadData();
        } catch (err) {
        console.error(
            "Product deletion failed:",
            err
        );

        setError(
            err.response?.data?.message ||
            "Failed to delete product."
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
            <h2>Loading products...</h2>
        </main>
        );
    }

    // =========================
    // UI
    // =========================

    return (
        <main
        className="container"
        style={{
            padding: "40px 0",
        }}
        >
        {/* =========================
            HEADER
        ========================= */}

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
            <h1>Manage Products</h1>

            <p>
                Add, edit and remove products from
                ShopSphere.
            </p>
            </div>

            <div>
            <Link
                to="/admin"
                className="btn"
                style={{
                marginRight: "10px",
                }}
            >
                Back to Admin
            </Link>

            <button
                className="btn btn-primary"
                onClick={openAddForm}
            >
                + Add Product
            </button>
            </div>
        </div>

        {/* =========================
            SUCCESS MESSAGE
        ========================= */}

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

        {/* =========================
            ERROR MESSAGE
        ========================= */}

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

        {/* =========================
            ADD / EDIT FORM
        ========================= */}

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
                ? "Edit Product"
                : "Add Product"}
            </h2>

            {/* PRODUCT NAME */}

            <div
                style={{
                marginBottom: "15px",
                }}
            >
                <label>
                Product Name
                </label>

                <input
                type="text"
                name="name"
                value={form.name}
                onChange={handleChange}
                required
                maxLength={150}
                placeholder="e.g. iPhone 16"
                style={{
                    width: "100%",
                    padding: "10px",
                    marginTop: "5px",
                    boxSizing: "border-box",
                }}
                />
            </div>

            {/* DESCRIPTION */}

            <div
                style={{
                marginBottom: "15px",
                }}
            >
                <label>
                Description
                </label>

                <textarea
                name="description"
                value={form.description}
                onChange={handleChange}
                rows="4"
                maxLength={2000}
                placeholder="Enter product description"
                style={{
                    width: "100%",
                    padding: "10px",
                    marginTop: "5px",
                    boxSizing: "border-box",
                    resize: "vertical",
                }}
                />
            </div>

            {/* PRICE */}

            <div
                style={{
                marginBottom: "15px",
                }}
            >
                <label>
                Price (₹)
                </label>

                <input
                type="number"
                name="price"
                value={form.price}
                onChange={handleChange}
                min="0.01"
                step="0.01"
                required
                placeholder="79999"
                style={{
                    width: "100%",
                    padding: "10px",
                    marginTop: "5px",
                    boxSizing: "border-box",
                }}
                />
            </div>

            {/* STOCK */}

            <div
                style={{
                marginBottom: "15px",
                }}
            >
                <label>
                Stock Quantity
                </label>

                <input
                type="number"
                name="stockQuantity"
                value={form.stockQuantity}
                onChange={handleChange}
                min="0"
                required
                placeholder="20"
                style={{
                    width: "100%",
                    padding: "10px",
                    marginTop: "5px",
                    boxSizing: "border-box",
                }}
                />
            </div>

            {/* CATEGORY */}

            <div
                style={{
                marginBottom: "15px",
                }}
            >
                <label>
                Category
                </label>

                <select
                name="categoryId"
                value={form.categoryId}
                onChange={handleChange}
                required
                style={{
                    width: "100%",
                    padding: "10px",
                    marginTop: "5px",
                    boxSizing: "border-box",
                }}
                >
                <option value="">
                    Select category
                </option>

                {categories.map(
                    (category) => (
                    <option
                        key={category.id}
                        value={category.id}
                    >
                        {category.name}
                    </option>
                    )
                )}
                </select>
            </div>

            {/* IMAGE URL */}

            <div
                style={{
                marginBottom: "20px",
                }}
            >
                <label>
                Product Image URL
                </label>

                <input
                type="url"
                name="imageUrl"
                value={form.imageUrl}
                onChange={handleChange}
                placeholder="https://example.com/iphone.jpg"
                style={{
                    width: "100%",
                    padding: "10px",
                    marginTop: "5px",
                    boxSizing: "border-box",
                }}
                />

                <small
                style={{
                    display: "block",
                    marginTop: "6px",
                    color: "#777",
                }}
                >
                Paste a publicly accessible image
                URL.
                </small>

                {/* IMAGE PREVIEW */}

                {form.imageUrl && (
                <div
                    style={{
                    marginTop: "15px",
                    }}
                >
                    <p
                    style={{
                        marginBottom: "8px",
                    }}
                    >
                    Preview:
                    </p>

                    <img
                    src={form.imageUrl}
                    alt="Product preview"
                    onError={(e) => {
                        e.currentTarget.style.display =
                        "none";
                    }}
                    style={{
                        width: "150px",
                        height: "150px",
                        objectFit: "cover",
                        borderRadius: "8px",
                        border: "1px solid #ddd",
                    }}
                    />
                </div>
                )}
            </div>

            {/* FORM BUTTONS */}

            <button
                type="submit"
                className="btn btn-primary"
                disabled={saving}
            >
                {saving
                ? "Saving..."
                : editingId
                ? "Update Product"
                : "Add Product"}
            </button>

            <button
                type="button"
                className="btn"
                onClick={closeForm}
                style={{
                marginLeft: "10px",
                }}
                disabled={saving}
            >
                Cancel
            </button>
            </form>
        )}

        {/* =========================
            PRODUCTS LIST
        ========================= */}

        {products.length === 0 ? (
            <p>No products found.</p>
        ) : (
            <div
            style={{
                display: "grid",
                gap: "15px",
            }}
            >
            {products.map((product) => (
                <div
                key={product.id}
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
                {/* PRODUCT IMAGE */}

                <div
                    style={{
                    flexShrink: 0,
                    }}
                >
                    {product.imageUrl ? (
                    <img
                        src={product.imageUrl}
                        alt={product.name}
                        onError={(e) => {
                        e.currentTarget.style.display =
                            "none";
                        }}
                        style={{
                        width: "100px",
                        height: "100px",
                        objectFit: "cover",
                        borderRadius: "8px",
                        border: "1px solid #ddd",
                        }}
                    />
                    ) : (
                    <div
                        style={{
                        width: "100px",
                        height: "100px",
                        borderRadius: "8px",
                        border: "1px solid #ddd",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        color: "#888",
                        fontSize: "12px",
                        textAlign: "center",
                        }}
                    >
                        No Image
                    </div>
                    )}
                </div>

                {/* PRODUCT INFORMATION */}

                <div
                    style={{
                    flex: 1,
                    }}
                >
                    <h3>
                    {product.name}
                    </h3>

                    <p>
                    Category:{" "}
                    {product.categoryName ||
                        "N/A"}
                    </p>

                    <p>
                    Price: ₹
                    {product.price}
                    </p>

                    <p>
                    Stock:{" "}
                    {product.stockQuantity}
                    </p>

                    {product.description && (
                    <p>
                        {product.description}
                    </p>
                    )}
                </div>

                {/* ACTIONS */}

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
                        openEditForm(product)
                    }
                    >
                    Edit
                    </button>

                    <button
                    className="btn"
                    onClick={() =>
                        handleDelete(product)
                    }
                    disabled={
                        deletingId ===
                        product.id
                    }
                    >
                    {deletingId ===
                    product.id
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