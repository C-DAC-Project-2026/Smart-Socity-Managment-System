package com.society.util;
public final class AppConstants {
    private AppConstants() {}
    /** Platform-level: manages societies. Not scoped to any society. */
    public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    /** Society-level admin: manages only their own society. */
    public static final String ROLE_ADMIN    = "ROLE_ADMIN";
    public static final String ROLE_RESIDENT = "ROLE_RESIDENT";
    public static final String ROLE_STAFF    = "ROLE_STAFF";
    public static final int    DEFAULT_PAGE_SIZE = 10;
    public static final int    MAX_PAGE_SIZE     = 100;
}
