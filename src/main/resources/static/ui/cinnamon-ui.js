function treeExpandedPaths() {
    return [...document.querySelectorAll('#folder-tree-panel li[data-expanded]')]
        .map(li => li.dataset.path)
        .join(',');
}

function treeCurrentPath() {
    const aside = document.querySelector('#folder-tree-panel aside[data-current-path]');
    return aside ? aside.dataset.currentPath : '';
}

function treeExpand(path) {
    const expanded = new Set([...document.querySelectorAll('#folder-tree-panel li[data-expanded]')]
        .map(li => li.dataset.path));
    expanded.add(path);
    _treeRefresh(expanded);
}

function treeCollapse(path) {
    const expanded = new Set([...document.querySelectorAll('#folder-tree-panel li[data-expanded]')]
        .map(li => li.dataset.path));
    expanded.delete(path);
    // also remove descendants so they start collapsed when re-expanded
    for (const p of [...expanded]) {
        if (p.startsWith(path + '/')) expanded.delete(p);
    }
    _treeRefresh(expanded);
}

function _treeRefresh(expanded) {
    const params = new URLSearchParams({
        folderPath: treeCurrentPath(),
        expandedPaths: [...expanded].join(',')
    });
    htmx.ajax('GET', '/ui/folders/tree?' + params, {target: '#folder-tree-panel', swap: 'innerHTML'});
}
