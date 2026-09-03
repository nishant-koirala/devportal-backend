package com.fonepay.devportal.modules.cms.document;


public interface BlockData {
    default void sanitize() {
        // By default, do nothing. Blocks with rich text should override this.
    }
}
