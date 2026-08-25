package com.fonepay.devportal.modules.cms.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.fonepay.devportal.modules.cms.document.BlockData;
import com.fonepay.devportal.modules.cms.document.Page;
import com.fonepay.devportal.modules.cms.dto.request.PageHierarchyUpdateDto;
import com.fonepay.devportal.modules.cms.enums.PageStatus;
import com.mongodb.client.result.UpdateResult;

public class PageCustomRepositoryImpl implements PageCustomRepository {

    private final MongoTemplate mongoTemplate;

    public PageCustomRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void bulkUpdateHierarchy(List<PageHierarchyUpdateDto> updates, Instant updatedAt) {
        if (updates == null || updates.isEmpty()) {
            return;
        }

        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Page.class);
        for (PageHierarchyUpdateDto item : updates) {
            Query query = Query.query(Criteria.where("_id").is(item.getPageId()));
            Update update = new Update()
                    .set("parent_id", normalizeParentId(item.getParentId()))
                    .set("page_order", item.getPageOrder())
                    .set("updated_at", updatedAt);
            bulkOps.updateOne(query, update);
        }
        bulkOps.execute();
    }

    @Override
    public void bulkSetStatus(List<String> pageIds, PageStatus status, Instant updatedAt) {
        if (pageIds == null || pageIds.isEmpty()) {
            return;
        }

        Query query = Query.query(Criteria.where("_id").in(pageIds));
        Update update = new Update()
                .set("status", status)
                .set("updated_at", updatedAt);
        mongoTemplate.updateMulti(query, update, Page.class);
    }

    @Override
    public boolean updateDraftBlock(String pageId, String blockId, BlockData newData, long currentVersion) {
        Query query = new Query(Criteria.where("_id").is(pageId));

        Update update = new Update()
                .set("draftBlocks.$[block].data", newData)
                .inc("draftBlocks.$[block].blockVersion", 1L);

        update.filterArray(Criteria.where("block.id").is(blockId).and("block.blockVersion").is(currentVersion));

        UpdateResult result = mongoTemplate.updateFirst(query, update, Page.class);

        return result.getModifiedCount() > 0;
    }

    private static String normalizeParentId(String parentId) {
        if (parentId == null || parentId.isBlank()) {
            return null;
        }
        return parentId;
    }
}
