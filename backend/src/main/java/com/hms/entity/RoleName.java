package com.hms.entity;

/**
 * The four roles the system supports. Kept as an enum because permissions are hardcoded
 * into Spring Security method rules; a DB-editable role table would be false flexibility.
 */
public enum RoleName {
    ADMIN,
    DOCTOR,
    PATIENT,
    RECEPTIONIST
}
