export function productPhotoUrl(product, size = 600) {
    if (product?.imageUrl) {
        const url = product.imageUrl;

        // Optimize Unsplash images
        if (url.includes("images.unsplash.com")) {
            const separator = url.includes("?") ? "&" : "?";

            // Avoid adding optimization parameters twice
            if (url.includes("fit=crop")) {
                return url;
            }

            return `${url}${separator}auto=format&fit=crop&w=${size}&q=70`;
        }

        return url;
    }

    // Fallback image
    const seed = product?.id ?? product?.name ?? "shopsphere";

    return `https://picsum.photos/seed/product-${seed}/${size}/${size}`;
}