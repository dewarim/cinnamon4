@import com.dewarim.cinnamon.model.Acl
@import com.dewarim.cinnamon.model.Format
@import com.dewarim.cinnamon.model.Lifecycle
@import com.dewarim.cinnamon.model.ObjectType
@import java.util.List
@param long parentId
@param String parentPath
@param String username
@param List<Acl> acls
@param List<ObjectType> objectTypes
@param List<Format> formats
@param List<Lifecycle> lifecycles
@param Long defaultAclId
@param String error
@template.layout(
    title = "Create Document",
    username = username,
    pageContent = @`
<section class="section">
    <div class="container" style="max-width: 720px;">
        <h1 class="title is-4">Create Document</h1>
        <p class="subtitle is-6 has-text-grey">
            <a href="/ui/folders?folderPath=${parentPath}">${parentPath}</a>
        </p>

        @if(!error.isEmpty())
            <div class="notification is-danger is-light">${error}</div>
        @endif

        <form method="POST" action="/ui/osd/create" enctype="multipart/form-data">
            <input type="hidden" name="parentId" value="${parentId}">

            <div class="field">
                <label class="label">Name <span class="has-text-danger">*</span></label>
                <div class="control">
                    <input class="input" type="text" name="name" autofocus required
                           pattern=".*\S.*" title="Name must not be blank">
                </div>
            </div>

            <div class="field">
                <label class="label">ACL <span class="has-text-danger">*</span></label>
                <div class="control">
                    <div class="select is-fullwidth">
                        <select name="aclId" required>
                            @for(Acl acl : acls)
                                @if(acl.getId().equals(defaultAclId))
                                    <option value="${acl.getId()}" selected>${acl.getName()}</option>
                                @else
                                    <option value="${acl.getId()}">${acl.getName()}</option>
                                @endif
                            @endfor
                        </select>
                    </div>
                </div>
            </div>

            <div class="field">
                <label class="label">Object Type <span class="has-text-danger">*</span></label>
                <div class="control">
                    <div class="select is-fullwidth">
                        <select name="typeId" required>
                            @for(ObjectType ot : objectTypes)
                                <option value="${ot.getId()}">${ot.getName()}</option>
                            @endfor
                        </select>
                    </div>
                </div>
            </div>

            <div class="field">
                <label class="label">Lifecycle</label>
                <div class="control">
                    <div class="select is-fullwidth">
                        <select name="lifecycleStateId">
                            <option value="">— none —</option>
                            @for(Lifecycle lc : lifecycles)
                                <option value="${lc.getId()}">${lc.getName()}</option>
                            @endfor
                        </select>
                    </div>
                </div>
            </div>

            <div class="field">
                <label class="label">Summary <span class="is-size-7 has-text-grey">(XML)</span></label>
                <div class="control">
                    <textarea class="textarea is-small" name="summary" rows="2"
                              placeholder="&lt;summary/&gt;"></textarea>
                </div>
            </div>

            <hr>
            <p class="heading">Content — file upload or text (one or neither)</p>

            <div class="field">
                <label class="label">Format</label>
                <div class="control">
                    <div class="select is-fullwidth">
                        <select name="formatId">
                            <option value="">— none —</option>
                            @for(Format f : formats)
                                <option value="${f.getId()}">${f.getName()} (${f.getContentType()})</option>
                            @endfor
                        </select>
                    </div>
                </div>
                <p class="help">Required when uploading a file or providing text content.</p>
            </div>

            <div class="field">
                <label class="label">File Upload</label>
                <div class="control">
                    <input class="input" type="file" name="file">
                </div>
            </div>

            <div class="field">
                <label class="label">Text Content</label>
                <div class="control">
                    <textarea class="textarea is-small" name="textContent" rows="6"
                              placeholder="Paste or type text content here (ignored if a file is uploaded)"></textarea>
                </div>
            </div>

            <div class="field is-grouped">
                <div class="control">
                    <button class="button is-primary" type="submit">Create Document</button>
                </div>
                <div class="control">
                    <a class="button is-light" href="/ui/folders?folderPath=${parentPath}">Cancel</a>
                </div>
            </div>
        </form>
    </div>
</section>
`)
