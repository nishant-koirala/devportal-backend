package com.fonepay.devportal.modules.cms.repository;

import com.fonepay.devportal.modules.cms.document.BlockData;
import com.fonepay.devportal.modules.cms.document.Page;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PageCustomRepositoryImpl implements PageCustomRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public boolean updateDraftBlock(String pageId, String blockId, BlockData newData, long currentVersion) {
        // Query to match the correct page
        Query query = new Query(Criteria.where("_id").is(pageId));

        // Update to set the new block data and increment its version
        Update update = new Update()
                .set("draftBlocks.$[block].data", newData)
                .inc("draftBlocks.$[block].blockVersion", 1L);

        // Array filter to match the specific block by ID AND its current version (optimistic locking)
        update.filterArray(Criteria.where("block.id").is(blockId).and("block.blockVersion").is(currentVersion));

        // Execute the update
        UpdateResult result = mongoTemplate.updateFirst(query, update, Page.class);

        // If modifiedCount is 0, it means either the block wasn't found or the version didn't match
        return result.getModifiedCount() > 0;
    }
}
