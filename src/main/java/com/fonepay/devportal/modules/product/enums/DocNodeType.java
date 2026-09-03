package com.fonepay.devportal.modules.product.enums;

/**
 * Type of node in the versioned documentation navigation tree.
 */
public enum DocNodeType {
    PAGE,           // Standard documentation content page
    SECTION,        // Section folder/header grouping child pages
    CATEGORY,       // Top-level navigation category
    GUIDE,          // Step-by-step tutorial or guide
    API_REFERENCE,  // API reference endpoint or module
    EXTERNAL_LINK,  // External URL link in navigation sidebar
    DIVIDER         // Visual separator in navigation sidebar
}
