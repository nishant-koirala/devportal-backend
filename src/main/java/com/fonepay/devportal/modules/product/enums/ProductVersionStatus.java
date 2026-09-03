package com.fonepay.devportal.modules.product.enums;

/**
 * Lifecycle status of a major product release version.
 */
public enum ProductVersionStatus {
    DRAFT,          // Under drafting / not yet publicly published
    ACTIVE,         // Actively supported release version
    CURRENT,        // The current default stable release (e.g., v19 in angular.dev)
    LTS,            // Long-Term Support release version
    MAINTENANCE,    // Maintenance mode (critical/security updates only)
    DEPRECATED,     // Deprecated with visible deprecation notice/banner
    ARCHIVED        // Fully archived historical snapshot
}
