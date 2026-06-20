@import com.dewarim.cinnamon.model.Acl
@import com.dewarim.cinnamon.model.Format
@import com.dewarim.cinnamon.model.Meta
@import com.dewarim.cinnamon.model.ObjectSystemData
@import com.dewarim.cinnamon.model.ObjectType
@import com.dewarim.cinnamon.model.UserAccount
@import com.dewarim.cinnamon.model.relations.Relation
@import java.util.List
@param ObjectSystemData osd
@param String ownerName
@param Format format
@param String folderPath
@param String username
@param List<Acl> acls
@param List<ObjectType> objectTypes
@param List<Format> formats
@param List<UserAccount> users
@param List<Meta> metas
@param List<Relation> relations
@param String error
@template.layout(
    title = "Edit: " + osd.getName(),
    username = username,
    pageContent = @`
<section class="section">
    <div class="container" style="max-width: 800px;">

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
                    <a class="button is-light" href="/ui/osd/view?id=${osd.getId()}">View</a>
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

        @if(!error.isEmpty())
            <div class="notification is-danger is-light">${error}</div>
        @endif

        <%-- System Metadata --%>
        <div class="box">
            <h2 class="title is-5">System Metadata</h2>
            <form method="POST" action="/ui/osd/edit">
                <input type="hidden" name="id" value="${osd.getId()}">

                <div class="field">
                    <label class="label">Name</label>
                    <div class="control">
                        <input class="input" type="text" name="name" value="${osd.getName()}">
                    </div>
                </div>

                <div class="columns">
                    <div class="column">
                        <div class="field">
                            <label class="label">ACL</label>
                            <div class="control">
                                <div class="select is-fullwidth">
                                    <select name="aclId">
                                        @for(Acl acl : acls)
                                            @if(acl.getId().equals(osd.getAclId()))
                                                <option value="${acl.getId()}" selected>${acl.getName()}</option>
                                            @else
                                                <option value="${acl.getId()}">${acl.getName()}</option>
                                            @endif
                                        @endfor
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="column">
                        <div class="field">
                            <label class="label">Object Type</label>
                            <div class="control">
                                <div class="select is-fullwidth">
                                    <select name="typeId">
                                        @for(ObjectType ot : objectTypes)
                                            @if(ot.getId().equals(osd.getTypeId()))
                                                <option value="${ot.getId()}" selected>${ot.getName()}</option>
                                            @else
                                                <option value="${ot.getId()}">${ot.getName()}</option>
                                            @endif
                                        @endfor
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="column">
                        <div class="field">
                            <label class="label">Owner</label>
                            <div class="control">
                                <div class="select is-fullwidth">
                                    <select name="ownerId">
                                        @for(UserAccount u : users)
                                            @if(u.getId().equals(osd.getOwnerId()))
                                                <option value="${u.getId()}" selected>${u.getName()}</option>
                                            @else
                                                <option value="${u.getId()}">${u.getName()}</option>
                                            @endif
                                        @endfor
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="field is-grouped">
                    <div class="control">
                        <button class="button is-primary" type="submit">Save</button>
                    </div>
                </div>
            </form>
        </div>

        <%-- Read-only info --%>
        <div class="box is-size-7">
            <h2 class="title is-6">Details</h2>
            <table class="table is-narrow is-fullwidth">
                <tbody>
                <tr><th>Owner</th><td>${ownerName}</td></tr>
                <tr><th>Version</th><td>${osd.getCmnVersion()}</td></tr>
                <tr><th>Created</th><td>${osd.getCreated().toString()}</td></tr>
                <tr><th>Modified</th><td>${osd.getModified().toString()}</td></tr>
                @if(format != null)
                    <tr><th>Format</th><td>${format.getName()} (${format.getContentType()})</td></tr>
                @endif
                @if(osd.getContentSize() != null && osd.getContentSize() > 0)
                    <tr><th>Content size</th><td>${osd.getContentSize()} B</td></tr>
                @endif
                </tbody>
            </table>
        </div>

        <%-- Custom Metadata --%>
        @if(!metas.isEmpty())
            <div class="box">
                <h2 class="title is-5">Custom Metadata</h2>
                @for(Meta meta : metas)
                    <div class="mb-2">
                        <p class="is-size-7 has-text-grey">Type ID: ${meta.getTypeId()}</p>
                        <pre class="is-size-7" style="white-space:pre-wrap;">${meta.getContent()}</pre>
                    </div>
                @endfor
            </div>
        @endif

        <%-- Relations --%>
        @if(!relations.isEmpty())
            <div class="box">
                <h2 class="title is-5">Relations</h2>
                <table class="table is-narrow is-fullwidth is-size-7">
                    <thead>
                        <tr><th>Left ID</th><th>Right ID</th><th>Type ID</th></tr>
                    </thead>
                    <tbody>
                    @for(Relation rel : relations)
                        <tr>
                            <td>${rel.getLeftId()}</td>
                            <td>${rel.getRightId()}</td>
                            <td>${rel.getTypeId()}</td>
                        </tr>
                    @endfor
                    </tbody>
                </table>
            </div>
        @endif

    </div>
</section>
`)
