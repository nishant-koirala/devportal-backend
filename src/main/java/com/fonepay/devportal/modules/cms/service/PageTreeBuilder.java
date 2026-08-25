package com.fonepay.devportal.modules.cms.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fonepay.devportal.modules.cms.document.Page;
import com.fonepay.devportal.modules.cms.dto.response.PageTreeNodeResponse;

public final class PageTreeBuilder {

    private static final Comparator<Page> SIBLING_ORDER = Comparator
            .comparingInt(Page::getPageOrder)
            .thenComparing(Page::getTitle, Comparator.nullsLast(String::compareToIgnoreCase));

    private PageTreeBuilder() {
    }

    public static List<PageTreeNodeResponse> build(List<Page> pages) {
        if (pages == null || pages.isEmpty()) {
            return List.of();
        }

        Set<String> ids = pages.stream().map(Page::getId).collect(Collectors.toSet());
        Map<String, List<Page>> childrenByParent = new HashMap<>();
        List<Page> roots = new ArrayList<>();

        for (Page page : pages) {
            String parentId = page.getParentId();
            if (parentId == null || parentId.isBlank() || !ids.contains(parentId)) {
                roots.add(page);
            } else {
                childrenByParent.computeIfAbsent(parentId, key -> new ArrayList<>()).add(page);
            }
        }

        roots.sort(SIBLING_ORDER);
        childrenByParent.values().forEach(siblings -> siblings.sort(SIBLING_ORDER));

        Set<String> visiting = new HashSet<>();
        Set<String> placed = new HashSet<>();
        List<PageTreeNodeResponse> tree = new ArrayList<>();
        for (Page root : roots) {
            tree.add(toNode(root, childrenByParent, visiting, placed));
        }

        List<Page> leftover = new ArrayList<>();
        for (Page page : pages) {
            if (!placed.contains(page.getId())) {
                leftover.add(page);
            }
        }
        leftover.sort(SIBLING_ORDER);
        for (Page page : leftover) {
            if (!placed.contains(page.getId())) {
                tree.add(toNode(page, childrenByParent, visiting, placed));
            }
        }

        return tree;
    }

    private static PageTreeNodeResponse toNode(
            Page page,
            Map<String, List<Page>> childrenByParent,
            Set<String> visiting,
            Set<String> placed) {

        placed.add(page.getId());

        PageTreeNodeResponse node = PageTreeNodeResponse.builder()
                .id(page.getId())
                .parentId(page.getParentId())
                .title(page.getTitle())
                .slug(page.getSlug())
                .status(page.getStatus())
                .pageOrder(page.getPageOrder())
                .children(new ArrayList<>())
                .build();

        if (!visiting.add(page.getId())) {
            return node;
        }

        try {
            List<Page> children = childrenByParent.getOrDefault(page.getId(), List.of());
            for (Page child : children) {
                node.getChildren().add(toNode(child, childrenByParent, visiting, placed));
            }
        } finally {
            visiting.remove(page.getId());
        }

        return node;
    }
}
