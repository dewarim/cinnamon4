package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.model.Folder;

import java.util.List;

public record UiTreeNode(
        Folder folder,
        String path,
        boolean isCurrent,
        List<UiTreeNode> children,
        boolean hasChildren
) {}
