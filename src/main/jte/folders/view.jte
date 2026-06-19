@param String folderPath
@param String username
@template.layout(
    title = "Folders",
    username = username,
    pageContent = @`
<div class="columns is-gapless" style="height: calc(100vh - 52px); overflow: hidden;">
    <div class="column is-2" style="border-right: 1px solid #dbdbdb; overflow-y: auto; height: 100%;">
        <div id="folder-tree-panel"
             hx-get="/ui/folders/tree?folderPath=${folderPath}"
             hx-trigger="load, every 10s"
             hx-swap="innerHTML">
            <p class="p-3 has-text-grey is-size-7">Loading tree…</p>
        </div>
    </div>
    <div class="column" style="overflow-y: auto; height: 100%;">
        <div id="folder-content-panel"
             hx-get="/ui/folders/content?folderPath=${folderPath}"
             hx-trigger="load"
             hx-swap="innerHTML">
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
