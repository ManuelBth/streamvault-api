package com.betha.streamvault.shared.constants;

public final class ApiConstants {

    private ApiConstants() {}

    // API Version
    public static final String API_V1 = "/api/v1";

    // Endpoints
    public static final String AUTH = API_V1 + "/auth";
    public static final String USERS = API_V1 + "/users";
    public static final String PROFILES = API_V1 + "/profiles";

    // Profiles
    public static final int MAX_PROFILES_PER_USER = 3;
    public static final int MAX_PROFILE_NAME_LENGTH = 50;

    // Password
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 100;

    // User
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_EMAIL_LENGTH = 255;
}
