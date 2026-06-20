@import com.dewarim.cinnamon.model.Acl
@import com.dewarim.cinnamon.model.Folder
@import com.dewarim.cinnamon.model.FolderType
@import com.dewarim.cinnamon.model.UserAccount
@import java.util.List
@param Folder folder
@param String folderPath
@param String username
@param List<Acl> acls
@param List<FolderType> folderTypes
@param List<UserAccount> users
@param String error
@template.layout(
    title = "Edit Folder",
    username = username,
    pageContent = @`
<section class="section">
    <div class="container" style="max-width: 640px;">
        <h1 class="title is-4">Edit Folder</h1>
        <p class="subtitle is-6 has-text-grey">${folderPath}</p>

        @if(!error.isEmpty())
            <div class="notification is-danger is-light">${error}</div>
        @endif

        <form method="POST" action="/ui/folder/edit">
            <input type="hidden" name="id" value="${folder.getId()}">

            <div class="field">
                <label class="label">Name</label>
                <div class="control">
                    <input class="input" type="text" name="name" value="${folder.getName()}">
                </div>
            </div>

            <div class="field">
                <label class="label">ACL</label>
                <div class="control">
                    <div class="select is-fullwidth">
                        <select name="aclId">
                            @for(Acl acl : acls)
                                @if(acl.getId().equals(folder.getAclId()))
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
                                @if(ft.getId().equals(folder.getTypeId()))
                                    <option value="${ft.getId()}" selected>${ft.getName()}</option>
                                @else
                                    <option value="${ft.getId()}">${ft.getName()}</option>
                                @endif
                            @endfor
                        </select>
                    </div>
                </div>
            </div>

            <div class="field">
                <label class="label">Owner</label>
                <div class="control">
                    <div class="select is-fullwidth">
                        <select name="ownerId">
                            @for(UserAccount u : users)
                                @if(u.getId().equals(folder.getOwnerId()))
                                    <option value="${u.getId()}" selected>${u.getName()}</option>
                                @else
                                    <option value="${u.getId()}">${u.getName()}</option>
                                @endif
                            @endfor
                        </select>
                    </div>
                </div>
            </div>

            <div class="field is-grouped">
                <div class="control">
                    <button class="button is-primary" type="submit">Save Changes</button>
                </div>
                <div class="control">
                    <a class="button is-light" href="/ui/folders?folderPath=${folderPath}">Cancel</a>
                </div>
            </div>
        </form>

        <hr>

        <h2 class="title is-5 has-text-danger">Delete Folder</h2>
        <form method="POST" action="/ui/folder/delete"
              onsubmit="return confirm('Are you sure you want to delete this folder?');">
            <input type="hidden" name="id" value="${folder.getId()}">
            <div class="field">
                <label class="checkbox">
                    <input type="checkbox" name="deleteContent"> Delete content (OSDs inside this folder)
                </label>
            </div>
            <div class="field">
                <label class="checkbox">
                    <input type="checkbox" name="deleteRecursively"> Delete recursively (including sub-folders)
                </label>
            </div>
            <div class="control">
                <button class="button is-danger" type="submit">Delete Folder</button>
            </div>
        </form>
    </div>
</section>
`)
