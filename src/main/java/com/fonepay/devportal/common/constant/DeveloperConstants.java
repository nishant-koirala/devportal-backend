package com.fonepay.devportal.common.constant;

import java.util.Map;
import java.util.Set;

public final class DeveloperConstants {

        private DeveloperConstants() {
        }

        public static final String DEVELOPER_ROLE = "DEVELOPER";

        public static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
                        "name",
                        "email",
                        "company",
                        "createdAt",
                        "lastLoginAt",
                        "status");

        public static final Map<String, String> SORT_FIELD_MAPPING = Map.of(
                        "name", "fullName",
                        "email", "email",
                        "company", "companyName",
                        "createdAt", "createdAt",
                        "lastLoginAt", "lastLoginAt",
                        "status", "status");

        public static final String INVALID_SORT_FIELD_MESSAGE = "Invalid sort field. Allowed fields are: "
                        + ALLOWED_SORT_FIELDS;
        public static final String INVALID_SORT_DIRECTION_MESSAGE = "Invalid sort direction. Allowed values are: ASC, DESC";
}
