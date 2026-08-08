import client from "./client";

export async function processPayment(orderId, success = true) {
    const response = await client.post(
        `/payments/${orderId}?success=${success}`
    );

    return response.data.data;
}