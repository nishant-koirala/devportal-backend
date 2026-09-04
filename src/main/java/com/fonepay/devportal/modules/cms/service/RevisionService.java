package com.fonepay.devportal.modules.cms.service;

import com.fonepay.devportal.modules.cms.document.Block;
import com.fonepay.devportal.modules.cms.document.Revision;

import java.util.List;

public interface RevisionService {
    Revision createSnapshot(String pageId, List<Block> blocks, String commitMessage, String createdBy);
    List<Revision> getRevisions(String pageId);
    Revision getRevision(String pageId, int version);
    Revision revertToVersion(String pageId, int version, String revertedBy);
}
