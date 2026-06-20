@import com.dewarim.cinnamon.model.Acl
@import com.dewarim.cinnamon.model.FolderType
@import com.dewarim.cinnamon.model.Lifecycle
@import java.util.List
@param long parentId
@param String parentName
@param String username
@param List<Acl> acls
@param List<FolderType> folderTypes
@param List<Lifecycle> lifecycles
@param Long defaultAclId
@param String error
@template.layout(
    title = "Create Folder",
    username = username,
    pageContent = @`
<section class="section">
    <div class="container" style="max-width: 640px;">
        <h1 class="title is-4">Create Folder</h1>
        <p class="subtitle is-6 has-text-grey">Inside: ${parentName}</p>

        @if(!error.isEmpty())
            <div class="notification is-danger is-light">${error}</div>
        @endif

        <form method="POST" action="/ui/folder/create">
            <input type="hidden" name="parentId" value="${parentId}">

            <div class="field">
                <label class="label">Name <span class="has-text-danger">*</span></label>
                <div class="control">
                    <input class="input" type="text" name="name" autofocus required
                           pattern=".*\S.*" title="Name must not be blank">
                </div>
            </div>

            <div class="field">
                <label class="label">ACL</label>
                <div class="control">
                    <div class="select is-fullwidth">
                        <select name="aclId">
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
                <label class="label">Folder Type</label>
                <div class="control">
                    <div class="select is-fullwidth">
                        <select name="typeId">
                            @for(FolderType ft : folderTypes)
                                <option value="${ft.getId()}">${ft.getName()}</option>
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
                    <textarea class="textarea is-small" name="summary" rows="3"
                              placeholder="&lt;summary/&gt;"></textarea>
                </div>
            </div>

            <div class="field is-grouped">
                <div class="control">
                    <button class="button is-primary" type="submit">Create Folder</button>
                </div>
                <div class="control">
                    <a class="button is-light" href="/ui/folders">Cancel</a>
                </div>
            </div>
        </form>
    </div>
</section>
`)
