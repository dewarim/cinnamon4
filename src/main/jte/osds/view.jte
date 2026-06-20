@import com.dewarim.cinnamon.model.Format
@import com.dewarim.cinnamon.model.ObjectSystemData
@param ObjectSystemData osd
@param String ownerName
@param Format format
@param String folderPath
@param String username
@param String error
@template.layout(
    title = osd.getName(),
    username = username,
    pageContent = @`
<section class="section">
    <div class="container">

        @if(!error.isEmpty())
            <div class="notification is-danger is-light">${error}</div>
        @endif

        <div class="level">
            <div class="level-left">
                <div class="level-item">
                    <div>
                        <p class="title is-4">${osd.getName()}</p>
                        <p class="subtitle is-6 has-text-grey">
                            <a href="/ui/folders?folderPath=${folderPath}">${folderPath}</a>
                            &nbsp;·&nbsp; version ${osd.getCmnVersion()}
                        </p>
                    </div>
                </div>
            </div>
            <div class="level-right">
                <div class="level-item buttons">
                    <a class="button is-info is-light" href="/ui/osd/edit?id=${osd.getId()}">Edit</a>
                    <form method="POST" action="/ui/osd/copy" style="display:inline;">
                        <input type="hidden" name="id" value="${osd.getId()}">
                        <button class="button is-light" type="submit">Copy</button>
                    </form>
                    <form method="POST" action="/ui/osd/version" style="display:inline;"
                          onsubmit="return confirm('Create a new version of this document?');">
                        <input type="hidden" name="id" value="${osd.getId()}">
                        <button class="button is-light" type="submit">Version</button>
                    </form>
                    @if(osd.getContentSize() != null && osd.getContentSize() > 0)
                        <a class="button is-light" href="/ui/content/${osd.getId()}" download="${osd.getName()}">Download</a>
                    @endif
                    <form method="POST" action="/ui/osd/delete" style="display:inline;"
                          onsubmit="return confirm('Delete this document? This cannot be undone.');">
                        <input type="hidden" name="id" value="${osd.getId()}">
                        <button class="button is-danger is-light" type="submit">Delete</button>
                    </form>
                </div>
            </div>
        </div>

        <div class="columns">
            <div class="column is-8">
                @if(osd.getContentSize() != null && osd.getContentSize() > 0 && format != null)
                    <div class="box">
                        @if(format.getContentType().startsWith("image/"))
                            <figure class="image">
                                <img src="/ui/content/${osd.getId()}" alt="${osd.getName()}">
                            </figure>
                        @elseif(format.getContentType().equals("application/pdf"))
                            <iframe src="/ui/content/${osd.getId()}"
                                    style="width:100%;height:600px;border:none;"></iframe>
                        @elseif(format.getContentType().startsWith("text/"))
                            <pre style="white-space:pre-wrap;word-break:break-all;max-height:600px;overflow:auto;"
                                 hx-get="/ui/content/${osd.getId()}"
                                 hx-trigger="load"
                                 hx-swap="innerHTML"></pre>
                        @else
                            <p class="has-text-grey">
                                Preview not available for ${format.getName()}.
                                <a href="/ui/content/${osd.getId()}" download="${osd.getName()}">Download the file</a>.
                            </p>
                        @endif
                    </div>
                @else
                    <div class="box has-text-grey">No content.</div>
                @endif
            </div>

            <div class="column is-4">
                <div class="box is-size-7">
                    <table class="table is-narrow is-fullwidth">
                        <tbody>
                        <tr><th>Owner</th><td>${ownerName}</td></tr>
                        <tr><th>Version</th><td>${osd.getCmnVersion()}</td></tr>
                        <tr><th>Created</th><td>${osd.getCreated().toString()}</td></tr>
                        <tr><th>Modified</th><td>${osd.getModified().toString()}</td></tr>
                        @if(format != null)
                            <tr><th>Format</th><td>${format.getName()}</td></tr>
                        @endif
                        @if(osd.getContentSize() != null && osd.getContentSize() > 0)
                            <tr><th>Size</th><td>${osd.getContentSize()} B</td></tr>
                        @endif
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

    </div>
</section>
`)
