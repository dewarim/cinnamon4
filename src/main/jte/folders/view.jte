@param String folderPath
@param String username
@param String filter
@template.layout(
    title = "Folders",
    username = username,
    pageContent = @`
<div class="columns is-gapless" style="height: calc(100vh - 52px); overflow: hidden;">
    <div class="column is-2" style="border-right: 1px solid #dbdbdb; overflow-y: auto; height: 100%;">
        <div id="folder-tree-panel"
             hx-get="/ui/folders/navigate?folderPath=${folderPath}@if(!filter.isEmpty())&filter=${filter}@endif"
             hx-trigger="load"
             hx-swap="innerHTML">
            <p class="p-3 has-text-grey is-size-7">Loading…</p>
        </div>
    </div>
    <div class="column" style="overflow-y: auto; height: 100%;">
        <div id="folder-content-panel">
            <p class="p-3 has-text-grey is-size-7">Loading content…</p>
        </div>
    </div>
    <div class="column is-3" style="border-left: 1px solid #dbdbdb; overflow-y: auto; height: 100%;">
        <div id="osd-meta-panel" class="p-3">
            <p class="has-text-grey is-size-7">Click on a document to see its metadata.</p>
        </div>
    </div>
</div>
`)
