CREATE TABLE departments (
    department_id   CHAR(26)     NOT NULL,
    department_name VARCHAR(255) NOT NULL,
    department_description VARCHAR(1000) NULL,
    is_active       TINYINT(1)   NOT NULL DEFAULT 1,
    created_at      DATETIME(6)  NOT NULL,
    PRIMARY KEY (department_id),
    UNIQUE KEY uk_departments_name (department_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE roles (
    role_id     CHAR(26)     NOT NULL,
    role_name   VARCHAR(50)  NOT NULL,
    description VARCHAR(500) NULL,
    created_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (role_id),
    UNIQUE KEY uk_roles_name (role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE role_permissions (
    role_id    CHAR(26)     NOT NULL,
    permission VARCHAR(100) NOT NULL,
    PRIMARY KEY (role_id, permission),
    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id) REFERENCES roles (role_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE users (
    user_id        CHAR(26)     NOT NULL,
    email          VARCHAR(255) NOT NULL,
    pending_email  VARCHAR(255) NULL,
    password_hash  VARCHAR(255) NULL,
    full_name      VARCHAR(255) NULL,
    company_name   VARCHAR(255) NULL,
    status         VARCHAR(32)  NOT NULL,
    email_verified TINYINT(1)   NOT NULL DEFAULT 0,
    last_login_at  DATETIME(6)  NULL,
    department_id  CHAR(26)     NULL,
    deactivated_at DATETIME(6)  NULL,
    created_at     DATETIME(6)  NOT NULL,
    updated_at     DATETIME(6)  NOT NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_users_email (email),
    KEY idx_users_status (status),
    KEY idx_users_created_at (created_at),
    KEY idx_users_department (department_id),
    CONSTRAINT fk_users_department
        FOREIGN KEY (department_id) REFERENCES departments (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_roles (
    id          CHAR(26)    NOT NULL,
    user_id     CHAR(26)    NOT NULL,
    role_id     CHAR(26)    NOT NULL,
    assigned_at DATETIME(6) NOT NULL,
    assigned_by CHAR(26)    NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_roles_user_role (user_id, role_id),
    KEY idx_user_roles_role (role_id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_sessions (
    session_id       CHAR(26)     NOT NULL,
    user_id          CHAR(26)     NOT NULL,
    ip_address       VARCHAR(45)  NULL,
    user_agent       VARCHAR(512) NULL,
    created_at       DATETIME(6)  NOT NULL,
    last_activity_at DATETIME(6)  NULL,
    expires_at       DATETIME(6)  NOT NULL,
    revoked_at       DATETIME(6)  NULL,
    status           VARCHAR(32)  NOT NULL,
    PRIMARY KEY (session_id),
    KEY idx_sessions_user_status (user_id, status),
    CONSTRAINT fk_sessions_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_tokens (
    id          CHAR(26)     NOT NULL,
    user_id     CHAR(26)     NOT NULL,
    token_hash  VARCHAR(128) NOT NULL,
    token_type  VARCHAR(32)  NOT NULL,
    expires_at  DATETIME(6)  NOT NULL,
    used_at     DATETIME(6)  NULL,
    created_at  DATETIME(6)  NOT NULL,
    attempts    INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_tokens_hash (token_hash),
    KEY idx_user_tokens_user_type (user_id, token_type),
    CONSTRAINT fk_user_tokens_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE activities (
    id          CHAR(26)     NOT NULL,
    user_id     CHAR(26)     NOT NULL,
    type        VARCHAR(32)  NOT NULL,
    occurred_at DATETIME(6)  NOT NULL,
    ip_address  VARCHAR(45)  NULL,
    user_agent  VARCHAR(512) NULL,
    success     TINYINT(1)   NULL,
    PRIMARY KEY (id),
    KEY idx_activities_user_occurred (user_id, occurred_at),
    CONSTRAINT fk_activities_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_bookmarks (
    id         CHAR(26)      NOT NULL,
    user_id    CHAR(26)      NOT NULL,
    page_id    CHAR(26)      NULL,
    page_url   VARCHAR(2048) NULL,
    title      VARCHAR(500)  NULL,
    created_at DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_bookmarks_page (user_id, page_id),
    UNIQUE KEY uk_user_bookmarks_url (user_id, page_url(512)),
    KEY idx_user_bookmarks_created (user_id, created_at),
    CONSTRAINT fk_user_bookmarks_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_products (
    user_id     CHAR(26)    NOT NULL,
    product_id  CHAR(26)    NOT NULL,
    selected_at DATETIME(6) NOT NULL,
    PRIMARY KEY (user_id, product_id),
    CONSTRAINT fk_user_products_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE broadcasts (
    id             CHAR(26)      NOT NULL,
    title          VARCHAR(255)  NOT NULL,
    message        TEXT          NOT NULL,
    target_role    VARCHAR(32)   NOT NULL,
    priority       VARCHAR(32)   NOT NULL,
    category       VARCHAR(32)   NOT NULL,
    is_dismissible TINYINT(1)    NOT NULL DEFAULT 1,
    action_url     VARCHAR(2048) NULL,
    action_label   VARCHAR(255)  NULL,
    starts_at      DATETIME(6)   NOT NULL,
    expires_at     DATETIME(6)   NULL,
    status         VARCHAR(32)   NOT NULL,
    created_by     CHAR(26)      NOT NULL,
    created_at     DATETIME(6)   NOT NULL,
    updated_at     DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    KEY idx_broadcasts_status_target_starts (status, target_role, starts_at),
    KEY idx_broadcasts_status_expires (status, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE broadcast_display_modes (
    broadcast_id CHAR(26)    NOT NULL,
    display_mode VARCHAR(32) NOT NULL,
    PRIMARY KEY (broadcast_id, display_mode),
    CONSTRAINT fk_broadcast_display_modes
        FOREIGN KEY (broadcast_id) REFERENCES broadcasts (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE broadcast_interactions (
    id           CHAR(26)    NOT NULL,
    user_id      CHAR(26)    NOT NULL,
    broadcast_id CHAR(26)    NOT NULL,
    is_read      TINYINT(1)  NOT NULL DEFAULT 0,
    read_at      DATETIME(6) NULL,
    is_dismissed TINYINT(1)  NOT NULL DEFAULT 0,
    dismissed_at DATETIME(6) NULL,
    created_at   DATETIME(6) NOT NULL,
    updated_at   DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_broadcast_interactions_user_broadcast (user_id, broadcast_id),
    KEY idx_broadcast_interactions_broadcast_read (broadcast_id, is_read),
    CONSTRAINT fk_broadcast_interactions_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_broadcast_interactions_broadcast
        FOREIGN KEY (broadcast_id) REFERENCES broadcasts (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
