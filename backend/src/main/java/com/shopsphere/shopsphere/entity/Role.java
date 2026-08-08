package com.shopsphere.shopsphere.entity;

/**
 * Kept as a simple enum (not a table) — fine for a fixed, small set of roles.
 * If you later need dynamic roles/permissions, promote this to its own entity.
 */
public enum Role {
    ROLE_CUSTOMER,
    ROLE_ADMIN
}
