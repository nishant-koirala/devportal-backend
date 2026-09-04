package com.fonepay.devportal.modules.cms.listener;

import org.bson.Document;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.stereotype.Component;

import com.fonepay.devportal.modules.cms.document.Page;
import com.fonepay.devportal.modules.developer.service.UserBookmarkService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PageCascadeDeleteListener extends AbstractMongoEventListener<Page> {

    private final UserBookmarkService userBookmarkService;

    public PageCascadeDeleteListener(@Lazy UserBookmarkService userBookmarkService) {
        this.userBookmarkService = userBookmarkService;
    }

    @Override
    public void onAfterDelete(AfterDeleteEvent<Page> event) {
        Document source = event.getSource();
        if (source != null && source.containsKey("_id")) {
            String pageId = source.get("_id").toString();
            log.info("Detected physical deletion of Page [{}]. Cascading delete to bookmarks.", pageId);
            try {
                userBookmarkService.deleteBookmarksByPageId(pageId);
            } catch (Exception e) {
                log.error("Failed to cascade delete bookmarks for page {}: {}", pageId, e.getMessage());
            }
        }
    }
}
